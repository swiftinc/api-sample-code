package com.swift.apidev.icr.jwt;

public class JwtException extends RuntimeException {
    public JwtException(Throwable t) {
        super(t);
    }

    public JwtException(String message) {
        super(message);
    }
}
