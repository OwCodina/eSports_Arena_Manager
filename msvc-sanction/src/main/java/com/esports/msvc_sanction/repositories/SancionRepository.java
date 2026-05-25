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

    /** Lista sanciones por usuario */
    List<Sancion> findByUsuarioId(Long usuarioId);

    /** Lista sanciones por equipo */
    List<Sancion> findByEquipoId(Long equipoId);

    /** Lista sanciones por estado (ACTIVA, CERRADA) */
    List<Sancion> findByEstado(String estado);

    /** Lista sanciones de un usuario con un estado específico */
    List<Sancion> findByUsuarioIdAndEstado(Long usuarioId, String estado);

    /** Lista sanciones de un equipo con un estado específico */
    List<Sancion> findByEquipoIdAndEstado(Long equipoId, String estado);

    /**
     * Verifica si un usuario tiene alguna sanción ACTIVA que no haya vencido.
     * Usado por registration-service para bloquear inscripciones.
     */
    @Query("SELECT COUNT(s) > 0 FROM Sancion s WHERE s.usuarioId = :usuarioId " +
           "AND s.estado = 'ACTIVA' AND s.fechaFin >= :hoy")
    boolean tieneUsuarioSancionActiva(@Param("usuarioId") Long usuarioId,
                                      @Param("hoy") LocalDate hoy);

    /**
     * Verifica si un equipo tiene alguna sanción ACTIVA que no haya vencido.
     */
    @Query("SELECT COUNT(s) > 0 FROM Sancion s WHERE s.equipoId = :equipoId " +
           "AND s.estado = 'ACTIVA' AND s.fechaFin >= :hoy")
    boolean tieneEquipoSancionActiva(@Param("equipoId") Long equipoId,
                                     @Param("hoy") LocalDate hoy);
}
