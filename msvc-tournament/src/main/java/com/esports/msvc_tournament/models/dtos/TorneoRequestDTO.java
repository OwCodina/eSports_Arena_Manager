package com.esports.msvc_tournament.models.dtos;
import jakarta.validation.constraints.*;
import lombok.*;
import java.time.LocalDate;
@Getter @Setter @NoArgsConstructor
public class TorneoRequestDTO {
    @NotBlank(message = "El nombre del torneo no puede estar vacío")
    private String nombre;

    @NotNull(message = "El ID del juego no puede ser nulo")
    private Long juegoId;

    @NotNull(message = "La fecha de inicio no puede ser nula")
    private LocalDate fechaInicio;

    @NotNull(message = "La fecha de fin no puede ser nula")
    private LocalDate fechaFin;

    @NotNull(message = "La fecha de cierre de inscripción no puede ser nula")
    private LocalDate fechaCierreInscripcion;

    @NotNull @Min(value = 2, message = "El cupo máximo debe ser al menos 2")
    private Integer cupoMaximo;

    @NotBlank(message = "La modalidad no puede estar vacía")
    private String modalidad;

}