package com.duoc.cursosonline.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Value("${spring.security.oauth2.resourceserver.jwt.jwk-set-uri}")
    private String jwkSetUri;

    @Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri}")
    private String issuerUri;

    @Value("${app.security.enabled:true}")
    private boolean securityEnabled;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        if (!securityEnabled) {
            http
                    .authorizeHttpRequests(authorize -> authorize
                            .anyRequest().permitAll()
                    )
                    .csrf(csrf -> csrf.disable())
                    .headers(headers -> headers
                            .frameOptions(frame -> frame.sameOrigin())
                    );
            return http.build();
        }

        http
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/h2-console/**", "/actuator/**").permitAll()
                        .anyRequest().authenticated()
                )
                .csrf(csrf -> csrf
                        .ignoringRequestMatchers("/h2-console/**")
                )
                .headers(headers -> headers
                        .frameOptions(frame -> frame.sameOrigin())
                )
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()));
        return http.build();
    }

    @Bean
    @ConditionalOnExpression("${app.security.enabled:true}")
    public JwtDecoder jwtDecoder() {
        NimbusJwtDecoder decoder = NimbusJwtDecoder.withJwkSetUri(jwkSetUri).build();
        decoder.setJwtValidator(JwtValidators.createDefaultWithIssuer(issuerUri));
        return decoder;
    }
}
