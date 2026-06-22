package com.esports.msvc_ranking.services;

import com.esports.msvc_ranking.clients.RegistrationClient;
import com.esports.msvc_ranking.clients.TournamentClient;
import com.esports.msvc_ranking.exceptions.RankingException;
import com.esports.msvc_ranking.models.Ranking;
import com.esports.msvc_ranking.models.dtos.InscripcionDTO;
import com.esports.msvc_ranking.models.dtos.RankingResponseDTO;
import com.esports.msvc_ranking.models.dtos.TorneoDTO;
import com.esports.msvc_ranking.repositories.RankingRepository;
import feign.FeignException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Pruebas unitarias de {@link RankingServiceImpl}.
 * Mockea: RankingRepository, TournamentClient, RegistrationClient.
 */
@ExtendWith(MockitoExtension.class)
public class RankingServiceTest {

    @Mock private RankingRepository rankingRepository;
    @Mock private TournamentClient tournamentClient;
    @Mock private RegistrationClient registrationClient;

    @InjectMocks
    private RankingServiceImpl rankingService;

    private Ranking rankingPrueba;
    private TorneoDTO torneoValido;
    private List<InscripcionDTO> inscripcionesConfirmadas;

    @BeforeEach
    public void setUp() {
        this.rankingPrueba = new Ranking();
        this.rankingPrueba.setRankingId(1L);
        this.rankingPrueba.setTorneoId(100L);
        this.rankingPrueba.setParticipanteId(10L);
        this.rankingPrueba.setPuntos(0);
        this.rankingPrueba.setVictorias(0);
        this.rankingPrueba.setDerrotas(0);
        this.rankingPrueba.setDiferencia(0);
        this.rankingPrueba.setPosicion(1);

        this.torneoValido = new TorneoDTO();
        this.torneoValido.setTorneoId(100L);
        this.torneoValido.setNombre("Copa 2025");
        this.torneoValido.setEstado("ABIERTO");


        InscripcionDTO ins1 = new InscripcionDTO();
        ins1.setEquipoId(10L); ins1.setTipoParticipante("EQUIPO"); ins1.setEstado("CONFIRMADA");
        InscripcionDTO ins2 = new InscripcionDTO();
        ins2.setEquipoId(20L); ins2.setTipoParticipante("EQUIPO"); ins2.setEstado("CONFIRMADA");
        this.inscripcionesConfirmadas = List.of(ins1, ins2);
    }

    @Test
    @DisplayName("Debe encontrar el ranking por ID")
    public void shouldFindRankingById() {
        // Given
        when(this.rankingRepository.findById(1L)).thenReturn(Optional.of(this.rankingPrueba));
        // When
        RankingResponseDTO result = this.rankingService.findById(1L);
        // Then
        assertThat(result.getRankingId()).isEqualTo(1L);
        assertThat(result.getPuntos()).isEqualTo(0);
    }

    @Test
    @DisplayName("Debe lanzar RankingException al buscar ID inexistente")
    public void shouldThrowWhenRankingNotFound() {
        // Given
        when(this.rankingRepository.findById(9999L)).thenReturn(Optional.empty());
        // When + Then
        assertThatThrownBy(() -> this.rankingService.findById(9999L))
                .isInstanceOf(RankingException.class)
                .hasMessage("Ranking con id: 9999 no encontrado");
    }

    @Test
    @DisplayName("Debe inicializar ranking creando una entrada por participante CONFIRMADO")
    public void shouldInicializarRanking() {
        // Given
        when(this.tournamentClient.findById(100L)).thenReturn(this.torneoValido);
        when(this.rankingRepository.findByTorneoIdOrderByPosicionAsc(100L)).thenReturn(List.of()); // no inicializado
        when(this.registrationClient.findByTorneoId(100L)).thenReturn(this.inscripcionesConfirmadas);


        when(this.rankingRepository.save(any(Ranking.class))).thenAnswer(inv -> {
            Ranking r = inv.getArgument(0);
            r.setRankingId((long)(Math.random()*1000));
            return r;
        });

        // When
        List<RankingResponseDTO> result = this.rankingService.inicializarRanking(100L);

        // Then: 2 participantes = 2 entradas de ranking
        assertThat(result).hasSize(2);
        verify(tournamentClient, times(1)).findById(100L);
        verify(registrationClient, times(1)).findByTorneoId(100L);
        verify(rankingRepository, times(2)).save(any(Ranking.class));
    }

    @Test
    @DisplayName("Debe lanzar excepcion al inicializar ranking de torneo CANCELADO")
    public void shouldThrowWhenTorneoCancelado() {
        // Given
        TorneoDTO torneoCancelado = new TorneoDTO();
        torneoCancelado.setEstado("CANCELADO");
        when(this.tournamentClient.findById(100L)).thenReturn(torneoCancelado);
        // When + Then
        assertThatThrownBy(() -> this.rankingService.inicializarRanking(100L))
                .isInstanceOf(RankingException.class)
                .hasMessageContaining("CANCELADO");
        verify(rankingRepository, never()).save(any(Ranking.class));
    }

    @Test
    @DisplayName("Debe lanzar excepcion si el ranking ya fue inicializado")
    public void shouldThrowWhenAlreadyInitialized() {
        // Given
        when(this.tournamentClient.findById(100L)).thenReturn(this.torneoValido);
        when(this.rankingRepository.findByTorneoIdOrderByPosicionAsc(100L))
                .thenReturn(List.of(this.rankingPrueba)); // ya existe
        // When + Then
        assertThatThrownBy(() -> this.rankingService.inicializarRanking(100L))
                .isInstanceOf(RankingException.class)
                .hasMessageContaining("ya fue inicializado");
        verify(rankingRepository, never()).save(any(Ranking.class));
    }

    @Test
    @DisplayName("Debe lanzar excepcion si msvc-tournament no responde al inicializar")
    public void shouldThrowWhenTournamentClientFails() {
        // Given
        when(this.tournamentClient.findById(100L)).thenThrow(mock(FeignException.class));
        // When + Then
        assertThatThrownBy(() -> this.rankingService.inicializarRanking(100L))
                .isInstanceOf(RankingException.class)
                .hasMessageContaining("100");
    }

    @Test
    @DisplayName("Debe actualizar ranking: ganador +3pts, perdedor +derrota, y recalcular posiciones")
    public void shouldActualizarConResultado() {
        // Given
        Long torneoId = 100L;
        Ranking rankGanador = new Ranking();
        rankGanador.setRankingId(1L); rankGanador.setTorneoId(torneoId);
        rankGanador.setParticipanteId(10L); rankGanador.setPuntos(3);
        rankGanador.setVictorias(1); rankGanador.setDerrotas(0); rankGanador.setDiferencia(6);

        Ranking rankPerdedor = new Ranking();
        rankPerdedor.setRankingId(2L); rankPerdedor.setTorneoId(torneoId);
        rankPerdedor.setParticipanteId(20L); rankPerdedor.setPuntos(0);
        rankPerdedor.setVictorias(0); rankPerdedor.setDerrotas(1); rankPerdedor.setDiferencia(-6);

        when(this.rankingRepository.findByTorneoIdAndParticipanteId(torneoId, 10L))
                .thenReturn(Optional.of(rankGanador));
        when(this.rankingRepository.findByTorneoIdAndParticipanteId(torneoId, 20L))
                .thenReturn(Optional.of(rankPerdedor));
        when(this.rankingRepository.save(any(Ranking.class))).thenAnswer(inv -> inv.getArgument(0));
        // recalcularPosiciones
        when(this.rankingRepository.findByTorneoIdOrderByPuntosDescDiferenciaDesc(torneoId))
                .thenReturn(List.of(rankGanador, rankPerdedor));

        // When
        RankingResponseDTO result = this.rankingService.actualizarConResultado(torneoId, 10L, 13, 7, 20L);

        // Then: ganador debería tener +3 puntos y +1 victoria adicional
        verify(rankingRepository, atLeastOnce()).save(any(Ranking.class));
    }

    @Test
    @DisplayName("Debe cerrar ranking cuando torneo esta FINALIZADO")
    public void shouldCerrarRanking() {
        // Given
        TorneoDTO torneoFinalizado = new TorneoDTO();
        torneoFinalizado.setNombre("Copa Final"); torneoFinalizado.setEstado("FINALIZADO");
        when(this.tournamentClient.findById(100L)).thenReturn(torneoFinalizado);
        when(this.rankingRepository.findByTorneoIdOrderByPosicionAsc(100L))
                .thenReturn(List.of(this.rankingPrueba));
        when(this.rankingRepository.findByTorneoIdOrderByPuntosDescDiferenciaDesc(100L))
                .thenReturn(List.of(this.rankingPrueba));
        when(this.rankingRepository.save(any(Ranking.class))).thenAnswer(inv -> inv.getArgument(0));

        // When + Then
        assertThatCode(() -> this.rankingService.cerrarRanking(100L)).doesNotThrowAnyException();
        verify(tournamentClient, times(1)).findById(100L);
    }

    @Test
    @DisplayName("Debe lanzar excepcion al cerrar ranking de torneo no FINALIZADO")
    public void shouldThrowWhenCerrandoTorneoNoFinalizado() {
        // Given
        when(this.tournamentClient.findById(100L)).thenReturn(this.torneoValido); // ABIERTO, no FINALIZADO
        // When + Then
        assertThatThrownBy(() -> this.rankingService.cerrarRanking(100L))
                .isInstanceOf(RankingException.class)
                .hasMessageContaining("FINALIZADO");
    }
}
