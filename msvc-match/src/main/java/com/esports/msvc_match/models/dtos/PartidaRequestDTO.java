package com.esports.msvc_match.models.dtos;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;
import java.time.LocalDateTime;
@Getter @Setter @NoArgsConstructor
public class PartidaRequestDTO {
    @NotNull(message = "El ID del torneo no puede ser nulo") private Long torneoId;
    @NotNull(message = "El participante A no puede ser nulo") private Long participanteAId;
    @NotNull(message = "El participante B no puede ser nulo") private Long participanteBId;
    @NotNull @Positive(message = "La ronda debe ser positiva") private Integer ronda;
    private LocalDateTime fechaHora;
}