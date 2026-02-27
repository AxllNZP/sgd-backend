package com.mesapartes.sgd.controller;

import com.mesapartes.sgd.dto.NuevaPasswordDTO;
import com.mesapartes.sgd.dto.PreguntaSeguridadResponseDTO;
import com.mesapartes.sgd.dto.RecuperacionSolicitarDTO;
import com.mesapartes.sgd.dto.RecuperacionVerificarCodigoDTO;
import com.mesapartes.sgd.dto.VerificarPreguntaDTO;
import com.mesapartes.sgd.service.RecuperacionPasswordService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth/recuperar")
@RequiredArgsConstructor
public class RecuperacionPasswordController {

    private final RecuperacionPasswordService recuperacionService;

    // ===== PASO 1: SOLICITAR RECUPERACIÓN =====
    // POST /api/auth/recuperar/solicitar
    @PostMapping("/solicitar")
    public ResponseEntity<Void> solicitarRecuperacion(
            @RequestBody @Valid RecuperacionSolicitarDTO request
    ) {
        recuperacionService.solicitarRecuperacion(request);
        return ResponseEntity.ok().build();
    }

    // ===== PASO 2: VERIFICAR CÓDIGO =====
    // POST /api/auth/recuperar/verificar-codigo
    @PostMapping("/verificar-codigo")
    public ResponseEntity<Void> verificarCodigo(
            @RequestBody @Valid RecuperacionVerificarCodigoDTO request
    ) {
        recuperacionService.verificarCodigoRecuperacion(request);
        return ResponseEntity.ok().build();
    }

    // ===== PASO 3: NUEVA CONTRASEÑA =====
    // POST /api/auth/recuperar/nueva-password
    @PostMapping("/nueva-password")
    public ResponseEntity<Void> nuevaPassword(
            @RequestBody @Valid NuevaPasswordDTO request
    ) {
        recuperacionService.establecerNuevaPassword(request);
        return ResponseEntity.ok().build();
    }

    // ===== VÍA B - PASO 1: OBTENER PREGUNTA SECRETA =====
    // GET /api/auth/recuperar/pregunta?tipoPersna=NATURAL&identificador=12345678
    @GetMapping("/pregunta")
    public ResponseEntity<PreguntaSeguridadResponseDTO> obtenerPregunta(
            @RequestParam String tipoPersna,
            @RequestParam String identificador
    ) {
        return ResponseEntity.ok(
                recuperacionService.obtenerPreguntaSeguridad(tipoPersna, identificador)
        );
    }

    // ===== VÍA B - PASO 2: VERIFICAR RESPUESTA SECRETA =====
    // POST /api/auth/recuperar/verificar-pregunta
    @PostMapping("/verificar-pregunta")
    public ResponseEntity<Void> verificarPregunta(
            @RequestBody @Valid VerificarPreguntaDTO request
    ) {
        recuperacionService.verificarRespuestaSecreta(request);
        return ResponseEntity.ok().build();
    }
}