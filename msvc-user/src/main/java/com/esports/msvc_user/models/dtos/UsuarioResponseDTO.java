package com.esports.msvc_user.models.dtos;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter @Setter @NoArgsConstructor
public class UsuarioResponseDTO {
    private Long usuarioId;
    private String nombre;
    private String nickname;
    private String email;
    private String rol;
    private String estado;
    private LocalDate fechaRegistro;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
