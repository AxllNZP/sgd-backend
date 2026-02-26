package com.mesapartes.sgd.service.impl;

import com.mesapartes.sgd.dto.CambioEstadoDTO;
import com.mesapartes.sgd.dto.DocumentoFiltroDTO;
import com.mesapartes.sgd.dto.DocumentoRequestDTO;
import com.mesapartes.sgd.dto.DocumentoResponseDTO;
import com.mesapartes.sgd.entity.Area;
import com.mesapartes.sgd.entity.Documento;
import com.mesapartes.sgd.entity.EstadoDocumento;
import com.mesapartes.sgd.entity.HistorialEstado;
import com.mesapartes.sgd.repository.AreaRepository;
import com.mesapartes.sgd.repository.DocumentoRepository;
import com.mesapartes.sgd.repository.DocumentoSpecification;
import com.mesapartes.sgd.repository.HistorialEstadoRepository;
import com.mesapartes.sgd.service.DocumentoService;
import com.mesapartes.sgd.service.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DocumentoServiceImpl implements DocumentoService {

    private final DocumentoRepository documentoRepository;
    private final HistorialEstadoRepository historialEstadoRepository;
    private final EmailService emailService;
    private final AreaRepository areaRepository;

    @Value("${storage.location}")
    private String storageLocation;

    @Override
    public DocumentoResponseDTO registrarDocumento(DocumentoRequestDTO request, MultipartFile archivo) throws IOException {

        String numeroTramite = generarNumeroTramite();

        String rutaArchivo = null;
        if (archivo != null && !archivo.isEmpty()) {
            rutaArchivo = guardarArchivo(archivo, numeroTramite);
        }

        Documento documento = new Documento();
        documento.setNumeroTramite(numeroTramite);
        documento.setRemitente(request.getRemitente());
        documento.setDniRuc(request.getDniRuc());
        documento.setAsunto(request.getAsunto());
        documento.setTipoDocumento(request.getTipoDocumento());
        documento.setRutaArchivo(rutaArchivo);
        documento.setEmailRemitente(request.getEmailRemitente());

        Documento guardado = documentoRepository.save(documento);

        registrarHistorial(guardado, EstadoDocumento.RECIBIDO,
                "Documento registrado en Mesa de Partes", "SISTEMA");

        // Enviar email de confirmación
        if (request.getEmailRemitente() != null && !request.getEmailRemitente().isEmpty()) {
            emailService.enviarConfirmacionRegistro(
                    request.getEmailRemitente(),
                    numeroTramite,
                    request.getAsunto(),
                    request.getRemitente()
            );
        }

        return mapearRespuesta(guardado);
    }

    @Override
    public DocumentoResponseDTO consultarPorNumeroTramite(String numeroTramite) {
        Documento documento = documentoRepository.findByNumeroTramite(numeroTramite)
                .orElseThrow(() -> new RuntimeException("Documento no encontrado: " + numeroTramite));
        return mapearRespuesta(documento);
    }

    @Override
    public List<DocumentoResponseDTO> listarTodos() {
        return documentoRepository.findAll()
                .stream()
                .map(this::mapearRespuesta)
                .collect(Collectors.toList());
    }

    @Override
    public List<DocumentoResponseDTO> listarPorEstado(EstadoDocumento estado) {
        return documentoRepository.findByEstado(estado)
                .stream()
                .map(this::mapearRespuesta)
                .collect(Collectors.toList());
    }

    @Override
    public DocumentoResponseDTO cambiarEstado(String numeroTramite, CambioEstadoDTO cambioEstadoDTO) {
        Documento documento = documentoRepository.findByNumeroTramite(numeroTramite)
                .orElseThrow(() -> new RuntimeException("Documento no encontrado: " + numeroTramite));

        documento.setEstado(cambioEstadoDTO.getEstado());
        Documento actualizado = documentoRepository.save(documento);

        registrarHistorial(actualizado, cambioEstadoDTO.getEstado(),
                cambioEstadoDTO.getObservacion(), cambioEstadoDTO.getUsuarioResponsable());

        // Enviar email de cambio de estado
        if (actualizado.getEmailRemitente() != null && !actualizado.getEmailRemitente().isEmpty()) {
            emailService.enviarCambioEstado(
                    actualizado.getEmailRemitente(),
                    numeroTramite,
                    cambioEstadoDTO.getEstado().name(),
                    cambioEstadoDTO.getObservacion()
            );
        }

        return mapearRespuesta(actualizado);
    }

    // ===== MÉTODOS PRIVADOS =====

    private String generarNumeroTramite() {
        String fecha = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String unico = UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        return "MP-" + fecha + "-" + unico;
    }

    private String guardarArchivo(MultipartFile archivo, String numeroTramite) throws IOException {
        Path carpeta = Paths.get(storageLocation);
        if (!Files.exists(carpeta)) {
            Files.createDirectories(carpeta);
        }
        String nombreArchivo = numeroTramite + "_" + archivo.getOriginalFilename();
        Path destino = carpeta.resolve(nombreArchivo);
        Files.copy(archivo.getInputStream(), destino);
        return destino.toString();
    }

    private void registrarHistorial(Documento documento, EstadoDocumento estado,
                                    String observacion, String usuarioResponsable) {
        HistorialEstado historial = new HistorialEstado();
        historial.setDocumento(documento);
        historial.setEstado(estado);
        historial.setObservacion(observacion);
        historial.setUsuarioResponsable(usuarioResponsable);
        historialEstadoRepository.save(historial);
    }

    private DocumentoResponseDTO mapearRespuesta(Documento documento) {
        DocumentoResponseDTO response = new DocumentoResponseDTO();
        response.setId(documento.getId());
        response.setNumeroTramite(documento.getNumeroTramite());
        response.setRemitente(documento.getRemitente());
        response.setDniRuc(documento.getDniRuc());
        response.setAsunto(documento.getAsunto());
        response.setTipoDocumento(documento.getTipoDocumento());
        response.setRutaArchivo(documento.getRutaArchivo());
        response.setFechaHoraRegistro(documento.getFechaHoraRegistro());
        response.setEstado(documento.getEstado());
        response.setEmailRemitente(documento.getEmailRemitente());
        if (documento.getArea() != null) {
            response.setAreaId(documento.getArea().getId());
            response.setAreaNombre(documento.getArea().getNombre());
        }
        return response;
    }


    // ===== DESCARGAR EL ARCHIVO =====
    @Override
    public Resource descargarArchivo(String numeroTramite) {
        Documento documento = documentoRepository.findByNumeroTramite(numeroTramite)
                .orElseThrow(() -> new RuntimeException("Documento no encontrado: " + numeroTramite));

        if (documento.getRutaArchivo() == null) {
            throw new RuntimeException("Este documento no tiene archivo adjunto");
        }

        try {
            Path ruta = Paths.get(documento.getRutaArchivo());
            Resource resource = new org.springframework.core.io.UrlResource(ruta.toUri());

            if (!resource.exists()) {
                throw new RuntimeException("El archivo no existe en el servidor");
            }

            return resource;
        } catch (Exception e) {
            throw new RuntimeException("Error al descargar el archivo: " + e.getMessage());
        }
    }

    // ===== BUSCAR POR FILTROS =====
    @Override
    public List<DocumentoResponseDTO> buscarPorFiltros(DocumentoFiltroDTO filtro) {
        return documentoRepository.findAll(DocumentoSpecification.conFiltros(filtro))
                .stream()
                .map(this::mapearRespuesta)
                .collect(Collectors.toList());
    }

    @Override
    public DocumentoResponseDTO asignarArea(String numeroTramite, UUID areaId) {
        Documento documento = documentoRepository.findByNumeroTramite(numeroTramite)
                .orElseThrow(() -> new RuntimeException("Documento no encontrado: " + numeroTramite));

        Area area = areaRepository.findById(areaId)
                .orElseThrow(() -> new RuntimeException("Área no encontrada: " + areaId));

        documento.setArea(area);
        documento.setEstado(EstadoDocumento.EN_PROCESO);
        Documento actualizado = documentoRepository.save(documento);

        registrarHistorial(actualizado, EstadoDocumento.EN_PROCESO,
                "Documento asignado al área: " + area.getNombre(), "SISTEMA");

        return mapearRespuesta(actualizado);
    }
}