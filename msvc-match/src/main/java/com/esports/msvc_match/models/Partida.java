package com.esports.msvc_match.models;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
@Entity @Table(name = "partidas") @Getter @Setter @ToString @NoArgsConstructor
public class Partida {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) @Column(name = "partida_id") private Long partidaId;
    @Column(name = "torneo_id", nullable = false) private Long torneoId;
    @Column(name = "participante_a_id", nullable = false) private Long participanteAId;
    @Column(name = "participante_b_id", nullable = false) private Long participanteBId;
    @Column(nullable = false) private Integer ronda;
    @Column(name = "fecha_hora") private LocalDateTime fechaHora;
    @Column(nullable = false) private String estado;
    @Embedded private Audit audit = new Audit();
}