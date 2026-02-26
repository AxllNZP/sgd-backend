package com.mesapartes.sgd.repository;

import com.mesapartes.sgd.entity.Derivacion;
import com.mesapartes.sgd.entity.Documento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

@Repository
public interface DerivacionRepository extends JpaRepository<Derivacion, UUID> {

    List<Derivacion> findByDocumentoOrderByFechaDerivacionAsc(Documento documento);
}