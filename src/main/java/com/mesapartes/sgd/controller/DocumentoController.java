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
import org.springframework.http.MediaType;
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
    // POST /api/documentos
    // Recibe: datos (JSON), archivo (PDF principal), anexo (PDF opcional)
    @PostMapping(consumes = "multipart/form-data")
    public ResponseEntity<DocumentoResponseDTO> registrar(
            @RequestPart("datos") @Valid DocumentoRequestDTO request,
            @RequestPart(value = "archivo", required = false) MultipartFile archivo,
            @RequestPart(value = "anexo", required = false) MultipartFile anexo
    ) throws IOException {
        DocumentoResponseDTO response = documentoService.registrarDocumento(
                request, archivo, anexo);
        return ResponseEntity.ok(response);
    }

    // ===== CONSULTAR POR NÚMERO DE TRÁMITE =====
    // GET /api/documentos/{numeroTramite}
    @GetMapping("/{numeroTramite}")
    public ResponseEntity<DocumentoResponseDTO> consultarPorNumeroTramite(
            @PathVariable String numeroTramite
    ) {
        return ResponseEntity.ok(
                documentoService.consultarPorNumeroTramite(numeroTramite));
    }

    // ===== LISTAR TODOS =====
    // GET /api/documentos
    @GetMapping
    public ResponseEntity<List<DocumentoResponseDTO>> listarTodos() {
        return ResponseEntity.ok(documentoService.listarTodos());
    }

    // ===== LISTAR POR ESTADO =====
    // GET /api/documentos/estado/{estado}
    @GetMapping("/estado/{estado}")
    public ResponseEntity<List<DocumentoResponseDTO>> listarPorEstado(
            @PathVariable EstadoDocumento estado
    ) {
        return ResponseEntity.ok(documentoService.listarPorEstado(estado));
    }

    // ===== CAMBIAR ESTADO =====
    // PATCH /api/documentos/{numeroTramite}/estado
    @PatchMapping("/{numeroTramite}/estado")
    public ResponseEntity<DocumentoResponseDTO> cambiarEstado(
            @PathVariable String numeroTramite,
            @RequestBody @Valid CambioEstadoDTO cambioEstadoDTO
    ) {
        return ResponseEntity.ok(
                documentoService.cambiarEstado(numeroTramite, cambioEstadoDTO));
    }

    // ===== DESCARGAR ARCHIVO PRINCIPAL =====
    // GET /api/documentos/{numeroTramite}/descargar
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

    // ===== DESCARGAR ANEXO =====
    // GET /api/documentos/{numeroTramite}/descargar-anexo
    @GetMapping("/{numeroTramite}/descargar-anexo")
    public ResponseEntity<Resource> descargarAnexo(
            @PathVariable String numeroTramite
    ) {
        Resource resource = documentoService.descargarAnexo(numeroTramite);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }

    // ===== BUSCAR POR FILTROS =====
    // POST /api/documentos/buscar
    @PostMapping("/buscar")
    public ResponseEntity<List<DocumentoResponseDTO>> buscarPorFiltros(
            @RequestBody DocumentoFiltroDTO filtro
    ) {
        return ResponseEntity.ok(documentoService.buscarPorFiltros(filtro));
    }

    // ===== ASIGNAR ÁREA =====
    // PATCH /api/documentos/{numeroTramite}/area/{areaId}
    @PatchMapping("/{numeroTramite}/area/{areaId}")
    public ResponseEntity<DocumentoResponseDTO> asignarArea(
            @PathVariable String numeroTramite,
            @PathVariable UUID areaId
    ) {
        return ResponseEntity.ok(documentoService.asignarArea(numeroTramite, areaId));
    }

    // ===== GENERAR CARGO EN PDF =====
    // GET /api/documentos/{numeroTramite}/cargo
    @GetMapping("/{numeroTramite}/cargo")
    public ResponseEntity<byte[]> generarCargo(
            @PathVariable String numeroTramite
    ) {
        byte[] pdf = documentoService.generarCargoPdf(numeroTramite);
        return ResponseEntity.ok()
                .contentType(MediaType.TEXT_HTML)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"cargo-" + numeroTramite + ".html\"")
                .body(pdf);
    }
}