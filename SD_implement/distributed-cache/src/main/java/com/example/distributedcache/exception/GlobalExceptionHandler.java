package com.example.distributedcache.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ProductNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Map<String,Object> handlerNotFound(ProductNotFoundException productNotFoundException){
        return Map.of(
                "timestamp",
                LocalDateTime.now(),
                "status",
                404,
                "error",
                "Not Found",
                "message",
                productNotFoundException.getMessage()
        );
    }
}
