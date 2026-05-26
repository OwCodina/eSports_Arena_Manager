package com.esports.msvc_ranking.models.dtos;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * DTO de entrada para actualizar el ranking tras un resultado validado.
 */
@Getter @Setter @NoArgsConstructor
public class ActualizarRankingRequestDTO {
    @NotNull(message = "El ganadorId no puede ser nulo") private Long ganadorId;
    @NotNull(message = "El perdedorId no puede ser nulo") private Long perdedorId;
    @NotNull(message = "El puntajeGanador no puede ser nulo") private Integer puntajeGanador;
    @NotNull(message = "El puntajePerdedor no puede ser nulo") private Integer puntajePerdedor;
}
