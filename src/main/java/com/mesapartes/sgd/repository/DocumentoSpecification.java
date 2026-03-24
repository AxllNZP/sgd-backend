package com.mesapartes.sgd.repository;

import com.mesapartes.sgd.dto.DocumentoFiltroDTO;
import com.mesapartes.sgd.entity.Documento;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class DocumentoSpecification {

    public static Specification<Documento> conFiltros(DocumentoFiltroDTO filtro) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (filtro.getRemitente() != null && !filtro.getRemitente().isEmpty()) {
                predicates.add(cb.like(
                        cb.lower(root.get("remitente")),
                        "%" + filtro.getRemitente().toLowerCase() + "%"
                ));
            }

            if (filtro.getAsunto() != null && !filtro.getAsunto().isEmpty()) {
                predicates.add(cb.like(
                        cb.lower(root.get("asunto")),
                        "%" + filtro.getAsunto().toLowerCase() + "%"
                ));
            }

            if (filtro.getEstado() != null) {
                predicates.add(cb.equal(root.get("estado"), filtro.getEstado()));
            }

            if (filtro.getFechaDesde() != null) {
                predicates.add(cb.greaterThanOrEqualTo(
                        root.get("fechaHoraRegistro"), filtro.getFechaDesde()
                ));
            }

            if (filtro.getFechaHasta() != null) {
                predicates.add(cb.lessThanOrEqualTo(
                        root.get("fechaHoraRegistro"), filtro.getFechaHasta()
                ));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}