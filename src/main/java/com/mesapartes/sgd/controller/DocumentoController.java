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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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

    // REGISTRAR DOCUMENTO → público
    @PostMapping(consumes = "multipart/form-data")
    public ResponseEntity<DocumentoResponseDTO> registrar(
            @RequestPart("datos") @Valid DocumentoRequestDTO request,
            @RequestPart(value = "archivo", required = false) MultipartFile archivo,
            @RequestPart(value = "anexo", required = false) MultipartFile anexo
    ) throws IOException {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(documentoService.registrarDocumento(request, archivo, anexo));
    }

    // CONSULTAR POR NÚMERO DE TRÁMITE → público
    @GetMapping("/{numeroTramite}")
    public ResponseEntity<DocumentoResponseDTO> consultarPorNumeroTramite(@PathVariable String numeroTramite) {
        return ResponseEntity.ok(documentoService.consultarPorNumeroTramite(numeroTramite));
    }

    // LISTAR TODOS → autenticado
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Page<DocumentoResponseDTO>> listarTodos(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "fechaHoraRegistro") String sortBy) {

        int safeSize = Math.min(size, 100); // máximo 100
        Pageable pageable = PageRequest.of(page, safeSize, Sort.by(sortBy).descending());

        Page<DocumentoResponseDTO> resultado = documentoService.listarTodos(pageable);
        return ResponseEntity.ok(resultado);
    }

    // LISTAR POR ESTADO → autenticado
    @GetMapping("/estado/{estado}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<DocumentoResponseDTO>> listarPorEstado(@PathVariable EstadoDocumento estado) {
        return ResponseEntity.ok(documentoService.listarPorEstado(estado));
    }

    // CAMBIAR ESTADO → MESA_PARTES o ADMINISTRADOR
    @PatchMapping("/{numeroTramite}/estado")
    @PreAuthorize("hasAnyRole('MESA_PARTES', 'ADMINISTRADOR')")
    public ResponseEntity<DocumentoResponseDTO> cambiarEstado(
            @PathVariable String numeroTramite,
            @RequestBody @Valid CambioEstadoDTO cambioEstadoDTO
    ) {
        return ResponseEntity.ok(documentoService.cambiarEstado(numeroTramite, cambioEstadoDTO));
    }

    // DESCARGAR ARCHIVO → autenticado
    @GetMapping("/{numeroTramite}/descargar")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Resource> descargarArchivo(@PathVariable String numeroTramite) {
        Resource resource = documentoService.descargarArchivo(numeroTramite);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }

    // DESCARGAR ANEXO → autenticado
    @GetMapping("/{numeroTramite}/descargar-anexo")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Resource> descargarAnexo(@PathVariable String numeroTramite) {
        Resource resource = documentoService.descargarAnexo(numeroTramite);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }

    // BUSCAR POR FILTROS → autenticado
    @PostMapping("/buscar")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Page<DocumentoResponseDTO>> buscar(
            DocumentoFiltroDTO filtro,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "fechaHoraRegistro") String sortBy) {

        int safeSize = Math.min(size, 100);
        Pageable pageable = PageRequest.of(page, safeSize, Sort.by(sortBy).descending());

        Page<DocumentoResponseDTO> resultado = documentoService.buscarPorFiltros(filtro, pageable);
        return ResponseEntity.ok(resultado);
    }
    // ASIGNAR ÁREA → MESA_PARTES o ADMINISTRADOR
    @PatchMapping("/{numeroTramite}/area/{areaId}")
    @PreAuthorize("hasAnyRole('MESA_PARTES', 'ADMINISTRADOR')")
    public ResponseEntity<DocumentoResponseDTO> asignarArea(@PathVariable String numeroTramite,
                                                            @PathVariable UUID areaId) {
        return ResponseEntity.ok(documentoService.asignarArea(numeroTramite, areaId));
    }

    // GENERAR CARGO → público
    @GetMapping("/{numeroTramite}/cargo")
    public ResponseEntity<byte[]> generarCargo(@PathVariable String numeroTramite) {
        byte[] pdf = documentoService.generarCargoPdf(numeroTramite);
        return ResponseEntity.ok()
                .contentType(MediaType.TEXT_HTML)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"cargo-" + numeroTramite + ".html\"")
                .body(pdf);
    }
}