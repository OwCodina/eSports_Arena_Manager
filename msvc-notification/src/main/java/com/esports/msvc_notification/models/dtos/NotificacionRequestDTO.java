package com.esports.msvc_notification.models.dtos;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
@Getter @Setter @NoArgsConstructor
public class NotificacionRequestDTO {
    private Long usuarioId;
    private Long equipoId;
    @NotBlank(message = "El tipo no puede estar vacío") private String tipo;
    @NotBlank(message = "El mensaje no puede estar vacío") private String mensaje;
}