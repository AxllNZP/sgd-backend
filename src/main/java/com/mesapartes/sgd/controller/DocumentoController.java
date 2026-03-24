package com.mesapartes.sgd.controller;

// =============================================================
// DocumentoController — anotaciones Swagger añadidas.
// Solo se muestran las anotaciones nuevas sobre cada método.
// El código de negocio NO se toca.
// =============================================================

import com.mesapartes.sgd.dto.CambioEstadoDTO;
import com.mesapartes.sgd.dto.DocumentoFiltroDTO;
import com.mesapartes.sgd.dto.DocumentoRequestDTO;
import com.mesapartes.sgd.dto.DocumentoResponseDTO;
import com.mesapartes.sgd.entity.EstadoDocumento;
import com.mesapartes.sgd.service.DocumentoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Documentos", description = "Registro, consulta y gestión de expedientes de Mesa de Partes")
public class DocumentoController {

    private final DocumentoService documentoService;

    @Operation(summary = "Registrar documento",
            description = "Endpoint público. Registra un nuevo expediente con archivo adjunto opcional.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Documento registrado exitosamente"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos"),
            @ApiResponse(responseCode = "413", description = "Archivo demasiado grande (máx. 50MB)")
    })
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

    @Operation(summary = "Consultar por número de trámite",
            description = "Endpoint público. Permite al ciudadano hacer seguimiento de su expediente.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Documento encontrado"),
            @ApiResponse(responseCode = "404", description = "Número de trámite no existe")
    })
    @GetMapping("/{numeroTramite}")
    public ResponseEntity<DocumentoResponseDTO> consultarPorNumeroTramite(
            @Parameter(description = "Número de trámite (ej: MP-20260305-ABC123)")
            @PathVariable String numeroTramite) {
        return ResponseEntity.ok(documentoService.consultarPorNumeroTramite(numeroTramite));
    }

    @Operation(summary = "Listar todos los documentos (paginado)",
            description = "Requiere autenticación. Retorna todos los expedientes ordenados por fecha de registro.")
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Page<DocumentoResponseDTO>> listarTodos(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "fechaHoraRegistro") String sortBy) {
        int safeSize = Math.min(size, 100);
        Pageable pageable = PageRequest.of(page, safeSize, Sort.by(sortBy).descending());
        return ResponseEntity.ok(documentoService.listarTodos(pageable));
    }

    @Operation(summary = "Listar por estado")
    @GetMapping("/estado/{estado}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<DocumentoResponseDTO>> listarPorEstado(
            @PathVariable EstadoDocumento estado) {
        return ResponseEntity.ok(documentoService.listarPorEstado(estado));
    }

    @Operation(summary = "Cambiar estado del documento",
            description = "Requiere rol MESA_PARTES o ADMINISTRADOR.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Estado actualizado"),
            @ApiResponse(responseCode = "403", description = "Sin permisos"),
            @ApiResponse(responseCode = "404", description = "Trámite no encontrado")
    })
    @PatchMapping("/{numeroTramite}/estado")
    @PreAuthorize("hasAnyRole('MESA_PARTES', 'ADMINISTRADOR')")
    public ResponseEntity<DocumentoResponseDTO> cambiarEstado(
            @PathVariable String numeroTramite,
            @RequestBody @Valid CambioEstadoDTO cambioEstadoDTO) {
        return ResponseEntity.ok(documentoService.cambiarEstado(numeroTramite, cambioEstadoDTO));
    }

    @Operation(summary = "Descargar archivo principal", description = "Requiere autenticación.")
    @GetMapping("/{numeroTramite}/descargar")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Resource> descargarArchivo(@PathVariable String numeroTramite) {
        Resource resource = documentoService.descargarArchivo(numeroTramite);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }

    @Operation(summary = "Descargar anexo", description = "Requiere autenticación.")
    @GetMapping("/{numeroTramite}/descargar-anexo")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Resource> descargarAnexo(@PathVariable String numeroTramite) {
        Resource resource = documentoService.descargarAnexo(numeroTramite);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }

    @Operation(summary = "Buscar documentos por filtros",
            description = "Requiere autenticación. Filtra por remitente, asunto, estado y rango de fechas.")
    @PostMapping("/buscar")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Page<DocumentoResponseDTO>> buscar(
            @RequestBody DocumentoFiltroDTO filtro,   // ← @RequestBody añadido
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "fechaHoraRegistro") String sortBy) {

        int safeSize = Math.min(size, 100);
        Pageable pageable = PageRequest.of(page, safeSize, Sort.by(sortBy).descending());
        return ResponseEntity.ok(documentoService.buscarPorFiltros(filtro, pageable));
    }

    @Operation(summary = "Asignar área al documento",
            description = "Requiere rol MESA_PARTES o ADMINISTRADOR.")
    @PatchMapping("/{numeroTramite}/area/{areaId}")
    @PreAuthorize("hasAnyRole('MESA_PARTES', 'ADMINISTRADOR')")
    public ResponseEntity<DocumentoResponseDTO> asignarArea(
            @PathVariable String numeroTramite,
            @PathVariable UUID areaId) {
        return ResponseEntity.ok(documentoService.asignarArea(numeroTramite, areaId));
    }

    @Operation(summary = "Generar cargo de recepción (PDF/HTML)",
            description = "Endpoint público. Descarga el cargo de ingreso del expediente.")
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