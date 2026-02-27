package com.mesapartes.sgd.service;

import com.mesapartes.sgd.dto.*;

import java.util.List;
import java.util.UUID;

public interface GestionCuentaService {

    // ===== PERSONA NATURAL =====
    PerfilNaturalResponseDTO obtenerPerfilNatural(String numeroDocumento);

    PerfilNaturalResponseDTO editarNatural(String numeroDocumento,
                                           EditarNaturalRequestDTO request);

    // ===== PERSONA JURÍDICA =====
    PerfilJuridicaResponseDTO obtenerPerfilJuridica(String ruc);

    PerfilJuridicaResponseDTO editarJuridica(String ruc, EditarJuridicaRequestDTO request);

    // ===== CONTACTOS DE NOTIFICACIÓN (Jurídica) =====
    List<ContactoNotificacionResponseDTO> listarContactos(String ruc);

    ContactoNotificacionResponseDTO agregarContacto(String ruc,
                                                    ContactoNotificacionDTO request);

    ContactoNotificacionResponseDTO toggleEstadoContacto(String ruc, UUID contactoId,
                                                         ToggleContactoDTO request);

    void eliminarContacto(String ruc, UUID contactoId);

    // ===== CAMBIAR CONTRASEÑA (ambos) =====
    void cambiarPassword(CambiarPasswordRequestDTO request);
}