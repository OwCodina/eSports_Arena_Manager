package com.esports.msvc_team.services;

import com.esports.msvc_team.clients.GameClient;
import com.esports.msvc_team.clients.UserClient;
import com.esports.msvc_team.exceptions.TeamException;
import com.esports.msvc_team.models.Equipo;
import com.esports.msvc_team.models.MiembroEquipo;
import com.esports.msvc_team.models.dtos.*;
import com.esports.msvc_team.repositories.EquipoRepository;
import com.esports.msvc_team.repositories.MiembroEquipoRepository;
import feign.FeignException;
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

/**
 * Pruebas unitarias de {@link EquipoServiceImpl}.
 * Mockea: EquipoRepository, MiembroEquipoRepository, UserClient (Feign), GameClient (Feign).
 */
@ExtendWith(MockitoExtension.class)
public class TeamServiceTest {

    @Mock private EquipoRepository equipoRepository;
    @Mock private MiembroEquipoRepository miembroEquipoRepository;
    @Mock private UserClient userClient;
    @Mock private GameClient gameClient;

    @InjectMocks
    private EquipoServiceImpl equipoService;

    private Equipo equipoPrueba;
    private UsuarioDTO capitanActivo;
    private JuegoDTO juegoActivo;

    @BeforeEach
    public void setUp() {
        this.equipoPrueba = new Equipo();
        this.equipoPrueba.setEquipoId(1L);
        this.equipoPrueba.setNombre("Team Fuego");
        this.equipoPrueba.setCapitanId(10L);
        this.equipoPrueba.setJuegoPrincipalId(20L);
        this.equipoPrueba.setEstado("ACTIVO");


        this.capitanActivo = new UsuarioDTO();
        this.capitanActivo.setUsuarioId(10L);
        this.capitanActivo.setNickname("CapitanPro");
        this.capitanActivo.setEstado("ACTIVO");


        this.juegoActivo = new JuegoDTO();
        this.juegoActivo.setJuegoId(20L);
        this.juegoActivo.setNombre("Valorant");
        this.juegoActivo.setEstado("ACTIVO");
    }



    @Test
    @DisplayName("Debe listar todos los equipos")
    public void shouldListAllEquipos() {
        // Given
        Faker faker = new Faker(Locale.of("es", "CL"));
        List<Equipo> lista = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            Equipo e = new Equipo();
            e.setEquipoId((long)(i+2));
            e.setNombre(faker.esports().team());
            e.setEstado("ACTIVO");
            lista.add(e);
        }
        lista.add(this.equipoPrueba);
        when(this.equipoRepository.findAll()).thenReturn(lista);
        // When
        List<EquipoResponseDTO> result = this.equipoService.findAll();
        // Then
        assertThat(result).hasSize(11);
        verify(equipoRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Debe encontrar un equipo por ID")
    public void shouldFindEquipoById() {
        // Given
        when(this.equipoRepository.findById(1L)).thenReturn(Optional.of(this.equipoPrueba));
        // When
        EquipoResponseDTO result = this.equipoService.findById(1L);
        // Then
        assertThat(result.getNombre()).isEqualTo("Team Fuego");
        assertThat(result.getEstado()).isEqualTo("ACTIVO");
    }

    @Test
    @DisplayName("Debe lanzar TeamException al buscar equipo inexistente")
    public void shouldThrowWhenEquipoNotFound() {
        // Given
        when(this.equipoRepository.findById(9999L)).thenReturn(Optional.empty());
        // When + Then
        assertThatThrownBy(() -> this.equipoService.findById(9999L))
                .isInstanceOf(TeamException.class)
                .hasMessage("Equipo con id: 9999 no encontrado");
    }



    @Test
    @DisplayName("Debe guardar equipo con capitán ACTIVO y juego ACTIVO")
    public void shouldSaveEquipoValid() {
        // Given
        EquipoRequestDTO dto = new EquipoRequestDTO();
        dto.setNombre("Los Campeones");
        dto.setCapitanId(10L);
        dto.setJuegoPrincipalId(20L);

        when(this.equipoRepository.findByNombre("Los Campeones")).thenReturn(Optional.empty());
        when(this.userClient.findById(10L)).thenReturn(this.capitanActivo);
        when(this.gameClient.findById(20L)).thenReturn(this.juegoActivo);

        Equipo guardado = new Equipo();
        guardado.setEquipoId(2L);
        guardado.setNombre("Los Campeones");
        guardado.setEstado("ACTIVO");
        when(this.equipoRepository.save(any(Equipo.class))).thenReturn(guardado);

        // When
        EquipoResponseDTO result = this.equipoService.save(dto);

        // Then
        assertThat(result.getEstado()).isEqualTo("ACTIVO");
        verify(userClient, times(1)).findById(10L);
        verify(gameClient, times(1)).findById(20L);
        verify(equipoRepository, times(1)).save(any(Equipo.class));
    }

    @Test
    @DisplayName("Debe lanzar TeamException si el nombre ya existe")
    public void shouldThrowWhenNombreDuplicated() {
        // Given
        EquipoRequestDTO dto = new EquipoRequestDTO();
        dto.setNombre("Team Fuego");
        dto.setCapitanId(10L);
        dto.setJuegoPrincipalId(20L);
        when(this.equipoRepository.findByNombre("Team Fuego")).thenReturn(Optional.of(this.equipoPrueba));
        // When + Then
        assertThatThrownBy(() -> this.equipoService.save(dto))
                .isInstanceOf(TeamException.class)
                .hasMessage("Ya existe un equipo con el nombre: Team Fuego");
        verify(equipoRepository, never()).save(any(Equipo.class));
    }

    @Test
    @DisplayName("Debe lanzar TeamException si el capitán está INACTIVO")
    public void shouldThrowWhenCapitanInactivo() {
        // Given
        EquipoRequestDTO dto = new EquipoRequestDTO();
        dto.setNombre("Nuevo Equipo");
        dto.setCapitanId(10L);
        dto.setJuegoPrincipalId(20L);

        UsuarioDTO capitanInactivo = new UsuarioDTO();
        capitanInactivo.setEstado("INACTIVO");

        when(this.equipoRepository.findByNombre("Nuevo Equipo")).thenReturn(Optional.empty());
        when(this.userClient.findById(10L)).thenReturn(capitanInactivo);
        // When + Then
        assertThatThrownBy(() -> this.equipoService.save(dto))
                .isInstanceOf(TeamException.class)
                .hasMessageContaining("ACTIVO");
        verify(equipoRepository, never()).save(any(Equipo.class));
    }

    @Test
    @DisplayName("Debe lanzar TeamException si el juego está INACTIVO")
    public void shouldThrowWhenJuegoInactivo() {
        // Given
        EquipoRequestDTO dto = new EquipoRequestDTO();
        dto.setNombre("Nuevo Equipo");
        dto.setCapitanId(10L);
        dto.setJuegoPrincipalId(20L);

        JuegoDTO juegoInactivo = new JuegoDTO();
        juegoInactivo.setNombre("Juego Viejo");
        juegoInactivo.setEstado("INACTIVO");

        when(this.equipoRepository.findByNombre("Nuevo Equipo")).thenReturn(Optional.empty());
        when(this.userClient.findById(10L)).thenReturn(this.capitanActivo);
        when(this.gameClient.findById(20L)).thenReturn(juegoInactivo);
        // When + Then
        assertThatThrownBy(() -> this.equipoService.save(dto))
                .isInstanceOf(TeamException.class)
                .hasMessageContaining("ACTIVO");
        verify(equipoRepository, never()).save(any(Equipo.class));
    }

    @Test
    @DisplayName("Debe lanzar TeamException si msvc-user no responde al crear equipo")
    public void shouldThrowWhenUserClientFails() {
        // Given
        EquipoRequestDTO dto = new EquipoRequestDTO();
        dto.setNombre("Equipo X");
        dto.setCapitanId(999L);
        dto.setJuegoPrincipalId(20L);
        when(this.equipoRepository.findByNombre("Equipo X")).thenReturn(Optional.empty());
        when(this.userClient.findById(999L)).thenThrow(mock(FeignException.class));
        // When + Then
        assertThatThrownBy(() -> this.equipoService.save(dto))
                .isInstanceOf(TeamException.class)
                .hasMessageContaining("999");
        verify(equipoRepository, never()).save(any(Equipo.class));
    }



    @Test
    @DisplayName("Debe desactivar un equipo cambiando estado a INACTIVO")
    public void shouldDesactivarEquipo() {
        // Given
        when(this.equipoRepository.findById(1L)).thenReturn(Optional.of(this.equipoPrueba));
        when(this.equipoRepository.save(any(Equipo.class))).thenAnswer(inv -> inv.getArgument(0));
        // When
        EquipoResponseDTO result = this.equipoService.desactivar(1L);
        // Then
        assertThat(result.getEstado()).isEqualTo("INACTIVO");
    }



    @Test
    @DisplayName("Debe agregar miembro a equipo ACTIVO con usuario ACTIVO")
    public void shouldAddMiembro() {
        // Given
        MiembroRequestDTO dto = new MiembroRequestDTO();
        dto.setUsuarioId(50L);
        dto.setRolDentroEquipo("JUGADOR");

        UsuarioDTO usuario = new UsuarioDTO();
        usuario.setUsuarioId(50L);
        usuario.setEstado("ACTIVO");

        when(this.equipoRepository.findById(1L)).thenReturn(Optional.of(this.equipoPrueba));
        when(this.userClient.findById(50L)).thenReturn(usuario);
        when(this.miembroEquipoRepository.findByEquipoIdAndUsuarioId(1L, 50L)).thenReturn(Optional.empty());

        MiembroEquipo miembro = new MiembroEquipo();
        miembro.setMiembroId(1L);
        miembro.setEquipoId(1L);
        miembro.setUsuarioId(50L);
        when(this.miembroEquipoRepository.save(any(MiembroEquipo.class))).thenReturn(miembro);

        // When
        MiembroEquipo result = this.equipoService.agregarMiembro(1L, dto);

        // Then
        assertThat(result.getUsuarioId()).isEqualTo(50L);
        verify(miembroEquipoRepository, times(1)).save(any(MiembroEquipo.class));
    }

    @Test
    @DisplayName("Debe lanzar TeamException si el usuario ya es miembro")
    public void shouldThrowWhenMiembroDuplicated() {
        // Given
        MiembroRequestDTO dto = new MiembroRequestDTO();
        dto.setUsuarioId(50L);
        dto.setRolDentroEquipo("JUGADOR");

        UsuarioDTO usuario = new UsuarioDTO();
        usuario.setEstado("ACTIVO");

        when(this.equipoRepository.findById(1L)).thenReturn(Optional.of(this.equipoPrueba));
        when(this.userClient.findById(50L)).thenReturn(usuario);
        when(this.miembroEquipoRepository.findByEquipoIdAndUsuarioId(1L, 50L))
                .thenReturn(Optional.of(new MiembroEquipo()));
        // When + Then
        assertThatThrownBy(() -> this.equipoService.agregarMiembro(1L, dto))
                .isInstanceOf(TeamException.class)
                .hasMessageContaining("ya es miembro");
        verify(miembroEquipoRepository, never()).save(any(MiembroEquipo.class));
    }



    @Test
    @DisplayName("Debe retornar true cuando el equipo está ACTIVO")
    public void shouldReturnTrueWhenEquipoActivo() {
        // Given
        when(this.equipoRepository.findById(1L)).thenReturn(Optional.of(this.equipoPrueba));
        // When + Then
        assertThat(this.equipoService.estaActivo(1L)).isTrue();
    }

    @Test
    @DisplayName("Debe retornar false cuando el equipo está INACTIVO")
    public void shouldReturnFalseWhenEquipoInactivo() {
        // Given
        this.equipoPrueba.setEstado("INACTIVO");
        when(this.equipoRepository.findById(1L)).thenReturn(Optional.of(this.equipoPrueba));
        // When + Then
        assertThat(this.equipoService.estaActivo(1L)).isFalse();
    }
}
