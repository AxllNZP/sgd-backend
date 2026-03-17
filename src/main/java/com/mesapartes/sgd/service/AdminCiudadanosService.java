package com.mesapartes.sgd.service;

import com.mesapartes.sgd.dto.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AdminCiudadanosService {

    Page<CiudadanoNaturalResumenDTO> listarNaturales(Pageable pageable);
    Page<CiudadanoJuridicaResumenDTO> listarJuridicas(Pageable pageable);

    Page<CiudadanoNaturalResumenDTO> buscarNaturales(CiudadanoFiltroDTO filtro, Pageable pageable);
    Page<CiudadanoJuridicaResumenDTO> buscarJuridicas(CiudadanoFiltroDTO filtro, Pageable pageable);

    EstadisticasCiudadanosDTO obtenerEstadisticas();

    CiudadanoNaturalResumenDTO toggleEstadoNatural(String numeroDocumento, boolean activo);
    CiudadanoJuridicaResumenDTO toggleEstadoJuridica(String ruc, boolean activo);

    void eliminarNatural(String numeroDocumento);
    void eliminarJuridica(String ruc);
}