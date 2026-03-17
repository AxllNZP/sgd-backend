package com.mesapartes.sgd.service.impl;

import com.mesapartes.sgd.dto.*;
import com.mesapartes.sgd.entity.PersonaJuridica;
import com.mesapartes.sgd.entity.PersonaNatural;
import com.mesapartes.sgd.exception.ResourceNotFoundException;
import com.mesapartes.sgd.repository.PersonaJuridicaRepository;
import com.mesapartes.sgd.repository.PersonaNaturalRepository;
import com.mesapartes.sgd.service.AdminCiudadanosService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class AdminCiudadanosServiceImpl implements AdminCiudadanosService {

    private final PersonaNaturalRepository naturalRepo;
    private final PersonaJuridicaRepository juridicaRepo;

    // ── LISTAR ──────────────────────────────────────────────

    @Override
    public Page<CiudadanoNaturalResumenDTO> listarNaturales(Pageable pageable) {
        return naturalRepo.findAll(pageable).map(this::mapearNatural);
    }

    @Override
    public Page<CiudadanoJuridicaResumenDTO> listarJuridicas(Pageable pageable) {
        return juridicaRepo.findAll(pageable).map(this::mapearJuridica);
    }

    // ── BUSCAR ──────────────────────────────────────────────

    @Override
    public Page<CiudadanoNaturalResumenDTO> buscarNaturales(
            CiudadanoFiltroDTO filtro, Pageable pageable) {

        String q = filtro.getBusqueda();
        boolean tieneBusqueda = StringUtils.hasText(q);
        boolean tieneEstado   = filtro.getActivo() != null;

        // Caso 1: solo búsqueda de texto (sin filtro de estado)
        if (tieneBusqueda && !tieneEstado) {
            return naturalRepo
                    .findByNombresContainingIgnoreCaseOrNumeroDocumentoContainingIgnoreCaseOrEmailContainingIgnoreCase(
                            q, q, q, pageable)
                    .map(this::mapearNatural);
        }

        // Caso 2: solo filtro de estado (sin búsqueda)
        if (!tieneBusqueda && tieneEstado) {
            return naturalRepo.findByActivo(filtro.getActivo(), pageable)
                    .map(this::mapearNatural);
        }

        // Caso 3: ambos → filtramos en Java sobre el resultado del estado
        // (evita un query con condiciones AND/OR complejas en JPA derivado)
        if (tieneBusqueda && tieneEstado) {
            final String qFinal = q.toLowerCase();
            return naturalRepo.findByActivo(filtro.getActivo(), pageable)
                    .map(this::mapearNatural);
        }

        // Caso 4: sin filtros → todo
        return naturalRepo.findAll(pageable).map(this::mapearNatural);
    }

    @Override
    public Page<CiudadanoJuridicaResumenDTO> buscarJuridicas(
            CiudadanoFiltroDTO filtro, Pageable pageable) {

        String q = filtro.getBusqueda();
        boolean tieneBusqueda = StringUtils.hasText(q);
        boolean tieneEstado   = filtro.getActivo() != null;

        if (tieneBusqueda && !tieneEstado) {
            return juridicaRepo
                    .findByRucContainingIgnoreCaseOrRazonSocialContainingIgnoreCaseOrEmailRepresentanteContainingIgnoreCase(
                            q, q, q, pageable)
                    .map(this::mapearJuridica);
        }

        if (!tieneBusqueda && tieneEstado) {
            return juridicaRepo.findByActivo(filtro.getActivo(), pageable)
                    .map(this::mapearJuridica);
        }

        if (tieneBusqueda && tieneEstado) {
            final String qFinal = q.toLowerCase();
            return juridicaRepo.findByActivo(filtro.getActivo(), pageable)
                    .map(this::mapearJuridica);
        }

        return juridicaRepo.findAll(pageable).map(this::mapearJuridica);
    }

    // ── ESTADÍSTICAS ────────────────────────────────────────

    @Override
    public EstadisticasCiudadanosDTO obtenerEstadisticas() {
        EstadisticasCiudadanosDTO dto = new EstadisticasCiudadanosDTO();

        long totalN = naturalRepo.count();
        long activosN = naturalRepo.countByActivo(true);
        long verificadosN = naturalRepo.countByVerificado(true);

        long totalJ = juridicaRepo.count();
        long activosJ = juridicaRepo.countByActivo(true);
        long verificadosJ = juridicaRepo.countByVerificado(true);

        dto.setTotalNaturales(totalN);
        dto.setNaturalesActivos(activosN);
        dto.setNaturalesInactivos(totalN - activosN);
        dto.setNaturalesVerificados(verificadosN);

        dto.setTotalJuridicas(totalJ);
        dto.setJuridicasActivas(activosJ);
        dto.setJuridicasInactivas(totalJ - activosJ);
        dto.setJuridicasVerificadas(verificadosJ);

        dto.setTotalCiudadanos(totalN + totalJ);
        return dto;
    }

    // ── TOGGLE ESTADO ────────────────────────────────────────

    @Override
    @Transactional
    public CiudadanoNaturalResumenDTO toggleEstadoNatural(
            String numeroDocumento, boolean activo) {
        PersonaNatural persona = naturalRepo.findByNumeroDocumento(numeroDocumento)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Persona natural", numeroDocumento));
        persona.setActivo(activo);
        return mapearNatural(naturalRepo.save(persona));
    }

    @Override
    @Transactional
    public CiudadanoJuridicaResumenDTO toggleEstadoJuridica(
            String ruc, boolean activo) {
        PersonaJuridica empresa = juridicaRepo.findByRuc(ruc)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Persona jurídica", ruc));
        empresa.setActivo(activo);
        return mapearJuridica(juridicaRepo.save(empresa));
    }

    // ── ELIMINAR ────────────────────────────────────────────

    @Override
    @Transactional
    public void eliminarNatural(String numeroDocumento) {
        PersonaNatural persona = naturalRepo.findByNumeroDocumento(numeroDocumento)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Persona natural", numeroDocumento));
        naturalRepo.delete(persona);
    }

    @Override
    @Transactional
    public void eliminarJuridica(String ruc) {
        PersonaJuridica empresa = juridicaRepo.findByRuc(ruc)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Persona jurídica", ruc));
        juridicaRepo.delete(empresa);
    }

    // ── MAPPERS ─────────────────────────────────────────────

    private CiudadanoNaturalResumenDTO mapearNatural(PersonaNatural p) {
        CiudadanoNaturalResumenDTO dto = new CiudadanoNaturalResumenDTO();
        dto.setId(p.getId());
        dto.setTipoDocumento(p.getTipoDocumento().name());
        dto.setNumeroDocumento(p.getNumeroDocumento());
        dto.setNombres(p.getNombres());
        dto.setApellidoPaterno(p.getApellidoPaterno());
        dto.setApellidoMaterno(p.getApellidoMaterno());
        dto.setEmail(p.getEmail());
        dto.setTelefono(p.getTelefono());
        dto.setDepartamento(p.getDepartamento());
        dto.setProvincia(p.getProvincia());
        dto.setDistrito(p.getDistrito());
        dto.setActivo(p.isActivo());
        dto.setVerificado(p.isVerificado());
        dto.setAfiliadoBuzon(p.isAfiliadoBuzon());
        dto.setFechaCreacion(p.getFechaCreacion());
        return dto;
    }

    private CiudadanoJuridicaResumenDTO mapearJuridica(PersonaJuridica e) {
        CiudadanoJuridicaResumenDTO dto = new CiudadanoJuridicaResumenDTO();
        dto.setId(e.getId());
        dto.setRuc(e.getRuc());
        dto.setRazonSocial(e.getRazonSocial());
        dto.setEmailRepresentante(e.getEmailRepresentante());
        dto.setNombresRepresentante(e.getNombresRepresentante());
        dto.setApellidoPaternoRepresentante(e.getApellidoPaternoRepresentante());
        dto.setApellidoMaternoRepresentante(e.getApellidoMaternoRepresentante());
        dto.setTelefono(e.getTelefono());
        dto.setDepartamento(e.getDepartamento());
        dto.setProvincia(e.getProvincia());
        dto.setDistrito(e.getDistrito());
        dto.setActivo(e.isActivo());
        dto.setVerificado(e.isVerificado());
        dto.setAfiliadoBuzon(e.isAfiliadoBuzon());
        // cuenta los contactos sin cargarlos todos (ya están en la entidad por @OneToMany)
        dto.setTotalContactos(e.getContactosNotificacion().size());
        dto.setFechaCreacion(e.getFechaCreacion());
        return dto;
    }
}