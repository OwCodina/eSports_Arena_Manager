package com.esports.msvc_prize.services;
import com.esports.msvc_prize.models.PremioAsignado;
import com.esports.msvc_prize.models.dtos.PremioRequestDTO;
import com.esports.msvc_prize.models.dtos.PremioResponseDTO;
import java.util.List;
public interface PremioService {

    List<PremioResponseDTO> findAll();
    List<PremioResponseDTO> findByTorneoId(Long torneoId);
    PremioResponseDTO findById(Long id);
    PremioResponseDTO save(PremioRequestDTO dto);
    PremioResponseDTO updateById(Long id, PremioRequestDTO dto);
    PremioResponseDTO desactivar(Long id);
    List<PremioAsignado> findAsignacionesByParticipanteId(Long participanteId);
    List<PremioAsignado> asignarPremiosTorneo(Long torneoId);
    PremioAsignado asignarPremio(Long premioId, Long participanteId);
}