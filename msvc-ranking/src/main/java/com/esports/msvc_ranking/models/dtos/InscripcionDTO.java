package com.esports.msvc_ranking.models.dtos;
import lombok.*;
import java.time.LocalDateTime;
@Getter @Setter @NoArgsConstructor
public class InscripcionDTO { private Long inscripcionId; private Long torneoId; private Long equipoId; private Long jugadorId; private String tipoParticipante; private String estado; private LocalDateTime fechaInscripcion; }