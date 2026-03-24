package com.mesapartes.sgd.service.impl;

import com.mesapartes.sgd.dto.AreaRequestDTO;
import com.mesapartes.sgd.dto.AreaResponseDTO;
import com.mesapartes.sgd.entity.Area;
import com.mesapartes.sgd.exception.BusinessException;
import com.mesapartes.sgd.exception.ResourceNotFoundException;
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
            throw new BusinessException(
                    "Ya existe un área con el nombre: " + request.getNombre());
        }

        Area area = new Area();
        area.setNombre(request.getNombre());
        area.setDescripcion(request.getDescripcion());

        Area guardada = areaRepository.save(area);

        return mapearRespuesta(guardada);
    }

    // =============================================================
    // CORRECCIÓN: listarAreas() ahora devuelve SOLO las activas.
    //
    // MOTIVO: este método alimenta el dropdown "Asignar Área" en
    // el detalle del documento. Si devuelve áreas desactivadas,
    // el usuario puede asignar un documento a un área que ya no
    // está operativa — lo cual no tiene sentido de negocio.
    //
    // 📚 LECCIÓN — filter() en Stream:
    //   .filter(a -> a.isActiva()) conserva solo los elementos
    //   donde la condición es true. Los demás se descartan antes
    //   de llegar al .map(). Es O(n) pero sobre un dataset pequeño
    //   (áreas de una institución) es perfectamente eficiente.
    //
    // El método desactivarArea() sigue funcionando igual —
    // simplemente marca activa=false y ese área deja de aparecer
    // en el listado la próxima vez que se consulte.
    // =============================================================
    @Override
    public List<AreaResponseDTO> listarAreas() {
        return areaRepository.findAll()
                .stream()
                .filter(Area::isActiva)          // ← CORRECCIÓN: solo activas
                .map(this::mapearRespuesta)
                .collect(Collectors.toList());
    }

    @Override
    public AreaResponseDTO obtenerPorId(UUID id) {

        Area area = areaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Área no encontrada con id: " + id));

        return mapearRespuesta(area);
    }

    @Override
    public void desactivarArea(UUID id) {

        Area area = areaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Área no encontrada con id: " + id));

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