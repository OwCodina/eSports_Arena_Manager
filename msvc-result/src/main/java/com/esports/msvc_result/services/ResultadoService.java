package com.esports.msvc_result.services;
import com.esports.msvc_result.models.dtos.ResultadoRequestDTO;
import com.esports.msvc_result.models.dtos.ResultadoResponseDTO;
import java.util.List;
public interface ResultadoService {
    List<ResultadoResponseDTO> findAll();
    List<ResultadoResponseDTO> findByEstadoValidacion(String estadoValidacion);
    ResultadoResponseDTO findById(Long id);
    ResultadoResponseDTO findByPartidaId(Long partidaId);
    ResultadoResponseDTO save(ResultadoRequestDTO dto);
    ResultadoResponseDTO updateById(Long id, ResultadoRequestDTO dto);
    ResultadoResponseDTO validar(Long id);
    ResultadoResponseDTO anular(Long id, String motivo);
}