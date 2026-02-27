package com.mesapartes.sgd.repository;

import com.mesapartes.sgd.entity.PersonaNatural;
import com.mesapartes.sgd.entity.TipoDocumento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface PersonaNaturalRepository extends JpaRepository<PersonaNatural, UUID> {

    Optional<PersonaNatural> findByNumeroDocumento(String numeroDocumento);

    Optional<PersonaNatural> findByEmail(String email);

    boolean existsByNumeroDocumento(String numeroDocumento);

    boolean existsByEmail(String email);

    // Para login: busca por tipo + número de documento
    Optional<PersonaNatural> findByTipoDocumentoAndNumeroDocumento(
            TipoDocumento tipoDocumento, String numeroDocumento);
}