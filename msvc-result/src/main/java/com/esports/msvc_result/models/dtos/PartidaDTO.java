package com.esports.msvc_result.models.dtos;
import lombok.*;
import java.time.LocalDateTime;
@Getter
@Setter
@NoArgsConstructor
public class PartidaDTO {
    private Long partidaId;
    private Long torneoId;
    private Long participanteAId;
    private Long participanteBId;
    private Integer ronda;
    private LocalDateTime fechaHora;
    private String estado;
}