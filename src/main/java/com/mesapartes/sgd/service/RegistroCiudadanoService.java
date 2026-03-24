package com.mesapartes.sgd.service;

import com.mesapartes.sgd.dto.*;

public interface RegistroCiudadanoService {

    // Registra persona natural y envía código de verificación
    RegistroResponseDTO registrarNatural(RegistroNaturalRequestDTO request);

    // Registra persona jurídica y envía código de verificación
    RegistroResponseDTO registrarJuridica(RegistroJuridicaRequestDTO request);

    // Verifica el código recibido por email y activa la cuenta
    void verificarCodigo(VerificacionCodigoDTO request);

    // Reenvía el código de verificación
    void reenviarCodigo(String tipoPersna, String identificador);

    // Login para ciudadanos (natural o jurídica)
    LoginResponseDTO loginCiudadano(LoginCiudadanoRequestDTO request);
}