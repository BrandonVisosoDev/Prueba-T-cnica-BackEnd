package com.ordenaris.riesgocrediticio.infrastructure.config;

import com.ordenaris.riesgocrediticio.domain.model.EmpresaNotFoundException;
import com.ordenaris.riesgocrediticio.domain.model.RiesgoEvaluacionException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(EmpresaNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleEmpresaNotFound(EmpresaNotFoundException ex) {
        log.warn("404 empresa no encontrada", ex);
        return buildResponse(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(RiesgoEvaluacionException.class)
    public ResponseEntity<Map<String, Object>> handleRiesgoEvaluacion(RiesgoEvaluacionException ex) {
        log.error("422 error de evaluacion", ex);
        return buildResponse(HttpStatus.UNPROCESSABLE_ENTITY, ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidaciones(MethodArgumentNotValidException ex) {
        log.warn("400 validacion fallida", ex);
        Map<String, Object> errores = new HashMap<>();
        errores.put("timestamp", LocalDateTime.now());
        errores.put("status", HttpStatus.BAD_REQUEST.value());
        errores.put("error", "Error de validacion");

        Map<String, String> campos = new HashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(fe -> campos.put(fe.getField(), fe.getDefaultMessage()));
        errores.put("campos", campos);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errores);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGenerico(Exception ex) {
        log.error("500 error inesperado", ex);
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR,
                "Error interno del servidor. Contacte al administrador.");
    }

    private ResponseEntity<Map<String, Object>> buildResponse(HttpStatus status, String mensaje) {
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", status.value());
        body.put("error", status.getReasonPhrase());
        body.put("mensaje", mensaje);
        return ResponseEntity.status(status).body(body);
    }
}
