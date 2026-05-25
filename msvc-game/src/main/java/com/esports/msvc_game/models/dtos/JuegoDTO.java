package com.esports.msvc_game.models.dtos;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * DTO que expone los datos de un Juego hacia otros microservicios.
 * Usado por tournament-service, team-service y match-service via Feign.
 */
@Getter @Setter @NoArgsConstructor
public class JuegoDTO {
    private Long juegoId;
    private String nombre;
    private String genero;
    private String modalidad;
    private Integer jugadoresPorEquipo;
    private String estado;
}
