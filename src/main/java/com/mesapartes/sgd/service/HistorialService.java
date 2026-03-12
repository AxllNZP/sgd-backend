package com.mesapartes.sgd.service;

import com.mesapartes.sgd.dto.HistorialResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;


public interface HistorialService {

    // Solo usuarios autenticados pueden ver el historial de un trámite
    @PreAuthorize("isAuthenticated()")
    Page<HistorialResponseDTO> obtenerHistorialPorNumeroTramite(String numeroTramite, Pageable pageable);

    @PreAuthorize("isAuthenticated()")
    @Deprecated
    List<HistorialResponseDTO> obtenerHistorialPorNumeroTramite(String numeroTramite);
}