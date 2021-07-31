package com.treasuredata.fileserver.component.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.concurrent.ExecutionException;

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
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ex.getMessage());
    }

    @ExceptionHandler({ExecutionException.class, InterruptedException.class})
    public ResponseEntity<String> connectionException(Exception ex, WebRequest request) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ex.getMessage());
    }

    @ExceptionHandler(StorageFileNotFoundException.class)
    public ResponseEntity<String> fileNotFoundExceptionHandler(StorageFileNotFoundException ex, WebRequest request) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }

    @ExceptionHandler(StorageException.class)
    public ResponseEntity<String> fileExceptionHandler(StorageException ex, WebRequest request) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ex.getMessage());
    }
}
