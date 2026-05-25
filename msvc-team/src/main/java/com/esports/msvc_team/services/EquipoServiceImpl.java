package com.esports.msvc_team.services;
import com.esports.msvc_team.clients.GameClient;
import com.esports.msvc_team.clients.UserClient;
import com.esports.msvc_team.exceptions.TeamException;
import com.esports.msvc_team.models.Equipo;
import com.esports.msvc_team.models.MiembroEquipo;
import com.esports.msvc_team.models.dtos.*;
import com.esports.msvc_team.repositories.EquipoRepository;
import com.esports.msvc_team.repositories.MiembroEquipoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
@Service
public class EquipoServiceImpl implements EquipoService {
    private static final Logger log = LoggerFactory.getLogger(EquipoServiceImpl.class);
    @Autowired private EquipoRepository equipoRepository;
    @Autowired private MiembroEquipoRepository miembroEquipoRepository;
    @Autowired private UserClient userClient;
    @Autowired private GameClient gameClient;

    private EquipoResponseDTO toResponse(Equipo e) {
        EquipoResponseDTO dto = new EquipoResponseDTO();
        dto.setEquipoId(e.getEquipoId()); dto.setNombre(e.getNombre());
        dto.setCapitanId(e.getCapitanId()); dto.setJuegoPrincipalId(e.getJuegoPrincipalId());
        dto.setEstado(e.getEstado());
        if (e.getAudit() != null) { dto.setCreatedAt(e.getAudit().getCreatedAt()); dto.setUpdatedAt(e.getAudit().getUpdatedAt()); }
        return dto;
    }

    @Transactional(readOnly=true) @Override public List<EquipoResponseDTO> findAll() { return equipoRepository.findAll().stream().map(this::toResponse).toList(); }
    @Transactional(readOnly=true) @Override public List<EquipoResponseDTO> findByEstado(String e) { return equipoRepository.findByEstado(e).stream().map(this::toResponse).toList(); }
    @Transactional(readOnly=true) @Override public List<EquipoResponseDTO> findByJuegoPrincipalId(Long id) { return equipoRepository.findByJuegoPrincipalId(id).stream().map(this::toResponse).toList(); }
    @Transactional(readOnly=true) @Override public List<EquipoResponseDTO> findByCapitanId(Long id) { return equipoRepository.findByCapitanId(id).stream().map(this::toResponse).toList(); }
    @Transactional(readOnly=true) @Override
    public EquipoResponseDTO findById(Long id) {
        return equipoRepository.findById(id).map(this::toResponse).orElseThrow(() -> new TeamException("Equipo con id: " + id + " no encontrado"));
    }
    @Transactional @Override
    public EquipoResponseDTO save(EquipoRequestDTO dto) {
        if (equipoRepository.findByNombre(dto.getNombre()).isPresent()) throw new TeamException("Ya existe un equipo con el nombre: " + dto.getNombre());
        UsuarioDTO capitan;
        try { capitan = userClient.findById(dto.getCapitanId()); } catch (Exception e) { throw new TeamException("No se pudo verificar el capitán con id: " + dto.getCapitanId()); }
        if (!"ACTIVO".equals(capitan.getEstado())) throw new TeamException("El capitán con id: " + dto.getCapitanId() + " no está ACTIVO.");
        JuegoDTO juego;
        try { juego = gameClient.findById(dto.getJuegoPrincipalId()); } catch (Exception e) { throw new TeamException("No se pudo verificar el juego con id: " + dto.getJuegoPrincipalId()); }
        if (!"ACTIVO".equals(juego.getEstado())) throw new TeamException("El juego '" + juego.getNombre() + "' no está ACTIVO.");
        Equipo equipo = new Equipo();
        equipo.setNombre(dto.getNombre()); equipo.setCapitanId(dto.getCapitanId());
        equipo.setJuegoPrincipalId(dto.getJuegoPrincipalId()); equipo.setEstado("ACTIVO");
        Equipo guardado = equipoRepository.save(equipo);
        log.info("Equipo creado: id={}", guardado.getEquipoId());
        return toResponse(guardado);
    }
    @Transactional @Override
    public EquipoResponseDTO updateById(Long id, EquipoRequestDTO dto) {
        return equipoRepository.findById(id).map(existing -> {
            if (dto.getNombre() != null && !dto.getNombre().equals(existing.getNombre())) {
                if (equipoRepository.findByNombre(dto.getNombre()).isPresent()) throw new TeamException("Ya existe un equipo con el nombre: " + dto.getNombre());
                existing.setNombre(dto.getNombre());
            }
            if (dto.getCapitanId() != null && !dto.getCapitanId().equals(existing.getCapitanId())) {
                try { UsuarioDTO c = userClient.findById(dto.getCapitanId()); if (!"ACTIVO".equals(c.getEstado())) throw new TeamException("El nuevo capitán no está ACTIVO."); }
                catch (TeamException te) { throw te; } catch (Exception e) { throw new TeamException("No se pudo verificar el nuevo capitán."); }
                existing.setCapitanId(dto.getCapitanId());
            }
            log.info("Equipo actualizado: id={}", id);
            return toResponse(equipoRepository.save(existing));
        }).orElseThrow(() -> new TeamException("Equipo con id: " + id + " no encontrado"));
    }
    @Transactional @Override
    public EquipoResponseDTO desactivar(Long id) {
        return equipoRepository.findById(id).map(existing -> {
            existing.setEstado("INACTIVO");
            return toResponse(equipoRepository.save(existing));
        }).orElseThrow(() -> new TeamException("Equipo con id: " + id + " no encontrado"));
    }
    @Transactional(readOnly=true) @Override
    public List<MiembroEquipo> findMiembrosByEquipoId(Long equipoId) {
        findById(equipoId);
        return miembroEquipoRepository.findByEquipoId(equipoId);
    }
    @Transactional @Override
    public MiembroEquipo agregarMiembro(Long equipoId, MiembroRequestDTO dto) {
        Equipo equipo = equipoRepository.findById(equipoId).orElseThrow(() -> new TeamException("Equipo con id: " + equipoId + " no encontrado"));
        if (!"ACTIVO".equals(equipo.getEstado())) throw new TeamException("No se pueden agregar miembros a un equipo INACTIVO.");
        try { UsuarioDTO u = userClient.findById(dto.getUsuarioId()); if (!"ACTIVO".equals(u.getEstado())) throw new TeamException("El usuario no está ACTIVO."); }
        catch (TeamException te) { throw te; } catch (Exception e) { throw new TeamException("No se pudo verificar el usuario con id: " + dto.getUsuarioId()); }
        if (miembroEquipoRepository.findByEquipoIdAndUsuarioId(equipoId, dto.getUsuarioId()).isPresent())
            throw new TeamException("El usuario con id: " + dto.getUsuarioId() + " ya es miembro del equipo.");
        MiembroEquipo m = new MiembroEquipo();
        m.setEquipoId(equipoId); m.setUsuarioId(dto.getUsuarioId()); m.setRolDentroEquipo(dto.getRolDentroEquipo());
        log.info("Miembro agregado: usuarioId={} al equipoId={}", dto.getUsuarioId(), equipoId);
        return miembroEquipoRepository.save(m);
    }
    @Transactional @Override
    public void eliminarMiembro(Long miembroId) {
        MiembroEquipo m = miembroEquipoRepository.findById(miembroId).orElseThrow(() -> new TeamException("Miembro con id: " + miembroId + " no encontrado"));
        miembroEquipoRepository.delete(m);
    }
    @Transactional(readOnly=true) @Override
    public boolean estaActivo(Long equipoId) {
        Equipo e = equipoRepository.findById(equipoId).orElseThrow(() -> new TeamException("Equipo con id: " + equipoId + " no encontrado"));
        return "ACTIVO".equals(e.getEstado());
    }
}