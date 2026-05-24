package com.jettech.api.solutions_clinic.audit;

import com.jettech.api.solutions_clinic.model.repository.UserRepository;
import org.hibernate.envers.RevisionListener;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.UUID;

/**
 * Instanciado por reflection pelo Envers — não é um Spring bean.
 * Acessa beans via ApplicationContextProvider para não depender de injeção.
 */
public class ClinicRevisionListener implements RevisionListener {

    @Override
    public void newRevision(Object revisionEntity) {
        try {
            ClinicRevisionEntity revision = (ClinicRevisionEntity) revisionEntity;

            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.getPrincipal() instanceof Jwt jwt) {
                try {
                    revision.setUserId(UUID.fromString(jwt.getSubject()));
                } catch (Exception ignored) {}

                try {
                    String clinicIdStr = jwt.getClaimAsString("clinicId");
                    if (clinicIdStr != null && !clinicIdStr.isBlank()) {
                        revision.setTenantId(UUID.fromString(clinicIdStr));
                    }
                } catch (Exception ignored) {}

                UUID userId = revision.getUserId();
                if (userId != null) {
                    try {
                        UserRepository userRepository = ApplicationContextProvider.getBean(UserRepository.class);
                        userRepository.findById(userId)
                                .ifPresent(user -> revision.setUserEmail(user.getEmail()));
                    } catch (Exception ignored) {}
                }
            }

            try {
                ServletRequestAttributes attrs =
                        (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
                if (attrs != null) {
                    revision.setIpAddress(attrs.getRequest().getRemoteAddr());
                }
            } catch (Exception ignored) {}

        } catch (Exception ignored) {
            // Nunca deixar o listener quebrar a transação principal
        }
    }
}
