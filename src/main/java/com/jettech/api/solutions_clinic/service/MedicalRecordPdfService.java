package com.jettech.api.solutions_clinic.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.jettech.api.solutions_clinic.model.entity.MedicalRecord;
import com.jettech.api.solutions_clinic.model.service.R2StorageService;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import io.pebbletemplates.pebble.PebbleEngine;
import io.pebbletemplates.pebble.loader.ClasspathLoader;
import io.pebbletemplates.pebble.template.PebbleTemplate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Gera PDF do prontuário usando template Pebble + OpenHTMLToPDF.
 */
@Service
@Slf4j
public class MedicalRecordPdfService {

    private static final DateTimeFormatter DATE_TIME_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy 'às' HH:mm", Locale.forLanguageTag("pt-BR"));
    private static final float WATERMARK_ALPHA = 0.07f;

    private static final Map<String, String> VITAL_SIGNS_LABELS = Map.of(
            "bloodPressure", "PA (mmHg)",
            "heartRate", "FC (bpm)",
            "temperature", "Temp. (°C)",
            "oxygenSaturation", "SpO2 (%)",
            "weight", "Peso (kg)",
            "height", "Altura (cm)",
            "imc", "IMC"
    );

    private final PebbleEngine pebbleEngine;
    private final R2StorageService r2StorageService;
    private volatile String systemLogoBase64;
    private volatile boolean systemLogoLoaded = false;

    public MedicalRecordPdfService(R2StorageService r2StorageService) {
        this.r2StorageService = r2StorageService;
        ClasspathLoader loader = new ClasspathLoader();
        loader.setPrefix("templates/");
        this.pebbleEngine = new PebbleEngine.Builder()
                .loader(loader)
                .defaultLocale(Locale.forLanguageTag("pt-BR"))
                .build();
    }

    /**
     * Gera o PDF do prontuário a partir da entidade já carregada (com appointment, template, patient, professional, tenant).
     */
    public byte[] generatePdf(MedicalRecord record) {
        Map<String, Object> context = buildContext(record);
        String html = renderPebble("prontuario.pebble", context);
        return htmlToPdf(html);
    }

    private Map<String, Object> buildContext(MedicalRecord record) {
        Map<String, Object> context = new HashMap<>();

        String clinicName = record.getAppointment().getTenant().getName();
        String patientName = record.getAppointment().getPatient().getFirstName();
        String professionalName = record.getAppointment().getProfessional().getUser().getFirstName()
                + (record.getAppointment().getProfessional().getUser().getLastName() != null ? " " + record.getAppointment().getProfessional().getUser().getLastName() : "");
        String appointmentDate = record.getAppointment().getScheduledAt().format(DATE_TIME_FORMAT);
        String templateName = record.getTemplate().getName();
        String signedAt = record.getSignedAt() != null ? record.getSignedAt().format(DATE_TIME_FORMAT) : null;
        String specialty = record.getAppointment().getProfessional().getSpecialty() != null
                ? specialtyLabel(record.getAppointment().getProfessional().getSpecialty().name())
                : null;

        context.put("clinicName", clinicName);
        context.put("patientName", patientName);
        context.put("professionalName", professionalName);
        context.put("appointmentDate", appointmentDate);
        context.put("templateName", templateName);
        context.put("signedAt", signedAt);
        context.put("specialty", specialty);
        context.put("recordId", record.getId().toString());

        // Logo do sistema
        context.put("systemLogoBase64", getSystemLogoBase64());

        // Logo da clínica
        String clinicLogoKey = record.getAppointment().getTenant().getLogoObjectKey();
        byte[] clinicLogoBytes = fetchLogoBytes(clinicLogoKey);
        if (clinicLogoBytes != null) {
            String mime = resolveMime(clinicLogoKey);
            context.put("clinicLogoBase64", "data:" + mime + ";base64,"
                    + Base64.getEncoder().encodeToString(clinicLogoBytes));
            context.put("clinicWatermarkBase64", buildWatermarkBase64(clinicLogoBytes));
        } else {
            context.put("clinicLogoBase64", null);
            context.put("clinicWatermarkBase64", null);
        }

        List<Map<String, String>> vitalSigns = buildVitalSignsList(record.getVitalSigns());
        context.put("vitalSigns", vitalSigns);

        List<Map<String, String>> fields = buildFieldsList(record.getTemplate().getSchema(), record.getContent());
        context.put("fields", fields);

        return context;
    }

    private List<Map<String, String>> buildVitalSignsList(JsonNode vitalSigns) {
        List<Map<String, String>> list = new ArrayList<>();
        if (vitalSigns == null || !vitalSigns.isObject()) {
            return list;
        }
        for (Map.Entry<String, String> entry : VITAL_SIGNS_LABELS.entrySet()) {
            JsonNode value = vitalSigns.get(entry.getKey());
            if (value != null && !value.isNull()) {
                String str = value.isNumber() ? String.valueOf(value.asDouble()) : value.asText("");
                if (str != null && !str.isBlank()) {
                    Map<String, String> item = new HashMap<>();
                    item.put("label", entry.getValue());
                    item.put("value", str);
                    list.add(item);
                }
            }
        }
        return list;
    }

    private List<Map<String, String>> buildFieldsList(JsonNode schema, JsonNode content) {
        List<Map<String, String>> list = new ArrayList<>();
        if (schema == null || !schema.isArray()) {
            return list;
        }
        for (JsonNode fieldNode : schema) {
            String id = fieldNode.has("id") ? fieldNode.get("id").asText() : null;
            String label = fieldNode.has("label") ? fieldNode.get("label").asText() : id;
            if (id == null) continue;

            String value = "";
            if (content != null && content.isObject() && content.has(id)) {
                JsonNode valNode = content.get(id);
                value = valNode.isNull() ? "" : valNode.asText("");
            }

            Map<String, String> item = new HashMap<>();
            item.put("label", label);
            item.put("value", value != null ? value : "");
            list.add(item);
        }
        return list;
    }

    private String getSystemLogoBase64() {
        if (!systemLogoLoaded) {
            synchronized (this) {
                if (!systemLogoLoaded) {
                    systemLogoBase64 = loadSystemLogoBase64();
                    systemLogoLoaded = true;
                }
            }
        }
        return systemLogoBase64;
    }

    private String loadSystemLogoBase64() {
        InputStream is = resolveLogoStream();
        if (is == null) return null;
        try (InputStream stream = is) {
            byte[] raw = stream.readAllBytes();
            byte[] png = extractPng(raw);
            if (png == null) return null;
            return "data:image/png;base64," + Base64.getEncoder().encodeToString(png);
        } catch (Exception e) {
            log.warn("Erro ao processar logo-sistema.png", e);
            return null;
        }
    }

    private InputStream resolveLogoStream() {
        try {
            ClassPathResource res = new ClassPathResource("images/logo-sistema.png");
            if (res.exists()) return res.getInputStream();
        } catch (Exception ignored) {}
        InputStream is = Thread.currentThread().getContextClassLoader()
                .getResourceAsStream("images/logo-sistema.png");
        if (is != null) return is;
        return MedicalRecordPdfService.class.getResourceAsStream("/images/logo-sistema.png");
    }

    private byte[] extractPng(byte[] data) {
        for (int i = 0; i < data.length - 8; i++) {
            if (data[i] == (byte) 0x89 && data[i + 1] == 0x50
                    && data[i + 2] == 0x4E && data[i + 3] == 0x47) {
                byte[] png = new byte[data.length - i];
                System.arraycopy(data, i, png, 0, png.length);
                return png;
            }
        }
        return null;
    }

    private byte[] fetchLogoBytes(String objectKey) {
        if (objectKey == null || objectKey.isBlank()) return null;
        try {
            String presignedUrl = r2StorageService.createPresignedGetUrl(objectKey, 5);
            if (presignedUrl == null) return null;
            try (InputStream is = new URL(presignedUrl).openStream()) {
                return is.readAllBytes();
            }
        } catch (Exception e) {
            log.warn("Erro ao buscar logo da clínica (key={}): {}", objectKey, e.getMessage());
            return null;
        }
    }

    private String buildWatermarkBase64(byte[] sourceBytes) {
        try {
            BufferedImage src = ImageIO.read(new ByteArrayInputStream(sourceBytes));
            if (src == null) return null;
            BufferedImage out = new BufferedImage(src.getWidth(), src.getHeight(), BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = out.createGraphics();
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, WATERMARK_ALPHA));
            g.drawImage(src, 0, 0, null);
            g.dispose();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(out, "png", baos);
            return "data:image/png;base64," + Base64.getEncoder().encodeToString(baos.toByteArray());
        } catch (Exception e) {
            log.warn("Erro ao gerar marca d'água", e);
            return null;
        }
    }

    private String resolveMime(String objectKey) {
        if (objectKey == null) return "image/png";
        String lower = objectKey.toLowerCase();
        if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) return "image/jpeg";
        if (lower.endsWith(".webp")) return "image/webp";
        if (lower.endsWith(".svg")) return "image/svg+xml";
        return "image/png";
    }

    private String specialtyLabel(String name) {
        return switch (name) {
            case "CLINICO_GERAL"        -> "Clínico Geral";
            case "CARDIOLOGISTA"        -> "Cardiologista";
            case "DERMATOLOGISTA"       -> "Dermatologista";
            case "ENDOCRINOLOGISTA"     -> "Endocrinologista";
            case "FISIOTERAPEUTA"       -> "Fisioterapeuta";
            case "GASTROENTEROLOGISTA"  -> "Gastroenterologista";
            case "GINECOLOGISTA"        -> "Ginecologista";
            case "MASTOLOGISTA"         -> "Mastologista";
            case "OBSTETRIACO"          -> "Obstetrício";
            case "OFTALMOLOGISTA"       -> "Oftalmologista";
            case "ORTOPEDISTA"          -> "Ortopedista";
            case "PEDIATRA"             -> "Pediatra";
            case "PSIQUIATRA"           -> "Psiquiatra";
            case "PSICOLOGISTA"         -> "Psicólogo(a)";
            case "UROLOGISTA"           -> "Urologista";
            case "DENTISTA"             -> "Dentista";
            case "ENFERMEIRO"           -> "Enfermeiro(a)";
            default -> name;
        };
    }

    private String renderPebble(String templateName, Map<String, Object> context) {
        try {
            PebbleTemplate template = pebbleEngine.getTemplate(templateName);
            Writer writer = new StringWriter();
            template.evaluate(writer, context);
            return writer.toString();
        } catch (Exception e) {
            log.error("Erro ao renderizar template Pebble {}", templateName, e);
            throw new RuntimeException("Erro ao gerar HTML do prontuário", e);
        }
    }

    private byte[] htmlToPdf(String html) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.useFastMode();
            builder.withHtmlContent(html, null);
            builder.toStream(out);
            builder.run();
            return out.toByteArray();
        } catch (Exception e) {
            log.error("Erro ao converter HTML para PDF", e);
            throw new RuntimeException("Erro ao gerar PDF", e);
        }
    }
}
