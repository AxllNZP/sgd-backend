package com.mesapartes.sgd.service.impl;

import com.mesapartes.sgd.dto.HistorialResponseDTO;
import com.mesapartes.sgd.entity.Documento;
import com.mesapartes.sgd.entity.HistorialEstado;
import com.mesapartes.sgd.repository.DocumentoRepository;
import com.mesapartes.sgd.repository.HistorialEstadoRepository;
import com.mesapartes.sgd.service.HistorialService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class HistorialServiceImpl implements HistorialService {

    private final HistorialEstadoRepository historialEstadoRepository;
    private final DocumentoRepository documentoRepository;

    @Override
    public List<HistorialResponseDTO> obtenerHistorialPorNumeroTramite(String numeroTramite) {
        Documento documento = documentoRepository.findByNumeroTramite(numeroTramite)
                .orElseThrow(() -> new RuntimeException("Documento no encontrado: " + numeroTramite));

        return historialEstadoRepository.findByDocumentoOrderByFechaCambioAsc(documento)
                .stream()
                .map(h -> mapearRespuesta(h, numeroTramite))
                .collect(Collectors.toList());
    }

    private HistorialResponseDTO mapearRespuesta(HistorialEstado historial, String numeroTramite) {
        HistorialResponseDTO response = new HistorialResponseDTO();
        response.setId(historial.getId());
        response.setNumeroTramite(numeroTramite);
        response.setEstado(historial.getEstado());
        response.setObservacion(historial.getObservacion());
        response.setUsuarioResponsable(historial.getUsuarioResponsable());
        response.setFechaCambio(historial.getFechaCambio());
        return response;
    }
}