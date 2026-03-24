package com.mesapartes.sgd.repository;

import com.mesapartes.sgd.entity.RespuestaDocumento;
import com.mesapartes.sgd.entity.Documento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

@Repository
public interface RespuestaDocumentoRepository extends JpaRepository<RespuestaDocumento, UUID> {

    List<RespuestaDocumento> findByDocumentoOrderByFechaRespuestaAsc(Documento documento);
}