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

    @Column(name = "asunto", nullable = false)
    private String asunto;

    @Column(name = "tipo_documento")
    private String tipoDocumento;

    @Column(name = "ruta_archivo")
    private String rutaArchivo;

    @Column(name = "fecha_hora_registro", nullable = false)
    private LocalDateTime fechaHoraRegistro;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false)
    private EstadoDocumento estado;

    @Column(name = "email_remitente")
    private String emailRemitente;

    @ManyToOne
    @JoinColumn(name = "area_id")
    private Area area;

    @PrePersist
    public void prePersist() {
        this.fechaHoraRegistro = LocalDateTime.now();
        this.estado = EstadoDocumento.RECIBIDO;
    }
}