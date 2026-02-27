package com.mesapartes.sgd.service;

import com.mesapartes.sgd.dto.*;

public interface RecuperacionPasswordService {

    // ===== VÍA A: POR CORREO =====

    // Paso 1: Valida identidad y envía código al correo
    void solicitarRecuperacion(RecuperacionSolicitarDTO request);

    // Paso 2: Verifica el código recibido por correo
    void verificarCodigoRecuperacion(RecuperacionVerificarCodigoDTO request);

    // ===== COMÚN PARA AMBAS VÍAS =====

    // Paso final: Guarda la nueva contraseña
    void establecerNuevaPassword(NuevaPasswordDTO request);

    // ===== VÍA B: POR PREGUNTA SECRETA =====

    // Paso 1: Devuelve la pregunta de seguridad del usuario
    PreguntaSeguridadResponseDTO obtenerPreguntaSeguridad(String tipoPersna, String identificador);

    // Paso 2: Verifica la respuesta y habilita el cambio de contraseña
    void verificarRespuestaSecreta(VerificarPreguntaDTO request);
}