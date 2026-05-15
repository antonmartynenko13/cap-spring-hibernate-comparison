package com.example.springdemo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
// @EnableMethodSecurity enables @PreAuthorize on controller/service methods.
// This replaces the deprecated @EnableGlobalMethodSecurity(prePostEnabled=true) from Spring Security 5.x.
@EnableMethodSecurity
public class SecurityConfiguration {

    // CAP equivalent: cds.security.mock.users in application.yaml
    @Bean
    public UserDetailsService userDetailsService() {
        // {noop} prefix tells Spring Security that the password is stored in plaintext.
        // Acceptable for a demo. Use BCryptPasswordEncoder in production.
        var admin = User.withUsername("admin1")
                .password("{noop}admin1pass")
                .roles("ADMIN")
                .build();

        var user = User.withUsername("user1")
                .password("{noop}user1pass")
                .roles("USER")
                .build();

        return new InMemoryUserDetailsManager(admin, user);
    }

    // CAP equivalent: @requires: 'authenticated-user' + @restrict on service entities
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // REST API – no browser sessions, so CSRF protection is unnecessary
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        // Allow H2 console (uses iframes, needs frameOptions disabled below)
                        .requestMatchers("/h2-console/**").permitAll()
                        // Everything else requires authentication;
                        // fine-grained role checks are done via @PreAuthorize
                        .anyRequest().authenticated()
                )
                .httpBasic(httpBasic -> {})
                // H2 console is served in an iframe – disable X-Frame-Options for it
                .headers(headers -> headers.frameOptions(frame -> frame.disable()));

        return http.build();
    }
}
