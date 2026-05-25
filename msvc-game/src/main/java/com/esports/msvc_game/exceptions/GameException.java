package com.esports.msvc_game.exceptions;

/**
 * Excepción de dominio para el microservicio de Juegos.
 * Se lanza cuando un juego no existe o se viola una regla de negocio.
 */
public class GameException extends RuntimeException {
    public GameException(String message) {
        super(message);
    }
}
