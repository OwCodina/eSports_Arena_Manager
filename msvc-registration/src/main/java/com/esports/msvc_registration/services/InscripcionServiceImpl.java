package com.esports.msvc_registration.services;
import com.esports.msvc_registration.clients.*;
import com.esports.msvc_registration.exceptions.RegistrationException;
import com.esports.msvc_registration.models.Inscripcion;
import com.esports.msvc_registration.models.dtos.*;
import com.esports.msvc_registration.repositories.InscripcionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
@Service
public class InscripcionServiceImpl implements InscripcionService {
    private static final Logger log = LoggerFactory.getLogger(InscripcionServiceImpl.class);
    @Autowired
    private InscripcionRepository inscripcionRepository;

    @Autowired
    private TournamentClient tournamentClient;

    @Autowired
    private TeamClient teamClient;

    @Autowired
    private UserClient userClient;

    @Autowired
    private SanctionClient sanctionClient;


    private InscripcionResponseDTO toResponse(Inscripcion i) {
        InscripcionResponseDTO dto = new InscripcionResponseDTO();
        dto.setInscripcionId(i.getInscripcionId()); dto.setTorneoId(i.getTorneoId());
        dto.setEquipoId(i.getEquipoId()); dto.setJugadorId(i.getJugadorId());
        dto.setTipoParticipante(i.getTipoParticipante()); dto.setEstado(i.getEstado());
        dto.setFechaInscripcion(i.getFechaInscripcion());
        if (i.getAudit() != null) dto.setCreatedAt(i.getAudit().getCreatedAt());
        return dto;
    }

    @Transactional(readOnly=true)
    @Override
    public List<InscripcionResponseDTO> findAll() { return inscripcionRepository.findAll().stream().map(this::toResponse).toList(); }

    @Transactional(readOnly=true)
    @Override
    public List<InscripcionResponseDTO> findByTorneoId(Long id) { return inscripcionRepository.findByTorneoId(id).stream().map(this::toResponse).toList(); }

    @Transactional(readOnly=true)
    @Override
    public List<InscripcionResponseDTO> findByEquipoId(Long id) { return inscripcionRepository.findByEquipoId(id).stream().map(this::toResponse).toList(); }

    @Transactional(readOnly=true)
    @Override
    public List<InscripcionResponseDTO> findByJugadorId(Long id) { return inscripcionRepository.findByJugadorId(id).stream().map(this::toResponse).toList(); }

    @Transactional(readOnly=true)
    @Override
    public InscripcionResponseDTO findById(Long id) {
        return inscripcionRepository.findById(id).map(this::toResponse).orElseThrow(() -> new RegistrationException("Inscripción con id: " + id + " no encontrada"));
    }

    @Transactional @Override
    public InscripcionResponseDTO save(InscripcionRequestDTO dto) {
        if (!"EQUIPO".equals(dto.getTipoParticipante()) && !"INDIVIDUAL".equals(dto.getTipoParticipante()))
            throw new RegistrationException("El tipoParticipante debe ser EQUIPO o INDIVIDUAL.");
        if ("EQUIPO".equals(dto.getTipoParticipante()) && dto.getEquipoId() == null)
            throw new RegistrationException("Se requiere equipoId para inscripción de tipo EQUIPO.");
        if ("INDIVIDUAL".equals(dto.getTipoParticipante()) && dto.getJugadorId() == null)
            throw new RegistrationException("Se requiere jugadorId para inscripción de tipo INDIVIDUAL.");
        TorneoDTO torneo;
        try { torneo = tournamentClient.findById(dto.getTorneoId()); }
        catch (Exception e) { throw new RegistrationException("No se pudo verificar el torneo con id: " + dto.getTorneoId()); }
        if (!"ABIERTO".equals(torneo.getEstado())) throw new RegistrationException("El torneo no está ABIERTO. Estado actual: " + torneo.getEstado());
        if (torneo.getFechaCierreInscripcion() != null && LocalDate.now().isAfter(torneo.getFechaCierreInscripcion()))
            throw new RegistrationException("El plazo de inscripción ha vencido.");
        long inscritos = inscripcionRepository.countByTorneoIdAndEstado(dto.getTorneoId(), "CONFIRMADA");
        if (inscritos >= torneo.getCupoMaximo()) throw new RegistrationException("El torneo ha alcanzado su cupo máximo.");
        if ("EQUIPO".equals(dto.getTipoParticipante())) {
            if (inscripcionRepository.findByTorneoIdAndEquipoId(dto.getTorneoId(), dto.getEquipoId()).isPresent())
                throw new RegistrationException("El equipo ya está inscrito en este torneo.");
            try { Map<String,Boolean> r = teamClient.estaActivo(dto.getEquipoId()); if (!Boolean.TRUE.equals(r.get("estaActivo"))) throw new RegistrationException("El equipo no está ACTIVO."); }
            catch (RegistrationException re) { throw re; } catch (Exception e) { throw new RegistrationException("No se pudo verificar el equipo."); }
            try { Map<String,Boolean> r = sanctionClient.tieneEquipoSancionActiva(dto.getEquipoId()); if (Boolean.TRUE.equals(r.get("tieneSancionActiva"))) throw new RegistrationException("El equipo tiene una sanción activa."); }
            catch (RegistrationException re) { throw re; } catch (Exception e) { throw new RegistrationException("No se pudo verificar sanciones del equipo."); }
        } else {
            if (inscripcionRepository.findByTorneoIdAndJugadorId(dto.getTorneoId(), dto.getJugadorId()).isPresent())
                throw new RegistrationException("El jugador ya está inscrito en este torneo.");
            try { Map<String,Boolean> r = userClient.puedeCompetitr(dto.getJugadorId()); if (!Boolean.TRUE.equals(r.get("puedeCompetitr"))) throw new RegistrationException("El jugador no está habilitado para competir."); }
            catch (RegistrationException re) { throw re; } catch (Exception e) { throw new RegistrationException("No se pudo verificar el jugador."); }
            try { Map<String,Boolean> r = sanctionClient.tieneUsuarioSancionActiva(dto.getJugadorId()); if (Boolean.TRUE.equals(r.get("tieneSancionActiva"))) throw new RegistrationException("El jugador tiene una sanción activa."); }
            catch (RegistrationException re) { throw re; } catch (Exception e) { throw new RegistrationException("No se pudo verificar sanciones del jugador."); }
        }
        Inscripcion ins = new Inscripcion();
        ins.setTorneoId(dto.getTorneoId()); ins.setEquipoId(dto.getEquipoId());
        ins.setJugadorId(dto.getJugadorId()); ins.setTipoParticipante(dto.getTipoParticipante());
        ins.setEstado("CONFIRMADA"); ins.setFechaInscripcion(LocalDateTime.now());
        Inscripcion guardada = inscripcionRepository.save(ins);
        log.info("Inscripción creada: id={}, torneoId={}", guardada.getInscripcionId(), guardada.getTorneoId());
        return toResponse(guardada);
    }
    @Transactional @Override
    public InscripcionResponseDTO updateEstado(Long id, String nuevoEstado) {
        return inscripcionRepository.findById(id).map(existing -> {
            if ("CANCELADA".equals(existing.getEstado())) throw new RegistrationException("No se puede modificar una inscripción CANCELADA.");
            existing.setEstado(nuevoEstado);
            return toResponse(inscripcionRepository.save(existing));
        }).orElseThrow(() -> new RegistrationException("Inscripción con id: " + id + " no encontrada"));
    }
    @Transactional @Override
    public InscripcionResponseDTO cancelar(Long id) {
        return inscripcionRepository.findById(id).map(existing -> {
            if ("CANCELADA".equals(existing.getEstado())) throw new RegistrationException("La inscripción ya está CANCELADA.");
            existing.setEstado("CANCELADA");
            log.info("Inscripción cancelada: id={}", id);
            return toResponse(inscripcionRepository.save(existing));
        }).orElseThrow(() -> new RegistrationException("Inscripción con id: " + id + " no encontrada"));
    }
}