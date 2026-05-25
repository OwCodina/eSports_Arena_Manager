package com.esports.msvc_game.models.dtos;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * DTO de entrada para crear o actualizar un Juego.
 * Contiene solo los campos que el cliente puede enviar.
 * Las validaciones Bean Validation se aplican aquí, no en la entidad.
 */
@Getter @Setter @NoArgsConstructor
public class JuegoRequestDTO {

    @NotBlank(message = "El nombre del juego no puede estar vacío")
    private String nombre;

    @NotBlank(message = "El género no puede estar vacío")
    private String genero;

    @NotBlank(message = "La modalidad no puede estar vacía")
    private String modalidad;

    @NotNull(message = "La cantidad de jugadores por equipo no puede ser nula")
    @Min(value = 1, message = "La cantidad de jugadores por equipo debe ser mayor a 0")
    private Integer jugadoresPorEquipo;
}
