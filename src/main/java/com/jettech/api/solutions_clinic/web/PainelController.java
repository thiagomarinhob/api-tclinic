package com.jettech.api.solutions_clinic.web;

import com.jettech.api.solutions_clinic.exception.EntityNotFoundException;
import com.jettech.api.solutions_clinic.model.entity.ChamadaPainel;
import com.jettech.api.solutions_clinic.model.repository.AppointmentRepository;
import com.jettech.api.solutions_clinic.model.repository.ChamadaPainelRepository;
import com.jettech.api.solutions_clinic.model.repository.RoomRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class PainelController implements PainelAPI {

    private final ChamadaPainelRepository chamadaPainelRepository;
    private final AppointmentRepository appointmentRepository;
    private final RoomRepository roomRepository;

    @Override
    public ResponseEntity<List<ChamadaPainelResponse>> chamadasHoje() {
        var chamadas = chamadaPainelRepository.findChamadasHoje()
                .stream()
                .map(ChamadaPainelResponse::from)
                .toList();
        return ResponseEntity.ok(chamadas);
    }

    @Override
    public ResponseEntity<ChamadaPainelResponse> chamar(UUID appointmentId, UUID roomId) {
        var appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new EntityNotFoundException("Agendamento", appointmentId));
        var room = roomRepository.findById(roomId)
                .orElseThrow(() -> new EntityNotFoundException("Sala", roomId));

        long count = chamadaPainelRepository.countByAppointmentId(appointmentId);

        var chamada = ChamadaPainel.builder()
                .appointment(appointment)
                .room(room)
                .numeroChamada((short) (count + 1))
                .horaChamada(OffsetDateTime.now())
                .build();

        var saved = chamadaPainelRepository.save(chamada);
        return ResponseEntity.status(HttpStatus.CREATED).body(ChamadaPainelResponse.from(saved));
    }
}
