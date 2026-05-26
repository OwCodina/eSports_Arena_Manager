package com.esports.msvc_tournament.services;
import com.esports.msvc_tournament.models.dtos.TorneoRequestDTO;
import com.esports.msvc_tournament.models.dtos.TorneoResponseDTO;
import java.util.List;
public interface TorneoService {
    List<TorneoResponseDTO> findAll();
    List<TorneoResponseDTO> findByEstado(String estado);
    List<TorneoResponseDTO> findByJuegoId(Long juegoId);
    List<TorneoResponseDTO> findByJuegoIdAndEstado(Long juegoId, String estado);
    TorneoResponseDTO findById(Long id);
    TorneoResponseDTO save(TorneoRequestDTO dto);
    TorneoResponseDTO updateById(Long id, TorneoRequestDTO dto);
    TorneoResponseDTO cancelar(Long id);
    TorneoResponseDTO cerrar(Long id);
    TorneoResponseDTO abrir(Long id)    ;
    boolean estaAbierto(Long id);
}