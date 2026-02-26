package com.mesapartes.sgd.service;

import com.mesapartes.sgd.dto.DerivacionRequestDTO;
import com.mesapartes.sgd.dto.DerivacionResponseDTO;
import java.util.List;

public interface DerivacionService {

    DerivacionResponseDTO derivarDocumento(String numeroTramite, DerivacionRequestDTO request);

    List<DerivacionResponseDTO> obtenerDerivacionesPorTramite(String numeroTramite);
}