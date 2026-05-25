package com.esports.msvc_game.services;

import com.esports.msvc_game.exceptions.GameException;
import com.esports.msvc_game.models.Juego;
import com.esports.msvc_game.models.dtos.JuegoRequestDTO;
import com.esports.msvc_game.models.dtos.JuegoResponseDTO;
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

    // ── Mapper ───────────────────────────────────────────────────
    private JuegoResponseDTO toResponse(Juego j) {
        JuegoResponseDTO dto = new JuegoResponseDTO();
        dto.setJuegoId(j.getJuegoId());
        dto.setNombre(j.getNombre());
        dto.setGenero(j.getGenero());
        dto.setModalidad(j.getModalidad());
        dto.setJugadoresPorEquipo(j.getJugadoresPorEquipo());
        dto.setEstado(j.getEstado());
        if (j.getAudit() != null) {
            dto.setCreatedAt(j.getAudit().getCreatedAt());
            dto.setUpdatedAt(j.getAudit().getUpdatedAt());
        }
        return dto;
    }

    // ── Service methods ──────────────────────────────────────────
    @Transactional(readOnly = true)
    @Override
    public List<JuegoResponseDTO> findAll() {
        log.info("Listando todos los juegos");
        return this.juegoRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    @Override
    public List<JuegoResponseDTO> findAllActivos() {
        log.info("Listando juegos con estado ACTIVO");
        return this.juegoRepository.findByEstado("ACTIVO").stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    @Override
    public JuegoResponseDTO findById(Long id) {
        return this.juegoRepository.findById(id).map(this::toResponse).orElseThrow(() -> {
            log.error("Juego con id {} no encontrado", id);
            return new GameException("Juego con id: " + id + " no encontrado");
        });
    }

    @Transactional
    @Override
    public JuegoResponseDTO save(JuegoRequestDTO dto) {
        if (this.juegoRepository.findByNombre(dto.getNombre()).isPresent()) {
            log.warn("Intento de crear juego con nombre duplicado: {}", dto.getNombre());
            throw new GameException("Ya existe un juego con el nombre: " + dto.getNombre());
        }
        Juego juego = new Juego();
        juego.setNombre(dto.getNombre());
        juego.setGenero(dto.getGenero());
        juego.setModalidad(dto.getModalidad());
        juego.setJugadoresPorEquipo(dto.getJugadoresPorEquipo());
        juego.setEstado("ACTIVO");
        Juego guardado = this.juegoRepository.save(juego);
        log.info("Juego creado: id={}, nombre={}", guardado.getJuegoId(), guardado.getNombre());
        return toResponse(guardado);
    }

    @Transactional
    @Override
    public JuegoResponseDTO updateById(Long id, JuegoRequestDTO dto) {
        return this.juegoRepository.findById(id).map(existing -> {
            if (dto.getNombre() != null && !dto.getNombre().equals(existing.getNombre())) {
                if (this.juegoRepository.findByNombre(dto.getNombre()).isPresent()) {
                    throw new GameException("Ya existe un juego con el nombre: " + dto.getNombre());
                }
                existing.setNombre(dto.getNombre());
            }
            if (dto.getGenero() != null)             existing.setGenero(dto.getGenero());
            if (dto.getModalidad() != null)           existing.setModalidad(dto.getModalidad());
            if (dto.getJugadoresPorEquipo() != null)  existing.setJugadoresPorEquipo(dto.getJugadoresPorEquipo());
            Juego actualizado = this.juegoRepository.save(existing);
            log.info("Juego actualizado: id={}", actualizado.getJuegoId());
            return toResponse(actualizado);
        }).orElseThrow(() -> new GameException("Juego con id: " + id + " no encontrado"));
    }

    @Transactional
    @Override
    public JuegoResponseDTO desactivar(Long id) {
        return this.juegoRepository.findById(id).map(existing -> {
            existing.setEstado("INACTIVO");
            Juego desactivado = this.juegoRepository.save(existing);
            log.info("Juego desactivado: id={}", desactivado.getJuegoId());
            return toResponse(desactivado);
        }).orElseThrow(() -> new GameException("Juego con id: " + id + " no encontrado"));
    }
}
