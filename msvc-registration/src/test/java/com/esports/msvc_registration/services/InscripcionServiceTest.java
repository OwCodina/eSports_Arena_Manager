package com.esports.msvc_registration.services;

import com.esports.msvc_registration.clients.SanctionClient;
import com.esports.msvc_registration.clients.TeamClient;
import com.esports.msvc_registration.clients.TournamentClient;
import com.esports.msvc_registration.clients.UserClient;
import com.esports.msvc_registration.exceptions.RegistrationException;
import com.esports.msvc_registration.models.Inscripcion;
import com.esports.msvc_registration.models.dtos.InscripcionRequestDTO;
import com.esports.msvc_registration.models.dtos.InscripcionResponseDTO;
import com.esports.msvc_registration.models.dtos.TorneoDTO;
import com.esports.msvc_registration.repositories.InscripcionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

/**
 * Pruebas unitarias de {@link InscripcionServiceImpl}.
 * 4 clientes Feign + 5 validaciones en cadena.
 */
@ExtendWith(MockitoExtension.class)
public class InscripcionServiceTest {

    @Mock private InscripcionRepository inscripcionRepository;
    @Mock private TournamentClient tournamentClient;
    @Mock private TeamClient teamClient;
    @Mock private UserClient userClient;
    @Mock private SanctionClient sanctionClient;

    @InjectMocks
    private InscripcionServiceImpl inscripcionService;

    private Inscripcion inscripcionPrueba;
    private TorneoDTO torneoAbierto;

    @BeforeEach
    public void setUp() {
        this.inscripcionPrueba = new Inscripcion();
        this.inscripcionPrueba.setInscripcionId(1L);
        this.inscripcionPrueba.setTorneoId(100L);
        this.inscripcionPrueba.setEquipoId(10L);
        this.inscripcionPrueba.setTipoParticipante("EQUIPO");
        this.inscripcionPrueba.setEstado("CONFIRMADA");
        this.inscripcionPrueba.setFechaInscripcion(LocalDateTime.now());

        this.torneoAbierto = new TorneoDTO();
        this.torneoAbierto.setTorneoId(100L);
        this.torneoAbierto.setEstado("ABIERTO");
        this.torneoAbierto.setCupoMaximo(8);
        this.torneoAbierto.setFechaCierreInscripcion(LocalDate.now().plusDays(5));
    }



    @Test
    @DisplayName("Debe encontrar una inscripcion por su ID")
    public void shouldFindInscripcionById() {
        // Given
        when(this.inscripcionRepository.findById(1L)).thenReturn(Optional.of(this.inscripcionPrueba));
        // When
        InscripcionResponseDTO result = this.inscripcionService.findById(1L);
        // Then
        assertThat(result.getInscripcionId()).isEqualTo(1L);
        assertThat(result.getEstado()).isEqualTo("CONFIRMADA");
    }

    @Test
    @DisplayName("Debe lanzar RegistrationException al buscar ID inexistente")
    public void shouldThrowWhenInscripcionNotFound() {
        // Given
        when(this.inscripcionRepository.findById(9999L)).thenReturn(Optional.empty());
        // When + Then
        assertThatThrownBy(() -> this.inscripcionService.findById(9999L))
                .isInstanceOf(RegistrationException.class)
                .hasMessage("Inscripcion con id: 9999 no encontrada");
    }



    @Test
    @DisplayName("Debe inscribir equipo cuando pasan las 5 validaciones")
    public void shouldSaveInscripcionEquipoValid() {
        // Given
        InscripcionRequestDTO dto = new InscripcionRequestDTO();
        dto.setTorneoId(100L);
        dto.setEquipoId(10L);
        dto.setTipoParticipante("EQUIPO");

        when(this.tournamentClient.findById(100L)).thenReturn(this.torneoAbierto);
        when(this.inscripcionRepository.countByTorneoIdAndEstado(100L, "CONFIRMADA")).thenReturn(3L); // cupo no lleno
        when(this.inscripcionRepository.findByTorneoIdAndEquipoId(100L, 10L)).thenReturn(Optional.empty()); // no duplicado
        when(this.teamClient.estaActivo(10L)).thenReturn(Map.of("estaActivo", true));
        when(this.sanctionClient.tieneEquipoSancionActiva(10L)).thenReturn(Map.of("tieneSancionActiva", false));
        when(this.inscripcionRepository.save(any(Inscripcion.class))).thenReturn(this.inscripcionPrueba);

        // When
        InscripcionResponseDTO result = this.inscripcionService.save(dto);

        // Then
        assertThat(result.getEstado()).isEqualTo("CONFIRMADA");
        verify(tournamentClient, times(1)).findById(100L);
        verify(teamClient, times(1)).estaActivo(10L);
        verify(sanctionClient, times(1)).tieneEquipoSancionActiva(10L);
        verify(inscripcionRepository, times(1)).save(any(Inscripcion.class));
    }

    @Test
    @DisplayName("Debe lanzar excepcion cuando torneo no esta ABIERTO")
    public void shouldThrowWhenTorneoNotAbierto() {
        // Given
        InscripcionRequestDTO dto = new InscripcionRequestDTO();
        dto.setTorneoId(100L);
        dto.setEquipoId(10L);
        dto.setTipoParticipante("EQUIPO");

        TorneoDTO torneoCerrado = new TorneoDTO();
        torneoCerrado.setEstado("CERRADO");
        torneoCerrado.setCupoMaximo(8);
        when(this.tournamentClient.findById(100L)).thenReturn(torneoCerrado);

        // When + Then
        assertThatThrownBy(() -> this.inscripcionService.save(dto))
                .isInstanceOf(RegistrationException.class)
                .hasMessageContaining("ABIERTO");
        verify(inscripcionRepository, never()).save(any(Inscripcion.class));
    }

    @Test
    @DisplayName("Debe lanzar excepcion cuando el cupo maximo esta lleno")
    public void shouldThrowWhenCupoLleno() {
        // Given
        InscripcionRequestDTO dto = new InscripcionRequestDTO();
        dto.setTorneoId(100L);
        dto.setEquipoId(10L);
        dto.setTipoParticipante("EQUIPO");

        when(this.tournamentClient.findById(100L)).thenReturn(this.torneoAbierto);
        when(this.inscripcionRepository.countByTorneoIdAndEstado(100L, "CONFIRMADA")).thenReturn(8L); // == cupoMaximo

        // When + Then
        assertThatThrownBy(() -> this.inscripcionService.save(dto))
                .isInstanceOf(RegistrationException.class)
                .hasMessageContaining("cupo");
        verify(inscripcionRepository, never()).save(any(Inscripcion.class));
    }

    @Test
    @DisplayName("Debe lanzar excepcion cuando equipo ya esta inscrito")
    public void shouldThrowWhenEquipoDuplicado() {
        // Given
        InscripcionRequestDTO dto = new InscripcionRequestDTO();
        dto.setTorneoId(100L);
        dto.setEquipoId(10L);
        dto.setTipoParticipante("EQUIPO");

        when(this.tournamentClient.findById(100L)).thenReturn(this.torneoAbierto);
        when(this.inscripcionRepository.countByTorneoIdAndEstado(100L, "CONFIRMADA")).thenReturn(2L);
        when(this.inscripcionRepository.findByTorneoIdAndEquipoId(100L, 10L))
                .thenReturn(Optional.of(this.inscripcionPrueba)); // ya existe

        // When + Then
        assertThatThrownBy(() -> this.inscripcionService.save(dto))
                .isInstanceOf(RegistrationException.class)
                .hasMessageContaining("inscrito");
        verify(inscripcionRepository, never()).save(any(Inscripcion.class));
    }

    @Test
    @DisplayName("Debe lanzar excepcion cuando el equipo tiene sancion activa")
    public void shouldThrowWhenEquipoHasSancion() {
        // Given
        InscripcionRequestDTO dto = new InscripcionRequestDTO();
        dto.setTorneoId(100L);
        dto.setEquipoId(10L);
        dto.setTipoParticipante("EQUIPO");

        when(this.tournamentClient.findById(100L)).thenReturn(this.torneoAbierto);
        when(this.inscripcionRepository.countByTorneoIdAndEstado(100L, "CONFIRMADA")).thenReturn(1L);
        when(this.inscripcionRepository.findByTorneoIdAndEquipoId(100L, 10L)).thenReturn(Optional.empty());
        when(this.teamClient.estaActivo(10L)).thenReturn(Map.of("estaActivo", true));
        when(this.sanctionClient.tieneEquipoSancionActiva(10L)).thenReturn(Map.of("tieneSancionActiva", true));

        // When + Then
        assertThatThrownBy(() -> this.inscripcionService.save(dto))
                .isInstanceOf(RegistrationException.class)
                .hasMessageContaining("sancion");
        verify(inscripcionRepository, never()).save(any(Inscripcion.class));
    }

    @Test
    @DisplayName("Debe lanzar excepcion cuando tipo participante es invalido")
    public void shouldThrowWhenTipoInvalido() {
        // Given
        InscripcionRequestDTO dto = new InscripcionRequestDTO();
        dto.setTorneoId(100L);
        dto.setTipoParticipante("CLAN"); // inválido
        // When + Then
        assertThatThrownBy(() -> this.inscripcionService.save(dto))
                .isInstanceOf(RegistrationException.class)
                .hasMessageContaining("EQUIPO o INDIVIDUAL");
        verify(tournamentClient, never()).findById(anyLong());
    }



    @Test
    @DisplayName("Debe cancelar una inscripcion CONFIRMADA")
    public void shouldCancelarInscripcion() {
        // Given
        when(this.inscripcionRepository.findById(1L)).thenReturn(Optional.of(this.inscripcionPrueba));
        when(this.inscripcionRepository.save(any(Inscripcion.class))).thenAnswer(inv -> inv.getArgument(0));
        // When
        InscripcionResponseDTO result = this.inscripcionService.cancelar(1L);
        // Then
        assertThat(result.getEstado()).isEqualTo("CANCELADA");
    }

    @Test
    @DisplayName("Debe lanzar excepcion al cancelar una inscripcion ya CANCELADA")
    public void shouldThrowWhenAlreadyCanceled() {
        // Given
        this.inscripcionPrueba.setEstado("CANCELADA");
        when(this.inscripcionRepository.findById(1L)).thenReturn(Optional.of(this.inscripcionPrueba));
        // When + Then
        assertThatThrownBy(() -> this.inscripcionService.cancelar(1L))
                .isInstanceOf(RegistrationException.class)
                .hasMessageContaining("CANCELADA");
        verify(inscripcionRepository, never()).save(any(Inscripcion.class));
    }
}
