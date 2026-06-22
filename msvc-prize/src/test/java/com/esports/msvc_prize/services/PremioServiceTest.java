package com.esports.msvc_prize.services;

import com.esports.msvc_prize.clients.RankingClient;
import com.esports.msvc_prize.clients.TournamentClient;
import com.esports.msvc_prize.exceptions.PrizeException;
import com.esports.msvc_prize.models.Premio;
import com.esports.msvc_prize.models.PremioAsignado;
import com.esports.msvc_prize.models.dtos.*;
import com.esports.msvc_prize.repositories.PremioAsignadoRepository;
import com.esports.msvc_prize.repositories.PremioRepository;
import feign.FeignException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Pruebas unitarias de {@link PremioServiceImpl}.
 * Mockea: PremioRepository, PremioAsignadoRepository, TournamentClient, RankingClient.
 */
@ExtendWith(MockitoExtension.class)
public class PremioServiceTest {

    @Mock private PremioRepository premioRepository;
    @Mock private PremioAsignadoRepository premioAsignadoRepository;
    @Mock private TournamentClient tournamentClient;
    @Mock private RankingClient rankingClient;

    @InjectMocks
    private PremioServiceImpl premioService;

    private Premio premioPrueba;
    private TorneoDTO torneoFinalizado;

    @BeforeEach
    public void setUp() {
        this.premioPrueba = new Premio();
        this.premioPrueba.setPremioId(1L);
        this.premioPrueba.setTorneoId(100L);
        this.premioPrueba.setPosicion(1);
        this.premioPrueba.setDescripcion("Medalla de oro + $500.000");
        this.premioPrueba.setValor(BigDecimal.valueOf(500000.0));
        this.premioPrueba.setEstado("DISPONIBLE");

        this.torneoFinalizado = new TorneoDTO();
        this.torneoFinalizado.setTorneoId(100L);
        this.torneoFinalizado.setEstado("FINALIZADO");
    }

    @Test
    @DisplayName("Debe encontrar un premio por su ID")
    public void shouldFindPremioById() {
        // Given
        when(this.premioRepository.findById(1L)).thenReturn(Optional.of(this.premioPrueba));
        // When
        PremioResponseDTO result = this.premioService.findById(1L);
        // Then
        assertThat(result.getPosicion()).isEqualTo(1);
        assertThat(result.getEstado()).isEqualTo("DISPONIBLE");
    }

    @Test
    @DisplayName("Debe lanzar PrizeException al buscar ID inexistente")
    public void shouldThrowWhenPremioNotFound() {
        // Given
        when(this.premioRepository.findById(9999L)).thenReturn(Optional.empty());
        // When + Then
        assertThatThrownBy(() -> this.premioService.findById(9999L))
                .isInstanceOf(PrizeException.class)
                .hasMessage("Premio con id: 9999 no encontrado");
    }

    @Test
    @DisplayName("Debe guardar premio DISPONIBLE cuando torneo existe")
    public void shouldSavePremio() {
        // Given
        PremioRequestDTO dto = new PremioRequestDTO();
        dto.setTorneoId(100L);
        dto.setPosicion(2);
        dto.setDescripcion("Medalla plata");
        dto.setValor(BigDecimal.valueOf(200000.0));

        when(this.tournamentClient.findById(100L)).thenReturn(this.torneoFinalizado);
        when(this.premioRepository.findByTorneoIdAndPosicion(100L, 2)).thenReturn(Optional.empty());

        Premio guardado = new Premio();
        guardado.setPremioId(2L); guardado.setEstado("DISPONIBLE"); guardado.setPosicion(2);
        when(this.premioRepository.save(any(Premio.class))).thenReturn(guardado);

        // When
        PremioResponseDTO result = this.premioService.save(dto);

        // Then
        assertThat(result.getEstado()).isEqualTo("DISPONIBLE");
        verify(tournamentClient, times(1)).findById(100L);
        verify(premioRepository, times(1)).save(any(Premio.class));
    }

    @Test
    @DisplayName("Debe lanzar excepcion al guardar con posicion duplicada en el torneo")
    public void shouldThrowWhenPosicionDuplicada() {
        // Given
        PremioRequestDTO dto = new PremioRequestDTO();
        dto.setTorneoId(100L);
        dto.setPosicion(1);

        when(this.tournamentClient.findById(100L)).thenReturn(this.torneoFinalizado);
        when(this.premioRepository.findByTorneoIdAndPosicion(100L, 1)).thenReturn(Optional.of(this.premioPrueba));

        // When + Then
        assertThatThrownBy(() -> this.premioService.save(dto))
                .isInstanceOf(PrizeException.class)
                .hasMessageContaining("posicion");
        verify(premioRepository, never()).save(any(Premio.class));
    }

    @Test
    @DisplayName("Debe lanzar excepcion si msvc-tournament no responde al guardar")
    public void shouldThrowWhenTournamentClientFails() {
        // Given
        PremioRequestDTO dto = new PremioRequestDTO();
        dto.setTorneoId(999L);
        dto.setPosicion(1);
        when(this.tournamentClient.findById(999L)).thenThrow(mock(FeignException.class));
        // When + Then
        assertThatThrownBy(() -> this.premioService.save(dto))
                .isInstanceOf(PrizeException.class)
                .hasMessageContaining("999");
        verify(premioRepository, never()).save(any(Premio.class));
    }

    @Test
    @DisplayName("Debe lanzar excepcion al modificar un premio ASIGNADO")
    public void shouldThrowWhenModifyingAsignado() {
        // Given
        this.premioPrueba.setEstado("ASIGNADO");
        when(this.premioRepository.findById(1L)).thenReturn(Optional.of(this.premioPrueba));
        // When + Then
        assertThatThrownBy(() -> this.premioService.updateById(1L, new PremioRequestDTO()))
                .isInstanceOf(PrizeException.class)
                .hasMessageContaining("ASIGNADO");
        verify(premioRepository, never()).save(any(Premio.class));
    }

    @Test
    @DisplayName("Debe desactivar un premio DISPONIBLE")
    public void shouldDesactivarPremio() {
        // Given
        when(this.premioRepository.findById(1L)).thenReturn(Optional.of(this.premioPrueba));
        when(this.premioRepository.save(any(Premio.class))).thenAnswer(inv -> inv.getArgument(0));
        // When
        PremioResponseDTO result = this.premioService.desactivar(1L);
        // Then
        assertThat(result.getEstado()).isEqualTo("DESACTIVADO");
    }

    @Test
    @DisplayName("Debe lanzar excepcion al desactivar un premio ASIGNADO")
    public void shouldThrowWhenDesactivatingAsignado() {
        // Given
        this.premioPrueba.setEstado("ASIGNADO");
        when(this.premioRepository.findById(1L)).thenReturn(Optional.of(this.premioPrueba));
        // When + Then
        assertThatThrownBy(() -> this.premioService.desactivar(1L))
                .isInstanceOf(PrizeException.class)
                .hasMessageContaining("ASIGNADO");
        verify(premioRepository, never()).save(any(Premio.class));
    }

    @Test
    @DisplayName("Debe asignar premios automaticamente segun ranking cuando torneo FINALIZADO")
    public void shouldAsignarPremiosTorneo() {
        // Given
        RankingDTO r1 = new RankingDTO(); r1.setPosicion(1); r1.setParticipanteId(10L);
        RankingDTO r2 = new RankingDTO(); r2.setPosicion(2); r2.setParticipanteId(20L);

        when(this.tournamentClient.findById(100L)).thenReturn(this.torneoFinalizado);
        when(this.rankingClient.findByTorneoId(100L)).thenReturn(List.of(r1, r2));
        when(this.premioRepository.findByTorneoIdAndEstado(100L, "DISPONIBLE")).thenReturn(List.of(this.premioPrueba));
        when(this.premioAsignadoRepository.findByPremioId(1L)).thenReturn(Optional.empty());
        when(this.premioAsignadoRepository.save(any(PremioAsignado.class))).thenAnswer(inv -> inv.getArgument(0));
        when(this.premioRepository.save(any(Premio.class))).thenAnswer(inv -> inv.getArgument(0));

        // When
        List<PremioAsignado> result = this.premioService.asignarPremiosTorneo(100L);

        // Then: 1 premio DISPONIBLE asignado al participante en posicion 1
        assertThat(result).hasSize(1);
        verify(premioAsignadoRepository, times(1)).save(any(PremioAsignado.class));
    }

    @Test
    @DisplayName("Debe lanzar excepcion si torneo no esta FINALIZADO al asignar premios")
    public void shouldThrowWhenTorneoNotFinalizadoParaAsignar() {
        // Given
        TorneoDTO torneoAbierto = new TorneoDTO(); torneoAbierto.setEstado("ABIERTO");
        when(this.tournamentClient.findById(100L)).thenReturn(torneoAbierto);
        // When + Then
        assertThatThrownBy(() -> this.premioService.asignarPremiosTorneo(100L))
                .isInstanceOf(PrizeException.class)
                .hasMessageContaining("FINALIZADO");
    }
}
