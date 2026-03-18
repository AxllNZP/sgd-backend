package com.mesapartes.sgd.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

// =============================================================
// OpenApiConfig — Swagger UI disponible en:
//   http://localhost:8080/swagger-ui/index.html
//   http://localhost:8080/v3/api-docs (JSON crudo)
// =============================================================
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Mesa de Partes Digital — API")
                        .version("1.0.0")
                        .description("""
                                Sistema de Gestión Documental para Mesa de Partes Digital.
                                Permite el registro, seguimiento y resolución de expedientes
                                presentados por personas naturales y jurídicas.
                                
                                **Roles del sistema:**
                                - `ADMINISTRADOR` — acceso total
                                - `MESA_PARTES` — gestión de documentos y respuestas
                                - `CIUDADANO` — gestión de su cuenta y expedientes propios
                                
                                **Autenticación:** Bearer JWT (obtenido en `/api/auth/login`)
                                """)
                        .contact(new Contact()
                                .name("Soporte SGD")
                                .email("soporte@mesapartes.gob.pe"))
                        .license(new License()
                                .name("Uso interno — todos los derechos reservados")))
                .servers(List.of(
                        new Server().url("http://localhost:8080").description("Desarrollo local"),
                        new Server().url("https://api.mesapartes.gob.pe").description("Producción")
                ))
                // Esquema de autenticación JWT
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"))
                .components(new Components()
                        .addSecuritySchemes("bearerAuth", new SecurityScheme()
                                .name("bearerAuth")
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("Ingrese el token JWT obtenido en /api/auth/login")));
    }
}