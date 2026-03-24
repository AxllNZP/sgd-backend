package com.mesapartes.sgd.controller;

import com.mesapartes.sgd.dto.AreaRequestDTO;
import com.mesapartes.sgd.dto.AreaResponseDTO;
import com.mesapartes.sgd.service.AreaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

// =============================================================
// AreaController.java
// CORRECCIÓN: listarAreas() abierto a MESA_PARTES.
//
// MOTIVO: MESA_PARTES necesita GET /api/areas para poblar
// el dropdown "Asignar Área" en el detalle del documento.
// Sin esto el dropdown aparece vacío y no se puede asignar.
//
// 📚 LECCIÓN — Spring tiene 3 capas de seguridad independientes:
//   1. SecurityConfig  → filtra por URL antes del controlador
//   2. @PreAuthorize en el CONTROLADOR → filtra al entrar al método
//   3. @PreAuthorize en el SERVICIO    → filtra al invocar el servicio
//   Las tres deben estar alineadas. Basta que UNA diga "no"
//   para que la petición sea rechazada con 403.
//
// Las operaciones de escritura (crear, desactivar, obtener por ID)
// siguen siendo exclusivas de ADMINISTRADOR.
// =============================================================
@RestController
@RequestMapping("/api/areas")
@RequiredArgsConstructor
public class AreaController {

    private final AreaService areaService;

    // Solo ADMINISTRADOR puede crear áreas — sin cambios
    @PostMapping
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<AreaResponseDTO> crearArea(
            @RequestBody @Valid AreaRequestDTO request) {
        return ResponseEntity.ok(areaService.crearArea(request));
    }

    // CORRECCIÓN: MESA_PARTES puede listar áreas (lectura para dropdown)
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'MESA_PARTES')")
    public ResponseEntity<List<AreaResponseDTO>> listarAreas() {
        return ResponseEntity.ok(areaService.listarAreas());
    }

    // Solo ADMINISTRADOR puede obtener área por ID — sin cambios
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<AreaResponseDTO> obtenerPorId(@PathVariable UUID id) {
        return ResponseEntity.ok(areaService.obtenerPorId(id));
    }

    // Solo ADMINISTRADOR puede desactivar áreas — sin cambios
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<Void> desactivarArea(@PathVariable UUID id) {
        areaService.desactivarArea(id);
        return ResponseEntity.noContent().build();
    }
}