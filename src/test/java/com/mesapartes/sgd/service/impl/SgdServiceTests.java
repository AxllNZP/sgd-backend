package com.mesapartes.sgd.service.impl;

// =============================================================
// SgdServiceTests.java — Tests unitarios clave del sistema.
//
// Cubre los 3 servicios más críticos para el negocio:
//   1. RespuestaServiceImpl  — emitir respuesta + @Transactional
//   2. GestionCuentaServiceImpl — editar perfil + verificar respuesta
//   3. RegistroCiudadanoServiceImpl — registro + login
//
// Estrategia: Mockito para dependencias externas (repos, emailService).
// No se usa base de datos real — tests son rápidos y aislados.
// =============================================================

import com.mesapartes.sgd.dto.*;
import com.mesapartes.sgd.entity.*;
import com.mesapartes.sgd.exception.BusinessConflictException;
import com.mesapartes.sgd.exception.ResourceNotFoundException;
import com.mesapartes.sgd.repository.*;
import com.mesapartes.sgd.service.EmailService;
import com.mesapartes.sgd.service.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SgdServiceTests {

    // ═══════════════════════════════════════════════════════
    // 1. RespuestaServiceImpl
    // ═══════════════════════════════════════════════════════
    @Nested
    @DisplayName("RespuestaServiceImpl")
    class RespuestaTests {

        @Mock RespuestaDocumentoRepository respuestaRepository;
        @Mock DocumentoRepository documentoRepository;
        @Mock HistorialEstadoRepository historialEstadoRepository;
        @Mock EmailService emailService;

        @InjectMocks RespuestaServiceImpl service;

        private Documento documento;
        private RespuestaRequestDTO request;

        @BeforeEach
        void setUp() {
            documento = new Documento();
            documento.setNumeroTramite("MP-TEST-001");
            documento.setEmailRemitente("ciudadano@test.com");
            documento.setRemitente("Juan Pérez");
            documento.setEstado(EstadoDocumento.EN_PROCESO);

            request = new RespuestaRequestDTO();
            request.setContenido("Respuesta oficial al trámite.");
            request.setUsuarioResponsable("Ana Admin");
            request.setEnviarEmail(false);
        }

        @Test
        @DisplayName("Debe guardar respuesta y archivar documento")
        void debeGuardarRespuestaYArchivarDocumento() {
            when(documentoRepository.findByNumeroTramite("MP-TEST-001"))
                    .thenReturn(Optional.of(documento));

            RespuestaDocumento saved = new RespuestaDocumento();
            saved.setId(UUID.randomUUID());
            saved.setContenido(request.getContenido());
            saved.setUsuarioResponsable(request.getUsuarioResponsable());
            saved.setEnviadoPorEmail(false);
            saved.setDocumento(documento);

            when(respuestaRepository.save(any())).thenReturn(saved);

            RespuestaResponseDTO result = service.emitirRespuesta("MP-TEST-001", request);

            // La respuesta se guardó
            assertThat(result.getContenido()).isEqualTo("Respuesta oficial al trámite.");
            // El documento se archivó
            assertThat(documento.getEstado()).isEqualTo(EstadoDocumento.ARCHIVADO);
            // El historial se registró
            verify(historialEstadoRepository).save(any(HistorialEstado.class));
            // El email NO se envió (request.enviarEmail = false)
            verifyNoInteractions(emailService);
        }

        @Test
        @DisplayName("Si email falla, respuesta debe guardarse igual (best-effort)")
        void siEmailFallaRespuestaDebeGuardarse() {
            request.setEnviarEmail(true);

            when(documentoRepository.findByNumeroTramite("MP-TEST-001"))
                    .thenReturn(Optional.of(documento));

            RespuestaDocumento saved = new RespuestaDocumento();
            saved.setId(UUID.randomUUID());
            saved.setContenido(request.getContenido());
            saved.setUsuarioResponsable(request.getUsuarioResponsable());
            saved.setEnviadoPorEmail(false);
            saved.setDocumento(documento);

            when(respuestaRepository.save(any())).thenReturn(saved);

            // SMTP falla
            doThrow(new RuntimeException("SMTP connection failed"))
                    .when(emailService).enviarRespuestaFormal(any(), any(), any(), any());

            // NO debe lanzar excepción — best-effort
            assertThatCode(() -> service.emitirRespuesta("MP-TEST-001", request))
                    .doesNotThrowAnyException();

            // La respuesta SÍ se guardó a pesar del fallo de email
            verify(respuestaRepository, atLeastOnce()).save(any());
        }

        @Test
        @DisplayName("Debe lanzar excepción si el trámite no existe")
        void debeLanzarExcepcionSiTramiteNoExiste() {
            when(documentoRepository.findByNumeroTramite("INEXISTENTE"))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.emitirRespuesta("INEXISTENTE", request))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("no encontrado");
        }
    }

    // ═══════════════════════════════════════════════════════
    // 2. GestionCuentaServiceImpl
    // ═══════════════════════════════════════════════════════
    @Nested
    @DisplayName("GestionCuentaServiceImpl")
    class GestionCuentaTests {

        @Mock PersonaNaturalRepository naturalRepo;
        @Mock PersonaJuridicaRepository juridicaRepo;
        @Mock ContactoNotificacionRepository contactoRepo;
        @Mock PasswordEncoder passwordEncoder;

        @InjectMocks GestionCuentaServiceImpl service;

        private PersonaNatural persona;

        @BeforeEach
        void setUp() {
            persona = new PersonaNatural();
            persona.setNumeroDocumento("73005065");
            persona.setNombres("Axell");
            persona.setApellidoPaterno("Zurita");
            persona.setApellidoMaterno("Pacheco");
            persona.setEmail("axell@test.com");
            persona.setDireccion("Av. Test 123");
            persona.setTelefono("987654321");
            persona.setDepartamento("Lima");
            persona.setProvincia("Lima");
            persona.setDistrito("SJM");
            persona.setPreguntaSeguridad(PreguntaSeguridad.CIUDAD_NACIMIENTO);
            persona.setRespuestaSeguridad("lima"); // guardada en lowercase
        }

        @Test
        @DisplayName("Editar perfil debe fallar si la respuesta de seguridad es incorrecta")
        void editarPerfilDebeRechazarRespuestaIncorrecta() {
            when(naturalRepo.findByNumeroDocumento("73005065"))
                    .thenReturn(Optional.of(persona));

            EditarNaturalRequestDTO request = new EditarNaturalRequestDTO();
            request.setDireccion("Av. Nueva 456");
            request.setTelefono("999888777");
            request.setEmail("axell@test.com");
            request.setPreguntaSeguridad(PreguntaSeguridad.CIUDAD_NACIMIENTO);
            request.setRespuestaSeguridad("medellin"); // INCORRECTA

            assertThatThrownBy(() -> service.editarNatural("73005065", request))
                    .isInstanceOf(BusinessConflictException.class)
                    .hasMessageContaining("incorrecta");
        }

        @Test
        @DisplayName("Editar perfil debe tener éxito con la respuesta correcta")
        void editarPerfilDebeActualizarConRespuestaCorrecta() {
            when(naturalRepo.findByNumeroDocumento("73005065"))
                    .thenReturn(Optional.of(persona));
            when(naturalRepo.save(any())).thenReturn(persona);

            EditarNaturalRequestDTO request = new EditarNaturalRequestDTO();
            request.setDireccion("Av. Nueva 456");
            request.setTelefono("999888777");
            request.setEmail("axell@test.com");
            request.setPreguntaSeguridad(PreguntaSeguridad.CIUDAD_NACIMIENTO);
            request.setRespuestaSeguridad("LIMA"); // correcta, diferente capitalización

            // No debe lanzar excepción
            assertThatCode(() -> service.editarNatural("73005065", request))
                    .doesNotThrowAnyException();

            // El campo fue actualizado
            assertThat(persona.getDireccion()).isEqualTo("Av. Nueva 456");
        }

        @Test
        @DisplayName("Obtener perfil debe lanzar excepción si el documento no existe")
        void obtenerPerfilDebeLanzarExcepcionSiNoExiste() {
            when(naturalRepo.findByNumeroDocumento("99999999"))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.obtenerPerfilNatural("99999999"))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    // ═══════════════════════════════════════════════════════
    // 3. RegistroCiudadanoServiceImpl — Login
    // ═══════════════════════════════════════════════════════
    @Nested
    @DisplayName("RegistroCiudadanoServiceImpl — Login")
    class LoginTests {

        @Mock PersonaNaturalRepository naturalRepo;
        @Mock PersonaJuridicaRepository juridicaRepo;
        @Mock PasswordEncoder passwordEncoder;
        @Mock EmailService emailService;
        @Mock JwtService jwtService;

        @InjectMocks RegistroCiudadanoServiceImpl service;

        @Test
        @DisplayName("Login debe fallar si la cuenta no está verificada")
        void loginDebeRechazarCuentaNoVerificada() {
            PersonaNatural persona = new PersonaNatural();
            persona.setNumeroDocumento("73005065");
            persona.setPassword("$2a$10$hashedpassword");
            persona.setVerificado(false);  // no verificada
            persona.setActivo(true);

            when(naturalRepo.findByNumeroDocumento("73005065"))
                    .thenReturn(Optional.of(persona));
            // No stub de passwordEncoder — el login falla ANTES de llegar al password
            // porque persona.isVerificado() == false

            LoginCiudadanoRequestDTO request = new LoginCiudadanoRequestDTO();
            request.setTipoPersna("NATURAL");
            request.setIdentificador("73005065");
            request.setPassword("MiPassword123!");

            assertThatThrownBy(() -> service.loginCiudadano(request))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("verificada");
        }

        @Test
        @DisplayName("Login debe fallar si la cuenta está desactivada")
        void loginDebeRechazarCuentaDesactivada() {
            PersonaNatural persona = new PersonaNatural();
            persona.setNumeroDocumento("73005065");
            persona.setPassword("$2a$10$hashedpassword");
            persona.setVerificado(true);
            persona.setActivo(false); // desactivada

            when(naturalRepo.findByNumeroDocumento("73005065"))
                    .thenReturn(Optional.of(persona));
            // No stub de passwordEncoder — el login falla ANTES de llegar al password
            // porque persona.isActivo() == false

            LoginCiudadanoRequestDTO request = new LoginCiudadanoRequestDTO();
            request.setTipoPersna("NATURAL");
            request.setIdentificador("73005065");
            request.setPassword("MiPassword123!");

            assertThatThrownBy(() -> service.loginCiudadano(request))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("desactivada");
        }

        @Test
        @DisplayName("Registro debe rechazar documento duplicado")
        void registroDebeRechazarDocumentoDuplicado() {
            when(naturalRepo.existsByNumeroDocumento("73005065")).thenReturn(true);

            RegistroNaturalRequestDTO request = new RegistroNaturalRequestDTO();
            request.setNumeroDocumento("73005065");

            assertThatThrownBy(() -> service.registrarNatural(request))
                    .isInstanceOf(BusinessConflictException.class)
                    .hasMessageContaining("documento");
        }
    }
}