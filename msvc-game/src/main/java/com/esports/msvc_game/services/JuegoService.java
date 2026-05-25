package com.esports.msvc_game.services;

import com.esports.msvc_game.models.Juego;

import java.util.List;

public interface JuegoService {

    /** Retorna todos los juegos registrados */
    List<Juego> findAll();

    /** Retorna solo los juegos con estado ACTIVO */
    List<Juego> findAllActivos();

    /** Busca un juego por su ID. Lanza GameException si no existe */
    Juego findById(Long id);

    /** Crea un nuevo juego. Valida nombre único y jugadoresPorEquipo > 0 */
    Juego save(Juego juego);

    /** Actualiza modalidad, reglas u otros campos de un juego existente */
    Juego updateById(Long id, Juego juego);

    /** Desactiva un juego (cambia estado a INACTIVO) */
    Juego desactivar(Long id);
}
