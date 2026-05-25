package com.esports.msvc_match.models.dtos;
import lombok.*;
import java.time.LocalDate;
@Getter @Setter @NoArgsConstructor
public class TorneoDTO { private Long torneoId; private String nombre; private String estado; private LocalDate fechaInicio; private LocalDate fechaFin; }