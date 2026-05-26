package com.esports.msvc_sanction.services;
import com.esports.msvc_sanction.clients.UserClient;
import com.esports.msvc_sanction.exceptions.SanctionException;
import com.esports.msvc_sanction.models.Sancion;
import com.esports.msvc_sanction.models.dtos.SancionRequestDTO;
import com.esports.msvc_sanction.models.dtos.SancionResponseDTO;
import com.esports.msvc_sanction.repositories.SancionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.util.List;

@Service
public class SancionServiceImpl implements SancionService {
    private static final Logger log = LoggerFactory.getLogger(SancionServiceImpl.class);

    @Autowired
    private SancionRepository sancionRepository;

    @Autowired
    private UserClient userClient;

    private SancionResponseDTO toResponse(Sancion s) {
        SancionResponseDTO dto = new SancionResponseDTO();
        dto.setSancionId(s.getSancionId()); dto.setUsuarioId(s.getUsuarioId());
        dto.setEquipoId(s.getEquipoId()); dto.setMotivo(s.getMotivo());
        dto.setFechaInicio(s.getFechaInicio()); dto.setFechaFin(s.getFechaFin());
        dto.setEstado(s.getEstado()); dto.setSeveridad(s.getSeveridad());
        if (s.getAudit() != null) { dto.setCreatedAt(s.getAudit().getCreatedAt());
            dto.setUpdatedAt(s.getAudit().getUpdatedAt());
        }
        return dto;
    }

    @Transactional(readOnly=true)
    @Override
    public List<SancionResponseDTO> findAll() {
        return sancionRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly=true)
    @Override
    public List<SancionResponseDTO> findByUsuarioId(Long id) {
        return sancionRepository.findByUsuarioId(id).stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly=true)
    @Override
    public List<SancionResponseDTO> findByEquipoId(Long id) {
        return sancionRepository.findByEquipoId(id).stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly=true)
    @Override
    public List<SancionResponseDTO> findByEstado(String estado) {
        return sancionRepository.findByEstado(estado).stream().map(this::toResponse).toList();
    }
    @Transactional(readOnly=true)
    @Override
    public SancionResponseDTO findById(Long id) {
        return sancionRepository.findById(id).map(this::toResponse).orElseThrow(
                () -> new SanctionException("Sanción con id: " + id + " no encontrada"));
    }

    @Transactional
    @Override
    public SancionResponseDTO save(SancionRequestDTO dto) {
        if (dto.getUsuarioId() == null && dto.getEquipoId() == null)
            throw new SanctionException("La sanción debe tener al menos un usuarioId o equipoId.");
        if (!dto.getFechaFin().isAfter(dto.getFechaInicio()))
            throw new SanctionException("La fecha de fin debe ser posterior a la fecha de inicio.");
        if (dto.getUsuarioId() != null) {
            try { userClient.findById(dto.getUsuarioId()); }
            catch (Exception e) {
                throw new SanctionException("No se pudo verificar el usuario con id: " + dto.getUsuarioId());
            }
        }

        Sancion s = new Sancion();
        s.setUsuarioId(dto.getUsuarioId());
        s.setEquipoId(dto.getEquipoId());
        s.setMotivo(dto.getMotivo());
        s.setFechaInicio(dto.getFechaInicio());
        s.setFechaFin(dto.getFechaFin());
        s.setSeveridad(dto.getSeveridad());
        s.setEstado("ACTIVA");
        Sancion guardada = sancionRepository.save(s);

        log.info("Sanción creada: id={}", guardada.getSancionId());
        return toResponse(guardada);
    }

    @Transactional
    @Override
    public SancionResponseDTO updateById(Long id, SancionRequestDTO dto) {
        return sancionRepository.findById(id).map(existing -> {
            if ("CERRADA".equals(existing.getEstado()))
                throw new SanctionException("No se puede modificar una sanción CERRADA.");
            LocalDate inicio = dto.getFechaInicio() != null ? dto.getFechaInicio() : existing.getFechaInicio();
            LocalDate fin    = dto.getFechaFin()    != null ? dto.getFechaFin()    : existing.getFechaFin();

            if (!fin.isAfter(inicio))
                throw new SanctionException("La fecha de fin debe ser posterior a la fecha de inicio.");
            if (dto.getMotivo() != null)
                existing.setMotivo(dto.getMotivo());
            if (dto.getSeveridad() != null)
                existing.setSeveridad(dto.getSeveridad());
            existing.setFechaInicio(inicio);
            existing.setFechaFin(fin);
            return toResponse(sancionRepository.save(existing));
        }).orElseThrow(
                () -> new SanctionException("Sanción con id: " + id + " no encontrada"));
    }

    @Transactional
    @Override
    public SancionResponseDTO cerrar(Long id) {
        return sancionRepository.findById(id).map(existing -> {
            if ("CERRADA".equals(existing.getEstado()))
                throw new SanctionException("La sanción con id: " + id + " ya está CERRADA.");
            existing.setEstado("CERRADA");
            log.info("Sanción cerrada: id={}", id);
            return toResponse(sancionRepository.save(existing));
        }).orElseThrow(
                () -> new SanctionException("Sanción con id: " + id + " no encontrada"));
    }

    @Transactional(readOnly=true)
    @Override
    public boolean tieneUsuarioSancionActiva(Long usuarioId) {
        return sancionRepository.tieneUsuarioSancionActiva(usuarioId, LocalDate.now());
    }

    @Transactional(readOnly=true)
    @Override
    public boolean tieneEquipoSancionActiva(Long equipoId)  {
        return sancionRepository.tieneEquipoSancionActiva(equipoId, LocalDate.now());
    }
}