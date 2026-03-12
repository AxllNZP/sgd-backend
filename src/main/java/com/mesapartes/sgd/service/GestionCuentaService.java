package com.mesapartes.sgd.service;

import com.mesapartes.sgd.dto.*;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;
import java.util.UUID;

public interface GestionCuentaService {

    // PERFIL NATURAL
    @PreAuthorize("#numeroDocumento == authentication.name or hasRole('ADMINISTRADOR')")
    PerfilNaturalResponseDTO editarNatural(String numeroDocumento, EditarNaturalRequestDTO req);

    @PreAuthorize("isAuthenticated()")
    PerfilNaturalResponseDTO obtenerPerfilNatural(String numeroDocumento);

    // PERFIL JURÍDICO
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    PerfilJuridicaResponseDTO editarJuridica(String ruc, EditarJuridicaRequestDTO req);

    @PreAuthorize("isAuthenticated()")
    PerfilJuridicaResponseDTO obtenerPerfilJuridica(String ruc);

    // CONTACTOS DE NOTIFICACIÓN
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    ContactoNotificacionResponseDTO agregarContacto(String ruc, ContactoNotificacionDTO request);

    @PreAuthorize("hasRole('ADMINISTRADOR')")
    ContactoNotificacionResponseDTO toggleEstadoContacto(String ruc, UUID contactoId, ToggleContactoDTO request);

    @PreAuthorize("hasRole('ADMINISTRADOR')")
    void eliminarContacto(String ruc, UUID contactoId);

    @PreAuthorize("isAuthenticated()")
    List<ContactoNotificacionResponseDTO> listarContactos(String ruc);

    // CAMBIO DE CONTRASEÑA
    @PreAuthorize("#request.numeroDocumento == authentication.name or hasRole('ADMINISTRADOR')")
    void cambiarPassword(CambiarPasswordRequestDTO request);
}