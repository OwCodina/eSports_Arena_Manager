package com.esports.msvc_game.services;

import com.esports.msvc_game.models.dtos.JuegoRequestDTO;
import com.esports.msvc_game.models.dtos.JuegoResponseDTO;

import java.util.List;

public interface JuegoService {
    List<JuegoResponseDTO> findAll();
    List<JuegoResponseDTO> findAllActivos();
    JuegoResponseDTO findById(Long id);
    JuegoResponseDTO save(JuegoRequestDTO dto);
    JuegoResponseDTO updateById(Long id, JuegoRequestDTO dto);
    JuegoResponseDTO desactivar(Long id);
}
