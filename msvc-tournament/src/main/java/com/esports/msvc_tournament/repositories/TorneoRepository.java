package com.esports.msvc_tournament.repositories;

import com.esports.msvc_tournament.models.Torneo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository

public interface TorneoRepository extends JpaRepository<Torneo, Long> {

    List<Torneo> findByJuegoId(Long juegoId);

    List<Torneo> findByEstado(String estado);

    List<Torneo> findByJuegoIdAndEstado(Long juegoId, String estado);

    List<Torneo> findByFechaInicioAfter(LocalDate fecha);
}
