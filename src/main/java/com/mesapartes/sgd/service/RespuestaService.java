package com.mesapartes.sgd.service;

import com.mesapartes.sgd.dto.RespuestaRequestDTO;
import com.mesapartes.sgd.dto.RespuestaResponseDTO;
import java.util.List;

public interface RespuestaService {

    RespuestaResponseDTO emitirRespuesta(String numeroTramite, RespuestaRequestDTO request);

    List<RespuestaResponseDTO> obtenerRespuestasPorTramite(String numeroTramite);
}