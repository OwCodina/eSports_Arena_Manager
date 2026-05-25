package com.esports.msvc_prize.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

@Entity
@Table(
    name = "premios_asignados",
    uniqueConstraints = @UniqueConstraint(
        name = "uk_premio_asignado",
        columnNames = "premio_id"
    )
)
@Getter @Setter @ToString @NoArgsConstructor
public class PremioAsignado {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "asignacion_id")
    private Long asignacionId;

    @NotNull(message = "El ID del premio no puede ser nulo")
    @Column(name = "premio_id", nullable = false, unique = true)
    private Long premioId;

    @NotNull(message = "El ID del participante no puede ser nulo")
    @Column(name = "participante_id", nullable = false)
    private Long participanteId;

    @Column(name = "fecha_asignacion", nullable = false)
    private LocalDateTime fechaAsignacion;
}
