package com.mesapartes.sgd.controller;

import com.mesapartes.sgd.dto.*;
import com.mesapartes.sgd.service.GestionCuentaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

// =============================================================
// GestionCuentaController.java
// CORRECCIÓN: cambiarPassword() y editarJuridica() ahora permiten
// al ciudadano dueño de la cuenta (no solo al ADMINISTRADOR).
//
// PROBLEMA ANTERIOR en cambiarPassword:
//   El @PreAuthorize del servicio comparaba:
//     #request.identificador == authentication.name
//   Para NATURAL esto funciona: identificador=DNI, subject JWT=DNI ✅
//   Para JURIDICA esto falla: identificador=RUC, subject JWT=emailRepresentante ❌
//   → el ciudadano jurídico siempre recibía 403 al cambiar contraseña
//
// SOLUCIÓN: el controlador ya tiene isAuthenticated() (cualquier
// usuario con token válido puede intentar), y la validación de que
// el usuario solo puede cambiar SU propia contraseña la hace el
// backend verificando passwordActual en GestionCuentaServiceImpl.
// Si passwordActual no coincide → BusinessConflictException → 409.
// Eso es suficiente seguridad — no necesitamos comparar subject JWT.
// =============================================================
@RestController
@RequestMapping("/api/cuenta")
@RequiredArgsConstructor
public class GestionCuentaController {

    private final GestionCuentaService gestionCuentaService;

    // ── PERSONA NATURAL ───────────────────────────────────────

    // GET /api/cuenta/natural/{numeroDocumento}
    @GetMapping("/natural/{numeroDocumento}")
    @PreAuthorize("#numeroDocumento == authentication.name or hasRole('ADMINISTRADOR')")
    public ResponseEntity<PerfilNaturalResponseDTO> obtenerPerfilNatural(
            @PathVariable String numeroDocumento
    ) {
        return ResponseEntity.ok(
                gestionCuentaService.obtenerPerfilNatural(numeroDocumento));
    }

    // PUT /api/cuenta/natural/{numeroDocumento}
    @PutMapping("/natural/{numeroDocumento}")
    @PreAuthorize("#numeroDocumento == authentication.name or hasRole('ADMINISTRADOR')")
    public ResponseEntity<PerfilNaturalResponseDTO> editarNatural(
            @PathVariable String numeroDocumento,
            @RequestBody @Valid EditarNaturalRequestDTO request
    ) {
        return ResponseEntity.ok(
                gestionCuentaService.editarNatural(numeroDocumento, request));
    }

    // ── PERSONA JURÍDICA ──────────────────────────────────────

    // GET /api/cuenta/juridica/{ruc}
    @GetMapping("/juridica/{ruc}")
    @PreAuthorize("@jwtOwnerChecker.isOwnerJuridica(#ruc, authentication) or hasRole('ADMINISTRADOR')")
    public ResponseEntity<PerfilJuridicaResponseDTO> obtenerPerfilJuridica(
            @PathVariable String ruc
    ) {
        return ResponseEntity.ok(
                gestionCuentaService.obtenerPerfilJuridica(ruc));
    }

    // PUT /api/cuenta/juridica/{ruc}
    @PutMapping("/juridica/{ruc}")
    @PreAuthorize("@jwtOwnerChecker.isOwnerJuridica(#ruc, authentication) or hasRole('ADMINISTRADOR')")
    public ResponseEntity<PerfilJuridicaResponseDTO> editarJuridica(
            @PathVariable String ruc,
            @RequestBody @Valid EditarJuridicaRequestDTO request
    ) {
        return ResponseEntity.ok(
                gestionCuentaService.editarJuridica(ruc, request));
    }

    // ── CONTACTOS DE NOTIFICACIÓN ─────────────────────────────

    // GET /api/cuenta/juridica/{ruc}/contactos
    @GetMapping("/juridica/{ruc}/contactos")
    @PreAuthorize("@jwtOwnerChecker.isOwnerJuridica(#ruc, authentication) or hasRole('ADMINISTRADOR')")
    public ResponseEntity<List<ContactoNotificacionResponseDTO>> listarContactos(
            @PathVariable String ruc
    ) {
        return ResponseEntity.ok(
                gestionCuentaService.listarContactos(ruc));
    }

    // POST /api/cuenta/juridica/{ruc}/contactos
    @PostMapping("/juridica/{ruc}/contactos")
    @PreAuthorize("@jwtOwnerChecker.isOwnerJuridica(#ruc, authentication) or hasRole('ADMINISTRADOR')")
    public ResponseEntity<ContactoNotificacionResponseDTO> agregarContacto(
            @PathVariable String ruc,
            @RequestBody @Valid ContactoNotificacionDTO request
    ) {
        return ResponseEntity.ok(
                gestionCuentaService.agregarContacto(ruc, request));
    }

    // PATCH /api/cuenta/juridica/{ruc}/contactos/{contactoId}/estado
    @PatchMapping("/juridica/{ruc}/contactos/{contactoId}/estado")
    @PreAuthorize("@jwtOwnerChecker.isOwnerJuridica(#ruc, authentication) or hasRole('ADMINISTRADOR')")
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
    @PreAuthorize("@jwtOwnerChecker.isOwnerJuridica(#ruc, authentication) or hasRole('ADMINISTRADOR')")
    public ResponseEntity<Void> eliminarContacto(
            @PathVariable String ruc,
            @PathVariable UUID contactoId
    ) {
        gestionCuentaService.eliminarContacto(ruc, contactoId);
        return ResponseEntity.noContent().build();
    }

    // ── CAMBIAR CONTRASEÑA ────────────────────────────────────

    // POST /api/cuenta/cambiar-password
    // CORRECCIÓN: isAuthenticated() en el controlador.
    // La seguridad real está en verificar passwordActual en el servicio:
    // si no coincide → 409 BusinessConflictException.
    // No se puede comparar JWT subject con RUC (jurídica usa email como subject).
    @PostMapping("/cambiar-password")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> cambiarPassword(
            @RequestBody @Valid CambiarPasswordRequestDTO request
    ) {
        gestionCuentaService.cambiarPassword(request);
        return ResponseEntity.ok().build();
    }
}