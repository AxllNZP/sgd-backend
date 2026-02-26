package com.mesapartes.sgd.service.impl;

import com.mesapartes.sgd.dto.AreaRequestDTO;
import com.mesapartes.sgd.dto.AreaResponseDTO;
import com.mesapartes.sgd.entity.Area;
import com.mesapartes.sgd.repository.AreaRepository;
import com.mesapartes.sgd.service.AreaService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AreaServiceImpl implements AreaService {

    private final AreaRepository areaRepository;

    @Override
    public AreaResponseDTO crearArea(AreaRequestDTO request) {
        if (areaRepository.existsByNombre(request.getNombre())) {
            throw new RuntimeException("Ya existe un área con el nombre: " + request.getNombre());
        }
        Area area = new Area();
        area.setNombre(request.getNombre());
        area.setDescripcion(request.getDescripcion());
        return mapearRespuesta(areaRepository.save(area));
    }

    @Override
    public List<AreaResponseDTO> listarAreas() {
        return areaRepository.findAll()
                .stream()
                .map(this::mapearRespuesta)
                .collect(Collectors.toList());
    }

    @Override
    public AreaResponseDTO obtenerPorId(UUID id) {
        Area area = areaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Área no encontrada con id: " + id));
        return mapearRespuesta(area);
    }

    @Override
    public void desactivarArea(UUID id) {
        Area area = areaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Área no encontrada con id: " + id));
        area.setActiva(false);
        areaRepository.save(area);
    }

    private AreaResponseDTO mapearRespuesta(Area area) {
        AreaResponseDTO response = new AreaResponseDTO();
        response.setId(area.getId());
        response.setNombre(area.getNombre());
        response.setDescripcion(area.getDescripcion());
        response.setActiva(area.isActiva());
        return response;
    }
}