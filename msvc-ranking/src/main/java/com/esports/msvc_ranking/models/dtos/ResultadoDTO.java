package com.esports.msvc_ranking.models.dtos;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * DTO espejo de Resultado (msvc-result).
 * Usado para deserializar la respuesta del Feign client hacia result-service.
 */
@Getter @Setter @NoArgsConstructor
public class ResultadoDTO {
    private Long resultadoId;
    private Long partidaId;
    private Long ganadorId;
    private Integer puntajeA;
    private Integer puntajeB;
    private String estadoValidacion;
    private LocalDateTime fechaRegistro;
}
