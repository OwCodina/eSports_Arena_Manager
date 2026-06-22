package com.esports.msvc_user.services;

import com.esports.msvc_user.exceptions.UserException;
import com.esports.msvc_user.models.Usuario;
import com.esports.msvc_user.models.dtos.UsuarioRequestDTO;
import com.esports.msvc_user.models.dtos.UsuarioResponseDTO;
import com.esports.msvc_user.repositories.UsuarioRepository;
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
 * Pruebas unitarias de {@link UsuarioServiceImpl}.
 * msvc-user no tiene Feign: solo se mockea el repositorio.
 */
@ExtendWith(MockitoExtension.class)
public class UsuarioServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private UsuarioServiceImpl usuarioService;

    private Usuario usuarioPrueba;
    private List<Usuario> usuarioList;

    @BeforeEach
    public void setUp() {
        this.usuarioPrueba = new Usuario();
        this.usuarioPrueba.setUsuarioId(1L);
        this.usuarioPrueba.setNombre("Felipe Alvarez");
        this.usuarioPrueba.setNickname("Felipe996");
        this.usuarioPrueba.setEmail("felipe@esports.cl");
        this.usuarioPrueba.setRol("JUGADOR");
        this.usuarioPrueba.setEstado("ACTIVO");

        this.usuarioList = new ArrayList<>();
        Faker faker = new Faker(Locale.of("es", "CL"));
        for (int i = 0; i < 40; i++) {
            Usuario u = new Usuario();
            u.setUsuarioId((long) (i + 2));
            u.setNombre(faker.name().fullName());
            u.setNickname(faker.name().firstName()+ faker.lorem().characters(1, 3, false, true));
            u.setEmail(faker.internet().emailAddress());
            u.setRol("JUGADOR");
            u.setEstado("ACTIVO");
            this.usuarioList.add(u);
        }
    }


    @Test
    @DisplayName("Debe listar todos los usuarios")
    public void shouldListAllUsuarios() {
        // Given
        List<Usuario> todos = new ArrayList<>(this.usuarioList);
        todos.add(this.usuarioPrueba);
        when(this.usuarioRepository.findAll()).thenReturn(todos);
        // When
        List<UsuarioResponseDTO> result = this.usuarioService.findAll();
        // Then
        assertThat(result).hasSize(41);
        verify(usuarioRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Debe filtrar usuarios por rol JUGADOR")
    public void shouldFindByRol() {
        // Given
        when(this.usuarioRepository.findByRol("JUGADOR")).thenReturn(this.usuarioList);
        // When
        List<UsuarioResponseDTO> result = this.usuarioService.findByRol("JUGADOR");
        // Then
        assertThat(result).hasSize(40);
        verify(usuarioRepository, times(1)).findByRol("JUGADOR");
    }

    @Test
    @DisplayName("Debe filtrar usuarios por estado ACTIVO")
    public void shouldFindByEstado() {
        // Given
        when(this.usuarioRepository.findByEstado("ACTIVO")).thenReturn(List.of(this.usuarioPrueba));
        // When
        List<UsuarioResponseDTO> result = this.usuarioService.findByEstado("ACTIVO");
        // Then
        assertThat(result).hasSize(1);
        verify(usuarioRepository, times(1)).findByEstado("ACTIVO");
    }



    @Test
    @DisplayName("Debe encontrar un usuario por su ID")
    public void shouldFindUsuarioById() {

        Long id = 1L;
        when(this.usuarioRepository.findById(id)).thenReturn(Optional.of(this.usuarioPrueba));

        UsuarioResponseDTO result = this.usuarioService.findById(id);

        assertThat(result).isNotNull();
        assertThat(result.getNickname()).isEqualTo("Felipe996");
        assertThat(result.getEstado()).isEqualTo("ACTIVO");
        verify(usuarioRepository, times(1)).findById(id);
    }

    @Test
    @DisplayName("Debe lanzar UserException al buscar ID inexistente")
    public void shouldThrowExceptionWhenUsuarioNotFound() {

        Long id = 9999L;
        when(this.usuarioRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> this.usuarioService.findById(id))
                .isInstanceOf(UserException.class)
                .hasMessage("Usuario con id: " + id + " no encontrado");
        verify(usuarioRepository, times(1)).findById(id);
    }

    @Test
    @DisplayName("Debe encontrar un usuario por su nickname")
    public void shouldFindByNickname() {

        when(this.usuarioRepository.findByNickname("Felipe996")).thenReturn(Optional.of(this.usuarioPrueba));

        UsuarioResponseDTO result = this.usuarioService.findByNickname("Felipe996");

        assertThat(result.getNickname()).isEqualTo("Felipe996");
    }



    @Test
    @DisplayName("Debe guardar un usuario nuevo con estado ACTIVO y fecha de registro")
    public void shouldSaveNewUsuario() {

        UsuarioRequestDTO dto = new UsuarioRequestDTO();
        dto.setNombre("Ana Torres");
        dto.setNickname("AnaT");
        dto.setEmail("ana@esports.cl");
        dto.setRol("ORGANIZADOR");

        when(this.usuarioRepository.findByNickname("AnaT")).thenReturn(Optional.empty());
        when(this.usuarioRepository.findByEmail("ana@esports.cl")).thenReturn(Optional.empty());

        Usuario guardado = new Usuario();
        guardado.setUsuarioId(2L);
        guardado.setNombre("Ana Torres");
        guardado.setNickname("AnaT");
        guardado.setEmail("ana@esports.cl");
        guardado.setRol("ORGANIZADOR");
        guardado.setEstado("ACTIVO");
        when(this.usuarioRepository.save(any(Usuario.class))).thenReturn(guardado);


        UsuarioResponseDTO result = this.usuarioService.save(dto);


        assertThat(result.getEstado()).isEqualTo("ACTIVO");
        assertThat(result.getNickname()).isEqualTo("AnaT");
        verify(usuarioRepository, times(1)).save(any(Usuario.class));
    }

    @Test
    @DisplayName("Debe lanzar UserException al guardar nickname duplicado")
    public void shouldThrowWhenNicknameDuplicated() {

        UsuarioRequestDTO dto = new UsuarioRequestDTO();
        dto.setNickname("Felipe996");
        dto.setEmail("\"felipe@esports.cl");
        when(this.usuarioRepository.findByNickname("Felipe996")).thenReturn(Optional.of(this.usuarioPrueba));

        assertThatThrownBy(() -> this.usuarioService.save(dto))
                .isInstanceOf(UserException.class)
                .hasMessage("Ya existe un usuario con el nickname: Felipe996");
        verify(usuarioRepository, never()).save(any(Usuario.class));
    }

    @Test
    @DisplayName("Debe lanzar UserException al guardar email duplicado")
    public void shouldThrowWhenEmailDuplicated() {

        UsuarioRequestDTO dto = new UsuarioRequestDTO();
        dto.setNickname("NuevoNick");
        dto.setEmail("felipe@esports.cl");
        when(this.usuarioRepository.findByNickname("NuevoNick")).thenReturn(Optional.empty());
        when(this.usuarioRepository.findByEmail("felipe@esports.cl")).thenReturn(Optional.of(this.usuarioPrueba));

        assertThatThrownBy(() -> this.usuarioService.save(dto))
                .isInstanceOf(UserException.class)
                .hasMessage("Ya existe un usuario con el email: felipe@esports.cl");
        verify(usuarioRepository, never()).save(any(Usuario.class));
    }



    @Test
    @DisplayName("Debe desactivar un usuario cambiando su estado a INACTIVO")
    public void shouldDesactivarUsuario() {

        Long id = 1L;
        when(this.usuarioRepository.findById(id)).thenReturn(Optional.of(this.usuarioPrueba));
        when(this.usuarioRepository.save(any(Usuario.class))).thenAnswer(inv -> inv.getArgument(0));

        UsuarioResponseDTO result = this.usuarioService.desactivar(id);

        assertThat(result.getEstado()).isEqualTo("INACTIVO");
        verify(usuarioRepository, times(1)).save(this.usuarioPrueba);
    }

    // ── puedeCompetitr ────────────────────────────────────────────

    @Test
    @DisplayName("Debe retornar true cuando usuario está ACTIVO")
    public void shouldReturnTrueWhenUsuarioActivo() {
        // Given
        when(this.usuarioRepository.findById(1L)).thenReturn(Optional.of(this.usuarioPrueba));
        // When + Then
        assertThat(this.usuarioService.puedeCompetitr(1L)).isTrue();
    }

    @Test
    @DisplayName("Debe retornar false cuando usuario está INACTIVO")
    public void shouldReturnFalseWhenUsuarioInactivo() {
        // Given
        this.usuarioPrueba.setEstado("INACTIVO");
        when(this.usuarioRepository.findById(1L)).thenReturn(Optional.of(this.usuarioPrueba));
        // When + Then
        assertThat(this.usuarioService.puedeCompetitr(1L)).isFalse();
    }
}
