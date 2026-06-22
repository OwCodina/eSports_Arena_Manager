package com.esports.msvc_result.services;

import com.esports.msvc_result.clients.MatchClient;
import com.esports.msvc_result.exceptions.ResultException;
import com.esports.msvc_result.models.Resultado;
import com.esports.msvc_result.models.dtos.*;
import com.esports.msvc_result.repositories.ResultadoRepository;
import feign.FeignException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Pruebas unitarias de {@link ResultadoServiceImpl}.
 * Mockea: ResultadoRepository y MatchClient (Feign hacia msvc-match).
 */
@ExtendWith(MockitoExtension.class)
public class ResultadoServiceTest {

    @Mock private ResultadoRepository resultadoRepository;
    @Mock private MatchClient matchClient;

    @InjectMocks
    private ResultadoServiceImpl resultadoService;

    private Resultado resultadoPrueba;
    private PartidaDTO partidaDTO;

    @BeforeEach
    public void setUp() {
        this.resultadoPrueba = new Resultado();
        this.resultadoPrueba.setResultadoId(1L);
        this.resultadoPrueba.setPartidaId(50L);
        this.resultadoPrueba.setGanadorId(10L);
        this.resultadoPrueba.setPuntajeA(13);
        this.resultadoPrueba.setPuntajeB(7);
        this.resultadoPrueba.setEstadoValidacion("PENDIENTE");
        this.resultadoPrueba.setFechaRegistro(LocalDateTime.now());

        // PartidaDTO: la partida que retorna msvc-match por Feign
        this.partidaDTO = new PartidaDTO();
        this.partidaDTO.setPartidaId(50L);
        this.partidaDTO.setParticipanteAId(10L);
        this.partidaDTO.setParticipanteBId(20L);
        this.partidaDTO.setEstado("PROGRAMADA");
    }

    @Test
    @DisplayName("Debe encontrar un resultado por su ID")
    public void shouldFindResultadoById() {
        // Given
        when(this.resultadoRepository.findById(1L)).thenReturn(Optional.of(this.resultadoPrueba));
        // When
        ResultadoResponseDTO result = this.resultadoService.findById(1L);
        // Then
        assertThat(result.getResultadoId()).isEqualTo(1L);
        assertThat(result.getEstadoValidacion()).isEqualTo("PENDIENTE");
    }

    @Test
    @DisplayName("Debe lanzar ResultException al buscar ID inexistente")
    public void shouldThrowWhenResultadoNotFound() {
        // Given
        when(this.resultadoRepository.findById(9999L)).thenReturn(Optional.empty());
        // When + Then
        assertThatThrownBy(() -> this.resultadoService.findById(9999L))
                .isInstanceOf(ResultException.class)
                .hasMessage("Resultado con id: 9999 no encontrado");
    }

    @Test
    @DisplayName("Debe guardar resultado valido con estado PENDIENTE")
    public void shouldSaveResultadoValid() {
        // Given
        ResultadoRequestDTO dto = new ResultadoRequestDTO();
        dto.setPartidaId(50L);
        dto.setGanadorId(10L);
        dto.setPuntajeA(13);
        dto.setPuntajeB(7);

        when(this.matchClient.findById(50L)).thenReturn(this.partidaDTO);
        when(this.resultadoRepository.findByPartidaId(50L)).thenReturn(Optional.empty()); // no hay resultado previo
        when(this.resultadoRepository.save(any(Resultado.class))).thenReturn(this.resultadoPrueba);

        // When
        ResultadoResponseDTO result = this.resultadoService.save(dto);

        // Then
        assertThat(result.getEstadoValidacion()).isEqualTo("PENDIENTE");
        verify(matchClient, times(1)).findById(50L);
        verify(resultadoRepository, times(1)).save(any(Resultado.class));
    }

    @Test
    @DisplayName("Debe lanzar excepcion cuando la partida esta CANCELADA")
    public void shouldThrowWhenPartidaCancelada() {
        // Given
        ResultadoRequestDTO dto = new ResultadoRequestDTO();
        dto.setPartidaId(50L);
        dto.setGanadorId(10L);
        dto.setPuntajeA(13);
        dto.setPuntajeB(7);

        PartidaDTO cancelada = new PartidaDTO();
        cancelada.setEstado("CANCELADA");
        cancelada.setParticipanteAId(10L);
        cancelada.setParticipanteBId(20L);
        when(this.matchClient.findById(50L)).thenReturn(cancelada);

        // When + Then
        assertThatThrownBy(() -> this.resultadoService.save(dto))
                .isInstanceOf(ResultException.class)
                .hasMessageContaining("CANCELADA");
        verify(resultadoRepository, never()).save(any(Resultado.class));
    }

    @Test
    @DisplayName("Debe lanzar excepcion cuando ya existe resultado para la partida")
    public void shouldThrowWhenResultadoDuplicado() {
        // Given
        ResultadoRequestDTO dto = new ResultadoRequestDTO();
        dto.setPartidaId(50L);
        dto.setGanadorId(10L);
        dto.setPuntajeA(13);
        dto.setPuntajeB(7);

        when(this.matchClient.findById(50L)).thenReturn(this.partidaDTO);
        when(this.resultadoRepository.findByPartidaId(50L)).thenReturn(Optional.of(this.resultadoPrueba)); // ya existe

        // When + Then
        assertThatThrownBy(() -> this.resultadoService.save(dto))
                .isInstanceOf(ResultException.class)
                .hasMessageContaining("Ya existe");
        verify(resultadoRepository, never()).save(any(Resultado.class));
    }

    @Test
    @DisplayName("Debe lanzar excepcion cuando ganadorId no es participante de la partida")
    public void shouldThrowWhenGanadorIdInvalid() {
        // Given
        ResultadoRequestDTO dto = new ResultadoRequestDTO();
        dto.setPartidaId(50L);
        dto.setGanadorId(99L);
        dto.setPuntajeA(13);
        dto.setPuntajeB(7);

        when(this.matchClient.findById(50L)).thenReturn(this.partidaDTO);
        when(this.resultadoRepository.findByPartidaId(50L)).thenReturn(Optional.empty());

        // When + Then
        assertThatThrownBy(() -> this.resultadoService.save(dto))
                .isInstanceOf(ResultException.class)
                .hasMessageContaining("ganadorId");
        verify(resultadoRepository, never()).save(any(Resultado.class));
    }

    @Test
    @DisplayName("Debe validar un resultado PENDIENTE cambiandolo a VALIDADO")
    public void shouldValidarResultado() {
        // Given
        when(this.resultadoRepository.findById(1L)).thenReturn(Optional.of(this.resultadoPrueba));
        when(this.resultadoRepository.save(any(Resultado.class))).thenAnswer(inv -> inv.getArgument(0));
        // When
        ResultadoResponseDTO result = this.resultadoService.validar(1L);
        // Then
        assertThat(result.getEstadoValidacion()).isEqualTo("VALIDADO");
    }

    @Test
    @DisplayName("Debe lanzar excepcion al validar un resultado ya VALIDADO")
    public void shouldThrowWhenAlreadyValidated() {
        // Given
        this.resultadoPrueba.setEstadoValidacion("VALIDADO");
        when(this.resultadoRepository.findById(1L)).thenReturn(Optional.of(this.resultadoPrueba));
        // When + Then
        assertThatThrownBy(() -> this.resultadoService.validar(1L))
                .isInstanceOf(ResultException.class)
                .hasMessageContaining("VALIDADO");
        verify(resultadoRepository, never()).save(any(Resultado.class));
    }

    @Test
    @DisplayName("Debe anular un resultado con motivo obligatorio")
    public void shouldAnularResultado() {
        // Given
        when(this.resultadoRepository.findById(1L)).thenReturn(Optional.of(this.resultadoPrueba));
        when(this.resultadoRepository.save(any(Resultado.class))).thenAnswer(inv -> inv.getArgument(0));
        // When
        ResultadoResponseDTO result = this.resultadoService.anular(1L, "Error en el marcador");
        // Then
        assertThat(result.getEstadoValidacion()).isEqualTo("ANULADO");
        assertThat(result.getMotivoAnulacion()).isEqualTo("Error en el marcador");
    }

    @Test
    @DisplayName("Debe lanzar excepcion al anular sin motivo")
    public void shouldThrowWhenAnulacionSinMotivo() {
        // When + Then
        assertThatThrownBy(() -> this.resultadoService.anular(1L, ""))
                .isInstanceOf(ResultException.class)
                .hasMessageContaining("motivo");
        verify(resultadoRepository, never()).save(any(Resultado.class));
    }
}
