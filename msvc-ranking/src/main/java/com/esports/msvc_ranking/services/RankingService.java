package com.esports.msvc_ranking.services;
import com.esports.msvc_ranking.models.dtos.RankingResponseDTO;
import java.util.List;
public interface RankingService {
    List<RankingResponseDTO> findByTorneoId(Long torneoId);
    RankingResponseDTO findByTorneoIdAndParticipanteId(Long torneoId, Long participanteId);
    RankingResponseDTO findById(Long id);
    List<RankingResponseDTO> inicializarRanking(Long torneoId);
    RankingResponseDTO actualizarConResultado(Long torneoId, Long ganadorId, Integer puntajeGanador, Integer puntajePerdedor, Long perdedorId);
    void recalcularPosiciones(Long torneoId);
    void cerrarRanking(Long torneoId);
}