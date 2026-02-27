package com.mesapartes.sgd.controller;

import com.mesapartes.sgd.dto.*;
import com.mesapartes.sgd.service.GestionCuentaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/cuenta")
@RequiredArgsConstructor
public class GestionCuentaController {

    private final GestionCuentaService gestionCuentaService;

    // ===== PERSONA NATURAL =====

    // GET /api/cuenta/natural/{numeroDocumento}
    @GetMapping("/natural/{numeroDocumento}")
    public ResponseEntity<PerfilNaturalResponseDTO> obtenerPerfilNatural(
            @PathVariable String numeroDocumento
    ) {
        return ResponseEntity.ok(
                gestionCuentaService.obtenerPerfilNatural(numeroDocumento));
    }

    // PUT /api/cuenta/natural/{numeroDocumento}
    @PutMapping("/natural/{numeroDocumento}")
    public ResponseEntity<PerfilNaturalResponseDTO> editarNatural(
            @PathVariable String numeroDocumento,
            @RequestBody @Valid EditarNaturalRequestDTO request
    ) {
        return ResponseEntity.ok(
                gestionCuentaService.editarNatural(numeroDocumento, request));
    }

    // ===== PERSONA JURÍDICA =====

    // GET /api/cuenta/juridica/{ruc}
    @GetMapping("/juridica/{ruc}")
    public ResponseEntity<PerfilJuridicaResponseDTO> obtenerPerfilJuridica(
            @PathVariable String ruc
    ) {
        return ResponseEntity.ok(
                gestionCuentaService.obtenerPerfilJuridica(ruc));
    }

    // PUT /api/cuenta/juridica/{ruc}
    @PutMapping("/juridica/{ruc}")
    public ResponseEntity<PerfilJuridicaResponseDTO> editarJuridica(
            @PathVariable String ruc,
            @RequestBody @Valid EditarJuridicaRequestDTO request
    ) {
        return ResponseEntity.ok(
                gestionCuentaService.editarJuridica(ruc, request));
    }

    // ===== CONTACTOS DE NOTIFICACIÓN =====

    // GET /api/cuenta/juridica/{ruc}/contactos
    @GetMapping("/juridica/{ruc}/contactos")
    public ResponseEntity<List<ContactoNotificacionResponseDTO>> listarContactos(
            @PathVariable String ruc
    ) {
        return ResponseEntity.ok(
                gestionCuentaService.listarContactos(ruc));
    }

    // POST /api/cuenta/juridica/{ruc}/contactos
    @PostMapping("/juridica/{ruc}/contactos")
    public ResponseEntity<ContactoNotificacionResponseDTO> agregarContacto(
            @PathVariable String ruc,
            @RequestBody @Valid ContactoNotificacionDTO request
    ) {
        return ResponseEntity.ok(
                gestionCuentaService.agregarContacto(ruc, request));
    }

    // PATCH /api/cuenta/juridica/{ruc}/contactos/{contactoId}/estado
    @PatchMapping("/juridica/{ruc}/contactos/{contactoId}/estado")
    public ResponseEntity<ContactoNotificacionResponseDTO> toggleEstado(
            @PathVariable String ruc,
            @PathVariable UUID contactoId,
            @RequestBody @Valid ToggleContactoDTO request
    ) {
        return ResponseEntity.ok(
                gestionCuentaService.toggleEstadoContacto(ruc, contactoId, request));
    }

    // DELETE /api/cuenta/juridica/{ruc}/contactos/{contactoId}
    @DeleteMapping("/juridica/{ruc}/contactos/{contactoId}")
    public ResponseEntity<Void> eliminarContacto(
            @PathVariable String ruc,
            @PathVariable UUID contactoId
    ) {
        gestionCuentaService.eliminarContacto(ruc, contactoId);
        return ResponseEntity.noContent().build();
    }

    // ===== CAMBIAR CONTRASEÑA =====

    // POST /api/cuenta/cambiar-password
    @PostMapping("/cambiar-password")
    public ResponseEntity<Void> cambiarPassword(
            @RequestBody @Valid CambiarPasswordRequestDTO request
    ) {
        gestionCuentaService.cambiarPassword(request);
        return ResponseEntity.ok().build();
    }
}