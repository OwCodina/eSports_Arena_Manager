package com.esports.msvc_sanction.models;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
@Entity @Table(name = "sanciones") @Getter @Setter @ToString @NoArgsConstructor
public class Sancion {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) @Column(name = "sancion_id") private Long sancionId;
    @Column(name = "usuario_id") private Long usuarioId;
    @Column(name = "equipo_id") private Long equipoId;
    @Column(nullable = false) private String motivo;
    @Column(name = "fecha_inicio", nullable = false) private LocalDate fechaInicio;
    @Column(name = "fecha_fin", nullable = false) private LocalDate fechaFin;
    @Column(nullable = false) private String estado;
    @Column(nullable = false) private String severidad;
    @Embedded private Audit audit = new Audit();
}