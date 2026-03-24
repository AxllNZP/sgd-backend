package com.mesapartes.sgd.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springdoc.core.utils.SpringDocUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

// =============================================================
// OpenApiConfig.java — VERSIÓN CORREGIDA
//
// PROBLEMA RAÍZ DEL 500:
//   SpringDoc no puede serializar org.springframework.data.domain.Pageable
//   (es una interfaz con métodos internos complejos). Al encontrarla en los
//   controllers que retornan Page<T>, intenta introspectarla → NullPointerException → 500.
//
// SOLUCIÓN:
//   SpringDocUtils.replaceWithClass() le indica a SpringDoc que sustituya
//   Pageable por nuestra clase simple PageableDocHelper, que tiene solo
//   3 campos planos (page, size, sort) que SÍ puede serializar.
// =============================================================
@Configuration
public class OpenApiConfig {

    static {
        // Este bloque static se ejecuta cuando Spring carga la clase,
        // ANTES del escaneo de controllers — es el momento correcto.
        SpringDocUtils.getConfig().replaceWithClass(
                org.springframework.data.domain.Pageable.class,
                PageableDocHelper.class
        );
    }

    // Clase auxiliar: reemplaza Pageable en la documentación generada.
    // SpringDoc la convierte en query params: ?page=0&size=20&sort=...
    public static class PageableDocHelper {
        public Integer page = 0;
        public Integer size = 20;
        public String  sort = "fechaHoraRegistro,desc";
    }

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Mesa de Partes Digital — API")
                        .version("1.0.0")
                        .description("""
                                Sistema de Gestión Documental para Mesa de Partes Digital.

                                **Roles del sistema:**
                                - `ADMINISTRADOR` — acceso total
                                - `MESA_PARTES` — gestión de documentos y respuestas
                                - `CIUDADANO` — gestión de su cuenta y expedientes propios

                                **Autenticación:** Bearer JWT.
                                Obtén el token en `POST /api/auth/login` y pégalo en "Authorize".
                                """)
                        .contact(new Contact()
                                .name("Soporte SGD")
                                .email("soporte@mesapartes.gob.pe"))
                        .license(new License()
                                .name("Uso interno — todos los derechos reservados")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8080")
                                .description("Desarrollo local")
                ))
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"))
                .components(new Components()
                        .addSecuritySchemes("bearerAuth", new SecurityScheme()
                                .name("bearerAuth")
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("Token JWT obtenido en POST /api/auth/login")));
    }
}