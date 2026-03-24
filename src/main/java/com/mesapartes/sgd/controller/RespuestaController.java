package com.mesapartes.sgd.controller;

import com.mesapartes.sgd.dto.RespuestaRequestDTO;
import com.mesapartes.sgd.dto.RespuestaResponseDTO;
import com.mesapartes.sgd.service.RespuestaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/respuestas")
@RequiredArgsConstructor
public class RespuestaController {

    private final RespuestaService respuestaService;

    // ===== EMITIR RESPUESTA =====
    @PostMapping("/{numeroTramite}")
    @PreAuthorize("hasAnyRole('MESA_PARTES', 'ADMINISTRADOR')")
    public ResponseEntity<RespuestaResponseDTO> emitirRespuesta(
            @PathVariable String numeroTramite,
            @RequestBody @Valid RespuestaRequestDTO request
    ) {
        return ResponseEntity.ok(respuestaService.emitirRespuesta(numeroTramite, request));
    }

    // ===== VER RESPUESTAS POR TRÁMITE =====
    @GetMapping("/{numeroTramite}")
    @PreAuthorize("hasAnyRole('MESA_PARTES', 'ADMINISTRADOR')")
    public ResponseEntity<List<RespuestaResponseDTO>> obtenerRespuestas(
            @PathVariable String numeroTramite
    ) {
        return ResponseEntity.ok(respuestaService.obtenerRespuestasPorTramite(numeroTramite));
    }
}