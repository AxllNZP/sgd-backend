package com.mesapartes.sgd.service.impl;

import com.mesapartes.sgd.dto.UsuarioRequestDTO;
import com.mesapartes.sgd.dto.UsuarioResponseDTO;
import com.mesapartes.sgd.entity.Usuario;
import com.mesapartes.sgd.exception.BusinessConflictException;
import com.mesapartes.sgd.exception.ResourceNotFoundException;
import com.mesapartes.sgd.repository.UsuarioRepository;
import com.mesapartes.sgd.service.UsuarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Service
@RequiredArgsConstructor
public class UsuarioServiceImpl implements UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Crea un nuevo usuario en el sistema.
     */
    @Override
    public UsuarioResponseDTO crearUsuario(UsuarioRequestDTO request) {

        validarEmailUnico(request.getEmail());

        Usuario usuario = construirUsuario(request);

        Usuario guardado = usuarioRepository.save(usuario);

        return mapearRespuesta(guardado);
    }

    /**
     * Obtiene un usuario por ID.
     */
    @Override
    public UsuarioResponseDTO obtenerPorId(UUID id) {
        Usuario usuario = obtenerUsuarioPorId(id);
        return mapearRespuesta(usuario);
    }

    /**
     * Obtiene un usuario por email.
     */
    @Override
    public UsuarioResponseDTO obtenerPorEmail(String email) {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException("Usuario no encontrado con email: " + email));
        return mapearRespuesta(usuario);
    }

    /**
     * Lista todos los usuarios del sistema.
     */
    @Override
    public List<UsuarioResponseDTO> listarTodos() {
        return usuarioRepository.findAll()
                .stream()
                .map(this::mapearRespuesta)
                .collect(Collectors.toList());
    }

    @Override
    public Page<UsuarioResponseDTO> listarTodos(Pageable pageable) {
        return usuarioRepository.findAll(pageable)
                .map(this::mapearRespuesta);
    }


    /**
     * Desactiva un usuario (soft delete).
     */
    @Override
    public void desactivarUsuario(UUID id) {

        Usuario usuario = obtenerUsuarioPorId(id);

        usuario.setActivo(false);

        usuarioRepository.save(usuario);
    }

    /**
     * Busca un usuario por ID.
     */
    private Usuario obtenerUsuarioPorId(UUID id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Usuario no encontrado con id: " + id));
    }

    /**
     * Valida que el email no exista.
     */
    private void validarEmailUnico(String email) {
        if (usuarioRepository.existsByEmail(email)) {
            throw new BusinessConflictException("Ya existe un usuario con el email: " + email);
        }
    }

    /**
     * Construye la entidad Usuario.
     */
    private Usuario construirUsuario(UsuarioRequestDTO request) {

        Usuario usuario = new Usuario();

        usuario.setNombre(request.getNombre());
        usuario.setEmail(request.getEmail());
        usuario.setPassword(passwordEncoder.encode(request.getPassword()));
        usuario.setRol(request.getRol());

        return usuario;
    }

    /**
     * Convierte entidad Usuario a DTO.
     */
    private UsuarioResponseDTO mapearRespuesta(Usuario usuario) {

        UsuarioResponseDTO response = new UsuarioResponseDTO();

        response.setId(usuario.getId());
        response.setNombre(usuario.getNombre());
        response.setEmail(usuario.getEmail());
        response.setRol(usuario.getRol());
        response.setActivo(usuario.isActivo());
        response.setFechaCreacion(usuario.getFechaCreacion());

        return response;
    }
}