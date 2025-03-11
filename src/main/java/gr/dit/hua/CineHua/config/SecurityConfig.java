package gr.dit.hua.CineHua.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().permitAll() // Allows all requests without authentication
                )
                .csrf(csrf -> csrf.disable()) // Disables CSRF protection (optional)
                .formLogin(login -> login.disable()) // Disables login form
                .httpBasic(basic -> basic.disable()); // Disables basic auth

        return http.build();
    }
}