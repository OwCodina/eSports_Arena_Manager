package com.esports.msvc_game.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * Entidad que representa un videojuego habilitado para torneos.
 * Ejemplos: Valorant, League of Legends, Rocket League, Counter-Strike.
 */
@Entity
@Table(name = "juegos")
@Getter @Setter @ToString @NoArgsConstructor
public class Juego {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "juego_id")
    private Long juegoId;

    @NotBlank(message = "El nombre del juego no puede estar vacío")
    @Column(nullable = false, unique = true)
    private String nombre;

    @NotBlank(message = "El género no puede estar vacío")
    @Column(nullable = false)
    private String genero;

    @NotBlank(message = "La modalidad no puede estar vacía")
    @Column(nullable = false)
    private String modalidad;

    @NotNull(message = "La cantidad de jugadores por equipo no puede ser nula")
    @Min(value = 1, message = "La cantidad de jugadores por equipo debe ser mayor a 0")
    @Column(name = "jugadores_por_equipo", nullable = false)
    private Integer jugadoresPorEquipo;

    /**
     * Estado del juego: ACTIVO o INACTIVO.
     * Un juego inactivo no permite la creación de nuevos torneos.
     */
    @Column(nullable = false)
    private String estado;

    @Embedded
    private Audit audit = new Audit();
}
