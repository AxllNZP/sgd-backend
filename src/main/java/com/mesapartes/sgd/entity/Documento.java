package com.mesapartes.sgd.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Entity
@Table(name = "documentos")
public class Documento {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "numero_tramite", unique = true, nullable = false)
    private String numeroTramite;

    @Column(name = "remitente", nullable = false)
    private String remitente;

    @Column(name = "dni_ruc", nullable = false)
    private String dniRuc;

    // Tipo de persona que envía (NATURAL o JURIDICA)
    @Column(name = "tipo_persona")
    private String tipoPersona;

    @Column(name = "asunto", nullable = false, length = 900)
    private String asunto;

    // Tipo de escrito: CARTA, OFICIO, SOLICITUD, etc.
    @Column(name = "tipo_documento")
    private String tipoDocumento;

    // Número identificador del documento del remitente
    @Column(name = "numero_documento_remitente")
    private String numeroDocumentoRemitente;

    // Cantidad total de páginas
    @Column(name = "numero_folios")
    private Integer numeroFolios;

    // Ruta del archivo principal (PDF, máx 50MB)
    @Column(name = "ruta_archivo")
    private String rutaArchivo;

    // Nombre original del archivo principal
    @Column(name = "nombre_archivo_original")
    private String nombreArchivoOriginal;

    // Ruta del anexo (PDF, máx 20MB)
    @Column(name = "ruta_anexo")
    private String rutaAnexo;

    // Nombre original del anexo
    @Column(name = "nombre_anexo_original")
    private String nombreAnexoOriginal;

    @Column(name = "fecha_hora_registro", nullable = false)
    private LocalDateTime fechaHoraRegistro;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false)
    private EstadoDocumento estado;

    // Email principal del remitente
    @Column(name = "email_remitente")
    private String emailRemitente;

    // Email adicional para notificación (Persona Natural)
    @Column(name = "email_notificacion_adicional")
    private String emailNotificacionAdicional;

    // IDs de contactos seleccionados para notificación (Persona Jurídica)
    // Se guardan como texto separado por comas: "uuid1,uuid2"
    @Column(name = "contactos_notificacion_ids")
    private String contactosNotificacionIds;

    @ManyToOne
    @JoinColumn(name = "area_id")
    private Area area;

    @PrePersist
    public void prePersist() {
        this.fechaHoraRegistro = LocalDateTime.now();
        this.estado = EstadoDocumento.RECIBIDO;
    }
}