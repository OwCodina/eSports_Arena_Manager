package com.esports.msvc_auth.models.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Datos para iniciar sesion. El login se hace por email + password.
 */
@Getter @Setter @NoArgsConstructor
public class LoginRequestDTO {

    @NotBlank(message = "El email no puede estar vacio")
    @Email(message = "El email debe tener un formato valido")
    private String email;

    @NotBlank(message = "La password no puede estar vacia")
    private String password;
}
