package com.esports.msvc_prize.repositories;

import com.esports.msvc_prize.models.PremioAsignado;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PremioAsignadoRepository extends JpaRepository<PremioAsignado, Long> {

    List<PremioAsignado> findByParticipanteId(Long participanteId);

    Optional<PremioAsignado> findByPremioId(Long premioId);
}
