package com.esports.msvc_match.services;

import com.esports.msvc_match.clients.RegistrationClient;
import com.esports.msvc_match.clients.TournamentClient;
import com.esports.msvc_match.exceptions.MatchException;
import com.esports.msvc_match.models.Partida;
import com.esports.msvc_match.models.dtos.InscripcionDTO;
import com.esports.msvc_match.models.dtos.PartidaRequestDTO;
import com.esports.msvc_match.models.dtos.PartidaResponseDTO;
import com.esports.msvc_match.models.dtos.TorneoDTO;
import com.esports.msvc_match.repositories.PartidaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

/**
 * Pruebas unitarias de {@link PartidaServiceImpl}.
 * Mockea: PartidaRepository, TournamentClient, RegistrationClient.
 */
@ExtendWith(MockitoExtension.class)
public class PartidaServiceTest {

    @Mock private PartidaRepository partidaRepository;
    @Mock private TournamentClient tournamentClient;
    @Mock private RegistrationClient registrationClient;

    @InjectMocks
    private PartidaServiceImpl partidaService;

    private Partida partidaPrueba;
    private TorneoDTO torneoValido;
    private List<InscripcionDTO> inscripciones;

    @BeforeEach
    public void setUp() {
        this.partidaPrueba = new Partida();
        this.partidaPrueba.setPartidaId(1L);
        this.partidaPrueba.setTorneoId(100L);
        this.partidaPrueba.setParticipanteAId(10L);
        this.partidaPrueba.setParticipanteBId(20L);
        this.partidaPrueba.setRonda(1);
        this.partidaPrueba.setFechaHora(LocalDateTime.now().plusDays(1));
        this.partidaPrueba.setEstado("PROGRAMADA");

        this.torneoValido = new TorneoDTO();
        this.torneoValido.setTorneoId(100L);
        this.torneoValido.setEstado("ABIERTO");


        InscripcionDTO inscA = new InscripcionDTO();
        inscA.setEquipoId(10L); inscA.setEstado("CONFIRMADA");
        InscripcionDTO inscB = new InscripcionDTO();
        inscB.setEquipoId(20L); inscB.setEstado("CONFIRMADA");
        this.inscripciones = List.of(inscA, inscB);
    }

    @Test
    @DisplayName("Debe encontrar una partida por su ID")
    public void shouldFindPartidaById() {
        // Given
        when(this.partidaRepository.findById(1L)).thenReturn(Optional.of(this.partidaPrueba));
        // When
        PartidaResponseDTO result = this.partidaService.findById(1L);
        // Then
        assertThat(result.getPartidaId()).isEqualTo(1L);
        assertThat(result.getEstado()).isEqualTo("PROGRAMADA");
    }

    @Test
    @DisplayName("Debe lanzar MatchException al buscar ID inexistente")
    public void shouldThrowWhenPartidaNotFound() {
        // Given
        when(this.partidaRepository.findById(9999L)).thenReturn(Optional.empty());
        // When + Then
        assertThatThrownBy(() -> this.partidaService.findById(9999L))
                .isInstanceOf(MatchException.class)
                .hasMessage("Partida con id: 9999 no encontrada");
    }

    @Test
    @DisplayName("Debe crear partida con participantes distintos inscritos y torneo valido")
    public void shouldSavePartidaValid() {
        // Given
        PartidaRequestDTO dto = new PartidaRequestDTO();
        dto.setTorneoId(100L);
        dto.setParticipanteAId(10L);
        dto.setParticipanteBId(20L);
        dto.setRonda(1);
        dto.setFechaHora(LocalDateTime.now().plusDays(1));

        when(this.tournamentClient.findById(100L)).thenReturn(this.torneoValido);
        when(this.registrationClient.findByTorneoId(100L)).thenReturn(this.inscripciones);
        when(this.partidaRepository.findByTorneoIdAndParticipanteAIdAndParticipanteBIdAndRonda(100L, 10L, 20L, 1))
                .thenReturn(Optional.empty());
        when(this.partidaRepository.findByTorneoIdAndParticipanteAIdAndRondaAndParticipanteBId(100L, 20L, 1, 10L))
                .thenReturn(Optional.empty());
        when(this.partidaRepository.save(any(Partida.class))).thenReturn(this.partidaPrueba);

        // When
        PartidaResponseDTO result = this.partidaService.save(dto);

        // Then
        assertThat(result.getEstado()).isEqualTo("PROGRAMADA");
        verify(tournamentClient, times(1)).findById(100L);
        verify(registrationClient, times(1)).findByTorneoId(100L);
        verify(partidaRepository, times(1)).save(any(Partida.class));
    }

    @Test
    @DisplayName("Debe lanzar excepcion cuando participante A y B son el mismo")
    public void shouldThrowWhenSameParticipant() {
        // Given
        PartidaRequestDTO dto = new PartidaRequestDTO();
        dto.setTorneoId(100L);
        dto.setParticipanteAId(10L);
        dto.setParticipanteBId(10L);
        dto.setRonda(1);
        // When + Then
        assertThatThrownBy(() -> this.partidaService.save(dto))
                .isInstanceOf(MatchException.class)
                .hasMessageContaining("Es el mismo participante");
        verify(partidaRepository, never()).save(any(Partida.class));
    }

    @Test
    @DisplayName("Debe lanzar excepcion cuando torneo esta CANCELADO")
    public void shouldThrowWhenTorneoCancelado() {
        // Given
        PartidaRequestDTO dto = new PartidaRequestDTO();
        dto.setTorneoId(100L);
        dto.setParticipanteAId(10L);
        dto.setParticipanteBId(20L);
        dto.setRonda(1);

        TorneoDTO torneoCancelado = new TorneoDTO();
        torneoCancelado.setEstado("CANCELADO");
        when(this.tournamentClient.findById(100L)).thenReturn(torneoCancelado);

        // When + Then
        assertThatThrownBy(() -> this.partidaService.save(dto))
                .isInstanceOf(MatchException.class)
                .hasMessageContaining("CANCELADO");
        verify(partidaRepository, never()).save(any(Partida.class));
    }

    @Test
    @DisplayName("Debe lanzar excepcion cuando participante A no esta inscrito")
    public void shouldThrowWhenParticipanteANotInscrito() {
        // Given
        PartidaRequestDTO dto = new PartidaRequestDTO();
        dto.setTorneoId(100L);
        dto.setParticipanteAId(99L);
        dto.setParticipanteBId(20L);
        dto.setRonda(1);

        when(this.tournamentClient.findById(100L)).thenReturn(this.torneoValido);
        when(this.registrationClient.findByTorneoId(100L)).thenReturn(this.inscripciones); // 10 y 20

        // When + Then
        assertThatThrownBy(() -> this.partidaService.save(dto))
                .isInstanceOf(MatchException.class)
                .hasMessageContaining("participante A");
        verify(partidaRepository, never()).save(any(Partida.class));
    }

    @Test
    @DisplayName("Debe lanzar excepcion cuando ya existe enfrentamiento duplicado en la misma ronda")
    public void shouldThrowWhenDuplicateMatch() {
        // Given
        PartidaRequestDTO dto = new PartidaRequestDTO();
        dto.setTorneoId(100L);
        dto.setParticipanteAId(10L);
        dto.setParticipanteBId(20L);
        dto.setRonda(1);

        when(this.tournamentClient.findById(100L)).thenReturn(this.torneoValido);
        when(this.registrationClient.findByTorneoId(100L)).thenReturn(this.inscripciones);
        when(this.partidaRepository.findByTorneoIdAndParticipanteAIdAndParticipanteBIdAndRonda(100L, 10L, 20L, 1))
                .thenReturn(Optional.of(this.partidaPrueba)); // ya existe

        // When + Then
        assertThatThrownBy(() -> this.partidaService.save(dto))
                .isInstanceOf(MatchException.class)
                .hasMessageContaining("ronda");
        verify(partidaRepository, never()).save(any(Partida.class));
    }

    @Test
    @DisplayName("Debe cancelar una partida PROGRAMADA")
    public void shouldCancelarPartida() {
        // Given
        when(this.partidaRepository.findById(1L)).thenReturn(Optional.of(this.partidaPrueba));
        when(this.partidaRepository.save(any(Partida.class))).thenAnswer(inv -> inv.getArgument(0));
        // When
        PartidaResponseDTO result = this.partidaService.cancelar(1L);
        // Then
        assertThat(result.getEstado()).isEqualTo("CANCELADA");
    }

    @Test
    @DisplayName("Debe lanzar excepcion al cancelar una partida FINALIZADA")
    public void shouldThrowWhenCancelingFinishedPartida() {
        // Given
        this.partidaPrueba.setEstado("FINALIZADA");
        when(this.partidaRepository.findById(1L)).thenReturn(Optional.of(this.partidaPrueba));
        // When + Then
        assertThatThrownBy(() -> this.partidaService.cancelar(1L))
                .isInstanceOf(MatchException.class)
                .hasMessageContaining("FINALIZADA");
        verify(partidaRepository, never()).save(any(Partida.class));
    }
}
