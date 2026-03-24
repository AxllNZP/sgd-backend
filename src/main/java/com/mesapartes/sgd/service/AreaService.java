package com.mesapartes.sgd.service;

import com.mesapartes.sgd.dto.AreaRequestDTO;
import com.mesapartes.sgd.dto.AreaResponseDTO;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;
import java.util.UUID;

// =============================================================
// AreaService.java
// CORRECCIÓN: listarAreas() y obtenerPorId() ahora permiten
// también a MESA_PARTES.
//
// MOTIVO: MESA_PARTES necesita listar áreas para poblar el
// dropdown "Asignar Área" en el detalle del documento.
// La operación es de solo lectura — no modifica nada.
//
// Las operaciones de escritura (crear, desactivar) siguen
// siendo exclusivas de ADMINISTRADOR.
//
// 📚 LECCIÓN — Doble capa de seguridad en Spring:
//   Spring Security tiene DOS capas de autorización:
//   1. SecurityConfig (a nivel HTTP/URL) — ya corregido
//   2. @PreAuthorize en los métodos del servicio — esta capa
//   Sin corregir AMBAS, la restricción sigue activa aunque
//   el SecurityConfig diga que puede pasar.
// =============================================================
public interface AreaService {

    @PreAuthorize("hasRole('ADMINISTRADOR')")
    AreaResponseDTO crearArea(AreaRequestDTO request);

    // CORRECCIÓN: MESA_PARTES puede listar áreas para el dropdown
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'MESA_PARTES')")
    List<AreaResponseDTO> listarAreas();

    // CORRECCIÓN: MESA_PARTES puede obtener un área por ID (lectura)
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'MESA_PARTES')")
    AreaResponseDTO obtenerPorId(UUID id);

    @PreAuthorize("hasRole('ADMINISTRADOR')")
    void desactivarArea(UUID id);
}