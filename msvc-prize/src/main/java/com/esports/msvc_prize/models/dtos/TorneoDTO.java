package com.esports.msvc_prize.models.dtos;
import lombok.*;
import java.time.LocalDate;
@Getter
@Setter
@NoArgsConstructor
public class TorneoDTO {

    private Long torneoId;
    private String nombre;
    private String estado;

}