package com.esports.msvc_match.services;
import com.esports.msvc_match.models.dtos.PartidaRequestDTO;
import com.esports.msvc_match.models.dtos.PartidaResponseDTO;
import java.util.List;
public interface PartidaService {
    List<PartidaResponseDTO> findAll();
    List<PartidaResponseDTO> findByTorneoId(Long torneoId);
    List<PartidaResponseDTO> findByTorneoIdAndRonda(Long torneoId, Integer ronda);
    List<PartidaResponseDTO> findByTorneoIdAndEstado(Long torneoId, String estado);
    PartidaResponseDTO findById(Long id);
    PartidaResponseDTO save(PartidaRequestDTO dto);
    PartidaResponseDTO updateById(Long id, PartidaRequestDTO dto);
    PartidaResponseDTO cancelar(Long id);
}