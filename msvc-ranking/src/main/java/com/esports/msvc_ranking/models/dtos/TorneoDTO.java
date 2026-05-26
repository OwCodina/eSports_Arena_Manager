package com.esports.msvc_ranking.models.dtos;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

/**
 * DTO espejo de Torneo (msvc-tournament).
 * Usado para deserializar la respuesta del Feign client hacia tournament-service.
 */
@Getter @Setter @NoArgsConstructor
public class TorneoDTO {
    private Long torneoId;
    private String nombre;
    private String estado;
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
}
