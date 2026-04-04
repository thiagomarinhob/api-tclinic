package com.jettech.api.solutions_clinic.service;

import com.jettech.api.solutions_clinic.model.entity.Appointment;
import com.jettech.api.solutions_clinic.model.entity.Professional;
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
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Gera PDFs de documentos médicos avulsos: receituário, solicitação de exames, atestado e encaminhamento.
 */
@Service
@Slf4j
public class DocumentPdfService {

    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.forLanguageTag("pt-BR"));

    /** Opacidade da marca d'água (0.0 = invisível, 1.0 = opaco). */
    private static final float WATERMARK_ALPHA = 0.07f;

    private final PebbleEngine pebbleEngine;
    private final R2StorageService r2StorageService;
    /** Carregado lazily na primeira chamada a baseContext(). */
    private volatile String systemLogoBase64;
    private volatile boolean systemLogoLoaded = false;

    public DocumentPdfService(R2StorageService r2StorageService) {
        this.r2StorageService = r2StorageService;
        ClasspathLoader loader = new ClasspathLoader();
        loader.setPrefix("templates/");
        this.pebbleEngine = new PebbleEngine.Builder()
                .loader(loader)
                .defaultLocale(Locale.forLanguageTag("pt-BR"))
                .build();
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

    // ─── Receituário ─────────────────────────────────────────────────────────

    public byte[] generateReceita(Appointment appointment, String prescricoes) {
        Map<String, Object> ctx = baseContext(appointment);
        ctx.put("prescricoes", prescricoes != null ? prescricoes : "");
        return render("receita.pebble", ctx);
    }

    // ─── Solicitação de Exames ───────────────────────────────────────────────

    public byte[] generateSolicitacaoExames(Appointment appointment, List<String> exames, String indicacao) {
        Map<String, Object> ctx = baseContext(appointment);
        ctx.put("exames", exames);
        ctx.put("indicacao", indicacao != null ? indicacao : "");
        return render("solicitacao_exames.pebble", ctx);
    }

    // ─── Atestado ────────────────────────────────────────────────────────────

    public byte[] generateAtestado(Appointment appointment, int dias, String motivo) {
        Map<String, Object> ctx = baseContext(appointment);
        String cpf = appointment.getPatient().getCpf();
        ctx.put("cpf", cpf != null ? formatCpf(cpf) : "");
        ctx.put("dias", dias);
        ctx.put("motivo", motivo != null ? motivo : "");
        return render("atestado.pebble", ctx);
    }

    // ─── Encaminhamento ──────────────────────────────────────────────────────

    public byte[] generateEncaminhamento(Appointment appointment, String encaminhamento) {
        Map<String, Object> ctx = baseContext(appointment);
        ctx.put("encaminhamento", encaminhamento != null ? encaminhamento : "");
        return render("encaminhamento.pebble", ctx);
    }

    // ─── Helpers ─────────────────────────────────────────────────────────────

    private Map<String, Object> baseContext(Appointment appointment) {
        Map<String, Object> ctx = new HashMap<>();

        Professional prof = appointment.getProfessional();
        String profName = prof.getUser().getFirstName()
                + (prof.getUser().getLastName() != null ? " " + prof.getUser().getLastName() : "");
        String docLabel = prof.getDocumentType().name()
                + " " + prof.getDocumentNumber()
                + (prof.getDocumentState() != null ? "/" + prof.getDocumentState() : "");
        String specialty = prof.getSpecialty() != null
                ? specialtyLabel(prof.getSpecialty().name())
                : "";

        ctx.put("clinicName",       appointment.getTenant().getName());
        ctx.put("patientName",      appointment.getPatient().getFirstName());
        ctx.put("professionalName", profName);
        ctx.put("documentLabel",    docLabel);
        ctx.put("specialty",        specialty);
        ctx.put("date",             appointment.getScheduledAt().format(DATE_FORMAT));

        // Logo do sistema (direita do cabeçalho)
        String sysLogo = getSystemLogoBase64();
        log.info("systemLogoBase64 no contexto: {} chars",
                sysLogo != null ? sysLogo.length() : "NULL");
        ctx.put("systemLogoBase64", sysLogo);

        // Logo da clínica: carrega os bytes uma vez e gera duas versões
        String clinicLogoKey = appointment.getTenant().getLogoObjectKey();
        byte[] clinicLogoBytes = fetchLogoBytes(clinicLogoKey);
        if (clinicLogoBytes != null) {
            String mime = resolveMime(clinicLogoKey);
            ctx.put("clinicLogoBase64", "data:" + mime + ";base64,"
                    + Base64.getEncoder().encodeToString(clinicLogoBytes));
            // Versão com transparência assada (usada na marca d'água)
            ctx.put("clinicWatermarkBase64", buildWatermarkBase64(clinicLogoBytes));
        } else {
            ctx.put("clinicLogoBase64", null);
            ctx.put("clinicWatermarkBase64", null);
        }

        return ctx;
    }

    // ── Carregamento do logo do sistema a partir do classpath ─────────────────

    private String loadSystemLogoBase64() {
        InputStream is = resolveLogoStream();
        if (is == null) {
            log.warn("logo-sistema.png nao encontrado em nenhum classloader");
            return null;
        }
        try (InputStream stream = is) {
            byte[] raw = stream.readAllBytes();
            log.info("logo-sistema.png lido: {} bytes raw", raw.length);
            byte[] png = extractPng(raw);
            if (png == null) {
                log.warn("Nao foi possivel extrair PNG do arquivo logo-sistema.png");
                return null;
            }
            log.info("PNG extraido com sucesso: {} bytes", png.length);
            return "data:image/png;base64," + Base64.getEncoder().encodeToString(png);
        } catch (Exception e) {
            log.warn("Erro ao processar logo-sistema.png", e);
            return null;
        }
    }

    /**
     * Extrai bytes PNG de dentro de um arquivo ICO (comum quando browsers salvam
     * favicon .ico com extensão .png). Se os bytes já forem PNG, retorna direto.
     */
    private byte[] extractPng(byte[] data) {
        // Procura pela assinatura PNG: 89 50 4E 47 0D 0A 1A 0A
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

    private InputStream resolveLogoStream() {
        // 1) ClassPathResource (Spring-managed)
        try {
            ClassPathResource res = new ClassPathResource("images/logo-sistema.png");
            if (res.exists()) {
                log.info("logo-sistema.png encontrado via ClassPathResource");
                return res.getInputStream();
            }
            log.info("ClassPathResource.exists() = false, tentando fallbacks...");
        } catch (Exception e) {
            log.info("ClassPathResource falhou: {}", e.getMessage());
        }
        // 2) Context classloader
        InputStream is = Thread.currentThread().getContextClassLoader()
                .getResourceAsStream("images/logo-sistema.png");
        if (is != null) {
            log.info("logo-sistema.png encontrado via ContextClassLoader");
            return is;
        }
        // 3) Class classloader com barra inicial
        is = DocumentPdfService.class.getResourceAsStream("/images/logo-sistema.png");
        if (is != null) {
            log.info("logo-sistema.png encontrado via Class.getResourceAsStream");
            return is;
        }
        return null;
    }

    // ── Carregamento do logo da clínica a partir do R2 ────────────────────────

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

    /**
     * Gera uma versão da imagem com canal alpha reduzido para usar como marca d'água.
     * A transparência fica gravada na própria imagem PNG, sem depender de CSS opacity.
     */
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
        if (lower.endsWith(".svg"))  return "image/svg+xml";
        return "image/png";
    }

    private String formatCpf(String cpf) {
        if (cpf == null) return "";
        String d = cpf.replaceAll("\\D", "");
        if (d.length() == 11) {
            return d.substring(0, 3) + "." + d.substring(3, 6) + "." + d.substring(6, 9) + "-" + d.substring(9);
        }
        return cpf;
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

    private byte[] render(String templateName, Map<String, Object> context) {
        try {
            PebbleTemplate template = pebbleEngine.getTemplate(templateName);
            Writer writer = new StringWriter();
            template.evaluate(writer, context);
            String html = writer.toString();
            try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
                PdfRendererBuilder builder = new PdfRendererBuilder();
                builder.useFastMode();
                builder.withHtmlContent(html, null);
                builder.toStream(out);
                builder.run();
                return out.toByteArray();
            }
        } catch (Exception e) {
            log.error("Erro ao gerar PDF com template {}", templateName, e);
            throw new RuntimeException("Erro ao gerar PDF", e);
        }
    }
}
