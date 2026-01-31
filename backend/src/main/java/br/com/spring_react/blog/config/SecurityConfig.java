package br.com.spring_react.blog.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final SecurityFilter securityFilter;

    public SecurityConfig(SecurityFilter securityFilter) {
        this.securityFilter = securityFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authorize -> authorize
                        // rotas públicas
                        .requestMatchers(HttpMethod.POST, "/sessions/login").permitAll()
                        .requestMatchers(HttpMethod.POST, "/users").permitAll()
                        .requestMatchers(HttpMethod.GET, "/users").permitAll()
                        .requestMatchers(HttpMethod.GET, "/users/profile/{id}").permitAll()
                        .requestMatchers(HttpMethod.GET, "/users/{id}").permitAll()

                        // rotas privadas (precisa estar logado)
                        .requestMatchers(HttpMethod.PATCH, "/users/{id}").authenticated()
                        .requestMatchers(HttpMethod.DELETE, "/users/{id}").authenticated()
                        .requestMatchers(HttpMethod.GET, "/posts").permitAll()
                        .requestMatchers(HttpMethod.GET, "/posts/{id}").permitAll()
                        .requestMatchers(HttpMethod.GET, "/posts/slug/{postSlug}").permitAll()
                        .requestMatchers(HttpMethod.GET, "/posts/author/{authorSlug}").permitAll()
                        .requestMatchers(HttpMethod.GET, "/posts/search").permitAll()
                        .requestMatchers(HttpMethod.POST, "/posts").authenticated()
                        .requestMatchers(HttpMethod.PATCH, "/posts/{id}").authenticated()
                        .requestMatchers(HttpMethod.DELETE, "/posts/{id}").authenticated()
                        .requestMatchers(HttpMethod.GET, "/sessions/me").authenticated()
                        .requestMatchers(HttpMethod.POST, "/sessions/logout").authenticated()

                        .anyRequest().authenticated() // tudo mais precisa de login, por enquanto
                )
                // o filtro vem ANTES do filtro padrão de usuário e senha
                .addFilterBefore(securityFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
