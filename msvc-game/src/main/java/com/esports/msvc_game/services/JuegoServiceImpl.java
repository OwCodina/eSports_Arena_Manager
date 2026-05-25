package com.esports.msvc_game.services;

import com.esports.msvc_game.exceptions.GameException;
import com.esports.msvc_game.models.Juego;
import com.esports.msvc_game.repositories.JuegoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class JuegoServiceImpl implements JuegoService {

    private static final Logger log = LoggerFactory.getLogger(JuegoServiceImpl.class);

    @Autowired
    private JuegoRepository juegoRepository;

    @Transactional(readOnly = true)
    @Override
    public List<Juego> findAll() {
        log.info("Listando todos los juegos");
        return this.juegoRepository.findAll();
    }

    @Transactional(readOnly = true)
    @Override
    public List<Juego> findAllActivos() {
        log.info("Listando juegos con estado ACTIVO");
        return this.juegoRepository.findByEstado("ACTIVO");
    }

    @Transactional(readOnly = true)
    @Override
    public Juego findById(Long id) {
        return this.juegoRepository.findById(id).orElseThrow(() -> {
            log.error("Juego con id {} no encontrado", id);
            return new GameException("Juego con id: " + id + " no encontrado");
        });
    }

    @Transactional
    @Override
    public Juego save(Juego juego) {
        // Regla: nombre único
        if (this.juegoRepository.findByNombre(juego.getNombre()).isPresent()) {
            log.warn("Intento de crear juego con nombre duplicado: {}", juego.getNombre());
            throw new GameException("Ya existe un juego con el nombre: " + juego.getNombre());
        }
        // Regla: jugadoresPorEquipo > 0 (ya validado por @Min, pero doble check explícito)
        if (juego.getJugadoresPorEquipo() == null || juego.getJugadoresPorEquipo() < 1) {
            throw new GameException("La cantidad de jugadores por equipo debe ser mayor a 0");
        }
        juego.setEstado("ACTIVO");
        Juego guardado = this.juegoRepository.save(juego);
        log.info("Juego creado: id={}, nombre={}", guardado.getJuegoId(), guardado.getNombre());
        return guardado;
    }

    @Transactional
    @Override
    public Juego updateById(Long id, Juego juego) {
        return this.juegoRepository.findById(id).map(existing -> {
            existing.setModalidad(juego.getModalidad());
            existing.setGenero(juego.getGenero());
            existing.setJugadoresPorEquipo(juego.getJugadoresPorEquipo());
            // El nombre solo se actualiza si viene distinto y no existe ya
            if (juego.getNombre() != null && !juego.getNombre().equals(existing.getNombre())) {
                if (this.juegoRepository.findByNombre(juego.getNombre()).isPresent()) {
                    throw new GameException("Ya existe un juego con el nombre: " + juego.getNombre());
                }
                existing.setNombre(juego.getNombre());
            }
            Juego actualizado = this.juegoRepository.save(existing);
            log.info("Juego actualizado: id={}, nombre={}", actualizado.getJuegoId(), actualizado.getNombre());
            return actualizado;
        }).orElseThrow(() -> {
            log.error("Intento de actualizar juego inexistente, id={}", id);
            return new GameException("Juego con id: " + id + " no encontrado");
        });
    }

    @Transactional
    @Override
    public Juego desactivar(Long id) {
        return this.juegoRepository.findById(id).map(existing -> {
            // Regla: juego inactivo no permite nuevos torneos (el estado lo bloquea en tournament-service)
            existing.setEstado("INACTIVO");
            Juego desactivado = this.juegoRepository.save(existing);
            log.info("Juego desactivado: id={}, nombre={}", desactivado.getJuegoId(), desactivado.getNombre());
            return desactivado;
        }).orElseThrow(() -> {
            log.error("Intento de desactivar juego inexistente, id={}", id);
            return new GameException("Juego con id: " + id + " no encontrado");
        });
    }
}
