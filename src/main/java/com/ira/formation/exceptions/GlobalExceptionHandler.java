package com.ira.formation.exceptions;

import com.ira.formation.dto.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import lombok.extern.slf4j.Slf4j;

import java.util.stream.Collectors;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    // RuntimeException → غالباً أخطاء métier
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiResponse<Object>> handleRuntime(RuntimeException ex){
        log.error("RuntimeException: ", ex);

        ApiResponse<Object> response = ApiResponse.builder()
                .success(false)
                .message(ex.getMessage()) // رسالة واضحة للfrontend
                .data(null)               // ما فماش data في الخطأ
                .build();

        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    // Exception → كل الأخطاء الأخرى
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Object>> handleException(Exception ex){
        log.error("Exception: ", ex);

        ApiResponse<Object> response = ApiResponse.builder()
                .success(false)
                .message("Erreur serveur") // generic message
                .data(null)
                .build();

        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    // Validation errors → كل الحقول الخطأ
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Object>> handleValidationErrors(MethodArgumentNotValidException ex){
        log.error("Validation errors: ", ex);

        // نجمع كل الأخطاء في رسالة واحدة مفصولة ب "; "
        String errorMessage = ex.getBindingResult().getFieldErrors()
                                 .stream()
                                 .map(err -> err.getField() + ": " + err.getDefaultMessage())
                                 .collect(Collectors.joining("; "));

        ApiResponse<Object> response = ApiResponse.builder()
                .success(false)
                .message(errorMessage)
                .data(null)
                .build();

        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }
}