package com.esports.msvc_ranking.services;
import com.esports.msvc_ranking.clients.RegistrationClient;
import com.esports.msvc_ranking.exceptions.RankingException;
import com.esports.msvc_ranking.models.Ranking;
import com.esports.msvc_ranking.models.dtos.*;
import com.esports.msvc_ranking.repositories.RankingRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;
@Service
public class RankingServiceImpl implements RankingService {

    private static final Logger log = LoggerFactory.getLogger(RankingServiceImpl.class);

    @Autowired
    private RankingRepository rankingRepository;

    @Autowired
    private RegistrationClient registrationClient;

    private RankingResponseDTO toResponse(Ranking r) {
        RankingResponseDTO dto = new RankingResponseDTO();
        dto.setRankingId(r.getRankingId()); dto.setTorneoId(r.getTorneoId());
        dto.setParticipanteId(r.getParticipanteId()); dto.setPuntos(r.getPuntos());
        dto.setVictorias(r.getVictorias()); dto.setDerrotas(r.getDerrotas());
        dto.setDiferencia(r.getDiferencia()); dto.setPosicion(r.getPosicion());
        if (r.getAudit() != null) dto.setUpdatedAt(r.getAudit().getUpdatedAt());
        return dto;
    }

    @Transactional(readOnly=true)
    @Override
    public List<RankingResponseDTO> findByTorneoId(Long id) { return rankingRepository.findByTorneoIdOrderByPosicionAsc(id).stream().map(this::toResponse).toList(); }
    @Transactional(readOnly=true) @Override public RankingResponseDTO findByTorneoIdAndParticipanteId(Long t, Long p) { return rankingRepository.findByTorneoIdAndParticipanteId(t, p).map(this::toResponse).orElseThrow(() -> new RankingException("No existe ranking para participanteId: " + p + " en torneoId: " + t)); }
    @Transactional(readOnly=true) @Override public RankingResponseDTO findById(Long id) { return rankingRepository.findById(id).map(this::toResponse).orElseThrow(() -> new RankingException("Ranking con id: " + id + " no encontrado")); }
    @Transactional @Override
    public List<RankingResponseDTO> inicializarRanking(Long torneoId) {
        if (!rankingRepository.findByTorneoIdOrderByPosicionAsc(torneoId).isEmpty()) throw new RankingException("El ranking del torneo id: " + torneoId + " ya fue inicializado.");
        List<InscripcionDTO> inscripciones;
        try { inscripciones = registrationClient.findByTorneoId(torneoId); } catch (Exception e) { throw new RankingException("No se pudo obtener los participantes del torneo."); }
        List<InscripcionDTO> confirmadas = inscripciones.stream().filter(i -> "CONFIRMADA".equals(i.getEstado())).toList();
        if (confirmadas.isEmpty()) throw new RankingException("No hay participantes CONFIRMADOS en el torneo.");
        List<RankingResponseDTO> result = new ArrayList<>();
        int pos = 1;
        for (InscripcionDTO ins : confirmadas) {
            Long pId = "EQUIPO".equals(ins.getTipoParticipante()) ? ins.getEquipoId() : ins.getJugadorId();
            Ranking r = new Ranking(); r.setTorneoId(torneoId); r.setParticipanteId(pId);
            r.setPuntos(0); r.setVictorias(0); r.setDerrotas(0); r.setDiferencia(0); r.setPosicion(pos++);
            result.add(toResponse(rankingRepository.save(r)));
        }
        log.info("Ranking inicializado para torneoId={}, {} participantes", torneoId, result.size());
        return result;
    }
    @Transactional
    @Override
    public RankingResponseDTO actualizarConResultado(Long torneoId, Long ganadorId, Integer pG, Integer pP, Long perdedorId) {
        Ranking g = rankingRepository.findByTorneoIdAndParticipanteId(torneoId, ganadorId).orElseThrow(() -> new RankingException("No existe ranking para ganadorId: " + ganadorId));
        g.setPuntos(g.getPuntos()+3); g.setVictorias(g.getVictorias()+1); g.setDiferencia(g.getDiferencia()+(pG-pP));
        rankingRepository.save(g);
        Ranking p = rankingRepository.findByTorneoIdAndParticipanteId(torneoId, perdedorId).orElseThrow(() -> new RankingException("No existe ranking para perdedorId: " + perdedorId));
        p.setDerrotas(p.getDerrotas()+1); p.setDiferencia(p.getDiferencia()-(pG-pP));
        rankingRepository.save(p);
        log.info("Ranking actualizado: ganador={}, perdedor={}", ganadorId, perdedorId);
        recalcularPosiciones(torneoId);
        return toResponse(rankingRepository.findByTorneoIdAndParticipanteId(torneoId, ganadorId).get());
    }
    @Transactional
    @Override
    public void recalcularPosiciones(Long torneoId) {
        List<Ranking> rankings = rankingRepository.findByTorneoIdOrderByPuntosDescDiferenciaDesc(torneoId);
        for (int i = 0; i < rankings.size(); i++) { rankings.get(i).setPosicion(i+1); rankingRepository.save(rankings.get(i)); }
    }
    @Transactional
    @Override
    public void cerrarRanking(Long torneoId) {
        if (rankingRepository.findByTorneoIdOrderByPosicionAsc(torneoId).isEmpty()) throw new RankingException("No existe ranking para el torneo id: " + torneoId);
        recalcularPosiciones(torneoId);
        log.info("Ranking cerrado para torneoId={}", torneoId);
    }
}