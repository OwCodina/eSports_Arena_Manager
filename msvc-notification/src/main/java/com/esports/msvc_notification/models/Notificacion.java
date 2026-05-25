package com.esports.msvc_notification.models;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
@Entity @Table(name = "notificaciones") @Getter @Setter @ToString @NoArgsConstructor
public class Notificacion {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) @Column(name = "notificacion_id")
    private Long notificacionId;

    @Column(name = "usuario_id")
    private Long usuarioId;

    @Column(name = "equipo_id")
    private Long equipoId;

    @Column(nullable = false)
    private String tipo;

    @Column(nullable = false, length = 500)
    private String mensaje;

    @Column(nullable = false)
    private Boolean leida;

    @Column(nullable = false)
    private String estado;

    @Column(nullable = false)
    private LocalDateTime fecha;

    @Embedded
    private Audit audit = new Audit();
}