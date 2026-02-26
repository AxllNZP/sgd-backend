package com.mesapartes.sgd.service.impl;

import com.mesapartes.sgd.dto.LoginRequestDTO;
import com.mesapartes.sgd.dto.LoginResponseDTO;
import com.mesapartes.sgd.entity.Usuario;
import com.mesapartes.sgd.repository.UsuarioRepository;
import com.mesapartes.sgd.service.AuthService;
import com.mesapartes.sgd.service.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Override
    public LoginResponseDTO login(LoginRequestDTO request) {
        // Buscar usuario por email
        Usuario usuario = usuarioRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Credenciales incorrectas"));

        // Verificar password
        if (!passwordEncoder.matches(request.getPassword(), usuario.getPassword())) {
            throw new RuntimeException("Credenciales incorrectas");
        }

        // Verificar que esté activo
        if (!usuario.isActivo()) {
            throw new RuntimeException("Usuario desactivado, contacte al administrador");
        }

        // Generar token
        String token = jwtService.generarToken(usuario.getEmail(), usuario.getRol().name());

        return new LoginResponseDTO(
                token,
                usuario.getEmail(),
                usuario.getRol().name(),
                usuario.getNombre()
        );
    }
}