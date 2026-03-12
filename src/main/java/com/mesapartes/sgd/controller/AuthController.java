package com.mesapartes.sgd.controller;

import com.mesapartes.sgd.dto.LoginRequestDTO;
import com.mesapartes.sgd.dto.LoginResponseDTO;
import com.mesapartes.sgd.service.AuthService;
import com.mesapartes.sgd.security.RateLimitService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final RateLimitService rateLimitService;

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(
            HttpServletRequest httpRequest,
            @RequestBody @Valid LoginRequestDTO request
    ) {

        String ip = httpRequest.getRemoteAddr();

        if (!rateLimitService.tryConsume(ip)) {
            return ResponseEntity
                    .status(HttpStatus.TOO_MANY_REQUESTS)
                    .header("Retry-After", "900")
                    .build();
        }

        return ResponseEntity.ok(authService.login(request));
    }
}