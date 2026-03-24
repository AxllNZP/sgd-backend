package com.mesapartes.sgd.service;

import com.mesapartes.sgd.dto.RespuestaRequestDTO;
import com.mesapartes.sgd.dto.RespuestaResponseDTO;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;

public interface RespuestaService {

    @PreAuthorize("hasAnyRole('MESA_PARTES', 'ADMINISTRADOR')")
    RespuestaResponseDTO emitirRespuesta(String numeroTramite, RespuestaRequestDTO request);

    @PreAuthorize("hasAnyRole('MESA_PARTES', 'ADMINISTRADOR')")
    List<RespuestaResponseDTO> obtenerRespuestasPorTramite(String numeroTramite);
}