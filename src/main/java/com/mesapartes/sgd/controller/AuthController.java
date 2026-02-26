package com.mesapartes.sgd.controller;

import com.mesapartes.sgd.dto.LoginRequestDTO;
import com.mesapartes.sgd.dto.LoginResponseDTO;
import com.mesapartes.sgd.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    // ===== LOGIN =====
    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(
            @RequestBody @Valid LoginRequestDTO request
    ) {
        return ResponseEntity.ok(authService.login(request));
    }
}