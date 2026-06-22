package com.esports.msvc_game.services;

import com.esports.msvc_game.exceptions.GameException;
import com.esports.msvc_game.models.Juego;
import com.esports.msvc_game.models.dtos.JuegoRequestDTO;
import com.esports.msvc_game.models.dtos.JuegoResponseDTO;
import com.esports.msvc_game.repositories.JuegoRepository;
import net.datafaker.Faker;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;



@ExtendWith(MockitoExtension.class)
public class JuegoServiceTest {


    @Mock
    private JuegoRepository juegoRepository;


    @InjectMocks
    private JuegoServiceImpl juegoService;

    private Juego juegoPrueba;
    private List<Juego> juegoList;


    @BeforeEach
    public void setUp() {

        this.juegoPrueba = new Juego();
        this.juegoPrueba.setJuegoId(1L);
        this.juegoPrueba.setNombre("Valorant");
        this.juegoPrueba.setGenero("FPS");
        this.juegoPrueba.setModalidad("EQUIPOS");
        this.juegoPrueba.setJugadoresPorEquipo(5);
        this.juegoPrueba.setEstado("ACTIVO");

        this.juegoList = new ArrayList<>();
        Faker faker = new Faker(Locale.of("es", "CL"));
        for (int i = 0; i < 50; i++) {
            Juego j = new Juego();
            j.setJuegoId((long) (i + 2));
            j.setNombre(faker.esports().game());
            j.setGenero("FPS");
            j.setModalidad("EQUIPOS");
            j.setJugadoresPorEquipo(5);
            j.setEstado("ACTIVO");
            this.juegoList.add(j);
        }
    }


    @Test
    @DisplayName("Debe listar todos los juegos")
    public void shouldListAllJuegos() {
        // Given
        List<Juego> todos = new ArrayList<>(this.juegoList);
        todos.add(this.juegoPrueba);
        when(this.juegoRepository.findAll()).thenReturn(todos);

        // When
        List<JuegoResponseDTO> result = this.juegoService.findAll();

        // Then
        assertThat(result).hasSize(51);
        verify(juegoRepository, times(1)).findAll();
    }


    @Test
    @DisplayName("Debe retornar lista vacía cuando no hay juegos")
    public void shouldReturnEmptyListWhenNoJuegos() {
        // Given
        when(this.juegoRepository.findAll()).thenReturn(List.of());

        // When
        List<JuegoResponseDTO> result = this.juegoService.findAll();

        // Then
        assertThat(result).isEmpty();
        verify(juegoRepository, times(1)).findAll();
    }


    @Test
    @DisplayName("Debe listar solo los juegos ACTIVOS")
    public void shouldListOnlyActiveJuegos() {
        // Given
        when(this.juegoRepository.findByEstado("ACTIVO")).thenReturn(List.of(this.juegoPrueba));

        // When
        List<JuegoResponseDTO> result = this.juegoService.findAllActivos();

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getEstado()).isEqualTo("ACTIVO");
        verify(juegoRepository, times(1)).findByEstado("ACTIVO");
    }

    @Test
    @DisplayName("Debe encontrar un juego por su ID")
    public void shouldFindJuegoById() {
        // Given
        Long id = 1L;
        when(this.juegoRepository.findById(id)).thenReturn(Optional.of(this.juegoPrueba));

        // When
        JuegoResponseDTO result = this.juegoService.findById(id);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getJuegoId()).isEqualTo(1L);
        assertThat(result.getNombre()).isEqualTo("Valorant");
        assertThat(result.getGenero()).isEqualTo("FPS");
        assertThat(result.getEstado()).isEqualTo("ACTIVO");
        verify(juegoRepository, times(1)).findById(id);
    }


    @Test
    @DisplayName("Debe lanzar GameException al buscar un ID inexistente")
    public void shouldThrowExceptionWhenJuegoNotFound() {
        // Given
        Long id = 9999L;
        when(this.juegoRepository.findById(id)).thenReturn(Optional.empty());

        // When + Then
        assertThatThrownBy(() -> this.juegoService.findById(id))
                .isInstanceOf(GameException.class)
                .hasMessage("Juego con id: " + id + " no encontrado");

        verify(juegoRepository, times(1)).findById(id);
    }


    @Test
    @DisplayName("Debe guardar un juego nuevo con estado ACTIVO")
    public void shouldSaveNewJuego() {
        // Given
        JuegoRequestDTO dto = new JuegoRequestDTO();
        dto.setNombre("Counter-Strike 2");
        dto.setGenero("FPS");
        dto.setModalidad("EQUIPOS");
        dto.setJugadoresPorEquipo(5);


        when(this.juegoRepository.findByNombre(dto.getNombre())).thenReturn(Optional.empty());

        Juego guardado = new Juego();
        guardado.setJuegoId(2L);
        guardado.setNombre(dto.getNombre());
        guardado.setGenero(dto.getGenero());
        guardado.setModalidad(dto.getModalidad());
        guardado.setJugadoresPorEquipo(dto.getJugadoresPorEquipo());
        guardado.setEstado("ACTIVO");
        when(this.juegoRepository.save(any(Juego.class))).thenReturn(guardado);

        // When
        JuegoResponseDTO result = this.juegoService.save(dto);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getNombre()).isEqualTo("Counter-Strike 2");

        assertThat(result.getEstado()).isEqualTo("ACTIVO");
        verify(juegoRepository, times(1)).findByNombre(dto.getNombre());
        verify(juegoRepository, times(1)).save(any(Juego.class));
    }


    @Test
    @DisplayName("Debe lanzar GameException al guardar nombre duplicado")
    public void shouldThrowExceptionWhenNombreDuplicated() {
        // Given
        JuegoRequestDTO dto = new JuegoRequestDTO();
        dto.setNombre("Valorant");
        dto.setGenero("FPS");
        dto.setModalidad("EQUIPOS");
        dto.setJugadoresPorEquipo(5);

        when(this.juegoRepository.findByNombre("Valorant")).thenReturn(Optional.of(this.juegoPrueba));

        // When + Then
        assertThatThrownBy(() -> this.juegoService.save(dto))
                .isInstanceOf(GameException.class)
                .hasMessage("Ya existe un juego con el nombre: Valorant");

        verify(juegoRepository, never()).save(any(Juego.class));
    }


    @Test
    @DisplayName("Debe actualizar un juego existente")
    public void shouldUpdateJuego() {
        // Given
        Long id = 1L;
        JuegoRequestDTO cambios = new JuegoRequestDTO();
        cambios.setNombre("Valorant 2025");
        cambios.setGenero("FPS Táctico");
        cambios.setModalidad("EQUIPOS");
        cambios.setJugadoresPorEquipo(5);

        when(this.juegoRepository.findById(id)).thenReturn(Optional.of(this.juegoPrueba));
        // Nombre nuevo no existe
        when(this.juegoRepository.findByNombre("Valorant 2025")).thenReturn(Optional.empty());
        // Devuelve el objeto que le pasaron (simula el save real)
        when(this.juegoRepository.save(any(Juego.class))).thenAnswer(inv -> inv.getArgument(0));

        // When
        JuegoResponseDTO result = this.juegoService.updateById(id, cambios);

        // Then
        assertThat(result.getNombre()).isEqualTo("Valorant 2025");
        assertThat(result.getGenero()).isEqualTo("FPS Táctico");
        verify(juegoRepository, times(1)).findById(id);
        verify(juegoRepository, times(1)).save(this.juegoPrueba);
    }


    @Test
    @DisplayName("Debe lanzar GameException al actualizar un ID inexistente")
    public void shouldThrowExceptionWhenUpdatingNonExistentJuego() {
        // Given
        Long id = 9999L;
        when(this.juegoRepository.findById(id)).thenReturn(Optional.empty());

        // When + Then
        assertThatThrownBy(() -> this.juegoService.updateById(id, new JuegoRequestDTO()))
                .isInstanceOf(GameException.class)
                .hasMessage("Juego con id: " + id + " no encontrado");

        verify(juegoRepository, never()).save(any(Juego.class));
    }


    @Test
    @DisplayName("Debe lanzar GameException al actualizar con nombre duplicado")
    public void shouldThrowExceptionWhenUpdatingWithDuplicatedNombre() {
        // Given
        Long id = 1L;
        JuegoRequestDTO cambios = new JuegoRequestDTO();
        cambios.setNombre("League of Legends");

        Juego otroJuego = new Juego();
        otroJuego.setJuegoId(2L);
        otroJuego.setNombre("League of Legends");

        when(this.juegoRepository.findById(id)).thenReturn(Optional.of(this.juegoPrueba));
        when(this.juegoRepository.findByNombre("League of Legends")).thenReturn(Optional.of(otroJuego));

        // When + Then
        assertThatThrownBy(() -> this.juegoService.updateById(id, cambios))
                .isInstanceOf(GameException.class)
                .hasMessage("Ya existe un juego con el nombre: League of Legends");

        verify(juegoRepository, never()).save(any(Juego.class));
    }


    @Test
    @DisplayName("Debe desactivar un juego cambiando su estado a INACTIVO")
    public void shouldDeactivateJuego() {
        // Given
        Long id = 1L;
        when(this.juegoRepository.findById(id)).thenReturn(Optional.of(this.juegoPrueba));
        when(this.juegoRepository.save(any(Juego.class))).thenAnswer(inv -> inv.getArgument(0));

        // When
        JuegoResponseDTO result = this.juegoService.desactivar(id);

        // Then
        assertThat(result.getEstado()).isEqualTo("INACTIVO");
        verify(juegoRepository, times(1)).findById(id);
        verify(juegoRepository, times(1)).save(this.juegoPrueba);
    }

    @Test
    @DisplayName("Debe lanzar GameException al desactivar un ID inexistente")
    public void shouldThrowExceptionWhenDeactivatingNonExistentJuego() {
        // Given
        Long id = 9999L;
        when(this.juegoRepository.findById(id)).thenReturn(Optional.empty());

        // When + Then
        assertThatThrownBy(() -> this.juegoService.desactivar(id))
                .isInstanceOf(GameException.class)
                .hasMessage("Juego con id: " + id + " no encontrado");

        verify(juegoRepository, never()).save(any(Juego.class));
    }
}
