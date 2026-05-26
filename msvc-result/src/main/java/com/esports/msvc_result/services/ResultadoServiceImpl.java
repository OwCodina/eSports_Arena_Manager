package com.esports.msvc_result.services;
import com.esports.msvc_result.clients.MatchClient;
import com.esports.msvc_result.exceptions.ResultException;
import com.esports.msvc_result.models.Resultado;
import com.esports.msvc_result.models.dtos.*;
import com.esports.msvc_result.repositories.ResultadoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class ResultadoServiceImpl implements ResultadoService {

    private static final Logger log = LoggerFactory.getLogger(ResultadoServiceImpl.class);

    @Autowired
    private ResultadoRepository resultadoRepository;
    @Autowired
    private MatchClient matchClient;

    private ResultadoResponseDTO toResponse(Resultado r) {
        ResultadoResponseDTO dto = new ResultadoResponseDTO();
        dto.setResultadoId(r.getResultadoId()); dto.setPartidaId(r.getPartidaId());
        dto.setGanadorId(r.getGanadorId()); dto.setPuntajeA(r.getPuntajeA());
        dto.setPuntajeB(r.getPuntajeB()); dto.setEstadoValidacion(r.getEstadoValidacion());
        dto.setMotivoAnulacion(r.getMotivoAnulacion()); dto.setFechaRegistro(r.getFechaRegistro());
        if (r.getAudit() != null) dto.setCreatedAt(r.getAudit().getCreatedAt());
        return dto;
    }

    @Transactional(readOnly=true)
    @Override
    public List<ResultadoResponseDTO> findAll() { return resultadoRepository.findAll().stream().map(this::toResponse).toList(); }

    @Transactional(readOnly=true)
    @Override
    public List<ResultadoResponseDTO> findByEstadoValidacion(String e) {
        return resultadoRepository.findByEstadoValidacion(e).stream().map(this::toResponse).toList(); }

    @Transactional(readOnly=true)
    @Override
    public ResultadoResponseDTO findById(Long id) {
        return resultadoRepository.findById(id).map(this::toResponse).orElseThrow(
                () -> new ResultException("Resultado con id: " + id + " no encontrado"));
    }

    @Transactional(readOnly=true)
    @Override
    public ResultadoResponseDTO findByPartidaId(Long pid) {
        return resultadoRepository.findByPartidaId(pid).map(this::toResponse).orElseThrow(
                () -> new ResultException("No existe resultado para la partida con id: " + pid));
    }

    @Transactional
    @Override
    public ResultadoResponseDTO save(ResultadoRequestDTO dto) {
        PartidaDTO partida;
        try { partida = matchClient.findById(dto.getPartidaId());
        } catch (Exception e) {
            throw new ResultException("No se pudo verificar la partida con id: " + dto.getPartidaId());
        }
        if ("CANCELADA".equals(partida.getEstado()))
            throw new ResultException("No se puede registrar resultado de una partida CANCELADA.");
        if (resultadoRepository.findByPartidaId(dto.getPartidaId()).isPresent())
            throw new ResultException("Ya existe un resultado para la partida con id: " + dto.getPartidaId());
        if (!dto.getGanadorId().equals(partida.getParticipanteAId()) && !dto.getGanadorId().equals(partida.getParticipanteBId()))
            throw new ResultException("El ganadorId no corresponde a ningún participante de la partida.");

        Resultado r = new Resultado();
        r.setPartidaId(dto.getPartidaId());
        r.setGanadorId(dto.getGanadorId());
        r.setPuntajeA(dto.getPuntajeA());
        r.setPuntajeB(dto.getPuntajeB());
        r.setEstadoValidacion("PENDIENTE");
        r.setFechaRegistro(LocalDateTime.now());
        Resultado guardado = resultadoRepository.save(r);
        log.info("Resultado registrado: id={}", guardado.getResultadoId());
        return toResponse(guardado);
    }
    @Transactional
    @Override
    public ResultadoResponseDTO updateById(Long id, ResultadoRequestDTO dto) {
        return resultadoRepository.findById(id).map(existing -> {
            if ("VALIDADO".equals(existing.getEstadoValidacion()))
                throw new ResultException("El resultado ya está VALIDADO y no puede modificarse.");
            if ("ANULADO".equals(existing.getEstadoValidacion()))
                throw new ResultException("El resultado está ANULADO y no puede modificarse.");
            if (dto.getGanadorId() != null) existing.setGanadorId(dto.getGanadorId());
            if (dto.getPuntajeA() != null) existing.setPuntajeA(dto.getPuntajeA());
            if (dto.getPuntajeB() != null) existing.setPuntajeB(dto.getPuntajeB());
            return toResponse(resultadoRepository.save(existing));
        }).orElseThrow(
                () -> new ResultException("Resultado con id: " + id + " no encontrado"));
    }

    @Transactional
    @Override
    public ResultadoResponseDTO validar(Long id) {
        return resultadoRepository.findById(id).map(existing -> {
            if ("VALIDADO".equals(existing.getEstadoValidacion()))
                throw new ResultException("El resultado ya está VALIDADO.");
            if ("ANULADO".equals(existing.getEstadoValidacion()))
                throw new ResultException("No se puede validar un resultado ANULADO.");
            existing.setEstadoValidacion("VALIDADO");
            log.info("Resultado VALIDADO: id={}", id);
            return toResponse(resultadoRepository.save(existing));

        }).orElseThrow(
                () -> new ResultException("Resultado con id: " + id + " no encontrado"));
    }

    @Transactional
    @Override
    public ResultadoResponseDTO anular(Long id, String motivo) {
        if (motivo == null || motivo.isBlank())
            throw new ResultException("Se requiere un motivo para anular el resultado.");
        return resultadoRepository.findById(id).map(existing -> {
            if ("ANULADO".equals(existing.getEstadoValidacion()))
                throw new ResultException("El resultado ya está ANULADO.");
            existing.setEstadoValidacion("ANULADO"); existing.setMotivoAnulacion(motivo);
            log.info("Resultado ANULADO: id={}", id);
            return toResponse(resultadoRepository.save(existing));
        }).orElseThrow(
                () -> new ResultException("Resultado con id: " + id + " no encontrado"));
    }
}