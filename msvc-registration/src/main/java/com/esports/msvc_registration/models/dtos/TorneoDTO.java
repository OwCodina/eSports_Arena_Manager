package com.esports.msvc_registration.models.dtos;
import lombok.*;
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
    private String modalidad; }