package com.esports.msvc_team.models.dtos;
import lombok.*;
import java.time.LocalDateTime;
@Getter @Setter @NoArgsConstructor
public class EquipoResponseDTO {
    private Long equipoId;
    private String nombre;
    private Long capitanId;
    private Long juegoPrincipalId;
    private String estado;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}