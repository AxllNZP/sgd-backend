package com.mesapartes.sgd.controller;

import com.mesapartes.sgd.dto.UsuarioRequestDTO;
import com.mesapartes.sgd.dto.UsuarioResponseDTO;
import com.mesapartes.sgd.service.UsuarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/usuarios")
@RequiredArgsConstructor
public class UsuarioController {

    private final UsuarioService usuarioService;

    // ===== CREAR USUARIO =====
    @PostMapping
    public ResponseEntity<UsuarioResponseDTO> crearUsuario(
            @RequestBody @Valid UsuarioRequestDTO request
    ) {
        return ResponseEntity.ok(usuarioService.crearUsuario(request));
    }

    // ===== OBTENER POR ID =====
    @GetMapping("/{id}")
    public ResponseEntity<UsuarioResponseDTO> obtenerPorId(
            @PathVariable UUID id
    ) {
        return ResponseEntity.ok(usuarioService.obtenerPorId(id));
    }

    // ===== OBTENER POR EMAIL =====
    @GetMapping("/email/{email}")
    public ResponseEntity<UsuarioResponseDTO> obtenerPorEmail(
            @PathVariable String email
    ) {
        return ResponseEntity.ok(usuarioService.obtenerPorEmail(email));
    }

    // ===== LISTAR TODOS =====
    @GetMapping
    public ResponseEntity<List<UsuarioResponseDTO>> listarTodos() {
        return ResponseEntity.ok(usuarioService.listarTodos());
    }

    // ===== DESACTIVAR USUARIO =====
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> desactivarUsuario(
            @PathVariable UUID id
    ) {
        usuarioService.desactivarUsuario(id);
        return ResponseEntity.noContent().build();
    }
}