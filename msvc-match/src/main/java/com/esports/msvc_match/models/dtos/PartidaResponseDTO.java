package com.esports.msvc_match.models.dtos;
import lombok.*;
import java.time.LocalDateTime;
@Getter @Setter @NoArgsConstructor
public class PartidaResponseDTO {
    private Long partidaId;
    private Long torneoId;
    private Long participanteAId;
    private Long participanteBId;
    private Integer ronda;
    private LocalDateTime fechaHora;
    private String estado;
    private LocalDateTime createdAt;
}