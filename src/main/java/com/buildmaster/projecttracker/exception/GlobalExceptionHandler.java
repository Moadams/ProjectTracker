package com.buildmaster.projecttracker.exception;

import com.buildmaster.projecttracker.dto.CustomApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<CustomApiResponse> handleResourceNotFoundException(ResourceNotFoundException ex, WebRequest request) {

        return new ResponseEntity<>(CustomApiResponse.error(ex.getMessage()), HttpStatus.NOT_FOUND);
    }


    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<CustomApiResponse<Map<String, String>>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
                errors.put(error.getField(), error.getDefaultMessage()));

        CustomApiResponse<Map<String, String>> response = CustomApiResponse.error("Validation failed", errors);
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<CustomApiResponse> handleGlobalException(Exception ex, WebRequest request) {

        return new ResponseEntity<>(CustomApiResponse.error(ex.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
