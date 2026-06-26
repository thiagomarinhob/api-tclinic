package com.jettech.api.solutions_clinic.model.usecase.user;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.jettech.api.solutions_clinic.exception.AuthenticationFailedException;
import com.jettech.api.solutions_clinic.model.entity.Tenant;
import com.jettech.api.solutions_clinic.model.entity.TenantStatus;
import com.jettech.api.solutions_clinic.model.entity.TypeTenant;
import com.jettech.api.solutions_clinic.model.entity.User;
import com.jettech.api.solutions_clinic.model.entity.UserTenantRole;
import com.jettech.api.solutions_clinic.model.repository.UserRepository;
import com.jettech.api.solutions_clinic.model.repository.UserTenantRoleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DefaultAuthUserUseCaseTest {

    static final String SECRET = "test-secret-test-secret-test-secret-test";

    @Mock UserRepository userRepository;
    @Mock UserTenantRoleRepository userTenantRoleRepository;
    @Mock PasswordEncoder passwordEncoder;

    @InjectMocks DefaultAuthUserUseCase useCase;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(useCase, "secretKey", SECRET);
        ReflectionTestUtils.setField(useCase, "issuer", "solutions-clinic");
    }

    @Test
    void whenIsPlatformAdminTrue_thenJwtContainsPermissionsClaim() throws AuthenticationFailedException {
        var user = user(true);
        when(userRepository.findByEmail("admin@test.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("pass", user.getPassword())).thenReturn(true);
        when(userTenantRoleRepository.findByUser_Id(user.getId())).thenReturn(List.of());

        var response = useCase.execute(new AuthUserRequest("admin@test.com", "pass"));

        var decoded = JWT.require(Algorithm.HMAC256(SECRET)).build().verify(response.access_token());
        List<String> permissions = decoded.getClaim("permissions").asList(String.class);
        assertThat(permissions).containsExactly("admin:tenant:manage");
    }

    @Test
    void whenIsPlatformAdminFalse_thenJwtHasNoPermissionsClaim() throws AuthenticationFailedException {
        var user = user(false);
        when(userRepository.findByEmail("user@test.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("pass", user.getPassword())).thenReturn(true);
        when(userTenantRoleRepository.findByUser_Id(user.getId())).thenReturn(List.of());

        var response = useCase.execute(new AuthUserRequest("user@test.com", "pass"));

        var decoded = JWT.require(Algorithm.HMAC256(SECRET)).build().verify(response.access_token());
        assertThat(decoded.getClaim("permissions").isMissing()).isTrue();
    }

    @Test
    void whenIsPlatformAdminTrueAndNoTenant_thenJwtContainsPermissionsButNoClinicId() throws AuthenticationFailedException {
        var user = user(true);
        when(userRepository.findByEmail("admin@test.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("pass", user.getPassword())).thenReturn(true);
        when(userTenantRoleRepository.findByUser_Id(user.getId())).thenReturn(List.of());

        var response = useCase.execute(new AuthUserRequest("admin@test.com", "pass"));

        var decoded = JWT.require(Algorithm.HMAC256(SECRET)).build().verify(response.access_token());
        assertThat(decoded.getClaim("permissions").asList(String.class)).containsExactly("admin:tenant:manage");
        assertThat(decoded.getClaim("clinicId").isMissing()).isTrue();
    }

    // ─── helpers ─────────────────────────────────────────────────────────────

    private User user(boolean platformAdmin) {
        var u = new User();
        ReflectionTestUtils.setField(u, "id", UUID.randomUUID());
        u.setFirstName("Test");
        u.setLastName("User");
        u.setEmail(platformAdmin ? "admin@test.com" : "user@test.com");
        u.setPassword("hashed");
        u.setPlatformAdmin(platformAdmin);
        return u;
    }
}
