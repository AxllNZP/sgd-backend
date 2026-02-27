package com.mesapartes.sgd.service.impl;

import com.mesapartes.sgd.dto.NuevaPasswordDTO;
import com.mesapartes.sgd.dto.PreguntaSeguridadResponseDTO;
import com.mesapartes.sgd.dto.RecuperacionSolicitarDTO;
import com.mesapartes.sgd.dto.RecuperacionVerificarCodigoDTO;
import com.mesapartes.sgd.dto.VerificarPreguntaDTO;
import com.mesapartes.sgd.entity.PersonaJuridica;
import com.mesapartes.sgd.entity.PersonaNatural;
import com.mesapartes.sgd.entity.PreguntaSeguridad;
import com.mesapartes.sgd.repository.PersonaJuridicaRepository;
import com.mesapartes.sgd.repository.PersonaNaturalRepository;
import com.mesapartes.sgd.service.EmailService;
import com.mesapartes.sgd.service.RecuperacionPasswordService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class RecuperacionPasswordServiceImpl implements RecuperacionPasswordService {

    private final PersonaNaturalRepository naturalRepo;
    private final PersonaJuridicaRepository juridicaRepo;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;

    private static final int MINUTOS_EXPIRACION = 10;

    // ===== PASO 1: SOLICITAR RECUPERACIÓN =====
    @Override
    public void solicitarRecuperacion(RecuperacionSolicitarDTO request) {
        String tipo = request.getTipoPersna().toUpperCase();

        if ("NATURAL".equals(tipo)) {
            PersonaNatural persona = naturalRepo
                    .findByNumeroDocumento(request.getIdentificador())
                    .orElseThrow(() -> new RuntimeException(
                            "No se encontró una cuenta con ese documento"));

            // Validar que el email coincida con el registrado
            if (!persona.getEmail().equalsIgnoreCase(request.getEmail())) {
                throw new RuntimeException(
                        "El email no coincide con el registrado en la cuenta");
            }

            if (!persona.isActivo()) {
                throw new RuntimeException(
                        "Cuenta desactivada. Contacte al administrador.");
            }

            // Generar y guardar código
            String codigo = generarCodigo();
            persona.setCodigoVerificacion(codigo);
            persona.setCodigoExpiracion(
                    LocalDateTime.now().plusMinutes(MINUTOS_EXPIRACION));
            naturalRepo.save(persona);

            // Enviar email
            emailService.enviarCodigoRecuperacion(
                    persona.getEmail(),
                    persona.getNombres() + " " + persona.getApellidoPaterno(),
                    codigo
            );

        } else if ("JURIDICA".equals(tipo)) {
            PersonaJuridica empresa = juridicaRepo
                    .findByRuc(request.getIdentificador())
                    .orElseThrow(() -> new RuntimeException(
                            "No se encontró una cuenta con ese RUC"));

            // Validar que el email coincida con el del representante
            if (!empresa.getEmailRepresentante().equalsIgnoreCase(request.getEmail())) {
                throw new RuntimeException(
                        "El email no coincide con el registrado en la cuenta");
            }

            if (!empresa.isActivo()) {
                throw new RuntimeException(
                        "Cuenta desactivada. Contacte al administrador.");
            }

            // Generar y guardar código
            String codigo = generarCodigo();
            empresa.setCodigoVerificacion(codigo);
            empresa.setCodigoExpiracion(
                    LocalDateTime.now().plusMinutes(MINUTOS_EXPIRACION));
            juridicaRepo.save(empresa);

            // Enviar email
            emailService.enviarCodigoRecuperacion(
                    empresa.getEmailRepresentante(),
                    empresa.getRazonSocial(),
                    codigo
            );

        } else {
            throw new RuntimeException("Tipo de persona inválido: " + tipo);
        }
    }

    // ===== PASO 2: VERIFICAR CÓDIGO =====
    @Override
    public void verificarCodigoRecuperacion(RecuperacionVerificarCodigoDTO request) {
        String tipo = request.getTipoPersna().toUpperCase();

        if ("NATURAL".equals(tipo)) {
            PersonaNatural persona = naturalRepo
                    .findByNumeroDocumento(request.getIdentificador())
                    .orElseThrow(() -> new RuntimeException("Cuenta no encontrada"));

            validarCodigo(
                    persona.getCodigoVerificacion(),
                    persona.getCodigoExpiracion(),
                    request.getCodigo()
            );

            // Código válido: lo dejamos activo para que el paso 3 también lo use
            // como token de autorización de cambio de contraseña

        } else if ("JURIDICA".equals(tipo)) {
            PersonaJuridica empresa = juridicaRepo
                    .findByRuc(request.getIdentificador())
                    .orElseThrow(() -> new RuntimeException("Cuenta no encontrada"));

            validarCodigo(
                    empresa.getCodigoVerificacion(),
                    empresa.getCodigoExpiracion(),
                    request.getCodigo()
            );

        } else {
            throw new RuntimeException("Tipo de persona inválido: " + tipo);
        }
    }

    // ===== PASO 3: ESTABLECER NUEVA CONTRASEÑA =====
    @Override
    public void establecerNuevaPassword(NuevaPasswordDTO request) {
        String tipo = request.getTipoPersna().toUpperCase();

        // Validar que ambas contraseñas coincidan
        if (!request.getNuevaPassword().equals(request.getConfirmarPassword())) {
            throw new RuntimeException(
                    "Las contraseñas no coinciden");
        }

        if ("NATURAL".equals(tipo)) {
            PersonaNatural persona = naturalRepo
                    .findByNumeroDocumento(request.getIdentificador())
                    .orElseThrow(() -> new RuntimeException("Cuenta no encontrada"));

            // Verificar que aún tenga un código activo y vigente
            // (garantiza que pasó por los pasos 1 y 2)
            if (persona.getCodigoVerificacion() == null) {
                throw new RuntimeException(
                        "Sesión de recuperación inválida. Inicie el proceso nuevamente.");
            }
            if (LocalDateTime.now().isAfter(persona.getCodigoExpiracion())) {
                throw new RuntimeException(
                        "La sesión de recuperación expiró. Inicie el proceso nuevamente.");
            }

            // Guardar nueva contraseña y limpiar código
            persona.setPassword(passwordEncoder.encode(request.getNuevaPassword()));
            persona.setCodigoVerificacion(null);
            persona.setCodigoExpiracion(null);
            naturalRepo.save(persona);

        } else if ("JURIDICA".equals(tipo)) {
            PersonaJuridica empresa = juridicaRepo
                    .findByRuc(request.getIdentificador())
                    .orElseThrow(() -> new RuntimeException("Cuenta no encontrada"));

            if (empresa.getCodigoVerificacion() == null) {
                throw new RuntimeException(
                        "Sesión de recuperación inválida. Inicie el proceso nuevamente.");
            }
            if (LocalDateTime.now().isAfter(empresa.getCodigoExpiracion())) {
                throw new RuntimeException(
                        "La sesión de recuperación expiró. Inicie el proceso nuevamente.");
            }

            empresa.setPassword(passwordEncoder.encode(request.getNuevaPassword()));
            empresa.setCodigoVerificacion(null);
            empresa.setCodigoExpiracion(null);
            juridicaRepo.save(empresa);

        } else {
            throw new RuntimeException("Tipo de persona inválido: " + tipo);
        }
    }

    // ===== VÍA B - PASO 1: OBTENER PREGUNTA SECRETA =====
    @Override
    public PreguntaSeguridadResponseDTO obtenerPreguntaSeguridad(String tipoPersna,
                                                                 String identificador) {
        String tipo = tipoPersna.toUpperCase();
        PreguntaSeguridad pregunta;

        if ("NATURAL".equals(tipo)) {
            PersonaNatural persona = naturalRepo
                    .findByNumeroDocumento(identificador)
                    .orElseThrow(() -> new RuntimeException(
                            "No se encontró una cuenta con ese documento"));

            if (!persona.isActivo()) {
                throw new RuntimeException("Cuenta desactivada. Contacte al administrador.");
            }

            pregunta = persona.getPreguntaSeguridad();

        } else if ("JURIDICA".equals(tipo)) {
            PersonaJuridica empresa = juridicaRepo
                    .findByRuc(identificador)
                    .orElseThrow(() -> new RuntimeException(
                            "No se encontró una cuenta con ese RUC"));

            if (!empresa.isActivo()) {
                throw new RuntimeException("Cuenta desactivada. Contacte al administrador.");
            }

            pregunta = empresa.getPreguntaSeguridad();

        } else {
            throw new RuntimeException("Tipo de persona inválido: " + tipo);
        }

        return new PreguntaSeguridadResponseDTO(pregunta, pregunta.getDescripcion());
    }

    // ===== VÍA B - PASO 2: VERIFICAR RESPUESTA SECRETA =====
    @Override
    public void verificarRespuestaSecreta(VerificarPreguntaDTO request) {
        String tipo = request.getTipoPersna().toUpperCase();

        if ("NATURAL".equals(tipo)) {
            PersonaNatural persona = naturalRepo
                    .findByNumeroDocumento(request.getIdentificador())
                    .orElseThrow(() -> new RuntimeException("Cuenta no encontrada"));

            // Verificar que la pregunta coincida
            if (!persona.getPreguntaSeguridad().equals(request.getPreguntaSeguridad())) {
                throw new RuntimeException("La pregunta de seguridad no coincide");
            }

            // Verificar respuesta (ignorando mayúsculas y espacios)
            String respuestaGuardada = persona.getRespuestaSeguridad().trim().toLowerCase();
            String respuestaIngresada = request.getRespuesta().trim().toLowerCase();

            if (!respuestaGuardada.equals(respuestaIngresada)) {
                throw new RuntimeException("La respuesta de seguridad es incorrecta");
            }

            // Respuesta correcta: generar token temporal de recuperación
            // Reutilizamos el campo codigoVerificacion como sesión de recuperación
            String tokenTemporal = generarCodigo();
            persona.setCodigoVerificacion(tokenTemporal);
            persona.setCodigoExpiracion(LocalDateTime.now().plusMinutes(MINUTOS_EXPIRACION));
            naturalRepo.save(persona);

        } else if ("JURIDICA".equals(tipo)) {
            PersonaJuridica empresa = juridicaRepo
                    .findByRuc(request.getIdentificador())
                    .orElseThrow(() -> new RuntimeException("Cuenta no encontrada"));

            if (!empresa.getPreguntaSeguridad().equals(request.getPreguntaSeguridad())) {
                throw new RuntimeException("La pregunta de seguridad no coincide");
            }

            String respuestaGuardada = empresa.getRespuestaSeguridad().trim().toLowerCase();
            String respuestaIngresada = request.getRespuesta().trim().toLowerCase();

            if (!respuestaGuardada.equals(respuestaIngresada)) {
                throw new RuntimeException("La respuesta de seguridad es incorrecta");
            }

            String tokenTemporal = generarCodigo();
            empresa.setCodigoVerificacion(tokenTemporal);
            empresa.setCodigoExpiracion(LocalDateTime.now().plusMinutes(MINUTOS_EXPIRACION));
            juridicaRepo.save(empresa);

        } else {
            throw new RuntimeException("Tipo de persona inválido: " + tipo);
        }
    }

    // ===== MÉTODOS PRIVADOS =====
    private String generarCodigo() {
        Random random = new Random();
        int numero = 100000 + random.nextInt(900000);
        return String.valueOf(numero);
    }

    private void validarCodigo(String codigoGuardado, LocalDateTime expiracion,
                               String codigoIngresado) {
        if (codigoGuardado == null) {
            throw new RuntimeException(
                    "No hay un proceso de recuperación activo. Inicie el proceso nuevamente.");
        }
        if (LocalDateTime.now().isAfter(expiracion)) {
            throw new RuntimeException(
                    "El código ha expirado. Solicite uno nuevo.");
        }
        if (!codigoGuardado.equals(codigoIngresado)) {
            throw new RuntimeException("El código ingresado es incorrecto");
        }
    }
}