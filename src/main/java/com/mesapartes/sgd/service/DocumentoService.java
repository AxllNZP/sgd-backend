package com.mesapartes.sgd.service;

import com.mesapartes.sgd.dto.CambioEstadoDTO;
import com.mesapartes.sgd.dto.DocumentoFiltroDTO;
import com.mesapartes.sgd.dto.DocumentoRequestDTO;
import com.mesapartes.sgd.dto.DocumentoResponseDTO;
import com.mesapartes.sgd.entity.EstadoDocumento;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

public interface DocumentoService {

    DocumentoResponseDTO registrarDocumento(DocumentoRequestDTO request,
                                            MultipartFile archivo,
                                            MultipartFile anexo) throws IOException;

    DocumentoResponseDTO consultarPorNumeroTramite(String numeroTramite);

    @PreAuthorize("isAuthenticated()")
    @Deprecated
    List<DocumentoResponseDTO> listarTodos();

    @PreAuthorize("isAuthenticated()")
    Page<DocumentoResponseDTO> listarTodos(Pageable pageable);

    @PreAuthorize("isAuthenticated()")
    List<DocumentoResponseDTO> listarPorEstado(EstadoDocumento estado);

    @PreAuthorize("hasAnyRole('MESA_PARTES', 'ADMINISTRADOR')")
    DocumentoResponseDTO cambiarEstado(String numeroTramite, CambioEstadoDTO cambioEstadoDTO);

    @PreAuthorize("isAuthenticated()")
    Resource descargarArchivo(String numeroTramite);

    @PreAuthorize("isAuthenticated()")
    Resource descargarAnexo(String numeroTramite);

    @PreAuthorize("isAuthenticated()")
    @Deprecated
    List<DocumentoResponseDTO> buscarPorFiltros(DocumentoFiltroDTO filtro);

    @PreAuthorize("isAuthenticated()")
    Page<DocumentoResponseDTO> buscarPorFiltros(DocumentoFiltroDTO filtro, Pageable pageable);

    @PreAuthorize("hasAnyRole('MESA_PARTES', 'ADMINISTRADOR')")
    DocumentoResponseDTO asignarArea(String numeroTramite, UUID areaId);

    byte[] generarCargoPdf(String numeroTramite);
}