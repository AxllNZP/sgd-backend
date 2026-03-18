package com.mesapartes.sgd.controller;

// =============================================================
// AdminCiudadanosController — CORRECCIÓN
// Se añade @RequestBody a buscarNaturales() y buscarJuridicas().
// Sin @RequestBody SpringDoc no puede clasificar el parámetro
// y lanza 500 al generar /v3/api-docs.
// El resto del archivo NO cambia.
// =============================================================

import com.mesapartes.sgd.dto.*;
import com.mesapartes.sgd.service.AdminCiudadanosService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/ciudadanos")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMINISTRADOR')")
@Tag(name = "Admin — Ciudadanos", description = "Gestión de cuentas de ciudadanos (solo ADMINISTRADOR)")
public class AdminCiudadanosController {

    private final AdminCiudadanosService service;

    // ── GET /api/admin/ciudadanos/naturales ──────────────────
    @Operation(summary = "Listar personas naturales (paginado)")
    @GetMapping("/naturales")
    public ResponseEntity<Page<CiudadanoNaturalResumenDTO>> listarNaturales(
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(
                page, Math.min(size, 100),
                Sort.by("fechaCreacion").descending());
        return ResponseEntity.ok(service.listarNaturales(pageable));
    }

    // ── GET /api/admin/ciudadanos/juridicas ──────────────────
    @Operation(summary = "Listar personas jurídicas (paginado)")
    @GetMapping("/juridicas")
    public ResponseEntity<Page<CiudadanoJuridicaResumenDTO>> listarJuridicas(
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(
                page, Math.min(size, 100),
                Sort.by("fechaCreacion").descending());
        return ResponseEntity.ok(service.listarJuridicas(pageable));
    }

    // ── POST /api/admin/ciudadanos/naturales/buscar ──────────
    @Operation(summary = "Buscar personas naturales por filtros")
    @PostMapping("/naturales/buscar")
    public ResponseEntity<Page<CiudadanoNaturalResumenDTO>> buscarNaturales(
            @RequestBody CiudadanoFiltroDTO filtro,    // ← @RequestBody AÑADIDO
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(
                page, Math.min(size, 100),
                Sort.by("fechaCreacion").descending());
        return ResponseEntity.ok(service.buscarNaturales(filtro, pageable));
    }

    // ── POST /api/admin/ciudadanos/juridicas/buscar ──────────
    @Operation(summary = "Buscar personas jurídicas por filtros")
    @PostMapping("/juridicas/buscar")
    public ResponseEntity<Page<CiudadanoJuridicaResumenDTO>> buscarJuridicas(
            @RequestBody CiudadanoFiltroDTO filtro,    // ← @RequestBody AÑADIDO
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(
                page, Math.min(size, 100),
                Sort.by("fechaCreacion").descending());
        return ResponseEntity.ok(service.buscarJuridicas(filtro, pageable));
    }

    // ── GET /api/admin/ciudadanos/estadisticas ───────────────
    @Operation(summary = "Estadísticas generales de ciudadanos")
    @GetMapping("/estadisticas")
    public ResponseEntity<EstadisticasCiudadanosDTO> estadisticas() {
        return ResponseEntity.ok(service.obtenerEstadisticas());
    }

    // ── PATCH /api/admin/ciudadanos/naturales/{doc}/estado ───
    @Operation(summary = "Activar / desactivar cuenta natural")
    @PatchMapping("/naturales/{numeroDocumento}/estado")
    public ResponseEntity<CiudadanoNaturalResumenDTO> toggleNatural(
            @PathVariable String numeroDocumento,
            @RequestParam boolean activo) {
        return ResponseEntity.ok(
                service.toggleEstadoNatural(numeroDocumento, activo));
    }

    // ── PATCH /api/admin/ciudadanos/juridicas/{ruc}/estado ───
    @Operation(summary = "Activar / desactivar cuenta jurídica")
    @PatchMapping("/juridicas/{ruc}/estado")
    public ResponseEntity<CiudadanoJuridicaResumenDTO> toggleJuridica(
            @PathVariable String ruc,
            @RequestParam boolean activo) {
        return ResponseEntity.ok(
                service.toggleEstadoJuridica(ruc, activo));
    }

    // ── DELETE /api/admin/ciudadanos/naturales/{doc} ─────────
    @Operation(summary = "Eliminar cuenta natural permanentemente")
    @DeleteMapping("/naturales/{numeroDocumento}")
    public ResponseEntity<Void> eliminarNatural(
            @PathVariable String numeroDocumento) {
        service.eliminarNatural(numeroDocumento);
        return ResponseEntity.noContent().build();
    }

    // ── DELETE /api/admin/ciudadanos/juridicas/{ruc} ─────────
    @Operation(summary = "Eliminar cuenta jurídica permanentemente")
    @DeleteMapping("/juridicas/{ruc}")
    public ResponseEntity<Void> eliminarJuridica(
            @PathVariable String ruc) {
        service.eliminarJuridica(ruc);
        return ResponseEntity.noContent().build();
    }
}