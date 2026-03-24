package com.mesapartes.sgd.config;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
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

import java.util.Arrays;
import java.util.List;

// =============================================================
// SecurityConfig.java
// CORRECCIÓN: línea de Swagger movida ANTES de anyRequest().
//
// ¿Por qué fallaba?
//   Spring Security construye la cadena de reglas de arriba
//   hacia abajo. Una vez que encuentra anyRequest(), considera
//   que la configuración está "cerrada" y no acepta más reglas.
//   Agregar requestMatchers() después lanza:
//   "Can't configure mvcMatchers after anyRequest"
//
// Regla de oro: anyRequest() SIEMPRE es la ÚLTIMA regla.
// =============================================================
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

                        // ===== SWAGGER UI — debe ir antes de anyRequest =====
                        // Permite acceso público a la documentación de la API
                        .requestMatchers(
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/v3/api-docs/**",
                                "/v3/api-docs"
                        ).permitAll()

                        // ===== ADMIN CIUDADANOS =====
                        .requestMatchers("/api/admin/**")
                        .hasRole("ADMINISTRADOR")

                        // ===== AUTH =====
                        .requestMatchers("/api/auth/**").permitAll()

                        // ===== DOCUMENTOS PÚBLICOS =====
                        .requestMatchers(HttpMethod.POST, "/api/documentos").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/documentos/*").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/documentos/*/cargo").permitAll()

                        // ===== OPERACIONES INTERNAS =====
                        .requestMatchers(HttpMethod.GET, "/api/documentos").authenticated()
                        .requestMatchers("/api/documentos/*/estado")
                        .hasAnyRole("MESA_PARTES", "ADMINISTRADOR")
                        .requestMatchers("/api/documentos/*/area/**")
                        .hasAnyRole("MESA_PARTES", "ADMINISTRADOR")
                        .requestMatchers("/api/documentos/*/descargar**").authenticated()

                        // ===== DERIVACIONES =====
                        .requestMatchers("/api/derivaciones/**")
                        .hasAnyRole("MESA_PARTES", "ADMINISTRADOR")

                        // ===== RESPUESTAS =====
                        .requestMatchers("/api/respuestas/**")
                        .hasAnyRole("MESA_PARTES", "ADMINISTRADOR")

                        // ===== AREAS =====
                                // Lectura: MESA_PARTES puede listar áreas
                                .requestMatchers(HttpMethod.GET, "/api/areas")
                                .hasAnyRole("ADMINISTRADOR", "MESA_PARTES")

                        // Escritura: solo ADMINISTRADOR puede crear/eliminar
                                .requestMatchers("/api/areas/**")
                                .hasRole("ADMINISTRADOR")

                        // ===== USUARIOS =====
                        .requestMatchers("/api/usuarios/**")
                        .hasRole("ADMINISTRADOR")

                        // ===== CUENTA =====
                        .requestMatchers("/api/cuenta/**").authenticated()

                        // ← anyRequest SIEMPRE al final — regla de cierre
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
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("Authorization", "Content-Type", "Accept"));
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