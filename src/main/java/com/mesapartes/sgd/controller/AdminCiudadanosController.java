package com.mesapartes.sgd.controller;

import com.mesapartes.sgd.dto.*;
import com.mesapartes.sgd.service.AdminCiudadanosService;
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
@PreAuthorize("hasRole('ADMINISTRADOR')")   // toda la clase requiere ADMIN
public class AdminCiudadanosController {

    private final AdminCiudadanosService service;

    // ── GET /api/admin/ciudadanos/naturales ──────────────────
    @GetMapping("/naturales")
    public ResponseEntity<Page<CiudadanoNaturalResumenDTO>> listarNaturales(
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "fechaCreacion") String sortBy) {

        Pageable pageable = PageRequest.of(
                page, Math.min(size, 100),
                Sort.by(sortBy).descending());
        return ResponseEntity.ok(service.listarNaturales(pageable));
    }

    // ── GET /api/admin/ciudadanos/juridicas ──────────────────
    @GetMapping("/juridicas")
    public ResponseEntity<Page<CiudadanoJuridicaResumenDTO>> listarJuridicas(
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "fechaCreacion") String sortBy) {

        Pageable pageable = PageRequest.of(
                page, Math.min(size, 100),
                Sort.by(sortBy).descending());
        return ResponseEntity.ok(service.listarJuridicas(pageable));
    }

    // ── POST /api/admin/ciudadanos/naturales/buscar ──────────
    @PostMapping("/naturales/buscar")
    public ResponseEntity<Page<CiudadanoNaturalResumenDTO>> buscarNaturales(
            @RequestBody CiudadanoFiltroDTO filtro,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(
                page, Math.min(size, 100),
                Sort.by("fechaCreacion").descending());
        return ResponseEntity.ok(service.buscarNaturales(filtro, pageable));
    }

    // ── POST /api/admin/ciudadanos/juridicas/buscar ──────────
    @PostMapping("/juridicas/buscar")
    public ResponseEntity<Page<CiudadanoJuridicaResumenDTO>> buscarJuridicas(
            @RequestBody CiudadanoFiltroDTO filtro,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(
                page, Math.min(size, 100),
                Sort.by("fechaCreacion").descending());
        return ResponseEntity.ok(service.buscarJuridicas(filtro, pageable));
    }

    // ── GET /api/admin/ciudadanos/estadisticas ───────────────
    @GetMapping("/estadisticas")
    public ResponseEntity<EstadisticasCiudadanosDTO> estadisticas() {
        return ResponseEntity.ok(service.obtenerEstadisticas());
    }

    // ── PATCH /api/admin/ciudadanos/naturales/{doc}/estado ───
    @PatchMapping("/naturales/{numeroDocumento}/estado")
    public ResponseEntity<CiudadanoNaturalResumenDTO> toggleNatural(
            @PathVariable String numeroDocumento,
            @RequestParam boolean activo) {
        return ResponseEntity.ok(
                service.toggleEstadoNatural(numeroDocumento, activo));
    }

    // ── PATCH /api/admin/ciudadanos/juridicas/{ruc}/estado ───
    @PatchMapping("/juridicas/{ruc}/estado")
    public ResponseEntity<CiudadanoJuridicaResumenDTO> toggleJuridica(
            @PathVariable String ruc,
            @RequestParam boolean activo) {
        return ResponseEntity.ok(
                service.toggleEstadoJuridica(ruc, activo));
    }

    // ── DELETE /api/admin/ciudadanos/naturales/{doc} ─────────
    @DeleteMapping("/naturales/{numeroDocumento}")
    public ResponseEntity<Void> eliminarNatural(
            @PathVariable String numeroDocumento) {
        service.eliminarNatural(numeroDocumento);
        return ResponseEntity.noContent().build();
    }

    // ── DELETE /api/admin/ciudadanos/juridicas/{ruc} ─────────
    @DeleteMapping("/juridicas/{ruc}")
    public ResponseEntity<Void> eliminarJuridica(
            @PathVariable String ruc) {
        service.eliminarJuridica(ruc);
        return ResponseEntity.noContent().build();
    }
}