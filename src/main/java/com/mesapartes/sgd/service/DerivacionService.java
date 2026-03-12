package com.mesapartes.sgd.service;

import com.mesapartes.sgd.dto.DerivacionRequestDTO;
import com.mesapartes.sgd.dto.DerivacionResponseDTO;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;

public interface DerivacionService {

    @PreAuthorize("hasAnyRole('MESA_PARTES', 'ADMINISTRADOR')")
    DerivacionResponseDTO derivarDocumento(String numeroTramite, DerivacionRequestDTO request);

    @PreAuthorize("hasAnyRole('MESA_PARTES', 'ADMINISTRADOR')")
    List<DerivacionResponseDTO> obtenerDerivacionesPorTramite(String numeroTramite);
}