package com.esports.msvc_prize.models.dtos;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter @NoArgsConstructor
public class RankingDTO {
    private Long rankingId;
    private Long torneoId;
    private Long participanteId;
    private Integer puntos;
    private Integer victorias;
    private Integer derrotas;
    private Integer diferencia;
    private Integer posicion;
}
