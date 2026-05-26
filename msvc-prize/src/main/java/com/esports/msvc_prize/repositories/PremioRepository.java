package com.esports.msvc_prize.repositories;

import com.esports.msvc_prize.models.Premio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PremioRepository extends JpaRepository<Premio, Long> {

    List<Premio> findByTorneoId(Long torneoId);

    List<Premio> findByTorneoIdAndEstado(Long torneoId, String estado);

    Optional<Premio> findByTorneoIdAndPosicion(Long torneoId, Integer posicion);
}
