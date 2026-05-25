package com.esports.msvc_game.models.dtos;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * DTO de salida que expone los datos de un Juego al cliente.
 * También usado por otros microservicios via Feign.
 */
@Getter @Setter @NoArgsConstructor
public class JuegoResponseDTO {
    private Long juegoId;
    private String nombre;
    private String genero;
    private String modalidad;
    private Integer jugadoresPorEquipo;
    private String estado;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
