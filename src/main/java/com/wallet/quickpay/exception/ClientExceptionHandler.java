package com.wallet.quickpay.exception;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.*;
import java.util.stream.Collectors;

@ControllerAdvice
public class ClientExceptionHandler extends ResponseEntityExceptionHandler {
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex, HttpHeaders headers,
            HttpStatus status, WebRequest request) {
        Map<String, List<String>> body = new HashMap<>();
        List<String> errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .collect(Collectors.toList());

        body.put("errors", errors);

        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }
    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(
            HttpMessageNotReadableException ex, HttpHeaders headers,
            HttpStatus status, WebRequest request){
        Map<String, List<String>> body = new HashMap<>();
        body.put("errors", Collections.singletonList(ex.getRootCause().getMessage()));
        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(InsufficientFundException.class)
    protected ResponseEntity<Object> insufficientFundExceptionHandler(InsufficientFundException ex) {
        Map<String, List<String>> body = new HashMap<>();
        body.put("errors", Collections.singletonList(ex.getMessage()));
        return new ResponseEntity<>(body, HttpStatus.NOT_ACCEPTABLE);
    }
    @ExceptionHandler(WalletNotFoundException.class)
    protected ResponseEntity<Object> walletNotFoundExceptionHandler(WalletNotFoundException ex) {
        Map<String, List<String>> body = new HashMap<>();
        body.put("errors", Collections.singletonList(ex.getMessage()));
        return new ResponseEntity<>(body, HttpStatus.NOT_FOUND);
    }
    @ExceptionHandler(ConcurrentRequestException.class)
    protected ResponseEntity<Object> concurrentRequestExceptionHandler(ConcurrentRequestException ex) {
        Map<String, List<String>> body = new HashMap<>();
        body.put("errors", Collections.singletonList(ex.getMessage()));
        return new ResponseEntity<>(body, HttpStatus.CONFLICT);
    }

}
