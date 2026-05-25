package com.esports.msvc_registration.services;
import com.esports.msvc_registration.models.dtos.InscripcionRequestDTO;
import com.esports.msvc_registration.models.dtos.InscripcionResponseDTO;
import java.util.List;
public interface InscripcionService {

    List<InscripcionResponseDTO> findAll();
    List<InscripcionResponseDTO> findByTorneoId(Long torneoId);
    List<InscripcionResponseDTO> findByEquipoId(Long equipoId);
    List<InscripcionResponseDTO> findByJugadorId(Long jugadorId);
    InscripcionResponseDTO findById(Long id);
    InscripcionResponseDTO save(InscripcionRequestDTO dto);
    InscripcionResponseDTO updateEstado(Long id, String nuevoEstado);
    InscripcionResponseDTO cancelar(Long id);
}