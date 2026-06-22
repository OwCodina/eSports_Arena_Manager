package com.esports.msvc_auth.models.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

/**
 * Datos para registrar una cuenta de acceso.
 *
 * Los roles se envian como un Set de strings (ej: ["ADMINISTRADOR"] o
 * ["JUGADOR", "ORGANIZADOR"]). Se normalizan a ROLE_X.
 *
 * nombre y nickname se reenvian a msvc-user para crear el perfil alli.
 */
@Getter @Setter @NoArgsConstructor
public class RegisterRequestDTO {

    @NotBlank(message = "El email no puede estar vacio")
    @Email(message = "El email debe tener un formato valido")
    private String email;

    @NotBlank(message = "La password no puede estar vacia")
    private String password;

    @NotEmpty(message = "Debe especificar al menos un rol")
    private Set<String> roles = new HashSet<>();

    @NotBlank(message = "El nombre no puede estar vacio")
    private String nombre;

    @NotBlank(message = "El nickname no puede estar vacio")
    private String nickname;
}
