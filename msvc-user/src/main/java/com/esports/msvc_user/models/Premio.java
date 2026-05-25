package com.esports.msvc_prize.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;

@Entity
@Table(
    name = "premios",
    uniqueConstraints = @UniqueConstraint(
        name = "uk_torneo_posicion",
        columnNames = {"torneo_id", "posicion"}
    )
)
@Getter @Setter @ToString @NoArgsConstructor
public class Premio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "premio_id")
    private Long premioId;

    @NotNull(message = "El ID del torneo no puede ser nulo")
    @Column(name = "torneo_id", nullable = false)
    private Long torneoId;

    @NotNull(message = "La posición no puede ser nula")
    @Min(value = 1, message = "La posición debe ser mayor a 0")
    @Column(nullable = false)
    private Integer posicion;

    @NotBlank(message = "La descripción del premio no puede estar vacía")
    @Column(nullable = false)
    private String descripcion;

    @Column(precision = 12, scale = 2)
    private BigDecimal valor;

    @Column(nullable = false)
    private String estado;

    @Embedded
    private Audit audit = new Audit();
}
