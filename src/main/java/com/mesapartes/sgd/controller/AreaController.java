package com.mesapartes.sgd.controller;

import com.mesapartes.sgd.dto.AreaRequestDTO;
import com.mesapartes.sgd.dto.AreaResponseDTO;
import com.mesapartes.sgd.service.AreaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/areas")
@RequiredArgsConstructor
public class AreaController {

    private final AreaService areaService;

    @PostMapping
    public ResponseEntity<AreaResponseDTO> crearArea(@RequestBody @Valid AreaRequestDTO request) {
        return ResponseEntity.ok(areaService.crearArea(request));
    }

    @GetMapping
    public ResponseEntity<List<AreaResponseDTO>> listarAreas() {
        return ResponseEntity.ok(areaService.listarAreas());
    }

    @GetMapping("/{id}")
    public ResponseEntity<AreaResponseDTO> obtenerPorId(@PathVariable UUID id) {
        return ResponseEntity.ok(areaService.obtenerPorId(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> desactivarArea(@PathVariable UUID id) {
        areaService.desactivarArea(id);
        return ResponseEntity.noContent().build();
    }
}