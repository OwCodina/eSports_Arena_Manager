package com.esports.msvc_tournament.models;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
@Entity
@Table(name = "torneos")
@Getter
@Setter
@ToString
@NoArgsConstructor
public class Torneo {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) @Column(name = "torneo_id") private Long torneoId;
    @Column(nullable = false) private String nombre;
    @Column(name = "juego_id", nullable = false)
    private Long juegoId;

    @Column(name = "fecha_inicio", nullable = false)
    private LocalDate fechaInicio;

    @Column(name = "fecha_fin", nullable = false)
    private LocalDate fechaFin;

    @Column(name = "fecha_cierre_inscripcion", nullable = false)
    private LocalDate fechaCierreInscripcion;

    @Column(name = "cupo_maximo", nullable = false)
    private Integer cupoMaximo;

    @Column(nullable = false)
    private String estado;

    @Column(nullable = false)
    private String modalidad;

    @Embedded
    private Audit audit = new Audit();
}