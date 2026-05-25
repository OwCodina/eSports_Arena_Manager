package com.esports.msvc_team.repositories;

import com.esports.msvc_team.models.MiembroEquipo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MiembroEquipoRepository extends JpaRepository<MiembroEquipo, Long> {

    List<MiembroEquipo> findByEquipoId(Long equipoId);

    List<MiembroEquipo> findByUsuarioId(Long usuarioId);

    Optional<MiembroEquipo> findByEquipoIdAndUsuarioId(Long equipoId, Long usuarioId);

    void deleteByEquipoId(Long equipoId);

}
