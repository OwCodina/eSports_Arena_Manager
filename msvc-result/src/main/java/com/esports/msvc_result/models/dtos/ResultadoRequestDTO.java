package com.esports.msvc_result.models.dtos;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;
@Getter
@Setter
@NoArgsConstructor
public class ResultadoRequestDTO {

    @NotNull(message = "El ID de la partida no puede ser nulo")
    private Long partidaId;

    @NotNull(message = "El ID del ganador no puede ser nulo")
    private Long ganadorId;

    @NotNull @Min(value = 0, message = "El puntaje A no puede ser negativo")
    private Integer puntajeA;

    @NotNull @Min(value = 0, message = "El puntaje B no puede ser negativo")
    private Integer puntajeB;
}