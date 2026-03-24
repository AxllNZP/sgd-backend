package com.mesapartes.sgd.service;

import com.mesapartes.sgd.dto.*;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;
import java.util.UUID;

// =============================================================
// GestionCuentaService.java — VERSIÓN FINAL CORREGIDA
//
// CORRECCIONES:
//
// 1. editarJuridica():
//    Antes: @PreAuthorize("hasRole('ADMINISTRADOR')")
//    Ahora: @PreAuthorize("@jwtOwnerChecker... or hasRole('ADMINISTRADOR')")
//    → El dueño de la empresa puede editar sus propios datos
//
// 2. cambiarPassword():
//    Antes: @PreAuthorize("#request.identificador == authentication.name ...")
//    Ahora: @PreAuthorize("isAuthenticated()")
//    → Para NATURAL: DNI == subject JWT  ✅ (funcionaba antes)
//    → Para JURIDICA: RUC != emailRepresentante ❌ (siempre 403 antes)
//    La seguridad real la garantiza verificar passwordActual en
//    GestionCuentaServiceImpl: si no coincide → 409. Eso es suficiente.
//
// 3. agregarContacto / toggleEstadoContacto / eliminarContacto:
//    Abiertos al dueño de la cuenta jurídica (no solo admin)
//    porque el ciudadano debe poder gestionar sus propios contactos.
// =============================================================
public interface GestionCuentaService {

    // ── PERFIL NATURAL ────────────────────────────────────────

    @PreAuthorize("#numeroDocumento == authentication.name or hasRole('ADMINISTRADOR')")
    PerfilNaturalResponseDTO editarNatural(String numeroDocumento, EditarNaturalRequestDTO req);

    @PreAuthorize("isAuthenticated()")
    PerfilNaturalResponseDTO obtenerPerfilNatural(String numeroDocumento);

    // ── PERFIL JURÍDICO ───────────────────────────────────────

    @PreAuthorize("@jwtOwnerChecker.isOwnerJuridica(#ruc, authentication) or hasRole('ADMINISTRADOR')")
    PerfilJuridicaResponseDTO editarJuridica(String ruc, EditarJuridicaRequestDTO req);

    @PreAuthorize("isAuthenticated()")
    PerfilJuridicaResponseDTO obtenerPerfilJuridica(String ruc);

    // ── CONTACTOS DE NOTIFICACIÓN ─────────────────────────────

    @PreAuthorize("@jwtOwnerChecker.isOwnerJuridica(#ruc, authentication) or hasRole('ADMINISTRADOR')")
    ContactoNotificacionResponseDTO agregarContacto(String ruc, ContactoNotificacionDTO request);

    @PreAuthorize("@jwtOwnerChecker.isOwnerJuridica(#ruc, authentication) or hasRole('ADMINISTRADOR')")
    ContactoNotificacionResponseDTO toggleEstadoContacto(String ruc, UUID contactoId, ToggleContactoDTO request);

    @PreAuthorize("@jwtOwnerChecker.isOwnerJuridica(#ruc, authentication) or hasRole('ADMINISTRADOR')")
    void eliminarContacto(String ruc, UUID contactoId);

    @PreAuthorize("isAuthenticated()")
    List<ContactoNotificacionResponseDTO> listarContactos(String ruc);

    // ── CAMBIAR CONTRASEÑA ────────────────────────────────────

    @PreAuthorize("isAuthenticated()")
    void cambiarPassword(CambiarPasswordRequestDTO request);
}