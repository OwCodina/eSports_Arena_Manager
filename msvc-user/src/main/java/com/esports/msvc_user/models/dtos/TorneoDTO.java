package com.esports.msvc_prize.models.dtos;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter @Setter @NoArgsConstructor
public class TorneoDTO {
    private Long torneoId;
    private String nombre;
    private Long juegoId;
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private LocalDate fechaCierreInscripcion;
    private Integer cupoMaximo;
    private String estado;
    private String modalidad;
}
