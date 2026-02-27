package com.mesapartes.sgd.controller;

import com.mesapartes.sgd.dto.*;
import com.mesapartes.sgd.service.RegistroCiudadanoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class RegistroCiudadanoController {

    private final RegistroCiudadanoService registroService;

    // ===== REGISTRAR PERSONA NATURAL =====
    // POST /api/auth/registro/natural
    @PostMapping("/registro/natural")
    public ResponseEntity<RegistroResponseDTO> registrarNatural(
            @RequestBody @Valid RegistroNaturalRequestDTO request
    ) {
        return ResponseEntity.ok(registroService.registrarNatural(request));
    }

    // ===== REGISTRAR PERSONA JURÍDICA =====
    // POST /api/auth/registro/juridica
    @PostMapping("/registro/juridica")
    public ResponseEntity<RegistroResponseDTO> registrarJuridica(
            @RequestBody @Valid RegistroJuridicaRequestDTO request
    ) {
        return ResponseEntity.ok(registroService.registrarJuridica(request));
    }

    // ===== VERIFICAR CÓDIGO =====
    // POST /api/auth/verificar
    @PostMapping("/verificar")
    public ResponseEntity<Void> verificarCodigo(
            @RequestBody @Valid VerificacionCodigoDTO request
    ) {
        registroService.verificarCodigo(request);
        return ResponseEntity.ok().build();
    }

    // ===== REENVIAR CÓDIGO =====
    // POST /api/auth/reenviar-codigo?tipoPersna=NATURAL&identificador=12345678
    @PostMapping("/reenviar-codigo")
    public ResponseEntity<Void> reenviarCodigo(
            @RequestParam String tipoPersna,
            @RequestParam String identificador
    ) {
        registroService.reenviarCodigo(tipoPersna, identificador);
        return ResponseEntity.ok().build();
    }

    // ===== LOGIN CIUDADANO =====
    // POST /api/auth/login/ciudadano
    @PostMapping("/login/ciudadano")
    public ResponseEntity<LoginResponseDTO> loginCiudadano(
            @RequestBody @Valid LoginCiudadanoRequestDTO request
    ) {
        return ResponseEntity.ok(registroService.loginCiudadano(request));
    }
}