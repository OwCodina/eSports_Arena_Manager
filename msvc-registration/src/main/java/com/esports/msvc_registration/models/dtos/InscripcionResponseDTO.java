package com.esports.msvc_registration.models.dtos;
import lombok.*;
import java.time.LocalDateTime;
@Getter
@Setter
@NoArgsConstructor

public class InscripcionResponseDTO {

    private Long inscripcionId;
    private Long torneoId;
    private Long equipoId;
    private Long jugadorId;
    private String tipoParticipante;
    private String estado;
    private LocalDateTime fechaInscripcion;
    private LocalDateTime createdAt;
}