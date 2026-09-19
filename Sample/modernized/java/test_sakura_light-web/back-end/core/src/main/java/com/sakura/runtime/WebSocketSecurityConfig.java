package com.sakura.runtime;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Security configuration for WebSocket screen mode. Open access — no login required. Active only
 * when screen.renderer=websocket. Kept as an explicit @Configuration class to override Spring Boot
 * Security's default auto-config (which would otherwise re-enable HTTP Basic with an auto-generated
 * password). The COBOL application performs its own sign-on (e.g. CHKLOG); no separate
 * infrastructure login gate is imposed.
 */
@Configuration
@EnableWebSecurity
@ConditionalOnProperty(name = "screen.renderer", havingValue = "websocket")
public class WebSocketSecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
                .csrf(csrf -> csrf.disable())
                .formLogin(form -> form.disable())
                .httpBasic(basic -> basic.disable());
        return http.build();
    }
}
