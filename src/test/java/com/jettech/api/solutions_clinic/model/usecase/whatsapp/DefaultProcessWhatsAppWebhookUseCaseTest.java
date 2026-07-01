package com.jettech.api.solutions_clinic.model.usecase.whatsapp;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jettech.api.solutions_clinic.model.entity.Appointment;
import com.jettech.api.solutions_clinic.model.entity.AppointmentStatus;
import com.jettech.api.solutions_clinic.model.entity.Patient;
import com.jettech.api.solutions_clinic.model.entity.Tenant;
import com.jettech.api.solutions_clinic.model.repository.AppointmentRepository;
import com.jettech.api.solutions_clinic.model.repository.PatientRepository;
import com.jettech.api.solutions_clinic.model.service.NotificationCreator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DefaultProcessWhatsAppWebhookUseCaseTest {

    private static final String SENDER = "5511999999999@s.whatsapp.net";

    @Mock AppointmentRepository appointmentRepository;
    @Mock PatientRepository patientRepository;
    @Mock NotificationCreator notificationCreator;

    private DefaultProcessWhatsAppWebhookUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new DefaultProcessWhatsAppWebhookUseCase(
                appointmentRepository, patientRepository, new ObjectMapper(), notificationCreator);
    }

    private Appointment appointmentWith(AppointmentStatus status, String confirmationCode) {
        Appointment appointment = new Appointment();
        ReflectionTestUtils.setField(appointment, "id", UUID.randomUUID());
        appointment.setStatus(status);
        appointment.setConfirmationCode(confirmationCode);
        appointment.setScheduledAt(LocalDateTime.of(2026, 7, 10, 15, 0));

        Tenant tenant = new Tenant();
        ReflectionTestUtils.setField(tenant, "id", UUID.randomUUID());
        tenant.setName("Clínica");
        appointment.setTenant(tenant);

        Patient patient = new Patient();
        ReflectionTestUtils.setField(patient, "id", UUID.randomUUID());
        patient.setFirstName("Maria");
        appointment.setPatient(patient);

        return appointment;
    }

    private void stubSinglePatientMatch(UUID... patientIds) {
        List<Patient> patients = List.of(patientIds).stream().map(id -> {
            Patient p = new Patient();
            ReflectionTestUtils.setField(p, "id", id);
            return p;
        }).toList();
        when(patientRepository.findByWhatsappNormalized(any(), any())).thenReturn(patients);
    }

    private void execute(String body) {
        useCase.execute(new ProcessWhatsAppWebhookRequest(body));
    }

    private static String textBody(String text, String stanzaId) {
        return """
                {
                  "event": "Message",
                  "data": {
                    "Info": {"Type": "text", "Sender": "%s", "IsFromMe": false, "MsgMetaInfo": {"TargetID": "%s"}},
                    "Message": {"conversation": "%s"}
                  }
                }
                """.formatted(SENDER, stanzaId, text);
    }

    private static String extendedTextBody(String text, String stanzaId) {
        return """
                {
                  "event": "Message",
                  "data": {
                    "Info": {"Type": "text", "Sender": "%s", "IsFromMe": false, "MsgMetaInfo": {"TargetID": "%s"}},
                    "Message": {"extendedTextMessage": {"text": "%s"}}
                  }
                }
                """.formatted(SENDER, stanzaId, text);
    }

    // Reproduz o payload real da Evolution Go para um reply/swipe: Info.MsgMetaInfo.TargetID vem
    // vazio (é usado para edição, não para reply); o stanzaId real vem em data.quoted.stanzaID.
    private static String quotedReplyBody(String text, String quotedStanzaId) {
        return """
                {
                  "event": "Message",
                  "data": {
                    "Info": {"Type": "text", "Sender": "%s", "IsFromMe": false, "MsgMetaInfo": {"TargetID": ""}},
                    "Message": {"extendedTextMessage": {"text": "%s"}},
                    "quoted": {"stanzaID": "%s"}
                  }
                }
                """.formatted(SENDER, text, quotedStanzaId);
    }

    private static String buttonBody(String buttonId, String stanzaId) {
        return """
                {
                  "event": "Message",
                  "data": {
                    "Info": {"Type": "buttonsResponse", "Sender": "%s", "IsFromMe": false, "MsgMetaInfo": {"TargetID": "%s"}},
                    "Message": {"buttonsResponseMessage": {"selectedButtonId": "%s"}}
                  }
                }
                """.formatted(SENDER, stanzaId, buttonId);
    }

    // WACONF-04: stanzaId conhecido tem prioridade máxima, sem tocar no fallback por telefone.
    @Test
    void whenStanzaIdMatchesKnownAppointment_thenAppliesDirectlyWithoutPhoneFallback() {
        Appointment appointment = appointmentWith(AppointmentStatus.AGENDADO, null);
        when(appointmentRepository.findByWhatsappMessageId("wamid-1")).thenReturn(Optional.of(appointment));

        execute(textBody("sim", "wamid-1"));

        assertThat(appointment.getStatus()).isEqualTo(AppointmentStatus.CONFIRMADO);
        verify(notificationCreator).createAppointmentConfirmation(appointment);
        verify(patientRepository, never()).findByWhatsappNormalized(any(), any());
    }

    // Payload real Evolution Go: reply/swipe com Info.MsgMetaInfo.TargetID vazio, mas
    // data.quoted.stanzaID presente -> deve resolver direto por stanzaId, sem cair no fallback
    // por telefone (que seria ambíguo com múltiplos agendamentos ativos).
    @Test
    void whenTargetIdIsEmptyButQuotedStanzaIdIsPresent_thenAppliesDirectlyWithoutPhoneFallback() {
        Appointment appointment = appointmentWith(AppointmentStatus.AGENDADO, "C331");
        when(appointmentRepository.findByWhatsappMessageId("3EB06FC647DA1AC3D0F602")).thenReturn(Optional.of(appointment));

        execute(quotedReplyBody("1", "3EB06FC647DA1AC3D0F602"));

        assertThat(appointment.getStatus()).isEqualTo(AppointmentStatus.CONFIRMADO);
        verify(notificationCreator).createAppointmentConfirmation(appointment);
        verify(patientRepository, never()).findByWhatsappNormalized(any(), any());
    }

    // WACONF-05: sem stanzaId, 1 candidato por telefone -> aplica direto sem exigir código.
    @Test
    void whenNoStanzaIdAndExactlyOneCandidateByPhone_thenAppliesWithoutRequiringCode() {
        Appointment appointment = appointmentWith(AppointmentStatus.AGENDADO, "C111");
        stubSinglePatientMatch(appointment.getPatient().getId());
        when(appointmentRepository.findByPatientIdInAndStatusIn(any(), any())).thenReturn(List.of(appointment));

        execute(textBody("confirmar", ""));

        assertThat(appointment.getStatus()).isEqualTo(AppointmentStatus.CONFIRMADO);
        verify(notificationCreator).createAppointmentConfirmation(appointment);
    }

    // Reply/preview de link chega em Message.extendedTextMessage.text em vez de Message.conversation.
    @Test
    void whenTextArrivesAsExtendedTextMessage_thenIsStillRecognized() {
        Appointment appointment = appointmentWith(AppointmentStatus.AGENDADO, "C331");
        stubSinglePatientMatch(appointment.getPatient().getId());
        when(appointmentRepository.findByPatientIdInAndStatusIn(any(), any())).thenReturn(List.of(appointment));

        execute(extendedTextBody("confirmar C331", ""));

        assertThat(appointment.getStatus()).isEqualTo(AppointmentStatus.CONFIRMADO);
        verify(notificationCreator).createAppointmentConfirmation(appointment);
    }

    // Texto contendo só o código (sem palavra-chave confirmar/cancelar) continua sendo ignorado.
    @Test
    void whenTextIsOnlyTheConfirmationCode_thenWebhookIsIgnored() {
        execute(extendedTextBody("C331", ""));

        verify(patientRepository, never()).findByWhatsappNormalized(any(), any());
        verify(appointmentRepository, never()).save(any());
    }

    // WACONF-06 / WACONF-08 / WACONF-12: 2+ candidatos (inclusive de tenants diferentes) + código correspondente -> só o certo muda.
    @Test
    void whenTwoCandidatesFromDifferentTenantsAndCodeMatchesOne_thenOnlyThatAppointmentChanges() {
        Appointment appointmentA = appointmentWith(AppointmentStatus.AGENDADO, "C111");
        Appointment appointmentB = appointmentWith(AppointmentStatus.AGENDADO, "C222");
        stubSinglePatientMatch(appointmentA.getPatient().getId(), appointmentB.getPatient().getId());
        when(appointmentRepository.findByPatientIdInAndStatusIn(any(), any()))
                .thenReturn(List.of(appointmentA, appointmentB));

        execute(textBody("cancelar C222", ""));

        assertThat(appointmentB.getStatus()).isEqualTo(AppointmentStatus.CANCELADO);
        assertThat(appointmentA.getStatus()).isEqualTo(AppointmentStatus.AGENDADO);
        verify(notificationCreator).createAppointmentCancellation(appointmentB);
        verify(notificationCreator, never()).createAppointmentCancellation(appointmentA);
    }

    // WACONF-12: código antes da palavra-chave também é reconhecido ("C843 1").
    @Test
    void whenCodeComesBeforeKeyword_thenBothAreExtractedCorrectly() {
        Appointment appointmentA = appointmentWith(AppointmentStatus.AGENDADO, "C111");
        Appointment appointmentB = appointmentWith(AppointmentStatus.AGENDADO, "C222");
        stubSinglePatientMatch(appointmentA.getPatient().getId(), appointmentB.getPatient().getId());
        when(appointmentRepository.findByPatientIdInAndStatusIn(any(), any()))
                .thenReturn(List.of(appointmentA, appointmentB));

        execute(textBody("C111 1", ""));

        assertThat(appointmentA.getStatus()).isEqualTo(AppointmentStatus.CONFIRMADO);
        assertThat(appointmentB.getStatus()).isEqualTo(AppointmentStatus.AGENDADO);
    }

    // WACONF-07: 2+ candidatos, sem código na mensagem -> nada é alterado.
    @Test
    void whenTwoCandidatesAndNoCodeProvided_thenNothingChanges() {
        Appointment appointmentA = appointmentWith(AppointmentStatus.AGENDADO, "C111");
        Appointment appointmentB = appointmentWith(AppointmentStatus.AGENDADO, "C222");
        stubSinglePatientMatch(appointmentA.getPatient().getId(), appointmentB.getPatient().getId());
        when(appointmentRepository.findByPatientIdInAndStatusIn(any(), any()))
                .thenReturn(List.of(appointmentA, appointmentB));

        execute(textBody("sim", ""));

        assertThat(appointmentA.getStatus()).isEqualTo(AppointmentStatus.AGENDADO);
        assertThat(appointmentB.getStatus()).isEqualTo(AppointmentStatus.AGENDADO);
        verify(notificationCreator, never()).createAppointmentConfirmation(any());
        verify(appointmentRepository, never()).save(any());
    }

    // WACONF-07: 2+ candidatos, código presente mas não corresponde a nenhum -> nada é alterado.
    @Test
    void whenTwoCandidatesAndCodeMatchesNone_thenNothingChanges() {
        Appointment appointmentA = appointmentWith(AppointmentStatus.AGENDADO, "C111");
        Appointment appointmentB = appointmentWith(AppointmentStatus.AGENDADO, "C222");
        stubSinglePatientMatch(appointmentA.getPatient().getId(), appointmentB.getPatient().getId());
        when(appointmentRepository.findByPatientIdInAndStatusIn(any(), any()))
                .thenReturn(List.of(appointmentA, appointmentB));

        execute(textBody("confirmar C999", ""));

        assertThat(appointmentA.getStatus()).isEqualTo(AppointmentStatus.AGENDADO);
        assertThat(appointmentB.getStatus()).isEqualTo(AppointmentStatus.AGENDADO);
        verify(appointmentRepository, never()).save(any());
    }

    // WACONF-09: variações de texto livre para confirmar.
    @ParameterizedTest
    @ValueSource(strings = {"1", "sim", "confirmar", "confirmo", "confirmado", "ok", "Confirmo!", "SIM"})
    void whenTextMatchesAnyConfirmKeyword_thenConfirms(String text) {
        Appointment appointment = appointmentWith(AppointmentStatus.AGENDADO, "C111");
        stubSinglePatientMatch(appointment.getPatient().getId());
        when(appointmentRepository.findByPatientIdInAndStatusIn(any(), any())).thenReturn(List.of(appointment));

        execute(textBody(text, ""));

        assertThat(appointment.getStatus()).isEqualTo(AppointmentStatus.CONFIRMADO);
    }

    // WACONF-10: variações de texto livre para cancelar, incluindo acentuação ("não").
    @ParameterizedTest
    @ValueSource(strings = {"2", "cancelar", "não", "nao", "Cancelar", "NÃO"})
    void whenTextMatchesAnyCancelKeyword_thenCancels(String text) {
        Appointment appointment = appointmentWith(AppointmentStatus.AGENDADO, "C111");
        stubSinglePatientMatch(appointment.getPatient().getId());
        when(appointmentRepository.findByPatientIdInAndStatusIn(any(), any())).thenReturn(List.of(appointment));

        execute(textBody(text, ""));

        assertThat(appointment.getStatus()).isEqualTo(AppointmentStatus.CANCELADO);
    }

    // WACONF-11: texto sem palavra-chave reconhecida é ignorado.
    @Test
    void whenTextHasNoRecognizedKeyword_thenWebhookIsIgnored() {
        execute(textBody("oi, tudo bem?", ""));

        verify(patientRepository, never()).findByWhatsappNormalized(any(), any());
        verify(appointmentRepository, never()).save(any());
    }

    // Edge case (spec.md): confirmação duplicada em agendamento já CONFIRMADO não reprocessa nem renotifica.
    @Test
    void whenAppointmentIsAlreadyConfirmed_thenDuplicateConfirmationIsIgnored() {
        Appointment appointment = appointmentWith(AppointmentStatus.CONFIRMADO, null);
        when(appointmentRepository.findByWhatsappMessageId("wamid-1")).thenReturn(Optional.of(appointment));

        execute(textBody("sim", "wamid-1"));

        verify(notificationCreator, never()).createAppointmentConfirmation(any());
        verify(appointmentRepository, never()).save(any());
    }

    // Done-when de T5: clique de botão/lista continua funcionando sem exigir código.
    @Test
    void whenButtonClickResolvesViaStanzaId_thenAppliesIntentWithoutRequiringCode() {
        Appointment appointment = appointmentWith(AppointmentStatus.AGENDADO, "C111");
        when(appointmentRepository.findByWhatsappMessageId("wamid-2")).thenReturn(Optional.of(appointment));

        execute(buttonBody("CANCEL", "wamid-2"));

        assertThat(appointment.getStatus()).isEqualTo(AppointmentStatus.CANCELADO);
        verify(notificationCreator).createAppointmentCancellation(appointment);
    }
}
