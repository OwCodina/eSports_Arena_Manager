package com.esports.msvc_team.repositories;

import com.esports.msvc_team.models.Equipo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EquipoRepository extends JpaRepository<Equipo, Long> {

    Optional<Equipo> findByNombre(String nombre);

    List<Equipo> findByEstado(String estado);

    List<Equipo> findByJuegoPrincipalId(Long juegoPrincipalId);

    List<Equipo> findByCapitanId(Long capitanId);

    List<Equipo> findByJuegoPrincipalIdAndEstado(Long juegoPrincipalId, String estado);
}
