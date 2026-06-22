package com.esports.msvc_user.services;

import com.esports.msvc_user.models.dtos.UsuarioRequestDTO;
import com.esports.msvc_user.models.dtos.UsuarioResponseDTO;

import java.util.List;

public interface UsuarioService {
    List<UsuarioResponseDTO> findAll();
    List<UsuarioResponseDTO> findByRol(String rol);
    List<UsuarioResponseDTO> findByEstado(String estado);
    List<UsuarioResponseDTO> findByRolAndEstado(String rol, String estado);
    UsuarioResponseDTO findById(Long id);
    UsuarioResponseDTO findByNickname(String nickname);
    UsuarioResponseDTO save(UsuarioRequestDTO dto);
    UsuarioResponseDTO updateById(Long id, UsuarioRequestDTO dto);
    UsuarioResponseDTO desactivar(Long id);
    boolean puedeCompetir(Long id);
}
