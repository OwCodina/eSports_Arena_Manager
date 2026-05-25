package com.esports.msvc_team.models.dtos;
import lombok.*;
@Getter @Setter @NoArgsConstructor
public class JuegoDTO {
    private Long juegoId;
    private String nombre;
    private String genero;
    private String modalidad;
    private Integer jugadoresPorEquipo;
    private String estado;
}
