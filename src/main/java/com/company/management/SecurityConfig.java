
package com.company.management;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;

@Configuration
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider(
            CustomUserDetailsService userDetailsService,
            PasswordEncoder passwordEncoder) {

        DaoAuthenticationProvider provider =
                new DaoAuthenticationProvider(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder);
        return provider;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            DaoAuthenticationProvider authenticationProvider)
            throws Exception {

        http
            .authenticationProvider(authenticationProvider)
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(
                    "/", "/index.html", "/style.css", "/login.html"
                ).permitAll()
                .requestMatchers("/api/auth/register").denyAll()
                .requestMatchers("/api/users/**")
                    .hasRole("ADMIN")
                .requestMatchers("/api/employees/my-profile")
                    .hasAnyRole("ADMIN", "MANAGER", "EMPLOYEE")
                .requestMatchers("/api/employees/my-manager")
                    .hasRole("EMPLOYEE")
                .requestMatchers("/api/employees/**")
                   .hasAnyRole("ADMIN", "MANAGER")
                
                .requestMatchers("/api/tasks/employee/**")
                    .hasAnyRole("ADMIN", "MANAGER", "EMPLOYEE")
                .requestMatchers(
                    org.springframework.http.HttpMethod.PUT,
                    "/api/tasks/*/status"
                ).hasAnyRole("ADMIN", "MANAGER", "EMPLOYEE")
                .requestMatchers("/api/tasks/**")
                    .hasAnyRole("ADMIN", "MANAGER")
                .requestMatchers("/admin-dashboard.html")
                    .hasRole("ADMIN")
                .requestMatchers("/manager-dashboard.html")
                    .hasAnyRole("ADMIN", "MANAGER")
                .requestMatchers("/employee-dashboard.html")
                    .hasAnyRole("ADMIN", "MANAGER", "EMPLOYEE")
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login.html")
                .loginProcessingUrl("/login")
                .failureHandler((request, response, exception) -> {
                    exception.printStackTrace();
                    response.sendRedirect("/login.html?error");
                })
                .successHandler((request, response, authentication) -> {
                    if (authentication.getAuthorities().stream()
                            .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
                        response.sendRedirect("/admin-dashboard.html");
                    } else if (authentication.getAuthorities().stream()
                            .anyMatch(a -> a.getAuthority().equals("ROLE_MANAGER"))) {
                        response.sendRedirect("/manager-dashboard.html");
                    } else if (authentication.getAuthorities().stream()
                            .anyMatch(a -> a.getAuthority().equals("ROLE_EMPLOYEE"))) {
                        response.sendRedirect("/employee-dashboard.html");
                    } else {
                        response.sendRedirect("/");
                    }
                })
                .permitAll()
            )
            .logout(logout -> logout
                .logoutSuccessUrl("/login.html")
            );

        return http.build();
    }
}
