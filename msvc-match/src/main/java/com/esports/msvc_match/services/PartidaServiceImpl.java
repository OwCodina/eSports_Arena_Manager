package com.esports.msvc_match.services;
import com.esports.msvc_match.clients.RegistrationClient;
import com.esports.msvc_match.clients.TournamentClient;
import com.esports.msvc_match.exceptions.MatchException;
import com.esports.msvc_match.models.Partida;
import com.esports.msvc_match.models.dtos.*;
import com.esports.msvc_match.repositories.PartidaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
@Service
public class PartidaServiceImpl implements PartidaService {
    private static final Logger log = LoggerFactory.getLogger(PartidaServiceImpl.class);
    @Autowired private PartidaRepository partidaRepository;
    @Autowired private TournamentClient tournamentClient;
    @Autowired private RegistrationClient registrationClient;

    private PartidaResponseDTO toResponse(Partida p) {
        PartidaResponseDTO dto = new PartidaResponseDTO();
        dto.setPartidaId(p.getPartidaId()); dto.setTorneoId(p.getTorneoId());
        dto.setParticipanteAId(p.getParticipanteAId()); dto.setParticipanteBId(p.getParticipanteBId());
        dto.setRonda(p.getRonda()); dto.setFechaHora(p.getFechaHora()); dto.setEstado(p.getEstado());
        if (p.getAudit() != null) dto.setCreatedAt(p.getAudit().getCreatedAt());
        return dto;
    }

    @Transactional(readOnly=true) @Override public List<PartidaResponseDTO> findAll() { return partidaRepository.findAll().stream().map(this::toResponse).toList(); }
    @Transactional(readOnly=true) @Override public List<PartidaResponseDTO> findByTorneoId(Long id) { return partidaRepository.findByTorneoId(id).stream().map(this::toResponse).toList(); }
    @Transactional(readOnly=true) @Override public List<PartidaResponseDTO> findByTorneoIdAndRonda(Long id, Integer r) { return partidaRepository.findByTorneoIdAndRonda(id, r).stream().map(this::toResponse).toList(); }
    @Transactional(readOnly=true) @Override public List<PartidaResponseDTO> findByTorneoIdAndEstado(Long id, String e) { return partidaRepository.findByTorneoIdAndEstado(id, e).stream().map(this::toResponse).toList(); }
    @Transactional(readOnly=true) @Override
    public PartidaResponseDTO findById(Long id) {
        return partidaRepository.findById(id).map(this::toResponse).orElseThrow(() -> new MatchException("Partida con id: " + id + " no encontrada"));
    }
    @Transactional @Override
    public PartidaResponseDTO save(PartidaRequestDTO dto) {
        if (dto.getParticipanteAId().equals(dto.getParticipanteBId())) throw new MatchException("Los dos participantes no pueden ser el mismo.");
        TorneoDTO torneo;
        try { torneo = tournamentClient.findById(dto.getTorneoId()); } catch (Exception e) { throw new MatchException("No se pudo verificar el torneo con id: " + dto.getTorneoId()); }
        if ("CANCELADO".equals(torneo.getEstado()) || "BORRADOR".equals(torneo.getEstado())) throw new MatchException("No se pueden crear partidas para un torneo en estado: " + torneo.getEstado());
        List<InscripcionDTO> inscripciones;
        try { inscripciones = registrationClient.findByTorneoId(dto.getTorneoId()); } catch (Exception e) { throw new MatchException("No se pudo verificar las inscripciones del torneo."); }
        boolean aInscrito = inscripciones.stream().filter(i -> "CONFIRMADA".equals(i.getEstado())).anyMatch(i -> dto.getParticipanteAId().equals(i.getEquipoId()) || dto.getParticipanteAId().equals(i.getJugadorId()));
        boolean bInscrito = inscripciones.stream().filter(i -> "CONFIRMADA".equals(i.getEstado())).anyMatch(i -> dto.getParticipanteBId().equals(i.getEquipoId()) || dto.getParticipanteBId().equals(i.getJugadorId()));
        if (!aInscrito) throw new MatchException("El participante A no tiene inscripción CONFIRMADA en el torneo.");
        if (!bInscrito) throw new MatchException("El participante B no tiene inscripción CONFIRMADA en el torneo.");
        boolean dup = partidaRepository.findByTorneoIdAndParticipanteAIdAndParticipanteBIdAndRonda(dto.getTorneoId(), dto.getParticipanteAId(), dto.getParticipanteBId(), dto.getRonda()).isPresent()
                   || partidaRepository.findByTorneoIdAndParticipanteAIdAndRondaAndParticipanteBId(dto.getTorneoId(), dto.getParticipanteBId(), dto.getRonda(), dto.getParticipanteAId()).isPresent();
        if (dup) throw new MatchException("Ya existe un enfrentamiento entre estos participantes en la ronda " + dto.getRonda());
        Partida p = new Partida();
        p.setTorneoId(dto.getTorneoId()); p.setParticipanteAId(dto.getParticipanteAId());
        p.setParticipanteBId(dto.getParticipanteBId()); p.setRonda(dto.getRonda());
        p.setFechaHora(dto.getFechaHora()); p.setEstado("PROGRAMADA");
        Partida guardada = partidaRepository.save(p);
        log.info("Partida creada: id={}, ronda={}", guardada.getPartidaId(), guardada.getRonda());
        return toResponse(guardada);
    }
    @Transactional @Override
    public PartidaResponseDTO updateById(Long id, PartidaRequestDTO dto) {
        return partidaRepository.findById(id).map(existing -> {
            if ("CANCELADA".equals(existing.getEstado()) || "FINALIZADA".equals(existing.getEstado())) throw new MatchException("No se puede modificar una partida con estado: " + existing.getEstado());
            if (dto.getFechaHora() != null) existing.setFechaHora(dto.getFechaHora());
            return toResponse(partidaRepository.save(existing));
        }).orElseThrow(() -> new MatchException("Partida con id: " + id + " no encontrada"));
    }
    @Transactional @Override
    public PartidaResponseDTO cancelar(Long id) {
        return partidaRepository.findById(id).map(existing -> {
            if ("FINALIZADA".equals(existing.getEstado())) throw new MatchException("No se puede cancelar una partida FINALIZADA.");
            existing.setEstado("CANCELADA");
            log.info("Partida cancelada: id={}", id);
            return toResponse(partidaRepository.save(existing));
        }).orElseThrow(() -> new MatchException("Partida con id: " + id + " no encontrada"));
    }
}