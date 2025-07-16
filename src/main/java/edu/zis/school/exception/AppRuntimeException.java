package edu.zis.school.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public class AppRuntimeException extends RuntimeException{
    private HttpStatus httpStatus;
    private String message;
}
