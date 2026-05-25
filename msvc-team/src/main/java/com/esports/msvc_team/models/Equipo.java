package com.esports.msvc_team.models;
import jakarta.persistence.*;
import lombok.*;
@Entity @Table(name = "equipos") @Getter @Setter @ToString @NoArgsConstructor
public class Equipo {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) @Column(name = "equipo_id") private Long equipoId;
    @Column(nullable = false, unique = true) private String nombre;
    @Column(name = "capitan_id", nullable = false) private Long capitanId;
    @Column(name = "juego_principal_id", nullable = false) private Long juegoPrincipalId;
    @Column(nullable = false) private String estado;
    @Embedded private Audit audit = new Audit();
}