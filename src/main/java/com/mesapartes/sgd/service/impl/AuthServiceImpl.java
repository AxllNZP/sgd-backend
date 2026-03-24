package com.mesapartes.sgd.service.impl;

import com.mesapartes.sgd.dto.LoginRequestDTO;
import com.mesapartes.sgd.dto.LoginResponseDTO;
import com.mesapartes.sgd.entity.Usuario;
import com.mesapartes.sgd.exception.BusinessException;
import com.mesapartes.sgd.exception.UnauthorizedException;
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

        Usuario usuario = usuarioRepository.findByEmail(request.getEmail())
                .orElseThrow(() ->
                        new UnauthorizedException("Credenciales incorrectas"));

        if (!passwordEncoder.matches(request.getPassword(), usuario.getPassword())) {
            throw new UnauthorizedException("Credenciales incorrectas");
        }

        if (!usuario.isActivo()) {
            throw new BusinessException(
                    "Usuario desactivado, contacte al administrador");
        }

        String token = jwtService.generarToken(
                usuario.getEmail(),
                usuario.getRol().name()
        );

        return new LoginResponseDTO(
                token,
                usuario.getEmail(),
                usuario.getRol().name(),
                usuario.getNombre()
        );
    }
}