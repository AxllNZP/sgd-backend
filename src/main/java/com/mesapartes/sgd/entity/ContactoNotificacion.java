package com.mesapartes.sgd.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.util.UUID;

@Data
@Entity
@Table(name = "contactos_notificacion")
public class ContactoNotificacion {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "persona_juridica_id", nullable = false)
    private PersonaJuridica personaJuridica;

    @Column(name = "nombres", nullable = false)
    private String nombres;

    @Column(name = "email", nullable = false)
    private String email;

    // Switch activo/inactivo (como muestra la guía en la tabla de contactos)
    @Column(name = "activo", nullable = false)
    private boolean activo = true;
}