package com.mesapartes.sgd.service.impl;

import com.mesapartes.sgd.dto.CambioEstadoDTO;
import com.mesapartes.sgd.dto.DocumentoFiltroDTO;
import com.mesapartes.sgd.dto.DocumentoRequestDTO;
import com.mesapartes.sgd.dto.DocumentoResponseDTO;
import com.mesapartes.sgd.entity.*;
import com.mesapartes.sgd.repository.*;
import com.mesapartes.sgd.service.DocumentoService;
import com.mesapartes.sgd.service.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
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
    private final PersonaNaturalRepository naturalRepo;
    private final PersonaJuridicaRepository juridicaRepo;
    private final ContactoNotificacionRepository contactoRepo;

    @Value("${storage.location}")
    private String storageLocation;

    // ===== REGISTRAR DOCUMENTO =====
    @Override
    public DocumentoResponseDTO registrarDocumento(DocumentoRequestDTO request,
                                                   MultipartFile archivo,
                                                   MultipartFile anexo) throws IOException {
        // Validar tamaño del archivo principal (máx 50MB)
        if (archivo != null && !archivo.isEmpty()) {
            long maxArchivo = 50L * 1024 * 1024;
            if (archivo.getSize() > maxArchivo) {
                throw new RuntimeException(
                        "El archivo principal supera el tamaño máximo permitido de 50MB");
            }
        }

        // Validar tamaño del anexo (máx 20MB)
        if (anexo != null && !anexo.isEmpty()) {
            long maxAnexo = 20L * 1024 * 1024;
            if (anexo.getSize() > maxAnexo) {
                throw new RuntimeException(
                        "El anexo supera el tamaño máximo permitido de 20MB");
            }
        }

        String numeroTramite = generarNumeroTramite();

        // Guardar archivo principal
        String rutaArchivo = null;
        String nombreArchivoOriginal = null;
        if (archivo != null && !archivo.isEmpty()) {
            rutaArchivo = guardarArchivo(archivo, numeroTramite, "DOC");
            nombreArchivoOriginal = archivo.getOriginalFilename();
        }

        // Guardar anexo
        String rutaAnexo = null;
        String nombreAnexoOriginal = null;
        if (anexo != null && !anexo.isEmpty()) {
            rutaAnexo = guardarArchivo(anexo, numeroTramite, "ANEXO");
            nombreAnexoOriginal = anexo.getOriginalFilename();
        }

        // Construir entidad
        Documento documento = new Documento();
        documento.setNumeroTramite(numeroTramite);
        documento.setTipoPersona(request.getTipoPersona());
        documento.setRemitente(request.getRemitente());
        documento.setDniRuc(request.getDniRuc());
        documento.setTipoDocumento(request.getTipoDocumento());
        documento.setNumeroDocumentoRemitente(request.getNumeroDocumentoRemitente());
        documento.setNumeroFolios(request.getNumeroFolios());
        documento.setAsunto(request.getAsunto());
        documento.setRutaArchivo(rutaArchivo);
        documento.setNombreArchivoOriginal(nombreArchivoOriginal);
        documento.setRutaAnexo(rutaAnexo);
        documento.setNombreAnexoOriginal(nombreAnexoOriginal);
        documento.setEmailRemitente(request.getEmailRemitente());
        documento.setEmailNotificacionAdicional(request.getEmailNotificacionAdicional());

        // Guardar IDs de contactos de notificación (solo Jurídica)
        if (request.getContactosNotificacionIds() != null
                && !request.getContactosNotificacionIds().isEmpty()) {
            String idsString = request.getContactosNotificacionIds()
                    .stream()
                    .map(UUID::toString)
                    .collect(Collectors.joining(","));
            documento.setContactosNotificacionIds(idsString);
        }

        Documento guardado = documentoRepository.save(documento);

        // Registrar en historial
        registrarHistorial(guardado, EstadoDocumento.RECIBIDO,
                "Documento registrado en Mesa de Partes", "SISTEMA");

        // ===== ENVIAR NOTIFICACIONES POR EMAIL =====
        enviarNotificaciones(guardado, request);

        return mapearRespuesta(guardado);
    }

    // ===== ENVIAR NOTIFICACIONES =====
    private void enviarNotificaciones(Documento documento, DocumentoRequestDTO request) {
        String tipo = request.getTipoPersona().toUpperCase();

        if ("NATURAL".equals(tipo)) {
            // Email principal del remitente
            if (request.getEmailRemitente() != null
                    && !request.getEmailRemitente().isEmpty()) {
                emailService.enviarConfirmacionExpediente(
                        request.getEmailRemitente(),
                        request.getRemitente(),
                        documento.getNumeroTramite(),
                        request.getAsunto(),
                        request.getTipoDocumento(),
                        request.getNumeroFolios()
                );
            }

            // Email adicional (copia)
            if (request.getEmailNotificacionAdicional() != null
                    && !request.getEmailNotificacionAdicional().isEmpty()) {
                emailService.enviarConfirmacionExpediente(
                        request.getEmailNotificacionAdicional(),
                        request.getRemitente(),
                        documento.getNumeroTramite(),
                        request.getAsunto(),
                        request.getTipoDocumento(),
                        request.getNumeroFolios()
                );
            }

        } else if ("JURIDICA".equals(tipo)) {
            // Email del representante legal
            if (request.getEmailRemitente() != null
                    && !request.getEmailRemitente().isEmpty()) {
                emailService.enviarConfirmacionExpediente(
                        request.getEmailRemitente(),
                        request.getRemitente(),
                        documento.getNumeroTramite(),
                        request.getAsunto(),
                        request.getTipoDocumento(),
                        request.getNumeroFolios()
                );
            }

            // Contactos de notificación seleccionados
            if (request.getContactosNotificacionIds() != null) {
                for (UUID contactoId : request.getContactosNotificacionIds()) {
                    contactoRepo.findById(contactoId).ifPresent(contacto -> {
                        if (contacto.isActivo()) {
                            emailService.enviarConfirmacionExpediente(
                                    contacto.getEmail(),
                                    request.getRemitente(),
                                    documento.getNumeroTramite(),
                                    request.getAsunto(),
                                    request.getTipoDocumento(),
                                    request.getNumeroFolios()
                            );
                        }
                    });
                }
            }
        }
    }

    // ===== CONSULTAR POR NÚMERO DE TRÁMITE =====
    @Override
    public DocumentoResponseDTO consultarPorNumeroTramite(String numeroTramite) {
        Documento documento = documentoRepository.findByNumeroTramite(numeroTramite)
                .orElseThrow(() -> new RuntimeException(
                        "Documento no encontrado: " + numeroTramite));
        return mapearRespuesta(documento);
    }

    // ===== LISTAR TODOS =====
    @Override
    public List<DocumentoResponseDTO> listarTodos() {
        return documentoRepository.findAll()
                .stream()
                .map(this::mapearRespuesta)
                .collect(Collectors.toList());
    }

    // ===== LISTAR POR ESTADO =====
    @Override
    public List<DocumentoResponseDTO> listarPorEstado(EstadoDocumento estado) {
        return documentoRepository.findByEstado(estado)
                .stream()
                .map(this::mapearRespuesta)
                .collect(Collectors.toList());
    }

    // ===== CAMBIAR ESTADO =====
    @Override
    public DocumentoResponseDTO cambiarEstado(String numeroTramite,
                                              CambioEstadoDTO cambioEstadoDTO) {
        Documento documento = documentoRepository.findByNumeroTramite(numeroTramite)
                .orElseThrow(() -> new RuntimeException(
                        "Documento no encontrado: " + numeroTramite));

        documento.setEstado(cambioEstadoDTO.getEstado());
        Documento actualizado = documentoRepository.save(documento);

        registrarHistorial(actualizado, cambioEstadoDTO.getEstado(),
                cambioEstadoDTO.getObservacion(),
                cambioEstadoDTO.getUsuarioResponsable());

        if (actualizado.getEmailRemitente() != null
                && !actualizado.getEmailRemitente().isEmpty()) {
            emailService.enviarCambioEstado(
                    actualizado.getEmailRemitente(),
                    numeroTramite,
                    cambioEstadoDTO.getEstado().name(),
                    cambioEstadoDTO.getObservacion()
            );
        }

        return mapearRespuesta(actualizado);
    }

    // ===== DESCARGAR ARCHIVO PRINCIPAL =====
    @Override
    public Resource descargarArchivo(String numeroTramite) {
        Documento documento = documentoRepository.findByNumeroTramite(numeroTramite)
                .orElseThrow(() -> new RuntimeException(
                        "Documento no encontrado: " + numeroTramite));

        if (documento.getRutaArchivo() == null) {
            throw new RuntimeException("Este documento no tiene archivo adjunto");
        }

        return obtenerResource(documento.getRutaArchivo());
    }

    // ===== DESCARGAR ANEXO =====
    @Override
    public Resource descargarAnexo(String numeroTramite) {
        Documento documento = documentoRepository.findByNumeroTramite(numeroTramite)
                .orElseThrow(() -> new RuntimeException(
                        "Documento no encontrado: " + numeroTramite));

        if (documento.getRutaAnexo() == null) {
            throw new RuntimeException("Este documento no tiene anexo adjunto");
        }

        return obtenerResource(documento.getRutaAnexo());
    }

    // ===== BUSCAR POR FILTROS =====
    @Override
    public List<DocumentoResponseDTO> buscarPorFiltros(DocumentoFiltroDTO filtro) {
        return documentoRepository.findAll(DocumentoSpecification.conFiltros(filtro))
                .stream()
                .map(this::mapearRespuesta)
                .collect(Collectors.toList());
    }

    // ===== ASIGNAR ÁREA =====
    @Override
    public DocumentoResponseDTO asignarArea(String numeroTramite, UUID areaId) {
        Documento documento = documentoRepository.findByNumeroTramite(numeroTramite)
                .orElseThrow(() -> new RuntimeException(
                        "Documento no encontrado: " + numeroTramite));

        Area area = areaRepository.findById(areaId)
                .orElseThrow(() -> new RuntimeException(
                        "Área no encontrada: " + areaId));

        documento.setArea(area);
        documento.setEstado(EstadoDocumento.EN_PROCESO);
        Documento actualizado = documentoRepository.save(documento);

        registrarHistorial(actualizado, EstadoDocumento.EN_PROCESO,
                "Documento asignado al área: " + area.getNombre(), "SISTEMA");

        return mapearRespuesta(actualizado);
    }

    // ===== GENERAR CARGO EN PDF =====
    @Override
    public byte[] generarCargoPdf(String numeroTramite) {
        Documento documento = documentoRepository.findByNumeroTramite(numeroTramite)
                .orElseThrow(() -> new RuntimeException(
                        "Documento no encontrado: " + numeroTramite));

        // Generamos el PDF manualmente en formato HTML → bytes
        // Usamos una implementación simple sin librerías externas
        String fechaFormateada = documento.getFechaHoraRegistro()
                .format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));

        String html = """
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="UTF-8"/>
                    <style>
                        body { font-family: Arial, sans-serif; margin: 40px; }
                        h2 { color: #1a5276; text-align: center; }
                        .subtitulo { text-align: center; color: #555; margin-bottom: 30px; }
                        table { width: 100%%; border-collapse: collapse; margin-top: 20px; }
                        td { padding: 10px; border: 1px solid #ccc; }
                        .label { background-color: #eaf4fb; font-weight: bold; width: 40%%; }
                        .footer { margin-top: 40px; text-align: center;
                                  font-size: 12px; color: #888; }
                        .numero { font-size: 20px; font-weight: bold;
                                  color: #1a5276; text-align: center;
                                  border: 2px solid #1a5276; padding: 10px;
                                  margin: 20px 0; }
                    </style>
                </head>
                <body>
                    <h2>CARGO DE RECEPCIÓN</h2>
                    <p class="subtitulo">Sistema de Mesa de Partes Digital</p>
                    <div class="numero">N° EXPEDIENTE: %s</div>
                    <table>
                        <tr>
                            <td class="label">Remitente</td>
                            <td>%s</td>
                        </tr>
                        <tr>
                            <td class="label">DNI / RUC</td>
                            <td>%s</td>
                        </tr>
                        <tr>
                            <td class="label">Tipo de Documento</td>
                            <td>%s</td>
                        </tr>
                        <tr>
                            <td class="label">Asunto</td>
                            <td>%s</td>
                        </tr>
                        <tr>
                            <td class="label">N° de Folios</td>
                            <td>%d</td>
                        </tr>
                        <tr>
                            <td class="label">Fecha y Hora de Registro</td>
                            <td>%s</td>
                        </tr>
                        <tr>
                            <td class="label">Estado</td>
                            <td>%s</td>
                        </tr>
                    </table>
                    <p class="footer">
                        Este documento es el comprobante oficial de recepción de su trámite.<br/>
                        Consérvelo para realizar el seguimiento correspondiente.
                    </p>
                </body>
                </html>
                """.formatted(
                documento.getNumeroTramite(),
                documento.getRemitente(),
                documento.getDniRuc(),
                documento.getTipoDocumento() != null ? documento.getTipoDocumento() : "-",
                documento.getAsunto(),
                documento.getNumeroFolios() != null ? documento.getNumeroFolios() : 0,
                fechaFormateada,
                documento.getEstado().name()
        );

        return html.getBytes();
    }

    // ===== MÉTODOS PRIVADOS =====

    private String generarNumeroTramite() {
        String fecha = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String unico = UUID.randomUUID().toString()
                .substring(0, 6).toUpperCase();
        return "MP-" + fecha + "-" + unico;
    }

    private String guardarArchivo(MultipartFile archivo, String numeroTramite,
                                  String prefijo) throws IOException {
        Path carpeta = Paths.get(storageLocation);
        if (!Files.exists(carpeta)) {
            Files.createDirectories(carpeta);
        }
        String nombreArchivo = numeroTramite + "_" + prefijo
                + "_" + archivo.getOriginalFilename();
        Path destino = carpeta.resolve(nombreArchivo);
        Files.copy(archivo.getInputStream(), destino);
        return destino.toString();
    }

    private Resource obtenerResource(String ruta) {
        try {
            Path path = Paths.get(ruta);
            Resource resource = new UrlResource(path.toUri());
            if (!resource.exists()) {
                throw new RuntimeException("El archivo no existe en el servidor");
            }
            return resource;
        } catch (Exception e) {
            throw new RuntimeException("Error al acceder al archivo: " + e.getMessage());
        }
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
        response.setTipoPersona(documento.getTipoPersona());
        response.setRemitente(documento.getRemitente());
        response.setDniRuc(documento.getDniRuc());
        response.setAsunto(documento.getAsunto());
        response.setTipoDocumento(documento.getTipoDocumento());
        response.setNumeroDocumentoRemitente(documento.getNumeroDocumentoRemitente());
        response.setNumeroFolios(documento.getNumeroFolios());
        response.setNombreArchivoOriginal(documento.getNombreArchivoOriginal());
        response.setRutaArchivo(documento.getRutaArchivo());
        response.setNombreAnexoOriginal(documento.getNombreAnexoOriginal());
        response.setRutaAnexo(documento.getRutaAnexo());
        response.setFechaHoraRegistro(documento.getFechaHoraRegistro());
        response.setEstado(documento.getEstado());
        response.setEmailRemitente(documento.getEmailRemitente());
        response.setEmailNotificacionAdicional(documento.getEmailNotificacionAdicional());
        response.setContactosNotificacionIds(documento.getContactosNotificacionIds());
        if (documento.getArea() != null) {
            response.setAreaId(documento.getArea().getId());
            response.setAreaNombre(documento.getArea().getNombre());
        }
        return response;
    }
}