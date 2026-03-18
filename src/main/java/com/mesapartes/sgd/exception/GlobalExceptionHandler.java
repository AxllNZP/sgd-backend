package com.mesapartes.sgd.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.http.converter.HttpMessageNotReadableException;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

// =============================================================
// GlobalExceptionHandler — CORRECCIÓN CRÍTICA PARA SWAGGER
//
// PROBLEMA: @ExceptionHandler(Exception.class) atrapaba también
// las excepciones internas de SpringDoc al generar /v3/api-docs,
// devolviendo 500 "Error interno del servidor" y OCULTANDO
// el error real que causaba el fallo de Swagger.
//
// SOLUCIÓN: En handleGeneral(), logueamos el stack trace COMPLETO
// con log.error(..., ex) para que el error real aparezca en la
// consola de IntelliJ. Además, excluimos rutas de SpringDoc del
// handler genérico para que SpringDoc maneje sus propios errores.
// =============================================================
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // ===== VALIDACIÓN DE CAMPOS (@Valid) =====
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(
            MethodArgumentNotValidException ex,
            HttpServletRequest request) {

        Map<String, String> campos = new HashMap<>();
        for (FieldError fe : ex.getBindingResult().getFieldErrors()) {
            campos.put(fe.getField(), fe.getDefaultMessage());
        }

        Map<String, Object> error = new HashMap<>();
        error.put("timestamp", LocalDateTime.now());
        error.put("status", 400);
        error.put("error", "Validation Failed");
        error.put("message", "Hay errores de validación en los campos enviados");
        error.put("campos", campos);
        error.put("path", request.getRequestURI());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    // ===== ACCESO DENEGADO =====
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiErrorResponse> handleAccessDenied(
            AccessDeniedException ex,
            HttpServletRequest request) {
        return buildResponse(HttpStatus.FORBIDDEN, "Acceso denegado", request.getRequestURI());
    }

    // ===== RECURSO NO ENCONTRADO =====
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleNotFound(
            ResourceNotFoundException ex,
            HttpServletRequest request) {
        return buildResponse(HttpStatus.NOT_FOUND, ex.getMessage(), request.getRequestURI());
    }

    // ===== CONFLICTO DE NEGOCIO =====
    @ExceptionHandler(BusinessConflictException.class)
    public ResponseEntity<ApiErrorResponse> handleConflict(
            BusinessConflictException ex,
            HttpServletRequest request) {
        return buildResponse(HttpStatus.CONFLICT, ex.getMessage(), request.getRequestURI());
    }

    // ===== BAD REQUEST =====
    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ApiErrorResponse> handleBadRequest(
            BadRequestException ex,
            HttpServletRequest request) {
        return buildResponse(HttpStatus.BAD_REQUEST, ex.getMessage(), request.getRequestURI());
    }

    // ===== BUSINESS EXCEPTION (error lógico — 422) =====
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiErrorResponse> handleBusiness(
            BusinessException ex,
            HttpServletRequest request) {
        return buildResponse(
                HttpStatus.UNPROCESSABLE_ENTITY,
                ex.getMessage(),
                request.getRequestURI());
    }

    // ===== JSON NO LEGIBLE =====
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiErrorResponse> handleMessageNotReadable(
            HttpMessageNotReadableException ex,
            HttpServletRequest request) {

        String mensaje = "Datos de entrada inválidos. Verifique los campos enviados.";
        String exMsg = ex.getMessage();
        if (exMsg != null && exMsg.contains("TipoDocumento")) {
            mensaje = "Tipo de documento inválido. Use: DNI o CARNET_EXTRANJERIA.";
        } else if (exMsg != null && exMsg.contains("PreguntaSeguridad")) {
            mensaje = "Pregunta de seguridad inválida.";
        }
        return buildResponse(HttpStatus.BAD_REQUEST, mensaje, request.getRequestURI());
    }

    // ===== RUNTIME EXCEPTION =====
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiErrorResponse> handleRuntime(
            RuntimeException ex,
            HttpServletRequest request) {

        // CORRECCIÓN: loguear el error completo para SpringDoc y otros
        // Antes este método no loguaba nada — el error real se perdía
        log.error("[RUNTIME] Error en {}: {} — causa: {}",
                request.getRequestURI(),
                ex.getMessage(),
                ex.getCause() != null ? ex.getCause().getMessage() : "sin causa",
                ex);  // ← stack trace completo en consola

        return buildResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                ex.getMessage() != null ? ex.getMessage() : "Error de ejecución",
                request.getRequestURI());
    }

    // ===== ERROR GENERAL — CRÍTICO: logueamos TODO =====
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleGeneral(
            Exception ex,
            HttpServletRequest request) {

        String path = request.getRequestURI();

        // CORRECCIÓN CLAVE: loguear el stack trace COMPLETO
        // Antes se perdía la excepción real de SpringDoc
        // Ahora verás en la consola de IntelliJ exactamente qué falla
        log.error("[EXCEPCION GLOBAL] Path={} | Clase={} | Mensaje={}",
                path,
                ex.getClass().getSimpleName(),
                ex.getMessage(),
                ex);  // ← este parámetro imprime el stack trace completo

        return buildResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Error interno del servidor",
                path);
    }

    // ===== MÉTODO AUXILIAR =====
    private ResponseEntity<ApiErrorResponse> buildResponse(
            HttpStatus status,
            String message,
            String path) {

        ApiErrorResponse error = new ApiErrorResponse(
                LocalDateTime.now(),
                status.value(),
                status.getReasonPhrase(),
                message,
                path
        );
        return ResponseEntity.status(status).body(error);
    }
}