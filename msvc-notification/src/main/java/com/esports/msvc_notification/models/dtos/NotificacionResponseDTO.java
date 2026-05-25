package com.esports.msvc_notification.models.dtos;
import lombok.*;
import java.time.LocalDateTime;
@Getter @Setter @NoArgsConstructor
public class NotificacionResponseDTO {
    private Long notificacionId;
    private Long usuarioId;
    private Long equipoId;
    private String tipo;
    private String mensaje;
    private Boolean leida;
    private String estado;
    private LocalDateTime fecha;
    private LocalDateTime createdAt;
}