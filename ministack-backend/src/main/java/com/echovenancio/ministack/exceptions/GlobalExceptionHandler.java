package com.echovenancio.ministack.exceptions;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingPathVariableException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import com.echovenancio.ministack.models.ErrorResponse;
import com.echovenancio.ministack.utils.Result;

@ControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Result<Void, ErrorResponse>> handleGenericException(Exception ex) {
        logger.error("An error occurred: " + ex.getMessage(), ex);
        ErrorResponse errorResponse = new ErrorResponse("Internal Server Error", "500");
        return ResponseEntity.status(500)
                .body(Result.error(errorResponse));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Result<Void, ErrorResponse>> handleValidationException(MethodArgumentNotValidException ex) {
        logger.error("Validation error: " + ex.getMessage(), ex);
        ErrorResponse errorResponse = new ErrorResponse("Validation Error", "400");
        return ResponseEntity.badRequest()
                .body(Result.error(errorResponse));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Result<Void, ErrorResponse>> handleMethodArgumentMismatchException(MethodArgumentTypeMismatchException ex) {
        logger.error("Method argument mismatch: " + ex.getMessage(), ex);
        ErrorResponse errorResponse = new ErrorResponse("Method Argument Mismatch", "400");
        return ResponseEntity.badRequest()
                .body(Result.error(errorResponse));
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<Result<Void, ErrorResponse>> handleNoResourceFoundException(NoResourceFoundException ex) {
        logger.error("Resource not found: " + ex.getMessage(), ex);
        ErrorResponse errorResponse = new ErrorResponse(ex.getMessage(), "404");
        return ResponseEntity.status(404)
                .body(Result.error(errorResponse));
    }

    @ExceptionHandler(MissingPathVariableException.class)
    public ResponseEntity<Result<Void, ErrorResponse>> handleMissingPathVariableException(MissingPathVariableException ex) {
        logger.error("Missing path variable: " + ex.getMessage(), ex);
        ErrorResponse errorResponse = new ErrorResponse("Missing Path Variable", "400");
        return ResponseEntity.badRequest()
                .body(Result.error(errorResponse));
    }
}
