package com.esports.msvc_sanction.services;
import com.esports.msvc_sanction.models.dtos.SancionRequestDTO;
import com.esports.msvc_sanction.models.dtos.SancionResponseDTO;
import java.util.List;

public interface SancionService {

    List<SancionResponseDTO> findAll();
    List<SancionResponseDTO> findByUsuarioId(Long usuarioId);
    List<SancionResponseDTO> findByEquipoId(Long equipoId);
    List<SancionResponseDTO> findByEstado(String estado);
    SancionResponseDTO findById(Long id);
    SancionResponseDTO save(SancionRequestDTO dto);
    SancionResponseDTO updateById(Long id, SancionRequestDTO dto);
    SancionResponseDTO cerrar(Long id);
    boolean tieneUsuarioSancionActiva(Long usuarioId);
    boolean tieneEquipoSancionActiva(Long equipoId);
}