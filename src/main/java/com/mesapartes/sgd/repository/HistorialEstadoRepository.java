package com.mesapartes.sgd.repository;

import com.mesapartes.sgd.entity.HistorialEstado;
import com.mesapartes.sgd.entity.Documento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

@Repository
public interface HistorialEstadoRepository extends JpaRepository<HistorialEstado, UUID> {

    List<HistorialEstado> findByDocumentoOrderByFechaCambioAsc(Documento documento);
}