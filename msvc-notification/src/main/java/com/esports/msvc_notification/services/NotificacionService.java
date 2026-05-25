package com.esports.msvc_notification.services;
import com.esports.msvc_notification.models.dtos.NotificacionRequestDTO;
import com.esports.msvc_notification.models.dtos.NotificacionResponseDTO;
import java.util.List;
public interface NotificacionService {

    List<NotificacionResponseDTO> findAll();
    List<NotificacionResponseDTO> findByUsuarioId(Long usuarioId);
    List<NotificacionResponseDTO> findByEquipoId(Long equipoId);
    List<NotificacionResponseDTO> findNoLeidasByUsuarioId(Long usuarioId);
    List<NotificacionResponseDTO> findNoLeidasByEquipoId(Long equipoId);
    NotificacionResponseDTO findById(Long id);
    NotificacionResponseDTO save(NotificacionRequestDTO dto);
    NotificacionResponseDTO marcarComoLeida(Long id);

    int marcarTodasLeidasByUsuarioId(Long usuarioId);
    int marcarTodasLeidasByEquipoId(Long equipoId);

    NotificacionResponseDTO archivar(Long id);
}