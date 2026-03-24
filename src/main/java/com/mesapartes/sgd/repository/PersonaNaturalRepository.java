package com.mesapartes.sgd.repository;

import com.mesapartes.sgd.entity.PersonaNatural;
import com.mesapartes.sgd.entity.TipoDocumento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Repository
public interface PersonaNaturalRepository extends JpaRepository<PersonaNatural, UUID> {

    Optional<PersonaNatural> findByNumeroDocumento(String numeroDocumento);

    Optional<PersonaNatural> findByEmail(String email);

    boolean existsByNumeroDocumento(String numeroDocumento);

    boolean existsByEmail(String email);

    // Para login: busca por tipo + número de documento
    Optional<PersonaNatural> findByTipoDocumentoAndNumeroDocumento(
            TipoDocumento tipoDocumento, String numeroDocumento);

    // Búsqueda dinámica por nombre, documento o email (case-insensitive)
    Page<PersonaNatural> findByNombresContainingIgnoreCaseOrNumeroDocumentoContainingIgnoreCaseOrEmailContainingIgnoreCase(
            String nombres, String numeroDocumento, String email, Pageable pageable);

    // Con filtro de estado
    Page<PersonaNatural> findByActivo(boolean activo, Pageable pageable);

    // Búsqueda + filtro de estado
    Page<PersonaNatural> findByActivoAndNombresContainingIgnoreCaseOrActivoAndNumeroDocumentoContainingIgnoreCaseOrActivoAndEmailContainingIgnoreCase(
            boolean activo1, String nombres,
            boolean activo2, String numeroDocumento,
            boolean activo3, String email,
            Pageable pageable);

    // Conteos para estadísticas
    long countByActivo(boolean activo);
    long countByVerificado(boolean verificado);
}