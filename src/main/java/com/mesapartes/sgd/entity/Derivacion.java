package com.mesapartes.sgd.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Entity
@Table(name = "derivaciones")
public class Derivacion {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "documento_id", nullable = false)
    private Documento documento;

    @ManyToOne
    @JoinColumn(name = "area_origen_id", nullable = false)
    private Area areaOrigen;

    @ManyToOne
    @JoinColumn(name = "area_destino_id", nullable = false)
    private Area areaDestino;

    @Column(name = "motivo", nullable = false)
    private String motivo;

    @Column(name = "usuario_responsable", nullable = false)
    private String usuarioResponsable;

    @Column(name = "fecha_derivacion", nullable = false)
    private LocalDateTime fechaDerivacion;

    @PrePersist
    public void prePersist() {
        this.fechaDerivacion = LocalDateTime.now();
    }
}