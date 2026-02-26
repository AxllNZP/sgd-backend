package com.mesapartes.sgd.controller;

import com.mesapartes.sgd.dto.CambioEstadoDTO;
import com.mesapartes.sgd.dto.DocumentoFiltroDTO;
import com.mesapartes.sgd.dto.DocumentoRequestDTO;
import com.mesapartes.sgd.dto.DocumentoResponseDTO;
import com.mesapartes.sgd.entity.EstadoDocumento;
import com.mesapartes.sgd.service.DocumentoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/documentos")
@RequiredArgsConstructor
public class DocumentoController {

    private final DocumentoService documentoService;

    // ===== REGISTRAR DOCUMENTO =====
    @PostMapping(consumes = "multipart/form-data")
    public ResponseEntity<DocumentoResponseDTO> registrar(
            @RequestPart("datos") @Valid DocumentoRequestDTO request,
            @RequestPart(value = "archivo", required = false) MultipartFile archivo
    ) throws IOException {
        DocumentoResponseDTO response = documentoService.registrarDocumento(request, archivo);
        return ResponseEntity.ok(response);
    }

    // ===== CONSULTAR POR NÚMERO DE TRÁMITE =====
    @GetMapping("/{numeroTramite}")
    public ResponseEntity<DocumentoResponseDTO> consultarPorNumeroTramite(
            @PathVariable String numeroTramite
    ) {
        DocumentoResponseDTO response = documentoService.consultarPorNumeroTramite(numeroTramite);
        return ResponseEntity.ok(response);
    }

    // ===== LISTAR TODOS =====
    @GetMapping
    public ResponseEntity<List<DocumentoResponseDTO>> listarTodos() {
        return ResponseEntity.ok(documentoService.listarTodos());
    }

    // ===== LISTAR POR ESTADO =====
    @GetMapping("/estado/{estado}")
    public ResponseEntity<List<DocumentoResponseDTO>> listarPorEstado(
            @PathVariable EstadoDocumento estado
    ) {
        return ResponseEntity.ok(documentoService.listarPorEstado(estado));
    }

    // ===== CAMBIAR ESTADO =====
    @PatchMapping("/{numeroTramite}/estado")
    public ResponseEntity<DocumentoResponseDTO> cambiarEstado(
            @PathVariable String numeroTramite,
            @RequestBody @Valid CambioEstadoDTO cambioEstadoDTO
    ) {
        DocumentoResponseDTO response = documentoService.cambiarEstado(numeroTramite, cambioEstadoDTO);
        return ResponseEntity.ok(response);
    }

    // ===== DESCARGAR EL ARCHIVO =====

    // ===== DESCARGAR ARCHIVO =====
    @GetMapping("/{numeroTramite}/descargar")
    public ResponseEntity<Resource> descargarArchivo(
            @PathVariable String numeroTramite
    ) {
        Resource resource = documentoService.descargarArchivo(numeroTramite);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }

    // ===== BUSCAR POR FILTROS =====
    @PostMapping("/buscar")
    public ResponseEntity<List<DocumentoResponseDTO>> buscarPorFiltros(
            @RequestBody DocumentoFiltroDTO filtro
    ) {
        return ResponseEntity.ok(documentoService.buscarPorFiltros(filtro));
    }

    // ===== ASIGNAR ÁREA =====
    @PatchMapping("/{numeroTramite}/area/{areaId}")
    public ResponseEntity<DocumentoResponseDTO> asignarArea(
            @PathVariable String numeroTramite,
            @PathVariable UUID areaId
    ) {
        return ResponseEntity.ok(documentoService.asignarArea(numeroTramite, areaId));
    }
}

