package com.esports.msvc_registration.models.dtos;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
@Getter
@Setter
@NoArgsConstructor

public class InscripcionRequestDTO {

    @NotNull(message = "El ID del torneo no puede ser nulo")
    private Long torneoId;

    private Long equipoId;
    private Long jugadorId;

    @NotBlank(message = "El tipo de participante no puede estar vacío")
    private String tipoParticipante;
}