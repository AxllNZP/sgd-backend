package com.mesapartes.sgd.service.impl;

import com.mesapartes.sgd.dto.*;
import com.mesapartes.sgd.entity.ContactoNotificacion;
import com.mesapartes.sgd.entity.PersonaJuridica;
import com.mesapartes.sgd.entity.PersonaNatural;
import com.mesapartes.sgd.repository.ContactoNotificacionRepository;
import com.mesapartes.sgd.repository.PersonaJuridicaRepository;
import com.mesapartes.sgd.repository.PersonaNaturalRepository;
import com.mesapartes.sgd.service.GestionCuentaService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GestionCuentaServiceImpl implements GestionCuentaService {

    private final PersonaNaturalRepository naturalRepo;
    private final PersonaJuridicaRepository juridicaRepo;
    private final ContactoNotificacionRepository contactoRepo;
    private final PasswordEncoder passwordEncoder;

    // ===== OBTENER PERFIL NATURAL =====
    @Override
    public PerfilNaturalResponseDTO obtenerPerfilNatural(String numeroDocumento) {
        PersonaNatural persona = naturalRepo.findByNumeroDocumento(numeroDocumento)
                .orElseThrow(() -> new RuntimeException(
                        "Cuenta no encontrada: " + numeroDocumento));
        return mapearPerfilNatural(persona);
    }

    // ===== EDITAR PERSONA NATURAL =====
    @Override
    public PerfilNaturalResponseDTO editarNatural(String numeroDocumento,
                                                  EditarNaturalRequestDTO request) {
        PersonaNatural persona = naturalRepo.findByNumeroDocumento(numeroDocumento)
                .orElseThrow(() -> new RuntimeException(
                        "Cuenta no encontrada: " + numeroDocumento));

        // Verificar si el nuevo email ya está en uso por otra cuenta
        if (!persona.getEmail().equalsIgnoreCase(request.getEmail())) {
            if (naturalRepo.existsByEmail(request.getEmail())) {
                throw new RuntimeException(
                        "El email ya está registrado en otra cuenta");
            }
        }

        // Actualizar campos editables
        persona.setDireccion(request.getDireccion());
        persona.setTelefono(request.getTelefono());
        persona.setEmail(request.getEmail());
        persona.setPreguntaSeguridad(request.getPreguntaSeguridad());
        persona.setRespuestaSeguridad(
                request.getRespuestaSeguridad().trim().toLowerCase());

        return mapearPerfilNatural(naturalRepo.save(persona));
    }

    // ===== OBTENER PERFIL JURÍDICA =====
    @Override
    public PerfilJuridicaResponseDTO obtenerPerfilJuridica(String ruc) {
        PersonaJuridica empresa = juridicaRepo.findByRuc(ruc)
                .orElseThrow(() -> new RuntimeException(
                        "Cuenta no encontrada con RUC: " + ruc));
        return mapearPerfilJuridica(empresa);
    }

    // ===== EDITAR PERSONA JURÍDICA =====
    @Override
    public PerfilJuridicaResponseDTO editarJuridica(String ruc,
                                                    EditarJuridicaRequestDTO request) {
        PersonaJuridica empresa = juridicaRepo.findByRuc(ruc)
                .orElseThrow(() -> new RuntimeException(
                        "Cuenta no encontrada con RUC: " + ruc));

        // Actualizar campos editables
        empresa.setDireccion(request.getDireccion());
        empresa.setTelefono(request.getTelefono());
        empresa.setDepartamento(request.getDepartamento());
        empresa.setProvincia(request.getProvincia());
        empresa.setDistrito(request.getDistrito());

        return mapearPerfilJuridica(juridicaRepo.save(empresa));
    }

    // ===== LISTAR CONTACTOS =====
    @Override
    public List<ContactoNotificacionResponseDTO> listarContactos(String ruc) {
        PersonaJuridica empresa = juridicaRepo.findByRuc(ruc)
                .orElseThrow(() -> new RuntimeException(
                        "Cuenta no encontrada con RUC: " + ruc));

        return contactoRepo.findByPersonaJuridica(empresa)
                .stream()
                .map(this::mapearContacto)
                .collect(Collectors.toList());
    }

    // ===== AGREGAR CONTACTO =====
    @Override
    public ContactoNotificacionResponseDTO agregarContacto(String ruc,
                                                           ContactoNotificacionDTO request) {
        PersonaJuridica empresa = juridicaRepo.findByRuc(ruc)
                .orElseThrow(() -> new RuntimeException(
                        "Cuenta no encontrada con RUC: " + ruc));

        ContactoNotificacion contacto = new ContactoNotificacion();
        contacto.setPersonaJuridica(empresa);
        contacto.setNombres(request.getNombres());
        contacto.setEmail(request.getEmail());
        contacto.setActivo(true);

        return mapearContacto(contactoRepo.save(contacto));
    }

    // ===== TOGGLE ESTADO CONTACTO =====
    @Override
    public ContactoNotificacionResponseDTO toggleEstadoContacto(String ruc,
                                                                UUID contactoId,
                                                                ToggleContactoDTO request) {
        // Verificar que el contacto pertenece a esta empresa
        PersonaJuridica empresa = juridicaRepo.findByRuc(ruc)
                .orElseThrow(() -> new RuntimeException(
                        "Cuenta no encontrada con RUC: " + ruc));

        ContactoNotificacion contacto = contactoRepo.findById(contactoId)
                .orElseThrow(() -> new RuntimeException(
                        "Contacto no encontrado: " + contactoId));

        if (!contacto.getPersonaJuridica().getId().equals(empresa.getId())) {
            throw new RuntimeException(
                    "El contacto no pertenece a esta empresa");
        }

        contacto.setActivo(request.getActivo());
        return mapearContacto(contactoRepo.save(contacto));
    }

    // ===== ELIMINAR CONTACTO =====
    @Override
    public void eliminarContacto(String ruc, UUID contactoId) {
        PersonaJuridica empresa = juridicaRepo.findByRuc(ruc)
                .orElseThrow(() -> new RuntimeException(
                        "Cuenta no encontrada con RUC: " + ruc));

        ContactoNotificacion contacto = contactoRepo.findById(contactoId)
                .orElseThrow(() -> new RuntimeException(
                        "Contacto no encontrado: " + contactoId));

        if (!contacto.getPersonaJuridica().getId().equals(empresa.getId())) {
            throw new RuntimeException(
                    "El contacto no pertenece a esta empresa");
        }

        contactoRepo.delete(contacto);
    }

    // ===== CAMBIAR CONTRASEÑA =====
    @Override
    public void cambiarPassword(CambiarPasswordRequestDTO request) {
        String tipo = request.getTipoPersna().toUpperCase();

        // Validar que las nuevas contraseñas coincidan
        if (!request.getNuevaPassword().equals(request.getConfirmarPassword())) {
            throw new RuntimeException("Las contraseñas nuevas no coinciden");
        }

        if ("NATURAL".equals(tipo)) {
            PersonaNatural persona = naturalRepo
                    .findByNumeroDocumento(request.getIdentificador())
                    .orElseThrow(() -> new RuntimeException("Cuenta no encontrada"));

            // Verificar contraseña actual
            if (!passwordEncoder.matches(request.getPasswordActual(),
                    persona.getPassword())) {
                throw new RuntimeException(
                        "La contraseña actual es incorrecta");
            }

            persona.setPassword(passwordEncoder.encode(request.getNuevaPassword()));
            naturalRepo.save(persona);

        } else if ("JURIDICA".equals(tipo)) {
            PersonaJuridica empresa = juridicaRepo
                    .findByRuc(request.getIdentificador())
                    .orElseThrow(() -> new RuntimeException("Cuenta no encontrada"));

            if (!passwordEncoder.matches(request.getPasswordActual(),
                    empresa.getPassword())) {
                throw new RuntimeException(
                        "La contraseña actual es incorrecta");
            }

            empresa.setPassword(passwordEncoder.encode(request.getNuevaPassword()));
            juridicaRepo.save(empresa);

        } else {
            throw new RuntimeException("Tipo de persona inválido: " + tipo);
        }
    }

    // ===== MÉTODOS PRIVADOS =====

    private PerfilNaturalResponseDTO mapearPerfilNatural(PersonaNatural persona) {
        PerfilNaturalResponseDTO response = new PerfilNaturalResponseDTO();
        response.setId(persona.getId());
        response.setTipoDocumento(persona.getTipoDocumento());
        response.setNumeroDocumento(persona.getNumeroDocumento());
        response.setNombres(persona.getNombres());
        response.setApellidoPaterno(persona.getApellidoPaterno());
        response.setApellidoMaterno(persona.getApellidoMaterno());
        response.setDepartamento(persona.getDepartamento());
        response.setProvincia(persona.getProvincia());
        response.setDistrito(persona.getDistrito());
        response.setDireccion(persona.getDireccion());
        response.setTelefono(persona.getTelefono());
        response.setEmail(persona.getEmail());
        response.setPreguntaSeguridad(persona.getPreguntaSeguridad());
        response.setDescripcionPregunta(
                persona.getPreguntaSeguridad().getDescripcion());
        response.setAfiliadoBuzon(persona.isAfiliadoBuzon());
        response.setFechaCreacion(persona.getFechaCreacion());
        return response;
    }

    private PerfilJuridicaResponseDTO mapearPerfilJuridica(PersonaJuridica empresa) {
        PerfilJuridicaResponseDTO response = new PerfilJuridicaResponseDTO();
        response.setId(empresa.getId());
        response.setRuc(empresa.getRuc());
        response.setRazonSocial(empresa.getRazonSocial());
        response.setPreguntaSeguridad(empresa.getPreguntaSeguridad());
        response.setDescripcionPregunta(
                empresa.getPreguntaSeguridad().getDescripcion());
        response.setTipoDocRepresentante(empresa.getTipoDocRepresentante());
        response.setNumDocRepresentante(empresa.getNumDocRepresentante());
        response.setNombresRepresentante(empresa.getNombresRepresentante());
        response.setApellidoPaternoRepresentante(
                empresa.getApellidoPaternoRepresentante());
        response.setApellidoMaternoRepresentante(
                empresa.getApellidoMaternoRepresentante());
        response.setEmailRepresentante(empresa.getEmailRepresentante());
        response.setDepartamento(empresa.getDepartamento());
        response.setProvincia(empresa.getProvincia());
        response.setDistrito(empresa.getDistrito());
        response.setDireccion(empresa.getDireccion());
        response.setTelefono(empresa.getTelefono());
        response.setAfiliadoBuzon(empresa.isAfiliadoBuzon());
        response.setFechaCreacion(empresa.getFechaCreacion());

        // Mapear contactos
        List<ContactoNotificacionResponseDTO> contactos =
                contactoRepo.findByPersonaJuridica(empresa)
                        .stream()
                        .map(this::mapearContacto)
                        .collect(Collectors.toList());
        response.setContactosNotificacion(contactos);

        return response;
    }

    private ContactoNotificacionResponseDTO mapearContacto(ContactoNotificacion contacto) {
        ContactoNotificacionResponseDTO response = new ContactoNotificacionResponseDTO();
        response.setId(contacto.getId());
        response.setNombres(contacto.getNombres());
        response.setEmail(contacto.getEmail());
        response.setActivo(contacto.isActivo());
        return response;
    }
}