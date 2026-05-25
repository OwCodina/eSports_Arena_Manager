package com.esports.msvc_tournament.models.dtos;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
@Getter @Setter @NoArgsConstructor
public class TorneoResponseDTO {
    private Long torneoId;
    private String nombre;
    private Long juegoId;
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private LocalDate fechaCierreInscripcion;
    private Integer cupoMaximo;
    private String estado;
    private String modalidad;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}