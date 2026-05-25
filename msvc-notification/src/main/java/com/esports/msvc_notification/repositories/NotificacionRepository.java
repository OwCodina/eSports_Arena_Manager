package com.esports.msvc_notification.repositories;

import com.esports.msvc_notification.models.Notificacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificacionRepository extends JpaRepository<Notificacion, Long> {

    List<Notificacion> findByUsuarioIdOrderByFechaDesc(Long usuarioId);

    List<Notificacion> findByEquipoIdOrderByFechaDesc(Long equipoId);

    List<Notificacion> findByUsuarioIdAndLeidaOrderByFechaDesc(Long usuarioId, Boolean leida);

    List<Notificacion> findByEquipoIdAndLeidaOrderByFechaDesc(Long equipoId, Boolean leida);

    List<Notificacion> findByTipo(String tipo);

    List<Notificacion> findByUsuarioIdAndEstadoOrderByFechaDesc(Long usuarioId, String estado);

    List<Notificacion> findByEquipoIdAndEstadoOrderByFechaDesc(Long equipoId, String estado);
}
