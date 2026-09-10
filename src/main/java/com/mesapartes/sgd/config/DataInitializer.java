package com.mesapartes.sgd.config;

import com.mesapartes.sgd.entity.RolUsuario;
import com.mesapartes.sgd.entity.Usuario;
import com.mesapartes.sgd.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${admin.default.email:}")
    private String adminEmail;

    @Value("${admin.default.password:}")
    private String adminPassword;

    @Value("${admin.default.nombre:Administrador}")
    private String adminNombre;

    @Override
    public void run(String... args) {

        if (usuarioRepository.existsByRol(RolUsuario.ADMINISTRADOR)) {
            log.info("Ya existe un usuario ADMINISTRADOR. No se crea otro.");
            return;
        }

        if (adminEmail.isBlank() || adminPassword.isBlank()) {
            log.warn(
                    "No existe ningún ADMINISTRADOR y no se definieron " +
                            "ADMIN_DEFAULT_EMAIL / ADMIN_DEFAULT_PASSWORD. " +
                            "Omitiendo bootstrap."
            );
            return;
        }

        Usuario admin = new Usuario();
        admin.setNombre(adminNombre);
        admin.setEmail(adminEmail);
        admin.setPassword(passwordEncoder.encode(adminPassword));
        admin.setRol(RolUsuario.ADMINISTRADOR);

        usuarioRepository.save(admin);

        log.info("Admin inicial creado: {}", adminEmail);
    }
}