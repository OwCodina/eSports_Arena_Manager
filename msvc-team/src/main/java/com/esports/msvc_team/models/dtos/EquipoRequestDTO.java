package com.esports.msvc_team.models.dtos;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
@Getter @Setter @NoArgsConstructor
public class EquipoRequestDTO {

    @NotBlank(message = "El nombre del equipo no puede estar vacío")
    private String nombre;

    @NotNull(message = "El ID del capitán no puede ser nulo")
    private Long capitanId;

    @NotNull(message = "El ID del juego principal no puede ser nulo")
    private Long juegoPrincipalId;
}
