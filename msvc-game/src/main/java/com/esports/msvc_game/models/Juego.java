package com.esports.msvc_game.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "juegos")
@Getter @Setter @ToString @NoArgsConstructor
public class Juego {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "juego_id")
    private Long juegoId;

    @Column(nullable = false, unique = true)
    private String nombre;

    @Column(nullable = false)
    private String genero;

    @Column(nullable = false)
    private String modalidad;

    @Column(name = "jugadores_por_equipo", nullable = false)
    private Integer jugadoresPorEquipo;

    @Column(nullable = false)
    private String estado;

    @Embedded
    private Audit audit = new Audit();
}
