package com.esports.msvc_sanction.repositories;

import com.esports.msvc_sanction.models.Sancion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository

public interface SancionRepository extends JpaRepository<Sancion, Long> {


    List<Sancion> findByUsuarioId(Long usuarioId);

    List<Sancion> findByEquipoId(Long equipoId);

    List<Sancion> findByEstado(String estado);

    List<Sancion> findByUsuarioIdAndEstado(Long usuarioId, String estado);

    List<Sancion> findByEquipoIdAndEstado(Long equipoId, String estado);

    @Query("SELECT COUNT(s) > 0 FROM Sancion s WHERE s.usuarioId = :usuarioId " +
           "AND s.estado = 'ACTIVA' AND s.fechaFin >= :hoy")
    boolean tieneUsuarioSancionActiva(@Param("usuarioId") Long usuarioId,
                                      @Param("hoy") LocalDate hoy);

    @Query("SELECT COUNT(s) > 0 FROM Sancion s WHERE s.equipoId = :equipoId " +
           "AND s.estado = 'ACTIVA' AND s.fechaFin >= :hoy")
    boolean tieneEquipoSancionActiva(@Param("equipoId") Long equipoId,
                                     @Param("hoy") LocalDate hoy);
}
