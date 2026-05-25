package com.esports.msvc_result.models.dtos;
import lombok.*;
import java.time.LocalDateTime;
@Getter
@Setter
@NoArgsConstructor
public class ResultadoResponseDTO {
    private Long resultadoId;
    private Long partidaId;
    private Long ganadorId;
    private Integer puntajeA;
    private Integer puntajeB;
    private String estadoValidacion;
    private String motivoAnulacion;
    private LocalDateTime fechaRegistro;
    private LocalDateTime createdAt;
}