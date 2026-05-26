package com.esports.msvc_ranking.services;

import com.esports.msvc_ranking.clients.RegistrationClient;
import com.esports.msvc_ranking.clients.TournamentClient;
import com.esports.msvc_ranking.exceptions.RankingException;
import com.esports.msvc_ranking.models.Ranking;
import com.esports.msvc_ranking.models.dtos.*;
import com.esports.msvc_ranking.repositories.RankingRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class RankingServiceImpl implements RankingService {

    private static final Logger log = LoggerFactory.getLogger(RankingServiceImpl.class);

    @Autowired private RankingRepository rankingRepository;
    @Autowired private RegistrationClient registrationClient;
    @Autowired private TournamentClient tournamentClient;

    // ── Mapper ───────────────────────────────────────────────────
    private RankingResponseDTO toResponse(Ranking r) {
        RankingResponseDTO dto = new RankingResponseDTO();
        dto.setRankingId(r.getRankingId());
        dto.setTorneoId(r.getTorneoId());
        dto.setParticipanteId(r.getParticipanteId());
        dto.setPuntos(r.getPuntos());
        dto.setVictorias(r.getVictorias());
        dto.setDerrotas(r.getDerrotas());
        dto.setDiferencia(r.getDiferencia());
        dto.setPosicion(r.getPosicion());
        if (r.getAudit() != null) dto.setUpdatedAt(r.getAudit().getUpdatedAt());
        return dto;
    }

    // ── Consultas ────────────────────────────────────────────────
    @Transactional(readOnly = true)
    @Override
    public List<RankingResponseDTO> findByTorneoId(Long torneoId) {
        return rankingRepository.findByTorneoIdOrderByPosicionAsc(torneoId)
                .stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    @Override
    public RankingResponseDTO findByTorneoIdAndParticipanteId(Long torneoId, Long participanteId) {
        return rankingRepository.findByTorneoIdAndParticipanteId(torneoId, participanteId)
                .map(this::toResponse)
                .orElseThrow(() -> new RankingException(
                        "No existe ranking para participanteId: " + participanteId + " en torneoId: " + torneoId));
    }

    @Transactional(readOnly = true)
    @Override
    public RankingResponseDTO findById(Long id) {
        return rankingRepository.findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> new RankingException("Ranking con id: " + id + " no encontrado"));
    }

    // ── Inicializar ──────────────────────────────────────────────
    @Transactional
    @Override
    public List<RankingResponseDTO> inicializarRanking(Long torneoId) {
        // 1. Verificar que el torneo existe en msvc-tournament
        TorneoDTO torneo;
        try {
            torneo = tournamentClient.findById(torneoId);
        } catch (Exception e) {
            log.error("No se pudo verificar el torneo con id={}: {}", torneoId, e.getMessage());
            throw new RankingException("No se pudo verificar el torneo con id: " + torneoId +
                    ". Asegúrese de que msvc-tournament esté activo.");
        }

        // 2. Verificar que el torneo no esté CANCELADO ni BORRADOR
        if ("CANCELADO".equals(torneo.getEstado()) || "BORRADOR".equals(torneo.getEstado())) {
            throw new RankingException(
                    "No se puede inicializar el ranking de un torneo en estado: " + torneo.getEstado());
        }

        // 3. Verificar que el ranking no esté ya inicializado
        if (!rankingRepository.findByTorneoIdOrderByPosicionAsc(torneoId).isEmpty()) {
            throw new RankingException("El ranking del torneo id: " + torneoId + " ya fue inicializado.");
        }

        // 4. Obtener inscripciones CONFIRMADAS desde msvc-registration
        List<InscripcionDTO> inscripciones;
        try {
            inscripciones = registrationClient.findByTorneoId(torneoId);
        } catch (Exception e) {
            log.error("No se pudo obtener los participantes del torneo id={}", torneoId);
            throw new RankingException("No se pudo obtener los participantes del torneo.");
        }

        List<InscripcionDTO> confirmadas = inscripciones.stream()
                .filter(i -> "CONFIRMADA".equals(i.getEstado()))
                .toList();

        if (confirmadas.isEmpty()) {
            throw new RankingException("No hay participantes CONFIRMADOS en el torneo id: " + torneoId);
        }

        // 5. Crear una entrada de ranking por cada participante
        List<RankingResponseDTO> result = new ArrayList<>();
        int pos = 1;
        for (InscripcionDTO ins : confirmadas) {
            Long pId = "EQUIPO".equals(ins.getTipoParticipante()) ? ins.getEquipoId() : ins.getJugadorId();
            Ranking r = new Ranking();
            r.setTorneoId(torneoId);
            r.setParticipanteId(pId);
            r.setPuntos(0); r.setVictorias(0); r.setDerrotas(0); r.setDiferencia(0);
            r.setPosicion(pos++);
            result.add(toResponse(rankingRepository.save(r)));
        }

        log.info("Ranking inicializado: torneo='{}' (id={}), {} participantes",
                torneo.getNombre(), torneoId, result.size());
        return result;
    }

    // ── Actualizar con resultado ─────────────────────────────────
    @Transactional
    @Override
    public RankingResponseDTO actualizarConResultado(Long torneoId, Long ganadorId,
                                                      Integer pG, Integer pP, Long perdedorId) {
        Ranking ganador = rankingRepository.findByTorneoIdAndParticipanteId(torneoId, ganadorId)
                .orElseThrow(() -> new RankingException("No existe ranking para ganadorId: " + ganadorId));

        ganador.setPuntos(ganador.getPuntos() + 3);
        ganador.setVictorias(ganador.getVictorias() + 1);
        ganador.setDiferencia(ganador.getDiferencia() + (pG - pP));
        rankingRepository.save(ganador);

        Ranking perdedor = rankingRepository.findByTorneoIdAndParticipanteId(torneoId, perdedorId)
                .orElseThrow(() -> new RankingException("No existe ranking para perdedorId: " + perdedorId));

        perdedor.setDerrotas(perdedor.getDerrotas() + 1);
        perdedor.setDiferencia(perdedor.getDiferencia() - (pG - pP));
        rankingRepository.save(perdedor);

        log.info("Ranking actualizado: torneo={}, ganador={} (+3pts), perdedor={}", torneoId, ganadorId, perdedorId);
        recalcularPosiciones(torneoId);

        return toResponse(rankingRepository.findByTorneoIdAndParticipanteId(torneoId, ganadorId).get());
    }

    // ── Recalcular posiciones ────────────────────────────────────
    @Transactional
    @Override
    public void recalcularPosiciones(Long torneoId) {
        List<Ranking> rankings = rankingRepository.findByTorneoIdOrderByPuntosDescDiferenciaDesc(torneoId);
        for (int i = 0; i < rankings.size(); i++) {
            rankings.get(i).setPosicion(i + 1);
            rankingRepository.save(rankings.get(i));
        }
        log.info("Posiciones recalculadas para torneoId={}", torneoId);
    }

    // ── Cerrar ranking ───────────────────────────────────────────
    @Transactional
    @Override
    public void cerrarRanking(Long torneoId) {
        // 1. Verificar que el torneo existe y está FINALIZADO
        TorneoDTO torneo;
        try {
            torneo = tournamentClient.findById(torneoId);
        } catch (Exception e) {
            log.error("No se pudo verificar el torneo con id={}: {}", torneoId, e.getMessage());
            throw new RankingException("No se pudo verificar el torneo con id: " + torneoId);
        }

        if (!"FINALIZADO".equals(torneo.getEstado())) {
            throw new RankingException(
                    "Solo se puede cerrar el ranking de un torneo FINALIZADO. Estado actual: " + torneo.getEstado());
        }

        // 2. Verificar que el ranking existe
        if (rankingRepository.findByTorneoIdOrderByPosicionAsc(torneoId).isEmpty()) {
            throw new RankingException("No existe ranking para el torneo id: " + torneoId);
        }

        // 3. Recalcular posiciones finales
        recalcularPosiciones(torneoId);
        log.info("Ranking cerrado: torneo='{}' (id={})", torneo.getNombre(), torneoId);
    }
}
