package com.mesapartes.sgd.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
@Entity
@Table(name = "personas_juridicas")
public class PersonaJuridica {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    // ===== DATOS DE LA EMPRESA =====
    @Column(name = "ruc", unique = true, nullable = false, length = 11)
    private String ruc;

    @Column(name = "razon_social", nullable = false)
    private String razonSocial;

    // ===== CREDENCIALES Y SEGURIDAD =====
    @Column(name = "password", nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(name = "pregunta_seguridad", nullable = false)
    private PreguntaSeguridad preguntaSeguridad;

    @Column(name = "respuesta_seguridad", nullable = false)
    private String respuestaSeguridad;

    // ===== DATOS DEL REPRESENTANTE LEGAL =====
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_doc_representante", nullable = false)
    private TipoDocumento tipoDocRepresentante;

    @Column(name = "num_doc_representante", nullable = false)
    private String numDocRepresentante;

    @Column(name = "nombres_representante", nullable = false)
    private String nombresRepresentante;

    @Column(name = "apellido_paterno_representante", nullable = false)
    private String apellidoPaternoRepresentante;

    @Column(name = "apellido_materno_representante", nullable = false)
    private String apellidoMaternoRepresentante;

    @Column(name = "email_representante", nullable = false)
    private String emailRepresentante;

    // ===== UBIGEO DE LA EMPRESA =====
    @Column(name = "departamento", nullable = false)
    private String departamento;

    @Column(name = "provincia", nullable = false)
    private String provincia;

    @Column(name = "distrito", nullable = false)
    private String distrito;

    @Column(name = "direccion", nullable = false)
    private String direccion;

    @Column(name = "telefono", nullable = false)
    private String telefono;

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

    // ===== CONTACTOS DE NOTIFICACIÓN (múltiples) =====
    @OneToMany(mappedBy = "personaJuridica", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ContactoNotificacion> contactosNotificacion = new ArrayList<>();

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