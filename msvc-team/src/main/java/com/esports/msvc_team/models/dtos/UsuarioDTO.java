package com.esports.msvc_team.models.dtos;
import lombok.*;
import java.time.LocalDate;
@Getter @Setter @NoArgsConstructor
public class UsuarioDTO { private Long usuarioId; private String nombre; private String nickname; private String email; private String rol; private String estado; private LocalDate fechaRegistro; }