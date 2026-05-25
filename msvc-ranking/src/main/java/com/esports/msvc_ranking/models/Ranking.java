package com.esports.msvc_ranking.models;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "rankings", uniqueConstraints = @UniqueConstraint(name = "uk_torneo_participante", columnNames = {"torneo_id","participante_id"}))
@Getter
@Setter
@ToString
@NoArgsConstructor
public class Ranking {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) @Column(name = "ranking_id")
    private Long rankingId;

    @Column(name = "torneo_id", nullable = false)
    private Long torneoId;

    @Column(name = "participante_id", nullable = false)
    private Long participanteId;

    @Column(nullable = false)
    private Integer puntos;

    @Column(nullable = false)
    ivate Integer victorias;

    @Column(nullable = false)
    private Integer derrotas;

    @Column(nullable = false)
    private Integer diferencia;

    @Column(nullable = false)
    private Integer posicion;

    @Embedded
    private Audit audit = new Audit();
}