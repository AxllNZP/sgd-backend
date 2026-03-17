package com.mesapartes.sgd.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.http.HttpMethod;
import org.springframework.beans.factory.annotation.Value;
import java.util.Arrays;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtFilter jwtFilter;
    @Value("${cors.allowed-origins}")
    private String allowedOrigins;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .authorizeHttpRequests(auth -> auth

                        // ===== ADMIN CIUDADANOS =====
                        .requestMatchers("/api/admin/**")
                        .hasRole("ADMINISTRADOR")

                        // ===== AUTH =====
                        .requestMatchers("/api/auth/**").permitAll()

                        // ===== DOCUMENTOS PUBLICOS =====
                        // Registro de documento
                        .requestMatchers(HttpMethod.POST, "/api/documentos").permitAll()

                        // Consulta pública por número de trámite
                        .requestMatchers(HttpMethod.GET, "/api/documentos/*").permitAll()

                        // Cargo público
                        .requestMatchers(HttpMethod.GET, "/api/documentos/*/cargo").permitAll()

                        // ===== OPERACIONES INTERNAS =====

                        // Listado completo
                        .requestMatchers(HttpMethod.GET, "/api/documentos").authenticated()

                        // Cambio de estado
                        .requestMatchers("/api/documentos/*/estado")
                        .hasAnyRole("MESA_PARTES", "ADMINISTRADOR")

                        // Asignación de área
                        .requestMatchers("/api/documentos/*/area/**")
                        .hasAnyRole("MESA_PARTES", "ADMINISTRADOR")

                        // Descarga de archivos internos
                        .requestMatchers("/api/documentos/*/descargar**")
                        .authenticated()

                        // ===== DERIVACIONES =====
                        .requestMatchers("/api/derivaciones/**")
                        .hasAnyRole("MESA_PARTES", "ADMINISTRADOR")

                        // ===== RESPUESTAS =====
                        .requestMatchers("/api/respuestas/**")
                        .hasAnyRole("MESA_PARTES", "ADMINISTRADOR")

                        // ===== AREAS =====
                        .requestMatchers("/api/areas/**")
                        .hasRole("ADMINISTRADOR")

                        // ===== USUARIOS =====
                        .requestMatchers("/api/usuarios/**")
                        .hasRole("ADMINISTRADOR")

                        // ===== CUENTA =====
                        .requestMatchers("/api/cuenta/**").authenticated()

                        // cualquier otro endpoint
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {

        CorsConfiguration config = new CorsConfiguration();

        List<String> origins = Arrays.asList(allowedOrigins.split(","));
        config.setAllowedOrigins(origins);

        config.setAllowedMethods(List.of(
                "GET",
                "POST",
                "PUT",
                "PATCH",
                "DELETE",
                "OPTIONS"
        ));

        config.setAllowedHeaders(List.of(
                "Authorization",
                "Content-Type",
                "Accept"
        ));

        config.setAllowCredentials(true);
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        return source;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}