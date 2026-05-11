package com.recetas_back.recetas_back.exception;

public class ContenidoNoPermitidoException extends RuntimeException {
    public ContenidoNoPermitidoException(String motivo) {
        super(motivo);
    }
}
