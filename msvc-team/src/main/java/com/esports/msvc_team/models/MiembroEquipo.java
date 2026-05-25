package com.esports.msvc_team.models;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
@Entity @Table(name = "miembros_equipo", uniqueConstraints = @UniqueConstraint(name = "uk_equipo_usuario", columnNames = {"equipo_id", "usuario_id"}))
@Getter @Setter @ToString @NoArgsConstructor
public class MiembroEquipo {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) @Column(name = "miembro_id") private Long miembroId;
    @NotNull @Column(name = "equipo_id", nullable = false) private Long equipoId;
    @NotNull @Column(name = "usuario_id", nullable = false) private Long usuarioId;
    @NotBlank @Column(name = "rol_dentro_equipo", nullable = false) private String rolDentroEquipo;
}