package com.esports.msvc_auth.models.dtos;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.AllArgsConstructor;

import java.util.Set;

/**
 * Respuesta del login/registro: el token JWT y datos basicos de la cuenta.
 * Se usa tambien para los endpoints de gestion de cuentas (token = null).
 */
@Getter @Setter @NoArgsConstructor
@AllArgsConstructor

public class AuthResponseDTO {
    private String token;
    private String tokenType;
    private String email;
    private Set<String> roles;

}
