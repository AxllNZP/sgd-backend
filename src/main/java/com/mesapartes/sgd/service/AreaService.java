package com.mesapartes.sgd.service;

import com.mesapartes.sgd.dto.AreaRequestDTO;
import com.mesapartes.sgd.dto.AreaResponseDTO;
import java.util.List;
import java.util.UUID;

public interface AreaService {

    AreaResponseDTO crearArea(AreaRequestDTO request);

    List<AreaResponseDTO> listarAreas();

    AreaResponseDTO obtenerPorId(UUID id);

    void desactivarArea(UUID id);
}