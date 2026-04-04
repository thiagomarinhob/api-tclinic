package com.jettech.api.solutions_clinic.job;

import com.jettech.api.solutions_clinic.model.entity.Tenant;
import com.jettech.api.solutions_clinic.model.entity.TenantStatus;
import com.jettech.api.solutions_clinic.model.repository.TenantRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j; // Para logs
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Component
@Slf4j // Log é essencial em jobs para saber se está rodando
@RequiredArgsConstructor
public class TrialExpirationJob {

    private final TenantRepository tenantRepository;

    // Roda todos os dias às 03:00 da manhã
    // Cron expression: Seg Min Hora Dia Mês DiaSemana
    //    @Scheduled(fixedRate = 10000)
    @Scheduled(cron = "0 0 3 * * *")
    @Transactional // Abre transação para poder salvar as alterações
    public void verifyExpiredTrials() {
        log.info("Iniciando verificação de períodos de teste expirados...");

        LocalDate now = LocalDate.now();

        // 1. Busca quem deve ser suspenso (tenants com status TRIAL e trial expirado)
        List<Tenant> expiredTenants = tenantRepository.findExpiredTrials(TenantStatus.TRIAL, now);

        if (expiredTenants.isEmpty()) {
            log.info("Nenhum tenant expirado encontrado hoje.");
            return;
        }

        log.info("Encontrados {} tenants com trial expirado. Iniciando suspensão...", expiredTenants.size());

        // 2. Atualiza o status
        for (Tenant tenant : expiredTenants) {
            try {
                tenant.setStatus(TenantStatus.SUSPENDED);
                tenant.setActive(false); // Bloqueia login imediatamente

                // Opcional: Aqui você enviaria um e-mail avisando "Seu teste acabou, assine agora!"
                // emailService.sendTrialExpiredEmail(tenant.getOwnerEmail());

                log.info("Tenant suspenso: {} ({})", tenant.getName(), tenant.getId());
            } catch (Exception e) {
                log.error("Erro ao suspender tenant {}", tenant.getId(), e);
            }
        }

        // 3. Salva todos (o @Transactional garante o commit no final)
        tenantRepository.saveAll(expiredTenants);

        log.info("Job finalizado com sucesso.");
    }
}