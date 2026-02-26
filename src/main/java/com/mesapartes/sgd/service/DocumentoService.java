package com.mesapartes.sgd.service;

import com.mesapartes.sgd.dto.CambioEstadoDTO;
import com.mesapartes.sgd.dto.DocumentoFiltroDTO;
import com.mesapartes.sgd.dto.DocumentoRequestDTO;
import com.mesapartes.sgd.dto.DocumentoResponseDTO;
import com.mesapartes.sgd.entity.EstadoDocumento;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

public interface DocumentoService {

    DocumentoResponseDTO registrarDocumento(DocumentoRequestDTO request, MultipartFile archivo) throws IOException;

    DocumentoResponseDTO consultarPorNumeroTramite(String numeroTramite);

    List<DocumentoResponseDTO> listarTodos();

    List<DocumentoResponseDTO> listarPorEstado(EstadoDocumento estado);

    DocumentoResponseDTO cambiarEstado(String numeroTramite, CambioEstadoDTO cambioEstadoDTO);

    org.springframework.core.io.Resource descargarArchivo(String numeroTramite);

    List<DocumentoResponseDTO> buscarPorFiltros(DocumentoFiltroDTO filtro);

    DocumentoResponseDTO asignarArea(String numeroTramite, UUID areaId);
}