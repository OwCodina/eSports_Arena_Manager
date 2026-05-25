package com.esports.msvc_team.models.dtos;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
@Getter @Setter @NoArgsConstructor
public class MiembroRequestDTO {
    @NotNull(message = "El ID del usuario no puede ser nulo")
    private Long usuarioId;

    @NotBlank(message = "El rol dentro del equipo no puede estar vacío")
    private String rolDentroEquipo;
}