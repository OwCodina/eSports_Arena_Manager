package com.esports.msvc_game.repositories;

import com.esports.msvc_game.models.Juego;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface JuegoRepository extends JpaRepository<Juego, Long> {

    /**
     * Busca un juego por nombre exacto (para validar unicidad).
     */
    Optional<Juego> findByNombre(String nombre);

    /**
     * Lista todos los juegos con un estado específico (ej: "ACTIVO").
     */
    List<Juego> findByEstado(String estado);
}
