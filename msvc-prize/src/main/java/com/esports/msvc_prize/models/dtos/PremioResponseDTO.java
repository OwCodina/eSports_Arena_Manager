package com.esports.msvc_prize.models.dtos;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
@Getter
@Setter
@NoArgsConstructor

public class PremioResponseDTO {

    private Long premioId;
    private Long torneoId;
    private Integer posicion;
    private String descripcion;
    private BigDecimal valor;
    private String estado;
    private LocalDateTime createdAt;
}