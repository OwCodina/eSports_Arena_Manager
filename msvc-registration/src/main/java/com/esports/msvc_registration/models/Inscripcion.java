package com.esports.msvc_registration.models;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "inscripciones", uniqueConstraints = {@UniqueConstraint(name = "uk_torneo_equipo", columnNames = {"torneo_id","equipo_id"}), @UniqueConstraint(name = "uk_torneo_jugador", columnNames = {"torneo_id","jugador_id"})})
@Getter
@Setter
@ToString
@NoArgsConstructor
public class Inscripcion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "inscripcion_id") private Long inscripcionId;
    @Column(name = "torneo_id", nullable = false)
    private Long torneoId;

    @Column(name = "equipo_id")
    private Long equipoId;

    @Column(name = "jugador_id")
    private Long jugadorId;

    @Column(name = "tipo_participante", nullable = false)
    private String tipoParticipante;

    @Column(nullable = false)
    private String estado;

    @Column(name = "fecha_inscripcion", nullable = false)
    private LocalDateTime fechaInscripcion;

    @Embedded
    private Audit audit = new Audit();
}