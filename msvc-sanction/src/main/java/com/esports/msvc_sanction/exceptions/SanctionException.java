package com.esports.msvc_sanction.exceptions;

/**
 * Excepción de dominio para el microservicio de Sanciones.
 * Se lanza cuando una sanción no existe o se viola una regla de negocio.
 */
public class SanctionException extends RuntimeException {
    public SanctionException(String message) {
        super(message);
    }
}
