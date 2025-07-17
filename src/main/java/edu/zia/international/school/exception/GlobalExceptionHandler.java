package edu.zia.international.school.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, Object>> handleRuntimeException(RuntimeException ex, WebRequest request) {
        Map<String, Object> body = Map.of(
                "timestamp", LocalDateTime.now(),
                "status", HttpStatus.BAD_REQUEST.value(),
                "error", "Runtime Error",
                "message", ex.getMessage(),
                "path", request.getDescription(false).replace("uri=", "")
        );

        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }
    @ExceptionHandler(AppRuntimeException.class)
    public ResponseEntity<ErrorDetails> handleTodoAPIException(AppRuntimeException appRuntimeException,
                                                               WebRequest webRequest){
        ErrorDetails errorDetails = new ErrorDetails(
                LocalDateTime.now(),
                appRuntimeException.getMessage(),
                webRequest.getDescription(false)
        );

        return new ResponseEntity<>(errorDetails, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationErrors(
            MethodArgumentNotValidException ex) {

        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(err ->
                errors.put(err.getField(), err.getDefaultMessage())
        );
        return ResponseEntity.badRequest().body(errors);
    }
}
