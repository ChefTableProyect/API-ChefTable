package com.proyecto.spring_proyecto.shared;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.dao.PessimisticLockingFailureException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ApiException.class)
    ResponseEntity<ApiErrorResponse> handleApiException(ApiException exception) {
        var body = new ApiErrorResponse(
                exception.getType(),
                exception.getMessage(),
                exception.getStatus().value(),
                Map.of());
        return ResponseEntity.status(exception.getStatus()).body(body);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ApiErrorResponse> handleValidation(MethodArgumentNotValidException exception) {
        Map<String, String> fields = new LinkedHashMap<>();
        exception.getBindingResult().getFieldErrors()
                .forEach(error -> fields.putIfAbsent(error.getField(), error.getDefaultMessage()));
        var body = new ApiErrorResponse(
                "VALIDATION_ERROR",
                "La solicitud contiene datos inválidos",
                HttpStatus.BAD_REQUEST.value(),
                fields);
        return ResponseEntity.badRequest().body(body);
    }

    @ExceptionHandler(MissingRequestHeaderException.class)
    ResponseEntity<ApiErrorResponse> handleMissingHeader(MissingRequestHeaderException exception) {
        var body = new ApiErrorResponse(
                "MISSING_REQUIRED_HEADER",
                "Falta la cabecera requerida " + exception.getHeaderName(),
                HttpStatus.BAD_REQUEST.value(),
                Map.of());
        return ResponseEntity.badRequest().body(body);
    }

    @ExceptionHandler(PessimisticLockingFailureException.class)
    ResponseEntity<ApiErrorResponse> handleLockTimeout(PessimisticLockingFailureException exception) {
        var body = new ApiErrorResponse(
                "INVENTORY_BUSY",
                "El inventario está recibiendo muchas solicitudes; intenta nuevamente",
                HttpStatus.SERVICE_UNAVAILABLE.value(),
                Map.of());
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .header("Retry-After", "1")
                .body(body);
    }

    @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
    ResponseEntity<ApiErrorResponse> handleConcurrentUpdate(ObjectOptimisticLockingFailureException exception) {
        var body = new ApiErrorResponse(
                "CONCURRENT_UPDATE",
                "El pedido cambió mientras se procesaba; actualiza e intenta nuevamente",
                HttpStatus.CONFLICT.value(),
                Map.of());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
    }
}
