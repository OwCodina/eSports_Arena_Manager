package com.esports.msvc_prize.services;
import com.esports.msvc_prize.clients.RankingClient;
import com.esports.msvc_prize.clients.TournamentClient;
import com.esports.msvc_prize.exceptions.PrizeException;
import com.esports.msvc_prize.models.Premio;
import com.esports.msvc_prize.models.PremioAsignado;
import com.esports.msvc_prize.models.dtos.*;
import com.esports.msvc_prize.repositories.PremioAsignadoRepository;
import com.esports.msvc_prize.repositories.PremioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.*;

@Service

public class PremioServiceImpl implements PremioService {
    private static final Logger log = LoggerFactory.getLogger(PremioServiceImpl.class);

    @Autowired
    private PremioRepository premioRepository;

    @Autowired
    private PremioAsignadoRepository premioAsignadoRepository;

    @Autowired
    private TournamentClient tournamentClient;

    @Autowired
    private RankingClient rankingClient;

    private PremioResponseDTO toResponse(Premio p) {
        PremioResponseDTO dto = new PremioResponseDTO();
        dto.setPremioId(p.getPremioId()); dto.setTorneoId(p.getTorneoId());
        dto.setPosicion(p.getPosicion()); dto.setDescripcion(p.getDescripcion());
        dto.setValor(p.getValor()); dto.setEstado(p.getEstado());
        if (p.getAudit() != null) dto.setCreatedAt(p.getAudit().getCreatedAt());
        return dto;
    }

    @Transactional(readOnly=true)
    @Override
    public List<PremioResponseDTO> findAll() {
        return premioRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly=true)
    @Override
    public List<PremioResponseDTO> findByTorneoId(Long id) {
        return premioRepository.findByTorneoId(id).stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly=true)
    @Override
    public PremioResponseDTO findById(Long id) {
        return premioRepository.findById(id).map(this::toResponse).orElseThrow(
                () -> new PrizeException("Premio con id: " + id + " no encontrado"));
    }

    @Transactional
    @Override
    public PremioResponseDTO save(PremioRequestDTO dto) {
        try { tournamentClient.findById(dto.getTorneoId());
        } catch (Exception e) {
            throw new PrizeException("No se pudo verificar el torneo con id: " + dto.getTorneoId());
        }
        if (premioRepository.findByTorneoIdAndPosicion(dto.getTorneoId(), dto.getPosicion()).isPresent())
            throw new PrizeException("Ya existe un premio para la posición " + dto.getPosicion() + " en el torneo id: " + dto.getTorneoId());

        Premio p = new Premio();
        p.setTorneoId(dto.getTorneoId());
        p.setPosicion(dto.getPosicion());
        p.setDescripcion(dto.getDescripcion());
        p.setValor(dto.getValor());
        p.setEstado("DISPONIBLE");

        Premio guardado = premioRepository.save(p);
        log.info("Premio creado: id={}", guardado.getPremioId());
        return toResponse(guardado);
    }

    @Transactional
    @Override
    public PremioResponseDTO updateById(Long id, PremioRequestDTO dto) {
        return premioRepository.findById(id).map(existing -> {
            if ("ASIGNADO".equals(existing.getEstado()))
                throw new PrizeException("No se puede modificar un premio ASIGNADO.");
            if (dto.getDescripcion() != null) existing.setDescripcion(dto.getDescripcion());
            if (dto.getValor() != null) existing.setValor(dto.getValor());
            return toResponse(premioRepository.save(existing));
        }).orElseThrow(
                () -> new PrizeException("Premio con id: " + id + " no encontrado"));
    }

    @Transactional
    @Override
    public PremioResponseDTO desactivar(Long id) {
        return premioRepository.findById(id).map(existing -> {
            if ("ASIGNADO".equals(existing.getEstado()))
                throw new PrizeException("No se puede desactivar un premio ASIGNADO.");
            existing.setEstado("DESACTIVADO");
            return toResponse(premioRepository.save(existing));
        }).orElseThrow(
                () -> new PrizeException("Premio con id: " + id + " no encontrado"));
    }

    @Transactional(readOnly=true)
    @Override
    public List<PremioAsignado> findAsignacionesByParticipanteId(Long id) {
        return premioAsignadoRepository.findByParticipanteId(id);
    }

    @Transactional
    @Override
    public List<PremioAsignado> asignarPremiosTorneo(Long torneoId) {
        TorneoDTO torneo;
        try { torneo = tournamentClient.findById(torneoId);
        } catch (Exception e) {
            throw new PrizeException("No se pudo verificar el torneo.");
        }
        if (!"FINALIZADO".equals(torneo.getEstado()))
            throw new PrizeException("El torneo debe estar FINALIZADO. Estado actual: " + torneo.getEstado());

        List<RankingDTO> ranking;
        try { ranking = rankingClient.findByTorneoId(torneoId);
        } catch (Exception e) {
            throw new PrizeException("No se pudo obtener el ranking del torneo.");
        }

        if (ranking.isEmpty())
            throw new PrizeException("No existe ranking para el torneo id: " + torneoId);

        List<Premio> disponibles = premioRepository.findByTorneoIdAndEstado(torneoId, "DISPONIBLE");
        if (disponibles.isEmpty())
            throw new PrizeException("No hay premios DISPONIBLES para el torneo.");

        List<PremioAsignado> asignaciones = new ArrayList<>();
        for (Premio premio : disponibles) {
            ranking.stream().filter(r -> r.getPosicion().equals(premio.getPosicion())).findFirst().ifPresent(
                    r -> {
                if (premioAsignadoRepository.findByPremioId(premio.getPremioId()).isEmpty()) {
                    PremioAsignado a = new PremioAsignado();
                    a.setPremioId(premio.getPremioId());
                    a.setParticipanteId(r.getParticipanteId());
                    a.setFechaAsignacion(LocalDateTime.now());
                    asignaciones.add(premioAsignadoRepository.save(a));
                    premio.setEstado("ASIGNADO");
                    premioRepository.save(premio);
                    log.info("Premio asignado: id={}, pos={}, participante={}",
                            premio.getPremioId(), premio.getPosicion(), r.getParticipanteId());
                }
            });
        }
        return asignaciones;
    }
    @Transactional
    @Override
    public PremioAsignado asignarPremio(Long premioId, Long participanteId) {
        Premio premio = premioRepository.findById(premioId).orElseThrow(
                () -> new PrizeException("Premio con id: " + premioId + " no encontrado"));

        try { TorneoDTO t = tournamentClient.findById(premio.getTorneoId());
            if (!"FINALIZADO".equals(t.getEstado()))
                throw new PrizeException("El torneo debe estar FINALIZADO.");
        } catch (PrizeException pe) {
            throw pe;
        } catch (Exception e) {
            throw new PrizeException("No se pudo verificar el torneo.");
        }
        if (!"DISPONIBLE".equals(premio.getEstado()))
            throw new PrizeException("El premio no está DISPONIBLE.");
        if (premioAsignadoRepository.findByPremioId(premioId).isPresent())
            throw new PrizeException("El premio ya fue asignado.");

        PremioAsignado a = new PremioAsignado();
        a.setPremioId(premioId);
        a.setParticipanteId(participanteId);
        a.setFechaAsignacion(LocalDateTime.now());
        PremioAsignado guardada = premioAsignadoRepository.save(a);
        premio.setEstado("ASIGNADO"); premioRepository.save(premio);

        log.info("Premio asignado manualmente: id={}", premioId);
        return guardada;
    }
}