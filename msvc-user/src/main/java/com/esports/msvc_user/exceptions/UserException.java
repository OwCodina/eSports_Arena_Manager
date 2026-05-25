package com.esports.msvc_user.exceptions;

/**
 * Excepción de dominio para el microservicio de Usuarios.
 * Se lanza cuando un usuario no existe o se viola una regla de negocio.
 */
public class UserException extends RuntimeException {
    public UserException(String message) {
        super(message);
    }
}
