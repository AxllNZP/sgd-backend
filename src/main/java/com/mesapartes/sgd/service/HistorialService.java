package com.mesapartes.sgd.service;

import com.mesapartes.sgd.dto.HistorialResponseDTO;
import java.util.List;

public interface HistorialService {

    List<HistorialResponseDTO> obtenerHistorialPorNumeroTramite(String numeroTramite);
}