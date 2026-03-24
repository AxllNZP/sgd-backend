package com.mesapartes.sgd.service;

import com.mesapartes.sgd.dto.UsuarioRequestDTO;
import com.mesapartes.sgd.dto.UsuarioResponseDTO;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;

public interface UsuarioService {

    @PreAuthorize("hasRole('ADMINISTRADOR')")
    UsuarioResponseDTO crearUsuario(UsuarioRequestDTO request);

    @PreAuthorize("hasRole('ADMINISTRADOR')")
    UsuarioResponseDTO obtenerPorId(UUID id);

    @PreAuthorize("hasRole('ADMINISTRADOR')")
    UsuarioResponseDTO obtenerPorEmail(String email);

    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Deprecated
    List<UsuarioResponseDTO> listarTodos();

    @PreAuthorize("hasRole('ADMINISTRADOR')")
    Page<UsuarioResponseDTO> listarTodos(Pageable pageable);

    @PreAuthorize("hasRole('ADMINISTRADOR')")
    void desactivarUsuario(UUID id);
}