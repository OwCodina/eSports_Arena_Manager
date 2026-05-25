package com.esports.msvc_sanction.models.dtos;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import java.time.LocalDate;
@Getter @Setter @NoArgsConstructor
public class SancionRequestDTO {
    private Long usuarioId;
    private Long equipoId;
    @NotBlank(message = "El motivo no puede estar vacío") private String motivo;
    @NotNull(message = "La fecha de inicio no puede ser nula") private LocalDate fechaInicio;
    @NotNull(message = "La fecha de fin no puede ser nula") private LocalDate fechaFin;
    @NotBlank(message = "La severidad no puede estar vacía") private String severidad;
}