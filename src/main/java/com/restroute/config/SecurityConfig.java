package com.restroute.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Value("${spring.h2.console.enabled:false}")
    private boolean h2ConsoleEnabled;

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(authorize -> {
                    authorize.requestMatchers("/admin/**", "/api/admin/**").hasRole("ADMIN");
                    if (h2ConsoleEnabled) {
                        authorize.requestMatchers("/h2-console/**").permitAll();
                    }
                    if (!h2ConsoleEnabled) {
                        authorize.requestMatchers("/h2-console/**").denyAll();
                    }
                    authorize
                            .requestMatchers("/", "/login", "/favicon.ico", "/css/**", "/js/**", "/api/**")
                            .permitAll();
                    authorize.anyRequest().permitAll();
                })
                .formLogin(form -> form.defaultSuccessUrl("/admin", true))
                .logout(logout -> logout.logoutSuccessUrl("/"))
                .csrf(csrf -> {
                    if (h2ConsoleEnabled) {
                        csrf.ignoringRequestMatchers("/h2-console/**");
                    }
                })
                .headers(headers -> {
                    if (h2ConsoleEnabled) {
                        headers.frameOptions(frame -> frame.sameOrigin());
                    }
                });

        return http.build();
    }
}
