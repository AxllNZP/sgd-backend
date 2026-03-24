package com.mesapartes.sgd.service.impl;

import com.mesapartes.sgd.dto.*;
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
import java.security.SecureRandom;



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
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    // ===== PASO 1: SOLICITAR RECUPERACIÓN =====
    @Override
    public void solicitarRecuperacion(RecuperacionSolicitarDTO request) {

        String tipo = request.getTipoPersna().toUpperCase();

        if ("NATURAL".equals(tipo)) {

            PersonaNatural persona = obtenerPersonaNatural(request.getIdentificador());

            validarEmail(persona.getEmail(), request.getEmail());
            validarCuentaActiva(persona.isActivo());

            String codigo = generarCodigo();

            persona.setCodigoVerificacion(codigo);
            persona.setCodigoExpiracion(LocalDateTime.now().plusMinutes(MINUTOS_EXPIRACION));

            naturalRepo.save(persona);

            emailService.enviarCodigoRecuperacion(
                    persona.getEmail(),
                    persona.getNombres() + " " + persona.getApellidoPaterno(),
                    codigo
            );

        } else if ("JURIDICA".equals(tipo)) {

            PersonaJuridica empresa = obtenerPersonaJuridica(request.getIdentificador());

            validarEmail(empresa.getEmailRepresentante(), request.getEmail());
            validarCuentaActiva(empresa.isActivo());

            String codigo = generarCodigo();

            empresa.setCodigoVerificacion(codigo);
            empresa.setCodigoExpiracion(LocalDateTime.now().plusMinutes(MINUTOS_EXPIRACION));

            juridicaRepo.save(empresa);

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

            PersonaNatural persona = obtenerPersonaNatural(request.getIdentificador());

            validarCodigo(
                    persona.getCodigoVerificacion(),
                    persona.getCodigoExpiracion(),
                    request.getCodigo()
            );

        } else if ("JURIDICA".equals(tipo)) {

            PersonaJuridica empresa = obtenerPersonaJuridica(request.getIdentificador());

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

        if (!request.getNuevaPassword().equals(request.getConfirmarPassword())) {
            throw new RuntimeException("Las contraseñas no coinciden");
        }

        if ("NATURAL".equals(tipo)) {

            PersonaNatural persona = obtenerPersonaNatural(request.getIdentificador());

            validarSesionRecuperacion(persona.getCodigoVerificacion(), persona.getCodigoExpiracion());

            persona.setPassword(passwordEncoder.encode(request.getNuevaPassword()));
            limpiarCodigoRecuperacion(persona);

            naturalRepo.save(persona);

        } else if ("JURIDICA".equals(tipo)) {

            PersonaJuridica empresa = obtenerPersonaJuridica(request.getIdentificador());

            validarSesionRecuperacion(empresa.getCodigoVerificacion(), empresa.getCodigoExpiracion());

            empresa.setPassword(passwordEncoder.encode(request.getNuevaPassword()));
            limpiarCodigoRecuperacion(empresa);

            juridicaRepo.save(empresa);

        } else {
            throw new RuntimeException("Tipo de persona inválido: " + tipo);
        }
    }

    // ===== VÍA B: OBTENER PREGUNTA =====
    @Override
    public PreguntaSeguridadResponseDTO obtenerPreguntaSeguridad(String tipoPersna, String identificador) {

        String tipo = tipoPersna.toUpperCase();
        PreguntaSeguridad pregunta;

        if ("NATURAL".equals(tipo)) {

            PersonaNatural persona = obtenerPersonaNatural(identificador);
            validarCuentaActiva(persona.isActivo());
            pregunta = persona.getPreguntaSeguridad();

        } else if ("JURIDICA".equals(tipo)) {

            PersonaJuridica empresa = obtenerPersonaJuridica(identificador);
            validarCuentaActiva(empresa.isActivo());
            pregunta = empresa.getPreguntaSeguridad();

        } else {
            throw new RuntimeException("Tipo de persona inválido: " + tipo);
        }

        return new PreguntaSeguridadResponseDTO(pregunta, pregunta.getDescripcion());
    }

    // ===== VÍA B: VERIFICAR RESPUESTA =====
    @Override
    public void verificarRespuestaSecreta(VerificarPreguntaDTO request) {

        String tipo = request.getTipoPersna().toUpperCase();

        if ("NATURAL".equals(tipo)) {

            PersonaNatural persona = obtenerPersonaNatural(request.getIdentificador());

            validarPregunta(persona.getPreguntaSeguridad(), request.getPreguntaSeguridad());
            validarRespuesta(persona.getRespuestaSeguridad(), request.getRespuesta());

            generarSesionRecuperacion(persona);
            naturalRepo.save(persona);

        } else if ("JURIDICA".equals(tipo)) {

            PersonaJuridica empresa = obtenerPersonaJuridica(request.getIdentificador());

            validarPregunta(empresa.getPreguntaSeguridad(), request.getPreguntaSeguridad());
            validarRespuesta(empresa.getRespuestaSeguridad(), request.getRespuesta());

            generarSesionRecuperacion(empresa);
            juridicaRepo.save(empresa);

        } else {
            throw new RuntimeException("Tipo de persona inválido: " + tipo);
        }
    }

    // ===== MÉTODOS PRIVADOS =====

    private PersonaNatural obtenerPersonaNatural(String documento) {
        return naturalRepo.findByNumeroDocumento(documento)
                .orElseThrow(() -> new RuntimeException("Cuenta no encontrada"));
    }

    private PersonaJuridica obtenerPersonaJuridica(String ruc) {
        return juridicaRepo.findByRuc(ruc)
                .orElseThrow(() -> new RuntimeException("Cuenta no encontrada"));
    }

    private void validarEmail(String registrado, String ingresado) {
        if (!registrado.equalsIgnoreCase(ingresado)) {
            throw new RuntimeException("El email no coincide con el registrado en la cuenta");
        }
    }

    private void validarCuentaActiva(boolean activo) {
        if (!activo) {
            throw new RuntimeException("Cuenta desactivada. Contacte al administrador.");
        }
    }

    private void validarSesionRecuperacion(String codigo, LocalDateTime expiracion) {

        if (codigo == null) {
            throw new RuntimeException("Sesión de recuperación inválida. Inicie el proceso nuevamente.");
        }

        if (LocalDateTime.now().isAfter(expiracion)) {
            throw new RuntimeException("La sesión de recuperación expiró. Inicie el proceso nuevamente.");
        }
    }

    private void limpiarCodigoRecuperacion(PersonaNatural persona) {
        persona.setCodigoVerificacion(null);
        persona.setCodigoExpiracion(null);
    }

    private void limpiarCodigoRecuperacion(PersonaJuridica empresa) {
        empresa.setCodigoVerificacion(null);
        empresa.setCodigoExpiracion(null);
    }

    private void validarPregunta(PreguntaSeguridad guardada, PreguntaSeguridad ingresada) {
        if (!guardada.equals(ingresada)) {
            throw new RuntimeException("La pregunta de seguridad no coincide");
        }
    }

    private void validarRespuesta(String guardada, String ingresada) {

        String respuestaGuardada = guardada.trim().toLowerCase();
        String respuestaIngresada = ingresada.trim().toLowerCase();

        if (!respuestaGuardada.equals(respuestaIngresada)) {
            throw new RuntimeException("La respuesta de seguridad es incorrecta");
        }
    }

    private void generarSesionRecuperacion(PersonaNatural persona) {
        persona.setCodigoVerificacion(generarCodigo());
        persona.setCodigoExpiracion(LocalDateTime.now().plusMinutes(MINUTOS_EXPIRACION));
    }

    private void generarSesionRecuperacion(PersonaJuridica empresa) {
        empresa.setCodigoVerificacion(generarCodigo());
        empresa.setCodigoExpiracion(LocalDateTime.now().plusMinutes(MINUTOS_EXPIRACION));
    }

    private String generarCodigo() {
        int codigo = 10_000_000 + SECURE_RANDOM.nextInt(90_000_000);
        return String.valueOf(codigo);
    }

    private void validarCodigo(String codigoGuardado, LocalDateTime expiracion, String codigoIngresado) {

        if (codigoGuardado == null) {
            throw new RuntimeException("No hay un proceso de recuperación activo. Inicie el proceso nuevamente.");
        }

        if (LocalDateTime.now().isAfter(expiracion)) {
            throw new RuntimeException("El código ha expirado. Solicite uno nuevo.");
        }

        if (!codigoGuardado.equals(codigoIngresado)) {
            throw new RuntimeException("El código ingresado es incorrecto");
        }
    }
}