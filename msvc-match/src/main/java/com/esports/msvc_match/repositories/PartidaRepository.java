package com.esports.msvc_match.repositories;

import com.esports.msvc_match.models.Partida;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PartidaRepository extends JpaRepository<Partida, Long> {

    /** Lista partidas de un torneo */
    List<Partida> findByTorneoId(Long torneoId);

    /** Lista partidas de un torneo por ronda */
    List<Partida> findByTorneoIdAndRonda(Long torneoId, Integer ronda);

    /** Lista partidas por estado */
    List<Partida> findByTorneoIdAndEstado(Long torneoId, String estado);

    /**
     * Busca enfrentamiento duplicado entre dos participantes en la misma ronda de un torneo.
     * Verifica ambas combinaciones A vs B y B vs A.
     */
    Optional<Partida> findByTorneoIdAndParticipanteAIdAndParticipanteBIdAndRonda(
            Long torneoId, Long participanteAId, Long participanteBId, Integer ronda);

    Optional<Partida> findByTorneoIdAndParticipanteAIdAndRondaAndParticipanteBId(
            Long torneoId, Long participanteAId, Integer ronda, Long participanteBId);
}
