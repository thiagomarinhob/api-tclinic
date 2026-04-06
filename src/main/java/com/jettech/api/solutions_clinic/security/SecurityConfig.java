package com.jettech.api.solutions_clinic.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.util.StringUtils;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> {
                    auth.requestMatchers("/users").permitAll()
                            .requestMatchers("/auth/**").permitAll()
                            .requestMatchers("/v1/auth/sign-in").permitAll()
                            .requestMatchers("/v1/auth/signup/**").permitAll()
                            .requestMatchers("/v1/subscriptions/webhook").permitAll()
                            .requestMatchers("/v1/whatsapp/webhook").permitAll()
                            .requestMatchers(
                                    "/swagger-ui.html",
                                    "/swagger-ui/**",
                                    "/v3/api-docs/**"
                            ).permitAll();
                    auth.anyRequest().authenticated();
                })
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint((request, response, authException) -> {
                            // Para rotas permitidas, permite que a requisição continue sem autenticação
                            String path = request.getRequestURI();
                            if (path.equals("/v1/auth/sign-in") ||
                                path.startsWith("/v1/auth/signup/") ||
                                path.startsWith("/auth/") || 
                                path.equals("/users") ||
                                path.equals("/v1/subscriptions/webhook") ||
                                path.equals("/v1/whatsapp/webhook")) {
                                // Não retorna erro, permite que a requisição continue
                                return;
                            }
                            // Para outras rotas, retorna 401
                            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                            response.setContentType("application/json");
                            response.getWriter().write("{\"error\":\"Unauthorized\",\"message\":\"Token de autenticação necessário\"}");
                        }))
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter())));

        return http.build();
    }

    @Bean
    JwtDecoder jwtDecoder(
            @Value("${security.jwt.jwk-set-uri:}") String jwkSetUri,
            @Value("${security.jwt.issuer:}") String issuer,
            @Value("${security.jwt.audience:}") String audience,
            @Value("${security.token.secret:}") String hmacSecret
    ) {
        NimbusJwtDecoder decoder;

        if (StringUtils.hasText(jwkSetUri)) {
            decoder = NimbusJwtDecoder.withJwkSetUri(jwkSetUri).build();
        } else {
            if (!StringUtils.hasText(hmacSecret)) {
                throw new IllegalStateException("Defina `security.jwt.jwk-set-uri` (Auth0/OIDC) ou `security.token.secret` (HS256) para habilitar autenticação JWT.");
            }
            
            // Valida que o secret tem pelo menos 256 bits (32 caracteres)
            if (hmacSecret.length() < 32) {
                throw new IllegalStateException(
                    "O `security.token.secret` deve ter pelo menos 32 caracteres (256 bits) para HS256. " +
                    "Tamanho atual: " + hmacSecret.length() + " caracteres."
                );
            }
            
            SecretKey secretKey = new SecretKeySpec(hmacSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            decoder = NimbusJwtDecoder.withSecretKey(secretKey).macAlgorithm(MacAlgorithm.HS256).build();
        }

        OAuth2TokenValidator<Jwt> validator = StringUtils.hasText(issuer)
                ? JwtValidators.createDefaultWithIssuer(issuer)
                : JwtValidators.createDefault();

        if (StringUtils.hasText(audience)) {
            validator = new DelegatingOAuth2TokenValidator<>(validator, new AudienceValidator(audience));
        }

        decoder.setJwtValidator(validator);
        return decoder;
    }

    @Bean
    JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter scopeConverter = new JwtGrantedAuthoritiesConverter();
        scopeConverter.setAuthorityPrefix("SCOPE_");
        scopeConverter.setAuthoritiesClaimName("scope");

        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(jwt -> {
            Set<GrantedAuthority> authorities = new HashSet<>();
            Collection<GrantedAuthority> scopeAuthorities = scopeConverter.convert(jwt);
            if (scopeAuthorities != null) {
                authorities.addAll(scopeAuthorities);
            }

            Object permissionsClaim = jwt.getClaims().get("permissions");
            if (permissionsClaim instanceof Collection<?> permissions) {
                authorities.addAll(
                        permissions.stream()
                                .filter(Objects::nonNull)
                                .map(Object::toString)
                                .filter(StringUtils::hasText)
                                .map(p -> "PERM_" + p)
                                .map(org.springframework.security.core.authority.SimpleGrantedAuthority::new)
                                .collect(Collectors.toSet())
                );
            }

            return authorities;
        });
        return converter;
    }

    static final class AudienceValidator implements OAuth2TokenValidator<Jwt> {
        private final String audience;

        AudienceValidator(String audience) {
            this.audience = audience;
        }

        @Override
        public OAuth2TokenValidatorResult validate(Jwt token) {
            if (token.getAudience().contains(audience)) {
                return OAuth2TokenValidatorResult.success();
            }

            OAuth2Error error = new OAuth2Error("invalid_token", "Token JWT sem o audience esperado.", null);
            return OAuth2TokenValidatorResult.failure(error);
        }
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        // Permite qualquer porta do localhost para desenvolvimento
        List<String> allowedOrigins = new java.util.ArrayList<>(List.of("http://localhost:*", "http://127.0.0.1:*"));
        String frontendUrl = System.getenv("FRONTEND_URL");
        if (frontendUrl != null && !frontendUrl.isBlank()) {
            allowedOrigins.add(frontendUrl);
        }
        configuration.setAllowedOriginPatterns(allowedOrigins);
        // Permite os métodos HTTP mais comuns
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        // Permite headers comuns, incluindo o de Autorização para o JWT
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "X-Requested-With"));
        // Permite credenciais (cookies, authorization headers, etc)
        configuration.setAllowCredentials(true);
        // Permite que o navegador exponha os headers de resposta
        configuration.setExposedHeaders(Arrays.asList("Authorization"));
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        // Aplica a configuração a todas as rotas da sua API
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
