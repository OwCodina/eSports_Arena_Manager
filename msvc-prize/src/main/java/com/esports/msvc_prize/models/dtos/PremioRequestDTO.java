package com.esports.msvc_prize.models.dtos;
import jakarta.validation.constraints.*;
import lombok.*;
import java.math.BigDecimal;
@Getter
@Setter
@NoArgsConstructor
public class PremioRequestDTO {
    @NotNull(message = "El ID del torneo no puede ser nulo")
    private Long torneoId;

    @NotNull
    @Min(value = 1, message = "La posición debe ser mayor a 0")
    private Integer posicion;

    @NotBlank(message = "La descripción no puede estar vacía")
    private String descripcion;

    private BigDecimal valor;
}