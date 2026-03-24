package com.mesapartes.sgd.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Entity
@Table(name = "personas_naturales")
public class PersonaNatural {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    // ===== DOCUMENTO DE IDENTIDAD =====
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_documento", nullable = false)
    private TipoDocumento tipoDocumento;

    @Column(name = "numero_documento", unique = true, nullable = false)
    private String numeroDocumento;

    // ===== DATOS PERSONALES =====
    @Column(name = "nombres", nullable = false)
    private String nombres;

    @Column(name = "apellido_paterno", nullable = false)
    private String apellidoPaterno;

    @Column(name = "apellido_materno", nullable = false)
    private String apellidoMaterno;

    // ===== UBIGEO =====
    @Column(name = "departamento", nullable = false)
    private String departamento;

    @Column(name = "provincia", nullable = false)
    private String provincia;

    @Column(name = "distrito", nullable = false)
    private String distrito;

    // ===== CONTACTO =====
    @Column(name = "direccion", nullable = false)
    private String direccion;

    @Column(name = "telefono", nullable = false)
    private String telefono;

    // ===== CREDENCIALES Y SEGURIDAD =====
    @Column(name = "email", unique = true, nullable = false)
    private String email;

    @Column(name = "password", nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(name = "pregunta_seguridad", nullable = false)
    private PreguntaSeguridad preguntaSeguridad;

    @Column(name = "respuesta_seguridad", nullable = false)
    private String respuestaSeguridad;

    // ===== OPCIONES =====
    @Column(name = "afiliado_buzon", nullable = false)
    private boolean afiliadoBuzon = false;

    // ===== ESTADO =====
    @Column(name = "activo", nullable = false)
    private boolean activo = false;

    @Column(name = "verificado", nullable = false)
    private boolean verificado = false;

    // ===== VERIFICACIÓN DE CUENTA =====
    @Column(name = "codigo_verificacion")
    private String codigoVerificacion;

    @Column(name = "codigo_expiracion")
    private LocalDateTime codigoExpiracion;

    // ===== AUDITORÍA =====
    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion;

    @PrePersist
    public void prePersist() {
        this.fechaCreacion = LocalDateTime.now();
        this.activo = false;
        this.verificado = false;
    }
}