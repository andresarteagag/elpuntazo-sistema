package com.elpuntazo.backend.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Traduce cualquier error interno a un mensaje entendible para
 * un usuario que no sabe de programacion. Nunca se expone al
 * frontend un stack trace ni un mensaje tecnico crudo.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(AuthenticationFailedException.class)
    public ResponseEntity<Object> handleAuthFailed(AuthenticationFailedException ex) {
        return buildResponse(HttpStatus.UNAUTHORIZED, ex.getMessage());
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<Object> handleBusiness(BusinessException ex) {
        return buildResponse(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    /**
     * Sin este handler, un permiso denegado dentro de un controlador caia
     * en handleGeneric y se devolvia 500; el frontend mostraba "problema
     * inesperado" en lugar del aviso de falta de permisos.
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Object> handleAccessDenied(AccessDeniedException ex) {
        return buildResponse(HttpStatus.FORBIDDEN, "No tienes permiso para realizar esta accion.");
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Object> handleValidation(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(f -> f.getDefaultMessage())
                .orElse("El valor ingresado no es valido.");
        return buildResponse(HttpStatus.BAD_REQUEST, message);
    }

    /**
     * Un filtro con un valor que no existe (por ejemplo status=CUALQUIERA)
     * es un error del que envia la peticion, no del servidor: 400, no 500.
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Object> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        return buildResponse(HttpStatus.BAD_REQUEST,
                "El valor enviado en \"" + ex.getName() + "\" no es valido.");
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Object> handleUnreadableBody(HttpMessageNotReadableException ex) {
        return buildResponse(HttpStatus.BAD_REQUEST, "Los datos enviados no tienen el formato esperado.");
    }

    /**
     * Ruta inexistente: debe seguir siendo 404. Si cayera en handleGeneric
     * se reportaria como una falla del servidor.
     */
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<Object> handleNotFound(NoResourceFoundException ex) {
        return buildResponse(HttpStatus.NOT_FOUND, "La direccion solicitada no existe.");
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleGeneric(Exception ex) {
        // Al usuario se le da un mensaje neutro, pero el detalle queda en
        // los logs del servidor: sin esto un error en produccion seria
        // imposible de diagnosticar.
        log.error("Error no controlado procesando la peticion", ex);
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR,
                "Ocurrio un problema inesperado. Intenta nuevamente.");
    }

    private ResponseEntity<Object> buildResponse(HttpStatus status, String message) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", status.value());
        body.put("message", message);
        return ResponseEntity.status(status).body(body);
    }
}
