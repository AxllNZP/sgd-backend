package com.mesapartes.sgd.service;

import com.mesapartes.sgd.dto.UsuarioRequestDTO;
import com.mesapartes.sgd.dto.UsuarioResponseDTO;
import java.util.List;
import java.util.UUID;

public interface UsuarioService {

    UsuarioResponseDTO crearUsuario(UsuarioRequestDTO request);

    UsuarioResponseDTO obtenerPorId(UUID id);

    UsuarioResponseDTO obtenerPorEmail(String email);

    List<UsuarioResponseDTO> listarTodos();

    void desactivarUsuario(UUID id);
}