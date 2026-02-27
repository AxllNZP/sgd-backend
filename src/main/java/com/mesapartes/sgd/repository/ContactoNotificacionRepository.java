package com.mesapartes.sgd.repository;

import com.mesapartes.sgd.entity.ContactoNotificacion;
import com.mesapartes.sgd.entity.PersonaJuridica;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ContactoNotificacionRepository extends JpaRepository<ContactoNotificacion, UUID> {

    // Obtener solo los contactos activos de una empresa
    List<ContactoNotificacion> findByPersonaJuridicaAndActivoTrue(PersonaJuridica personaJuridica);

    // Obtener todos los contactos de una empresa
    List<ContactoNotificacion> findByPersonaJuridica(PersonaJuridica personaJuridica);
}