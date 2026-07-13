package com.restroute.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/admin/**", "/api/admin/**")
                        .hasRole("ADMIN")
                        .requestMatchers("/", "/login", "/favicon.ico", "/css/**", "/js/**", "/api/**")
                        .permitAll()
                        .anyRequest()
                        .permitAll())
                .formLogin(form -> form.defaultSuccessUrl("/admin", true))
                .logout(logout -> logout.logoutSuccessUrl("/"))
                .csrf(Customizer.withDefaults());

        return http.build();
    }
}
