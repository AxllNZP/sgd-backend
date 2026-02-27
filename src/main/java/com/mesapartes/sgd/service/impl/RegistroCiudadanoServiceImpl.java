package com.mesapartes.sgd.service.impl;

import com.mesapartes.sgd.dto.*;
import com.mesapartes.sgd.entity.*;
import com.mesapartes.sgd.repository.PersonaJuridicaRepository;
import com.mesapartes.sgd.repository.PersonaNaturalRepository;
import com.mesapartes.sgd.service.EmailService;
import com.mesapartes.sgd.service.JwtService;
import com.mesapartes.sgd.service.RegistroCiudadanoService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

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

    // Tiempo de validez del código: 10 minutos (como dice la guía)
    private static final int MINUTOS_EXPIRACION = 10;

    // ===== REGISTRAR PERSONA NATURAL =====
    @Override
    public RegistroResponseDTO registrarNatural(RegistroNaturalRequestDTO request) {

        // Validar que no exista el documento
        if (naturalRepo.existsByNumeroDocumento(request.getNumeroDocumento())) {
            throw new RuntimeException("Ya existe una cuenta con el documento: "
                    + request.getNumeroDocumento());
        }

        // Validar que no exista el email
        if (naturalRepo.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Ya existe una cuenta con el email: "
                    + request.getEmail());
        }

        // Construir entidad
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
        persona.setRespuestaSeguridad(
                request.getRespuestaSeguridad().trim().toLowerCase()
        );
        persona.setAfiliadoBuzon(request.isAfiliadoBuzon());

        // Generar y asignar código de verificación
        String codigo = generarCodigo();
        persona.setCodigoVerificacion(codigo);
        persona.setCodigoExpiracion(LocalDateTime.now().plusMinutes(MINUTOS_EXPIRACION));

        naturalRepo.save(persona);

        // Enviar email con código
        emailService.enviarCodigoVerificacion(
                persona.getEmail(),
                persona.getNombres() + " " + persona.getApellidoPaterno(),
                codigo
        );

        return new RegistroResponseDTO(
                "Registro exitoso. Revise su correo para activar su cuenta.",
                persona.getNumeroDocumento(),
                "NATURAL",
                true
        );
    }

    // ===== REGISTRAR PERSONA JURÍDICA =====
    @Override
    public RegistroResponseDTO registrarJuridica(RegistroJuridicaRequestDTO request) {

        // Validar RUC
        if (juridicaRepo.existsByRuc(request.getRuc())) {
            throw new RuntimeException("Ya existe una cuenta con el RUC: " + request.getRuc());
        }

        // Construir entidad
        PersonaJuridica empresa = new PersonaJuridica();
        empresa.setRuc(request.getRuc());
        empresa.setRazonSocial(request.getRazonSocial());
        empresa.setPassword(passwordEncoder.encode(request.getPassword()));
        empresa.setPreguntaSeguridad(request.getPreguntaSeguridad());
        empresa.setRespuestaSeguridad(
                request.getRespuestaSeguridad().trim().toLowerCase()
        );

        // Representante legal
        empresa.setTipoDocRepresentante(request.getTipoDocRepresentante());
        empresa.setNumDocRepresentante(request.getNumDocRepresentante());
        empresa.setNombresRepresentante(request.getNombresRepresentante());
        empresa.setApellidoPaternoRepresentante(request.getApellidoPaternoRepresentante());
        empresa.setApellidoMaternoRepresentante(request.getApellidoMaternoRepresentante());
        empresa.setEmailRepresentante(request.getEmailRepresentante());

        // Ubigeo
        empresa.setDepartamento(request.getDepartamento());
        empresa.setProvincia(request.getProvincia());
        empresa.setDistrito(request.getDistrito());
        empresa.setDireccion(request.getDireccion());
        empresa.setTelefono(request.getTelefono());

        empresa.setAfiliadoBuzon(request.isAfiliadoBuzon());

        // Código de verificación
        String codigo = generarCodigo();
        empresa.setCodigoVerificacion(codigo);
        empresa.setCodigoExpiracion(LocalDateTime.now().plusMinutes(MINUTOS_EXPIRACION));

        // Guardar empresa primero para obtener el ID
        PersonaJuridica guardada = juridicaRepo.save(empresa);

        // Agregar contactos de notificación
        if (request.getContactosNotificacion() != null) {
            for (ContactoNotificacionDTO contactoDTO : request.getContactosNotificacion()) {
                ContactoNotificacion contacto = new ContactoNotificacion();
                contacto.setPersonaJuridica(guardada);
                contacto.setNombres(contactoDTO.getNombres());
                contacto.setEmail(contactoDTO.getEmail());
                contacto.setActivo(contactoDTO.isActivo());
                guardada.getContactosNotificacion().add(contacto);
            }
            juridicaRepo.save(guardada);
        }

        // Enviar código al email del representante legal
        emailService.enviarCodigoVerificacion(
                guardada.getEmailRepresentante(),
                guardada.getRazonSocial(),
                codigo
        );

        return new RegistroResponseDTO(
                "Registro exitoso. Revise el correo del representante legal para activar la cuenta.",
                guardada.getRuc(),
                "JURIDICA",
                true
        );
    }

    // ===== VERIFICAR CÓDIGO =====
    @Override
    public void verificarCodigo(VerificacionCodigoDTO request) {
        String tipo = request.getTipoPersna().toUpperCase();

        if ("NATURAL".equals(tipo)) {
            PersonaNatural persona = naturalRepo.findByNumeroDocumento(request.getIdentificador())
                    .orElseThrow(() -> new RuntimeException("Cuenta no encontrada"));

            validarYActivarCodigo(
                    persona.getCodigoVerificacion(),
                    persona.getCodigoExpiracion(),
                    persona.isVerificado(),
                    request.getCodigo()
            );

            persona.setVerificado(true);
            persona.setActivo(true);
            persona.setCodigoVerificacion(null);
            persona.setCodigoExpiracion(null);
            naturalRepo.save(persona);

        } else if ("JURIDICA".equals(tipo)) {
            PersonaJuridica empresa = juridicaRepo.findByRuc(request.getIdentificador())
                    .orElseThrow(() -> new RuntimeException("Cuenta no encontrada"));

            validarYActivarCodigo(
                    empresa.getCodigoVerificacion(),
                    empresa.getCodigoExpiracion(),
                    empresa.isVerificado(),
                    request.getCodigo()
            );

            empresa.setVerificado(true);
            empresa.setActivo(true);
            empresa.setCodigoVerificacion(null);
            empresa.setCodigoExpiracion(null);
            juridicaRepo.save(empresa);

        } else {
            throw new RuntimeException("Tipo de persona inválido: " + tipo);
        }
    }

    // ===== REENVIAR CÓDIGO =====
    @Override
    public void reenviarCodigo(String tipoPersna, String identificador) {
        String tipo = tipoPersna.toUpperCase();
        String nuevoCodigo = generarCodigo();
        LocalDateTime nuevaExpiracion = LocalDateTime.now().plusMinutes(MINUTOS_EXPIRACION);

        if ("NATURAL".equals(tipo)) {
            PersonaNatural persona = naturalRepo.findByNumeroDocumento(identificador)
                    .orElseThrow(() -> new RuntimeException("Cuenta no encontrada"));

            if (persona.isVerificado()) {
                throw new RuntimeException("Esta cuenta ya fue verificada");
            }

            persona.setCodigoVerificacion(nuevoCodigo);
            persona.setCodigoExpiracion(nuevaExpiracion);
            naturalRepo.save(persona);

            emailService.enviarCodigoVerificacion(
                    persona.getEmail(),
                    persona.getNombres() + " " + persona.getApellidoPaterno(),
                    nuevoCodigo
            );

        } else if ("JURIDICA".equals(tipo)) {
            PersonaJuridica empresa = juridicaRepo.findByRuc(identificador)
                    .orElseThrow(() -> new RuntimeException("Cuenta no encontrada"));

            if (empresa.isVerificado()) {
                throw new RuntimeException("Esta cuenta ya fue verificada");
            }

            empresa.setCodigoVerificacion(nuevoCodigo);
            empresa.setCodigoExpiracion(nuevaExpiracion);
            juridicaRepo.save(empresa);

            emailService.enviarCodigoVerificacion(
                    empresa.getEmailRepresentante(),
                    empresa.getRazonSocial(),
                    nuevoCodigo
            );

        } else {
            throw new RuntimeException("Tipo de persona inválido: " + tipo);
        }
    }

    // ===== LOGIN CIUDADANO =====
    @Override
    public LoginResponseDTO loginCiudadano(LoginCiudadanoRequestDTO request) {
        String tipo = request.getTipoPersna().toUpperCase();

        if ("NATURAL".equals(tipo)) {
            PersonaNatural persona = naturalRepo
                    .findByNumeroDocumento(request.getIdentificador())
                    .orElseThrow(() -> new RuntimeException("Credenciales incorrectas"));

            if (!persona.isVerificado()) {
                throw new RuntimeException("Cuenta no verificada. Revise su correo.");
            }

            if (!persona.isActivo()) {
                throw new RuntimeException("Cuenta desactivada. Contacte al administrador.");
            }

            if (!passwordEncoder.matches(request.getPassword(), persona.getPassword())) {
                throw new RuntimeException("Credenciales incorrectas");
            }

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

        } else if ("JURIDICA".equals(tipo)) {
            PersonaJuridica empresa = juridicaRepo
                    .findByRuc(request.getIdentificador())
                    .orElseThrow(() -> new RuntimeException("Credenciales incorrectas"));

            if (!empresa.isVerificado()) {
                throw new RuntimeException("Cuenta no verificada. Revise su correo.");
            }

            if (!empresa.isActivo()) {
                throw new RuntimeException("Cuenta desactivada. Contacte al administrador.");
            }

            if (!passwordEncoder.matches(request.getPassword(), empresa.getPassword())) {
                throw new RuntimeException("Credenciales incorrectas");
            }

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

        } else {
            throw new RuntimeException("Tipo de persona inválido: " + tipo);
        }
    }

    // ===== MÉTODOS PRIVADOS =====

    private String generarCodigo() {
        // Genera un código numérico de 6 dígitos
        Random random = new Random();
        int numero = 100000 + random.nextInt(900000);
        return String.valueOf(numero);
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
}