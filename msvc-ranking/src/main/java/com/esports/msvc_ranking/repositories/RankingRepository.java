package com.esports.msvc_ranking.repositories;

import com.esports.msvc_ranking.models.Ranking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RankingRepository extends JpaRepository<Ranking, Long> {


    List<Ranking> findByTorneoIdOrderByPosicionAsc(Long torneoId);


    Optional<Ranking> findByTorneoIdAndParticipanteId(Long torneoId, Long participanteId);


    List<Ranking> findByTorneoIdOrderByPuntosDescDiferenciaDesc(Long torneoId);


    void deleteByTorneoId(Long torneoId);
}
