package com.ejemplo.authjwt.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * Excepción personalizada para errores de autenticación
 */
@Getter
public class AuthException extends RuntimeException {
    private final HttpStatus status;

    public AuthException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }

    public AuthException(String message) {
        this(message, HttpStatus.BAD_REQUEST);
    }
}
