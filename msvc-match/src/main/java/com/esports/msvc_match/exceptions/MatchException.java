package com.esports.msvc_match.exceptions;

/**
 * Excepción de dominio para el microservicio de Partidas.
 */
public class MatchException extends RuntimeException {
    public MatchException(String message) {
        super(message);
    }
}
