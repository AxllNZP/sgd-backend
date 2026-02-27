package com.mesapartes.sgd.dto;

import com.mesapartes.sgd.entity.PreguntaSeguridad;
import com.mesapartes.sgd.entity.TipoDocumento;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
public class PerfilJuridicaResponseDTO {

    private UUID id;

    // Solo lectura
    private String ruc;
    private String razonSocial;
    private PreguntaSeguridad preguntaSeguridad;
    private String descripcionPregunta;

    // Representante legal (solo lectura)
    private TipoDocumento tipoDocRepresentante;
    private String numDocRepresentante;
    private String nombresRepresentante;
    private String apellidoPaternoRepresentante;
    private String apellidoMaternoRepresentante;
    private String emailRepresentante;

    // Editables
    private String departamento;
    private String provincia;
    private String distrito;
    private String direccion;
    private String telefono;

    private boolean afiliadoBuzon;
    private LocalDateTime fechaCreacion;

    // Lista de contactos de notificación
    private List<ContactoNotificacionResponseDTO> contactosNotificacion;
}