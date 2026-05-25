package com.esports.msvc_sanction.models.dtos;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
@Getter @Setter @NoArgsConstructor
public class SancionResponseDTO {
    private Long sancionId;
    private Long usuarioId;
    private Long equipoId;
    private String motivo;
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private String estado;
    private String severidad;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}