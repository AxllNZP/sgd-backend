package com.mesapartes.sgd.controller;

import com.mesapartes.sgd.dto.DerivacionRequestDTO;
import com.mesapartes.sgd.dto.DerivacionResponseDTO;
import com.mesapartes.sgd.service.DerivacionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/derivaciones")
@RequiredArgsConstructor
public class DerivacionController {

    private final DerivacionService derivacionService;

    // ===== DERIVAR DOCUMENTO =====
    @PostMapping("/{numeroTramite}")
    public ResponseEntity<DerivacionResponseDTO> derivarDocumento(
            @PathVariable String numeroTramite,
            @RequestBody @Valid DerivacionRequestDTO request
    ) {
        return ResponseEntity.ok(derivacionService.derivarDocumento(numeroTramite, request));
    }

    // ===== VER DERIVACIONES POR TRÁMITE =====
    @GetMapping("/{numeroTramite}")
    public ResponseEntity<List<DerivacionResponseDTO>> obtenerDerivaciones(
            @PathVariable String numeroTramite
    ) {
        return ResponseEntity.ok(derivacionService.obtenerDerivacionesPorTramite(numeroTramite));
    }
}