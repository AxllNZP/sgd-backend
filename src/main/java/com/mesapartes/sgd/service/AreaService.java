package com.mesapartes.sgd.service;

import com.mesapartes.sgd.dto.AreaRequestDTO;
import com.mesapartes.sgd.dto.AreaResponseDTO;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;
import java.util.UUID;

public interface AreaService {

    @PreAuthorize("hasRole('ADMINISTRADOR')")
    AreaResponseDTO crearArea(AreaRequestDTO request);

    @PreAuthorize("hasRole('ADMINISTRADOR')")
    List<AreaResponseDTO> listarAreas();

    @PreAuthorize("hasRole('ADMINISTRADOR')")
    AreaResponseDTO obtenerPorId(UUID id);

    @PreAuthorize("hasRole('ADMINISTRADOR')")
    void desactivarArea(UUID id);
}