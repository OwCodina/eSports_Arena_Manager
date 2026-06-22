package com.esports.msvc_auth.models.dtos;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.AllArgsConstructor;


import java.util.Set;


@Getter @Setter @NoArgsConstructor
@AllArgsConstructor

public class AuthAccount {
    private Long cuentaAccesoId;
    private String email;
    private Set<String> roles;
    private String estado;
}
