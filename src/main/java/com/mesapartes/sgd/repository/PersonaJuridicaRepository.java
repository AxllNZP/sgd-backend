package com.mesapartes.sgd.repository;

import com.mesapartes.sgd.entity.PersonaJuridica;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface PersonaJuridicaRepository extends JpaRepository<PersonaJuridica, UUID> {

    Optional<PersonaJuridica> findByRuc(String ruc);

    boolean existsByRuc(String ruc);

    // Para login: busca por RUC
    Optional<PersonaJuridica> findByRucAndActivoTrue(String ruc);
}