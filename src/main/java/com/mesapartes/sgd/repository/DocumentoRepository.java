package com.mesapartes.sgd.repository;

import com.mesapartes.sgd.entity.Documento;
import com.mesapartes.sgd.entity.EstadoDocumento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DocumentoRepository extends JpaRepository<Documento, UUID>,
        JpaSpecificationExecutor<Documento> {

    Optional<Documento> findByNumeroTramite(String numeroTramite);

    List<Documento> findByEstado(EstadoDocumento estado);

    List<Documento> findByRemitente(String remitente);
}