package com.treasuredata.fileserver.component.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import org.springframework.web.bind.annotation.ExceptionHandler;

/**
 * System exception handler
 */
@ControllerAdvice
public class SystemExceptionHandler extends ResponseEntityExceptionHandler {

    /**
     * Any other exceptions.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> anyExceptions(Exception ex, WebRequest request) {
        return ResponseEntity.internalServerError().body(ex.getMessage());
    }

    /**
     * Storage does not exist or file does not exist exception handler.
     */
    @ExceptionHandler(StorageException.class)
    public ResponseEntity<String> storageExceptionHandler(StorageFileNotFoundException ex, WebRequest request) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }
}
