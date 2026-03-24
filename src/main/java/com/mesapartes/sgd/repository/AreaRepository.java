package com.mesapartes.sgd.repository;

import com.mesapartes.sgd.entity.Area;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AreaRepository extends JpaRepository<Area, UUID> {

    Optional<Area> findByNombre(String nombre);

    boolean existsByNombre(String nombre);
}