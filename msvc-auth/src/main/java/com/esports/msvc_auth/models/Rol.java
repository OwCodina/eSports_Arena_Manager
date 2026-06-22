package com.esports.msvc_auth.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Entity
@Table(name = "roles")
@Getter @Setter @NoArgsConstructor
public class Rol {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "rol_id")
    private Long rolId;

    @Column(unique = true, nullable = false)
    private String nombre;

    public Rol(String nombre) {
        this.nombre = nombre;
    }
}
