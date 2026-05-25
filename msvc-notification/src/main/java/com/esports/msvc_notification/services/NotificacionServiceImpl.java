package com.esports.msvc_notification.services;
import com.esports.msvc_notification.exceptions.NotificationException;
import com.esports.msvc_notification.models.Notificacion;
import com.esports.msvc_notification.models.dtos.NotificacionRequestDTO;
import com.esports.msvc_notification.models.dtos.NotificacionResponseDTO;
import com.esports.msvc_notification.repositories.NotificacionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
@Service
public class NotificacionServiceImpl implements NotificacionService {
    private static final Logger log = LoggerFactory.getLogger(NotificacionServiceImpl.class);

    @Autowired
    private NotificacionRepository notificacionRepository;

    private NotificacionResponseDTO toResponse(Notificacion n) {
        NotificacionResponseDTO dto = new NotificacionResponseDTO();
        dto.setNotificacionId(n.getNotificacionId()); dto.setUsuarioId(n.getUsuarioId());
        dto.setEquipoId(n.getEquipoId()); dto.setTipo(n.getTipo()); dto.setMensaje(n.getMensaje());
        dto.setLeida(n.getLeida()); dto.setEstado(n.getEstado()); dto.setFecha(n.getFecha());
        if (n.getAudit() != null) dto.setCreatedAt(n.getAudit().getCreatedAt());
        return dto;
    }

    @Transactional(readOnly=true)
    @Override

    public List<NotificacionResponseDTO> findAll() { return notificacionRepository.findAll().stream().map(this::toResponse).toList(); }
    @Transactional(readOnly=true)
    @Override
    public List<NotificacionResponseDTO> findByUsuarioId(Long id) { return notificacionRepository.findByUsuarioIdOrderByFechaDesc(id).stream().map(this::toResponse).toList(); }

    @Transactional(readOnly=true)
    @Override
    public List<NotificacionResponseDTO> findByEquipoId(Long id) { return notificacionRepository.findByEquipoIdOrderByFechaDesc(id).stream().map(this::toResponse).toList(); }

    @Transactional(readOnly=true)
    @Override
    public List<NotificacionResponseDTO> findNoLeidasByUsuarioId(Long id) { return notificacionRepository.findByUsuarioIdAndLeidaOrderByFechaDesc(id, false).stream().map(this::toResponse).toList(); }

    @Transactional(readOnly=true)
    @Override
    public List<NotificacionResponseDTO> findNoLeidasByEquipoId(Long id) { return notificacionRepository.findByEquipoIdAndLeidaOrderByFechaDesc(id, false).stream().map(this::toResponse).toList(); }

    @Transactional(readOnly=true)
    @Override
    public NotificacionResponseDTO findById(Long id) { return notificacionRepository.findById(id).map(this::toResponse).orElseThrow(() -> new NotificationException("Notificación con id: " + id + " no encontrada")); }

    @Transactional
    @Override
    public NotificacionResponseDTO save(NotificacionRequestDTO dto) {
        if (dto.getUsuarioId() == null && dto.getEquipoId() == null) throw new NotificationException("La notificación debe tener al menos un usuarioId o equipoId.");
        Notificacion n = new Notificacion();
        n.setUsuarioId(dto.getUsuarioId()); n.setEquipoId(dto.getEquipoId());
        n.setTipo(dto.getTipo()); n.setMensaje(dto.getMensaje());
        n.setLeida(false); n.setEstado("ACTIVA"); n.setFecha(LocalDateTime.now());
        Notificacion guardada = notificacionRepository.save(n);
        log.info("Notificación creada: id={}, tipo={}", guardada.getNotificacionId(), guardada.getTipo());
        return toResponse(guardada);
    }
    @Transactional
    @Override
    public NotificacionResponseDTO marcarComoLeida(Long id) {
        return notificacionRepository.findById(id).map(existing -> {
            if (Boolean.TRUE.equals(existing.getLeida())) return toResponse(existing);
            existing.setLeida(true);
            log.info("Notificación leída: id={}", id);
            return toResponse(notificacionRepository.save(existing));
        }).orElseThrow(() -> new NotificationException("Notificación con id: " + id + " no encontrada"));
    }
    @Transactional
    @Override
    public int marcarTodasLeidasByUsuarioId(Long usuarioId) {
        List<Notificacion> noLeidas = notificacionRepository.findByUsuarioIdAndLeidaOrderByFechaDesc(usuarioId, false);
        noLeidas.forEach(n -> n.setLeida(true));
        notificacionRepository.saveAll(noLeidas);
        return noLeidas.size();
    }
    @Transactional
    @Override
    public int marcarTodasLeidasByEquipoId(Long equipoId) {
        List<Notificacion> noLeidas = notificacionRepository.findByEquipoIdAndLeidaOrderByFechaDesc(equipoId, false);
        noLeidas.forEach(n -> n.setLeida(true));
        notificacionRepository.saveAll(noLeidas);
        return noLeidas.size();
    }
    @Transactional
    @Override
    public NotificacionResponseDTO archivar(Long id) {
        return notificacionRepository.findById(id).map(existing -> {
            if ("ARCHIVADA".equals(existing.getEstado())) throw new NotificationException("La notificación ya está ARCHIVADA.");
            existing.setEstado("ARCHIVADA");
            log.info("Notificación archivada: id={}", id);
            return toResponse(notificacionRepository.save(existing));
        }).orElseThrow(() -> new NotificationException("Notificación con id: " + id + " no encontrada"));
    }
}