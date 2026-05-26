package com.esports.msvc_tournament.services;
import com.esports.msvc_tournament.clients.GameClient;
import com.esports.msvc_tournament.exceptions.TournamentException;
import com.esports.msvc_tournament.models.Torneo;
import com.esports.msvc_tournament.models.dtos.*;
import com.esports.msvc_tournament.repositories.TorneoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service

public class TorneoServiceImpl implements TorneoService {

    private static final Logger log = LoggerFactory.getLogger(TorneoServiceImpl.class);

    @Autowired
    private TorneoRepository torneoRepository;

    @Autowired
    private GameClient gameClient;

    private TorneoResponseDTO toResponse(Torneo t) {
        TorneoResponseDTO dto = new TorneoResponseDTO();
        dto.setTorneoId(t.getTorneoId()); dto.setNombre(t.getNombre());
        dto.setJuegoId(t.getJuegoId()); dto.setFechaInicio(t.getFechaInicio());
        dto.setFechaFin(t.getFechaFin()); dto.setFechaCierreInscripcion(t.getFechaCierreInscripcion());
        dto.setCupoMaximo(t.getCupoMaximo()); dto.setEstado(t.getEstado());
        dto.setModalidad(t.getModalidad());
        if (t.getAudit() != null) { dto.setCreatedAt(t.getAudit().getCreatedAt()); dto.setUpdatedAt(t.getAudit().getUpdatedAt()); }
        return dto;
    }

    @Transactional(readOnly = true)
    @Override
    public List<TorneoResponseDTO> findAll() {
        return torneoRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    @Override
    public List<TorneoResponseDTO> findByEstado(String estado) {
        return torneoRepository.findByEstado(estado).stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    @Override
    public List<TorneoResponseDTO> findByJuegoId(Long juegoId) {
        return torneoRepository.findByJuegoId(juegoId).stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    @Override
    public List<TorneoResponseDTO> findByJuegoIdAndEstado(Long juegoId, String estado) {
        return torneoRepository.findByJuegoIdAndEstado(juegoId, estado).stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    @Override
    public TorneoResponseDTO findById(Long id) {
        return torneoRepository.findById(id).map(this::toResponse).orElseThrow(
                () -> new TournamentException("Torneo con id: " + id + " no encontrado"));
    }

    @Transactional
    @Override
    public TorneoResponseDTO save(TorneoRequestDTO dto) {
        JuegoDTO juego;
        try { juego = gameClient.findById(dto.getJuegoId());
        } catch (Exception e) {
            throw new TournamentException("No se pudo verificar el juego con id: " + dto.getJuegoId());
        }
        if (!"ACTIVO".equals(juego.getEstado()))
            throw new TournamentException("El juego '" + juego.getNombre() + "' está INACTIVO.");

        if (!dto.getFechaCierreInscripcion().isBefore(dto.getFechaInicio()))
            throw new TournamentException("La fecha de cierre de inscripción debe ser anterior a la fecha de inicio.");

        if (!dto.getFechaFin().isAfter(dto.getFechaInicio()))
            throw new TournamentException("La fecha de fin debe ser posterior a la fecha de inicio.");

        Torneo t = new Torneo();
        t.setNombre(dto.getNombre());
        t.setJuegoId(dto.getJuegoId());
        t.setFechaInicio(dto.getFechaInicio());
        t.setFechaFin(dto.getFechaFin());
        t.setFechaCierreInscripcion(dto.getFechaCierreInscripcion());
        t.setCupoMaximo(dto.getCupoMaximo());
        t.setModalidad(dto.getModalidad());
        t.setEstado("BORRADOR");

        Torneo guardado = torneoRepository.save(t);

        log.info("Torneo creado: id={}", guardado.getTorneoId());
        return toResponse(guardado);
    }

    @Transactional
    @Override
    public TorneoResponseDTO updateById(Long id, TorneoRequestDTO dto) {
        return torneoRepository.findById(id).map(existing -> {
            if ("EN_CURSO".equals(existing.getEstado()))
                throw new TournamentException("No se pueden modificar las reglas de un torneo EN_CURSO.");
            if (dto.getNombre() != null) existing.setNombre(dto.getNombre());
            if (dto.getFechaInicio() != null) existing.setFechaInicio(dto.getFechaInicio());
            if (dto.getFechaFin() != null) existing.setFechaFin(dto.getFechaFin());
            if (dto.getFechaCierreInscripcion() != null) existing.setFechaCierreInscripcion(dto.getFechaCierreInscripcion());
            if (dto.getCupoMaximo() != null) existing.setCupoMaximo(dto.getCupoMaximo());
            if (dto.getModalidad() != null) existing.setModalidad(dto.getModalidad());
            log.info("Torneo actualizado: id={}", id);
            return toResponse(torneoRepository.save(existing));
        }).orElseThrow(() -> new TournamentException("Torneo con id: " + id + " no encontrado"));
    }

    @Transactional
    @Override
    public TorneoResponseDTO cancelar(Long id) {
        return torneoRepository.findById(id).map(existing -> {
            if ("FINALIZADO".equals(existing.getEstado()))
                throw new TournamentException("No se puede cancelar un torneo FINALIZADO.");
            existing.setEstado("CANCELADO");
            return toResponse(torneoRepository.save(existing));
        }).orElseThrow(() -> new TournamentException("Torneo con id: " + id + " no encontrado"));
    }

    @Transactional
    @Override
    public TorneoResponseDTO abrir(Long id) {
        return torneoRepository.findById(id).map(existing -> {
            if ("ABIERTO".equals(existing.getEstado()) || "CANCELADO".equals(existing.getEstado()))
                throw new TournamentException("No se puede abrir un torneo con estado: " + existing.getEstado());
            existing.setEstado("ABIERTO");
            return toResponse(torneoRepository.save(existing));
        }).orElseThrow(() -> new TournamentException("Torneo con id: " + id + " no encontrado"));
    }

    @Transactional
    @Override
    public TorneoResponseDTO cerrar(Long id) {
        return torneoRepository.findById(id).map(existing -> {
            if ("CANCELADO".equals(existing.getEstado()) || "CERRADO".equals(existing.getEstado()))
                throw new TournamentException("No se puede cerrar un torneo con estado: " + existing.getEstado());
            existing.setEstado("CERRADO");
            return toResponse(torneoRepository.save(existing));
        }).orElseThrow(() -> new TournamentException("Torneo con id: " + id + " no encontrado"));
    }

    @Transactional(readOnly = true)
    @Override
    public boolean estaAbierto(Long id) {
        Torneo t = torneoRepository.findById(id).orElseThrow(() -> new TournamentException("Torneo con id: " + id + " no encontrado"));
        return "ABIERTO".equals(t.getEstado());
    }
}