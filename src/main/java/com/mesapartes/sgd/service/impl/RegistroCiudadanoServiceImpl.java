package com.mesapartes.sgd.service.impl;

import com.mesapartes.sgd.dto.*;
import com.mesapartes.sgd.entity.ContactoNotificacion;
import com.mesapartes.sgd.entity.PersonaJuridica;
import com.mesapartes.sgd.entity.PersonaNatural;
import com.mesapartes.sgd.entity.RolUsuario;
import com.mesapartes.sgd.exception.BusinessConflictException;
import com.mesapartes.sgd.exception.InvalidCredentialsException;
import com.mesapartes.sgd.exception.ResourceNotFoundException;
import com.mesapartes.sgd.repository.PersonaJuridicaRepository;
import com.mesapartes.sgd.repository.PersonaNaturalRepository;
import com.mesapartes.sgd.service.EmailService;
import com.mesapartes.sgd.service.JwtService;
import com.mesapartes.sgd.service.RegistroCiudadanoService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.context.ApplicationEventPublisher;

import java.security.SecureRandom;

import java.time.LocalDateTime;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class RegistroCiudadanoServiceImpl implements RegistroCiudadanoService {

    private final PersonaNaturalRepository naturalRepo;
    private final PersonaJuridicaRepository juridicaRepo;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final JwtService jwtService;
    private final ApplicationEventPublisher applicationEventPublisher;


    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static final int MINUTOS_EXPIRACION = 10;
    private static final int INTENTOS_MAXIMOS = 3;


    // ===== REGISTRAR NATURAL =====
    @Override
    @Transactional(rollbackFor = Exception.class)
    public RegistroResponseDTO registrarNatural(RegistroNaturalRequestDTO request) {

        validarDocumentoUnico(request.getNumeroDocumento());
        validarEmailUnico(request.getEmail());
        PersonaNatural persona = construirPersonaNatural(request);
        asignarCodigoVerificacion(persona);
        PersonaNatural guardada = naturalRepo.save(persona);
        applicationEventPublisher.publishEvent(
                new com.mesapartes.sgd.event.CodigoVerificacionEvent(this, guardada, null)
        );

        return new RegistroResponseDTO(
                "Registro exitoso. Revise su correo para activar su cuenta.",
                persona.getNumeroDocumento(),
                "NATURAL",
                true
        );
    }

    // ===== REGISTRAR JURIDICA =====
    @Override
    @Transactional(rollbackFor = Exception.class)
    public RegistroResponseDTO registrarJuridica(RegistroJuridicaRequestDTO request) {
        validarRucUnico(request.getRuc());
        PersonaJuridica empresa = construirPersonaJuridica(request);
        asignarCodigoVerificacion(empresa);
        PersonaJuridica guardada = juridicaRepo.save(empresa);
        agregarContactosNotificacion(guardada, request);
        juridicaRepo.save(guardada);

        applicationEventPublisher.publishEvent(
                new com.mesapartes.sgd.event.CodigoVerificacionEvent(this, null, guardada)
        );

        return new RegistroResponseDTO(
                "Registro exitoso. Revise el correo del representante legal para activar la cuenta.",
                guardada.getRuc(),
                "JURIDICA",
                true
        );
    }

    // ===== VERIFICAR CODIGO =====
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void verificarCodigo(VerificacionCodigoDTO request) {

        String tipo = request.getTipoPersna().toUpperCase();

        if ("NATURAL".equals(tipo)) {

            PersonaNatural persona = obtenerPersonaNatural(request.getIdentificador());

            validarYActivarCodigo(
                    persona.getCodigoVerificacion(),
                    persona.getCodigoExpiracion(),
                    persona.isVerificado(),
                    request.getCodigo()
            );

            activarCuentaNatural(persona);

        } else if ("JURIDICA".equals(tipo)) {

            PersonaJuridica empresa = obtenerPersonaJuridica(request.getIdentificador());

            validarYActivarCodigo(
                    empresa.getCodigoVerificacion(),
                    empresa.getCodigoExpiracion(),
                    empresa.isVerificado(),
                    request.getCodigo()
            );

            activarCuentaJuridica(empresa);

        } else {
            throw new RuntimeException("Tipo de persona inválido: " + tipo);
        }

    }

    // ===== REENVIAR CODIGO =====
    @Override
    public void reenviarCodigo(String tipoPersna, String identificador) {

        String tipo = tipoPersna.toUpperCase();

        if ("NATURAL".equals(tipo)) {

            PersonaNatural persona = obtenerPersonaNatural(identificador);

            validarNoVerificado(persona.isVerificado());

            asignarCodigoVerificacion(persona);
            naturalRepo.save(persona);

            enviarCodigoNatural(persona);

        } else if ("JURIDICA".equals(tipo)) {

            PersonaJuridica empresa = obtenerPersonaJuridica(identificador);

            validarNoVerificado(empresa.isVerificado());

            asignarCodigoVerificacion(empresa);
            juridicaRepo.save(empresa);

            enviarCodigoJuridica(empresa);

        } else {
            throw new RuntimeException("Tipo de persona inválido: " + tipo);
        }
    }

    // ===== LOGIN =====
    @Override
    public LoginResponseDTO loginCiudadano(LoginCiudadanoRequestDTO request) {

        String tipo = request.getTipoPersna().toUpperCase();

        if ("NATURAL".equals(tipo)) {

            PersonaNatural persona = obtenerPersonaNatural(request.getIdentificador());

            validarLogin(
                    persona.isVerificado(),
                    persona.isActivo(),
                    request.getPassword(),
                    persona.getPassword()
            );

            return generarLoginNatural(persona);

        } else if ("JURIDICA".equals(tipo)) {

            PersonaJuridica empresa = obtenerPersonaJuridica(request.getIdentificador());

            validarLogin(
                    empresa.isVerificado(),
                    empresa.isActivo(),
                    request.getPassword(),
                    empresa.getPassword()
            );

            return generarLoginJuridica(empresa);

        } else {
            throw new RuntimeException("Tipo de persona inválido: " + tipo);
        }
    }

    // ===== METODOS PRIVADOS =====

    private PersonaNatural construirPersonaNatural(RegistroNaturalRequestDTO request) {

        PersonaNatural persona = new PersonaNatural();

        persona.setTipoDocumento(request.getTipoDocumento());
        persona.setNumeroDocumento(request.getNumeroDocumento());
        persona.setNombres(request.getNombres());
        persona.setApellidoPaterno(request.getApellidoPaterno());
        persona.setApellidoMaterno(request.getApellidoMaterno());
        persona.setDepartamento(request.getDepartamento());
        persona.setProvincia(request.getProvincia());
        persona.setDistrito(request.getDistrito());
        persona.setDireccion(request.getDireccion());
        persona.setTelefono(request.getTelefono());
        persona.setEmail(request.getEmail());
        persona.setPassword(passwordEncoder.encode(request.getPassword()));
        persona.setPreguntaSeguridad(request.getPreguntaSeguridad());
        persona.setRespuestaSeguridad(request.getRespuestaSeguridad().trim().toLowerCase());
        persona.setAfiliadoBuzon(request.isAfiliadoBuzon());

        return persona;
    }

    private PersonaJuridica construirPersonaJuridica(RegistroJuridicaRequestDTO request) {

        PersonaJuridica empresa = new PersonaJuridica();

        empresa.setRuc(request.getRuc());
        empresa.setRazonSocial(request.getRazonSocial());
        empresa.setPassword(passwordEncoder.encode(request.getPassword()));
        empresa.setPreguntaSeguridad(request.getPreguntaSeguridad());
        empresa.setRespuestaSeguridad(request.getRespuestaSeguridad().trim().toLowerCase());

        empresa.setTipoDocRepresentante(request.getTipoDocRepresentante());
        empresa.setNumDocRepresentante(request.getNumDocRepresentante());
        empresa.setNombresRepresentante(request.getNombresRepresentante());
        empresa.setApellidoPaternoRepresentante(request.getApellidoPaternoRepresentante());
        empresa.setApellidoMaternoRepresentante(request.getApellidoMaternoRepresentante());
        empresa.setEmailRepresentante(request.getEmailRepresentante());

        empresa.setDepartamento(request.getDepartamento());
        empresa.setProvincia(request.getProvincia());
        empresa.setDistrito(request.getDistrito());
        empresa.setDireccion(request.getDireccion());
        empresa.setTelefono(request.getTelefono());
        empresa.setAfiliadoBuzon(request.isAfiliadoBuzon());

        return empresa;
    }

    private void asignarCodigoVerificacion(PersonaNatural persona) {
        persona.setCodigoVerificacion(generarCodigo());
        persona.setCodigoExpiracion(LocalDateTime.now().plusMinutes(MINUTOS_EXPIRACION));
    }

    private void asignarCodigoVerificacion(PersonaJuridica empresa) {
        empresa.setCodigoVerificacion(generarCodigo());
        empresa.setCodigoExpiracion(LocalDateTime.now().plusMinutes(MINUTOS_EXPIRACION));
    }

    private void enviarCodigoNatural(PersonaNatural persona) {
        emailService.enviarCodigoVerificacion(
                persona.getEmail(),
                persona.getNombres() + " " + persona.getApellidoPaterno(),
                persona.getCodigoVerificacion()
        );
    }

    private void enviarCodigoJuridica(PersonaJuridica empresa) {
        emailService.enviarCodigoVerificacion(
                empresa.getEmailRepresentante(),
                empresa.getRazonSocial(),
                empresa.getCodigoVerificacion()
        );
    }

    private void validarDocumentoUnico(String documento) {
        if (naturalRepo.existsByNumeroDocumento(documento)) {
            throw new BusinessConflictException("Ya existe una cuenta con el documento: " + documento);
        }
    }

    private void validarEmailUnico(String email) {
        if (naturalRepo.existsByEmail(email)) {
            throw new BusinessConflictException("Ya existe una cuenta con el email: " + email);
        }
    }

    private void validarRucUnico(String ruc) {
        if (juridicaRepo.existsByRuc(ruc)) {
            throw new BusinessConflictException("Ya existe una cuenta con el RUC: " + ruc);
        }
    }

    private PersonaNatural obtenerPersonaNatural(String documento) {
        return naturalRepo.findByNumeroDocumento(documento)
                .orElseThrow(() -> new ResourceNotFoundException("Cuenta no encontrada"));
    }

    private PersonaJuridica obtenerPersonaJuridica(String ruc) {
        return juridicaRepo.findByRuc(ruc)
                .orElseThrow(() -> new ResourceNotFoundException("Cuenta no encontrada"));
    }

    private void validarNoVerificado(boolean verificado) {
        if (verificado) {
            throw new RuntimeException("Esta cuenta ya fue verificada");
        }
    }

    private void validarLogin(boolean verificado, boolean activo, String passwordIngresado, String passwordGuardado) {

        if (!verificado) {
            throw new RuntimeException("Cuenta no verificada. Revise su correo.");
        }

        if (!activo) {
            throw new RuntimeException("Cuenta desactivada. Contacte al administrador.");
        }

        if (!passwordEncoder.matches(passwordIngresado, passwordGuardado)) {
            throw new InvalidCredentialsException("Credenciales incorrectas");
        }
    }

    private LoginResponseDTO generarLoginNatural(PersonaNatural persona) {

        String token = jwtService.generarToken(
                persona.getEmail(),
                RolUsuario.CIUDADANO.name()
        );

        return new LoginResponseDTO(
                token,
                persona.getEmail(),
                RolUsuario.CIUDADANO.name(),
                persona.getNombres() + " " + persona.getApellidoPaterno()
        );
    }

    private LoginResponseDTO generarLoginJuridica(PersonaJuridica empresa) {

        String token = jwtService.generarToken(
                empresa.getEmailRepresentante(),
                RolUsuario.CIUDADANO.name()
        );

        return new LoginResponseDTO(
                token,
                empresa.getEmailRepresentante(),
                RolUsuario.CIUDADANO.name(),
                empresa.getRazonSocial()
        );
    }

    private String generarCodigo() {
        int codigo = 10_000_000 + SECURE_RANDOM.nextInt(90_000_000);
        return String.valueOf(codigo);
    }

    private void validarYActivarCodigo(String codigoGuardado, LocalDateTime expiracion,
                                       boolean yaVerificado, String codigoIngresado) {

        if (yaVerificado) {
            throw new RuntimeException("Esta cuenta ya fue verificada anteriormente");
        }

        if (codigoGuardado == null) {
            throw new RuntimeException("No hay un código de verificación activo");
        }

        if (LocalDateTime.now().isAfter(expiracion)) {
            throw new RuntimeException("El código ha expirado. Solicite uno nuevo.");
        }

        if (!codigoGuardado.equals(codigoIngresado)) {
            throw new RuntimeException("El código ingresado es incorrecto");
        }
    }

    private void activarCuentaNatural(PersonaNatural persona) {
        persona.setVerificado(true);
        persona.setActivo(true);
        persona.setCodigoVerificacion(null);
        persona.setCodigoExpiracion(null);
        naturalRepo.save(persona);
    }

    private void activarCuentaJuridica(PersonaJuridica empresa) {
        empresa.setVerificado(true);
        empresa.setActivo(true);
        empresa.setCodigoVerificacion(null);
        empresa.setCodigoExpiracion(null);
        juridicaRepo.save(empresa);
    }

    private void agregarContactosNotificacion(PersonaJuridica empresa,
                                              RegistroJuridicaRequestDTO request) {

        if (request.getContactosNotificacion() == null) return;

        for (ContactoNotificacionDTO contactoDTO : request.getContactosNotificacion()) {

            ContactoNotificacion contacto = new ContactoNotificacion();

            contacto.setPersonaJuridica(empresa);
            contacto.setNombres(contactoDTO.getNombres());
            contacto.setEmail(contactoDTO.getEmail());
            contacto.setActivo(contactoDTO.isActivo());

            empresa.getContactosNotificacion().add(contacto);
        }

        juridicaRepo.save(empresa);
    }


}