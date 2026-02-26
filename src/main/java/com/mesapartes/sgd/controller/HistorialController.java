package com.mesapartes.sgd.controller;

import com.mesapartes.sgd.dto.HistorialResponseDTO;
import com.mesapartes.sgd.service.HistorialService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/historial")
@RequiredArgsConstructor
public class HistorialController {

    private final HistorialService historialService;

    // ===== OBTENER HISTORIAL POR NÚMERO DE TRÁMITE =====
    @GetMapping("/{numeroTramite}")
    public ResponseEntity<List<HistorialResponseDTO>> obtenerHistorial(
            @PathVariable String numeroTramite
    ) {
        return ResponseEntity.ok(historialService.obtenerHistorialPorNumeroTramite(numeroTramite));
    }
}