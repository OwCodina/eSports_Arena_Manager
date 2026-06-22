package com.esports.msvc_tournament.services;

import com.esports.msvc_tournament.clients.GameClient;
import com.esports.msvc_tournament.exceptions.TournamentException;
import com.esports.msvc_tournament.models.Torneo;
import com.esports.msvc_tournament.models.dtos.JuegoDTO;
import com.esports.msvc_tournament.models.dtos.TorneoRequestDTO;
import com.esports.msvc_tournament.models.dtos.TorneoResponseDTO;
import com.esports.msvc_tournament.repositories.TorneoRepository;
import feign.FeignException;
import net.datafaker.Faker;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Pruebas unitarias de {@link TorneoServiceImpl}.
 *
 * <p>Se mockean tanto el repositorio como el cliente Feign de msvc-game
 * para verificar las reglas de negocio sin levantar la red real.</p>
 *
 * <p>Casos críticos que se prueban:</p>
 * <ul>
 *   <li>Validación de juego ACTIVO via Feign antes de guardar.</li>
 *   <li>Validación de orden de fechas: cierre < inicio < fin.</li>
 *   <li>Manejo de error Feign cuando msvc-game no responde.</li>
 *   <li>Transiciones de estado: cancelar y cerrar torneos.</li>
 * </ul>
 */
@ExtendWith(MockitoExtension.class)
public class TorneoServiceTest {


    @Mock
    private TorneoRepository torneoRepository;


    @Mock
    private GameClient gameClient;


    @InjectMocks
    private TorneoServiceImpl torneoService;

    private Torneo torneoPrueba;
    private JuegoDTO juegoPrueba;
    private List<Torneo> torneoList;


    @BeforeEach
    public void setUp() {

        this.torneoPrueba = new Torneo();
        this.torneoPrueba.setTorneoId(1L);
        this.torneoPrueba.setNombre("Copa Valorant 2025");
        this.torneoPrueba.setJuegoId(10L);
        this.torneoPrueba.setFechaCierreInscripcion(LocalDate.of(2025, 7, 25));
        this.torneoPrueba.setFechaInicio(LocalDate.of(2025, 8, 1));
        this.torneoPrueba.setFechaFin(LocalDate.of(2025, 8, 10));
        this.torneoPrueba.setCupoMaximo(8);
        this.torneoPrueba.setEstado("BORRADOR");
        this.torneoPrueba.setModalidad("EQUIPOS");

        // JuegoDTO ACTIVO: lo que devolvería msvc-game al ser consultado via Feign
        this.juegoPrueba = new JuegoDTO();
        this.juegoPrueba.setJuegoId(10L);
        this.juegoPrueba.setNombre("Valorant");
        this.juegoPrueba.setEstado("ACTIVO");


        this.torneoList = new ArrayList<>();
        Faker faker = new Faker(Locale.of("es", "CL"));
        for (int i = 0; i < 30; i++) {
            Torneo t = new Torneo();
            t.setTorneoId((long) (i + 2));
            t.setNombre(faker.esports().game() + " Cup");
            t.setJuegoId(10L);
            t.setFechaCierreInscripcion(LocalDate.of(2025, 7, 1));
            t.setFechaInicio(LocalDate.of(2025, 7, 10));
            t.setFechaFin(LocalDate.of(2025, 7, 20));
            t.setCupoMaximo(4);
            t.setEstado("ABIERTO");
            t.setModalidad("EQUIPOS");
            this.torneoList.add(t);
        }
    }



    @Test
    @DisplayName("Debe listar todos los torneos")
    public void shouldListAllTorneos() {
        // Given
        List<Torneo> todos = new ArrayList<>(this.torneoList);
        todos.add(this.torneoPrueba);
        when(this.torneoRepository.findAll()).thenReturn(todos);

        // When
        List<TorneoResponseDTO> result = this.torneoService.findAll();

        // Then
        assertThat(result).hasSize(31);
        verify(torneoRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Debe filtrar torneos por estado ABIERTO")
    public void shouldFindByEstado() {
        // Given
        when(this.torneoRepository.findByEstado("ABIERTO")).thenReturn(this.torneoList);

        // When
        List<TorneoResponseDTO> result = this.torneoService.findByEstado("ABIERTO");

        // Then
        assertThat(result).hasSize(30);
        verify(torneoRepository, times(1)).findByEstado("ABIERTO");
    }



    @Test
    @DisplayName("Debe encontrar un torneo por su ID")
    public void shouldFindTorneoById() {
        // Given
        Long id = 1L;
        when(this.torneoRepository.findById(id)).thenReturn(Optional.of(this.torneoPrueba));

        // When
        TorneoResponseDTO result = this.torneoService.findById(id);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getTorneoId()).isEqualTo(1L);
        assertThat(result.getNombre()).isEqualTo("Copa Valorant 2025");
        assertThat(result.getEstado()).isEqualTo("BORRADOR");
        verify(torneoRepository, times(1)).findById(id);
    }

    @Test
    @DisplayName("Debe lanzar TournamentException al buscar ID inexistente")
    public void shouldThrowExceptionWhenTorneoNotFound() {
        // Given
        Long id = 9999L;
        when(this.torneoRepository.findById(id)).thenReturn(Optional.empty());

        // When + Then
        assertThatThrownBy(() -> this.torneoService.findById(id))
                .isInstanceOf(TournamentException.class)
                .hasMessage("Torneo con id: " + id + " no encontrado");

        verify(torneoRepository, times(1)).findById(id);
    }


    @Test
    @DisplayName("Debe guardar un torneo con juego ACTIVO y fechas válidas")
    public void shouldSaveTorneoWithActiveJuego() {
        // Given
        TorneoRequestDTO dto = new TorneoRequestDTO();
        dto.setNombre("Liga Esports 2025");
        dto.setJuegoId(10L);
        dto.setFechaCierreInscripcion(LocalDate.of(2025, 7, 25));
        dto.setFechaInicio(LocalDate.of(2025, 8, 1));
        dto.setFechaFin(LocalDate.of(2025, 8, 10));
        dto.setCupoMaximo(8);
        dto.setModalidad("EQUIPOS");

        // Feign devuelve el juego ACTIVO (llamada a msvc-game simulada)
        when(this.gameClient.findById(10L)).thenReturn(this.juegoPrueba);

        Torneo guardado = new Torneo();
        guardado.setTorneoId(2L);
        guardado.setNombre(dto.getNombre());
        guardado.setEstado("BORRADOR");
        when(this.torneoRepository.save(any(Torneo.class))).thenReturn(guardado);

        // When
        TorneoResponseDTO result = this.torneoService.save(dto);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getNombre()).isEqualTo("Liga Esports 2025");

        assertThat(result.getEstado()).isEqualTo("BORRADOR");
        verify(gameClient, times(1)).findById(10L);
        verify(torneoRepository, times(1)).save(any(Torneo.class));
    }


    @Test
    @DisplayName("Debe lanzar TournamentException cuando el juego está INACTIVO")
    public void shouldNotSaveTorneoWhenJuegoIsInactive() {
        // Given
        TorneoRequestDTO dto = new TorneoRequestDTO();
        dto.setJuegoId(10L);
        dto.setNombre("Torneo Juego Inactivo");
        dto.setFechaCierreInscripcion(LocalDate.of(2025, 7, 25));
        dto.setFechaInicio(LocalDate.of(2025, 8, 1));
        dto.setFechaFin(LocalDate.of(2025, 8, 10));
        dto.setCupoMaximo(8);
        dto.setModalidad("EQUIPOS");

        JuegoDTO juegoInactivo = new JuegoDTO();
        juegoInactivo.setJuegoId(10L);
        juegoInactivo.setNombre("Juego Antiguo");
        juegoInactivo.setEstado("INACTIVO");
        when(this.gameClient.findById(10L)).thenReturn(juegoInactivo);

        // When + Then
        assertThatThrownBy(() -> this.torneoService.save(dto))
                .isInstanceOf(TournamentException.class)
                .hasMessageContaining("INACTIVO");


        verify(torneoRepository, never()).save(any(Torneo.class));
    }


    @Test
    @DisplayName("Debe lanzar TournamentException cuando msvc-game no responde")
    public void shouldNotSaveTorneoWhenGameServiceUnavailable() {
        // Given
        TorneoRequestDTO dto = new TorneoRequestDTO();
        dto.setJuegoId(999L);
        dto.setNombre("Torneo sin Juego");
        dto.setFechaCierreInscripcion(LocalDate.of(2025, 7, 25));
        dto.setFechaInicio(LocalDate.of(2025, 8, 1));
        dto.setFechaFin(LocalDate.of(2025, 8, 10));
        dto.setCupoMaximo(8);
        dto.setModalidad("EQUIPOS");

        // mock(FeignException.class): simula error de comunicación HTTP con msvc-game
        when(this.gameClient.findById(999L)).thenThrow(mock(FeignException.class));

        // When + Then
        assertThatThrownBy(() -> this.torneoService.save(dto))
                .isInstanceOf(TournamentException.class)
                .hasMessageContaining("999");

        verify(torneoRepository, never()).save(any(Torneo.class));
    }

    @Test
    @DisplayName("Debe lanzar TournamentException cuando fecha cierre no es anterior a inicio")
    public void shouldNotSaveTorneoWhenFechasCierreInvalid() {
        // Given
        TorneoRequestDTO dto = new TorneoRequestDTO();
        dto.setJuegoId(10L);
        dto.setNombre("Torneo Fechas Mal");
        dto.setFechaCierreInscripcion(LocalDate.of(2025, 8, 1)); // igual a inicio
        dto.setFechaInicio(LocalDate.of(2025, 8, 1));
        dto.setFechaFin(LocalDate.of(2025, 8, 10));
        dto.setCupoMaximo(8);
        dto.setModalidad("EQUIPOS");

        when(this.gameClient.findById(10L)).thenReturn(this.juegoPrueba);

        // When + Then
        assertThatThrownBy(() -> this.torneoService.save(dto))
                .isInstanceOf(TournamentException.class)
                .hasMessageContaining("cierre");

        verify(torneoRepository, never()).save(any(Torneo.class));
    }

    @Test
    @DisplayName("Debe cancelar un torneo cambiando su estado a CANCELADO")
    public void shouldCancelTorneo() {
        // Given
        Long id = 1L;
        when(this.torneoRepository.findById(id)).thenReturn(Optional.of(this.torneoPrueba));
        when(this.torneoRepository.save(any(Torneo.class))).thenAnswer(inv -> inv.getArgument(0));

        // When
        TorneoResponseDTO result = this.torneoService.cancelar(id);

        // Then
        assertThat(result.getEstado()).isEqualTo("CANCELADO");
        verify(torneoRepository, times(1)).findById(id);
        verify(torneoRepository, times(1)).save(this.torneoPrueba);
    }

    @Test
    @DisplayName("Debe lanzar excepción al cancelar un torneo FINALIZADO")
    public void shouldThrowExceptionWhenCancelingFinishedTorneo() {
        // Given
        this.torneoPrueba.setEstado("FINALIZADO");
        Long id = 1L;
        when(this.torneoRepository.findById(id)).thenReturn(Optional.of(this.torneoPrueba));

        // When + Then
        assertThatThrownBy(() -> this.torneoService.cancelar(id))
                .isInstanceOf(TournamentException.class);

        verify(torneoRepository, never()).save(any(Torneo.class));
    }

    @Test
    @DisplayName("Debe cerrar un torneo cambiando su estado a CERRADO")
    public void shouldCloseTorneo() {
        // Given
        Long id = 1L;
        when(this.torneoRepository.findById(id)).thenReturn(Optional.of(this.torneoPrueba));
        when(this.torneoRepository.save(any(Torneo.class))).thenAnswer(inv -> inv.getArgument(0));

        // When
        TorneoResponseDTO result = this.torneoService.cerrar(id);

        // Then
        assertThat(result.getEstado()).isEqualTo("CERRADO");
        verify(torneoRepository, times(1)).save(this.torneoPrueba);
    }

    @Test
    @DisplayName("Debe verificar que un torneo ABIERTO retorna true en estaAbierto()")
    public void shouldReturnTrueWhenTorneoIsAbierto() {
        // Given
        this.torneoPrueba.setEstado("ABIERTO");
        Long id = 1L;
        when(this.torneoRepository.findById(id)).thenReturn(Optional.of(this.torneoPrueba));

        // When
        boolean result = this.torneoService.estaAbierto(id);

        // Then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("Debe verificar que un torneo CERRADO retorna false en estaAbierto()")
    public void shouldReturnFalseWhenTorneoIsClosed() {
        // Given
        this.torneoPrueba.setEstado("CERRADO");
        Long id = 1L;
        when(this.torneoRepository.findById(id)).thenReturn(Optional.of(this.torneoPrueba));

        // When
        boolean result = this.torneoService.estaAbierto(id);

        // Then
        assertThat(result).isFalse();
    }
}
