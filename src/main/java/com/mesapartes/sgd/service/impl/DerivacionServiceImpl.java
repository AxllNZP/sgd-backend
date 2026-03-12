package com.mesapartes.sgd.service.impl;

import com.mesapartes.sgd.dto.DerivacionRequestDTO;
import com.mesapartes.sgd.dto.DerivacionResponseDTO;
import com.mesapartes.sgd.entity.*;
import com.mesapartes.sgd.exception.BusinessException;
import com.mesapartes.sgd.exception.ResourceNotFoundException;
import com.mesapartes.sgd.repository.AreaRepository;
import com.mesapartes.sgd.repository.DerivacionRepository;
import com.mesapartes.sgd.repository.DocumentoRepository;
import com.mesapartes.sgd.repository.HistorialEstadoRepository;
import com.mesapartes.sgd.service.DerivacionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DerivacionServiceImpl implements DerivacionService {

    private final DerivacionRepository derivacionRepository;
    private final DocumentoRepository documentoRepository;
    private final AreaRepository areaRepository;
    private final HistorialEstadoRepository historialEstadoRepository;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DerivacionResponseDTO derivarDocumento(String numeroTramite,
                                                  DerivacionRequestDTO request) {

        Documento documento = documentoRepository.findByNumeroTramite(numeroTramite)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Documento no encontrado: " + numeroTramite));

        if (documento.getArea() == null) {
            throw new BusinessException(
                    "El documento no tiene área asignada aún");
        }

        Area areaOrigen = documento.getArea();

        Area areaDestino = areaRepository.findById(request.getAreaDestinoId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Área destino no encontrada"));

        if (areaOrigen.getId().equals(areaDestino.getId())) {
            throw new BusinessException(
                    "El área origen y destino no pueden ser la misma");
        }

        Derivacion derivacion = new Derivacion();
        derivacion.setDocumento(documento);
        derivacion.setAreaOrigen(areaOrigen);
        derivacion.setAreaDestino(areaDestino);
        derivacion.setMotivo(request.getMotivo());
        derivacion.setUsuarioResponsable(request.getUsuarioResponsable());

        derivacionRepository.save(derivacion);

        documento.setArea(areaDestino);
        documentoRepository.save(documento);

        HistorialEstado historial = new HistorialEstado();
        historial.setDocumento(documento);
        historial.setEstado(documento.getEstado());
        historial.setObservacion(
                "Derivado de " + areaOrigen.getNombre() +
                        " a " + areaDestino.getNombre() +
                        ". Motivo: " + request.getMotivo());
        historial.setUsuarioResponsable(request.getUsuarioResponsable());

        historialEstadoRepository.save(historial);

        return mapearRespuesta(derivacion, numeroTramite);
    }

    @Override
    public List<DerivacionResponseDTO> obtenerDerivacionesPorTramite(String numeroTramite) {

        Documento documento = documentoRepository.findByNumeroTramite(numeroTramite)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Documento no encontrado: " + numeroTramite));

        return derivacionRepository
                .findByDocumentoOrderByFechaDerivacionAsc(documento)
                .stream()
                .map(d -> mapearRespuesta(d, numeroTramite))
                .collect(Collectors.toList());
    }

    private DerivacionResponseDTO mapearRespuesta(Derivacion derivacion, String numeroTramite) {
        DerivacionResponseDTO response = new DerivacionResponseDTO();
        response.setId(derivacion.getId());
        response.setNumeroTramite(numeroTramite);
        response.setAreaOrigen(derivacion.getAreaOrigen().getNombre());
        response.setAreaDestino(derivacion.getAreaDestino().getNombre());
        response.setMotivo(derivacion.getMotivo());
        response.setUsuarioResponsable(derivacion.getUsuarioResponsable());
        response.setFechaDerivacion(derivacion.getFechaDerivacion());
        return response;
    }
}