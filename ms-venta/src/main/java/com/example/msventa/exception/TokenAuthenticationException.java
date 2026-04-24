package com.example.msventa.exception;

public class TokenAuthenticationException extends RuntimeException {

    public TokenAuthenticationException(String message) {
        super(message);
    }
}