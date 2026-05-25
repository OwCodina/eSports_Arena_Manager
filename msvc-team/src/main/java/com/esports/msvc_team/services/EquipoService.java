package com.esports.msvc_team.services;
import com.esports.msvc_team.models.MiembroEquipo;
import com.esports.msvc_team.models.dtos.*;
import java.util.List;
public interface EquipoService {

    List<EquipoResponseDTO> findAll();
    List<EquipoResponseDTO> findByEstado(String estado);
    List<EquipoResponseDTO> findByJuegoPrincipalId(Long juegoId);
    List<EquipoResponseDTO> findByCapitanId(Long capitanId);
    EquipoResponseDTO findById(Long id);
    EquipoResponseDTO save(EquipoRequestDTO dto);
    EquipoResponseDTO updateById(Long id, EquipoRequestDTO dto);
    EquipoResponseDTO desactivar(Long id);
    List<MiembroEquipo> findMiembrosByEquipoId(Long equipoId);
    MiembroEquipo agregarMiembro(Long equipoId, MiembroRequestDTO dto);
    void eliminarMiembro(Long miembroId);
    boolean estaActivo(Long equipoId);
}