package com.esports.msvc_ranking.models.dtos;
import lombok.*;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor

public class RankingResponseDTO {
    private Long rankingId;
    private Long torneoId;
    private Long participanteId;
    private Integer puntos;
    private Integer victorias;
    private Integer derrotas;
    private Integer diferencia;
    private Integer posicion;
    private LocalDateTime updatedAt;
}