package com.esports.msvc_auth.services;

import com.esports.msvc_auth.clients.UserClient;
import com.esports.msvc_auth.models.AuthUser;
import com.esports.msvc_auth.models.Rol;
import com.esports.msvc_auth.models.dtos.AuthResponseDTO;
import com.esports.msvc_auth.models.dtos.LoginRequestDTO;
import com.esports.msvc_auth.models.dtos.RegisterRequestDTO;
import com.esports.msvc_auth.repositories.AuthUserRepository;
import com.esports.msvc_auth.repositories.RolRepository;
import com.esports.msvc_auth.security.JwtService;
import net.datafaker.Faker;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashSet;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Pruebas unitarias de {@link AuthService}.
 *
 * <p>Mockea: AuthUserRepository, RolRepository, PasswordEncoder, JwtService, UserClient.
 * No levanta Spring ni conexión real a la BD.</p>
 *
 * <p>Cubre los casos críticos de autenticación:
 * <ul>
 *   <li>Register exitoso → devuelve JWT</li>
 *   <li>Register con email duplicado → 409 CONFLICT</li>
 *   <li>Register con rol inválido → 400 BAD_REQUEST</li>
 *   <li>Login exitoso → devuelve JWT</li>
 *   <li>Login con email inexistente → 401 UNAUTHORIZED</li>
 *   <li>Login con password incorrecta → 401 UNAUTHORIZED</li>
 *   <li>Login con cuenta INACTIVA → 403 FORBIDDEN</li>
 *   <li>Desactivar cuenta existente → estado INACTIVO</li>
 *   <li>Desactivar cuenta inexistente → 404 NOT_FOUND</li>
 *   <li>updatePassword con nueva password válida → guarda hash</li>
 * </ul>
 * </p>
 */
@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {

    @Mock private AuthUserRepository authUserRepository;
    @Mock private RolRepository rolRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtService jwtService;
    @Mock private UserClient userClient;

    @InjectMocks
    private AuthService authService;

    private AuthUser cuentaPrueba;
    private Rol rolJugador;

    @BeforeEach
    public void setUp() {
        // Rol de prueba
        this.rolJugador = new Rol();
        this.rolJugador.setRolId(1L);
        this.rolJugador.setNombre("ROLE_JUGADOR");

        // Cuenta activa de prueba
        this.cuentaPrueba = new AuthUser();
        this.cuentaPrueba.setCuentaAccesoId(1L);
        this.cuentaPrueba.setEmail("seba@esports.cl");
        this.cuentaPrueba.setPasswordHash("$2a$10$hasheado");
        this.cuentaPrueba.setRoles(new HashSet<>(Set.of(this.rolJugador)));
        this.cuentaPrueba.setEstado("ACTIVO");
    }

    // ── register ──────────────────────────────────────────────────

    @Test
    @DisplayName("Debe registrar una cuenta nueva y devolver un JWT")
    public void shouldRegisterAndReturnToken() {
        // Given
        RegisterRequestDTO dto = new RegisterRequestDTO();
        dto.setEmail("nuevo@esports.cl");
        dto.setPassword("Password123!");
        dto.setNombre("Carlos Muñoz");
        dto.setNickname("CarlosM99");
        dto.setRoles(Set.of("ROLE_JUGADOR"));

        AuthUser guardada = new AuthUser();
        guardada.setCuentaAccesoId(2L);
        guardada.setEmail("nuevo@esports.cl");
        guardada.setPasswordHash("$2a$10$hashed_nuevo");
        guardada.setRoles(new HashSet<>(Set.of(this.rolJugador)));
        guardada.setEstado("ACTIVO");

        when(this.authUserRepository.existsByEmail("nuevo@esports.cl")).thenReturn(false);
        when(this.rolRepository.findByNombre("ROLE_JUGADOR")).thenReturn(Optional.of(this.rolJugador));
        when(this.passwordEncoder.encode("Password123!")).thenReturn("$2a$10$hashed_nuevo");
        when(this.authUserRepository.save(any(AuthUser.class))).thenReturn(guardada);
        when(this.jwtService.generarToken(guardada)).thenReturn("jwt.token.valido");

        // When
        AuthResponseDTO result = this.authService.register(dto);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getToken()).isEqualTo("jwt.token.valido");
        assertThat(result.getEmail()).isEqualTo("nuevo@esports.cl");
        assertThat(result.getRoles()).contains("ROLE_JUGADOR");
        verify(authUserRepository, times(1)).save(any(AuthUser.class));
        verify(jwtService, times(1)).generarToken(guardada);
    }

    @Test
    @DisplayName("Debe lanzar 409 CONFLICT al registrar con email ya existente")
    public void shouldThrowConflictWhenEmailAlreadyExists() {
        // Given
        RegisterRequestDTO dto = new RegisterRequestDTO();
        dto.setEmail("seba@esports.cl"); // ya existe
        dto.setPassword("Password123!");
        dto.setNombre("Seba");
        dto.setNickname("seba99");
        dto.setRoles(Set.of("ROLE_JUGADOR"));

        when(this.authUserRepository.existsByEmail("seba@esports.cl")).thenReturn(true);

        // When + Then
        assertThatThrownBy(() -> this.authService.register(dto))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Ya existe una cuenta con ese email");

        verify(authUserRepository, never()).save(any(AuthUser.class));
    }

    @Test
    @DisplayName("Debe lanzar 400 BAD_REQUEST al registrar con rol que no existe en la BD")
    public void shouldThrowWhenRolDoesNotExist() {
        // Given
        RegisterRequestDTO dto = new RegisterRequestDTO();
        dto.setEmail("nuevo2@esports.cl");
        dto.setPassword("Password123!");
        dto.setNombre("Test");
        dto.setNickname("test99");
        dto.setRoles(Set.of("ROLE_SUPERADMIN")); // no existe

        when(this.authUserRepository.existsByEmail("nuevo2@esports.cl")).thenReturn(false);
        when(this.rolRepository.findByNombre("ROLE_SUPERADMIN")).thenReturn(Optional.empty());

        // When + Then
        assertThatThrownBy(() -> this.authService.register(dto))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Rol no existe");

        verify(authUserRepository, never()).save(any(AuthUser.class));
    }

    // ── login ─────────────────────────────────────────────────────

    @Test
    @DisplayName("Debe hacer login exitoso y devolver JWT cuando credenciales son correctas")
    public void shouldLoginSuccessfully() {
        // Given
        LoginRequestDTO dto = new LoginRequestDTO();
        dto.setEmail("seba@esports.cl");
        dto.setPassword("Password123!");

        when(this.authUserRepository.findByEmail("seba@esports.cl"))
                .thenReturn(Optional.of(this.cuentaPrueba));
        when(this.passwordEncoder.matches("Password123!", "$2a$10$hasheado")).thenReturn(true);
        when(this.jwtService.generarToken(this.cuentaPrueba)).thenReturn("jwt.token.generado");

        // When
        AuthResponseDTO result = this.authService.login(dto);

        // Then
        assertThat(result.getToken()).isEqualTo("jwt.token.generado");
        assertThat(result.getEmail()).isEqualTo("seba@esports.cl");
        verify(jwtService, times(1)).generarToken(this.cuentaPrueba);
    }

    @Test
    @DisplayName("Debe lanzar 401 UNAUTHORIZED cuando el email no existe")
    public void shouldThrowUnauthorizedWhenEmailNotFound() {
        // Given
        LoginRequestDTO dto = new LoginRequestDTO();
        dto.setEmail("noexiste@esports.cl");
        dto.setPassword("cualquiera");

        when(this.authUserRepository.findByEmail("noexiste@esports.cl")).thenReturn(Optional.empty());

        // When + Then
        assertThatThrownBy(() -> this.authService.login(dto))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Credenciales invalidas");

        verify(jwtService, never()).generarToken(any());
    }

    @Test
    @DisplayName("Debe lanzar 401 UNAUTHORIZED cuando la password es incorrecta")
    public void shouldThrowUnauthorizedWhenPasswordWrong() {
        // Given
        LoginRequestDTO dto = new LoginRequestDTO();
        dto.setEmail("seba@esports.cl");
        dto.setPassword("password_incorrecta");

        when(this.authUserRepository.findByEmail("seba@esports.cl"))
                .thenReturn(Optional.of(this.cuentaPrueba));
        when(this.passwordEncoder.matches("password_incorrecta", "$2a$10$hasheado")).thenReturn(false);

        // When + Then
        assertThatThrownBy(() -> this.authService.login(dto))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Credenciales invalidas");

        verify(jwtService, never()).generarToken(any());
    }

    @Test
    @DisplayName("Debe lanzar 403 FORBIDDEN cuando la cuenta está INACTIVA")
    public void shouldThrowForbiddenWhenAccountInactive() {
        // Given
        this.cuentaPrueba.setEstado("INACTIVO");
        LoginRequestDTO dto = new LoginRequestDTO();
        dto.setEmail("seba@esports.cl");
        dto.setPassword("Password123!");

        when(this.authUserRepository.findByEmail("seba@esports.cl"))
                .thenReturn(Optional.of(this.cuentaPrueba));

        // When + Then
        assertThatThrownBy(() -> this.authService.login(dto))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Cuenta inactiva");

        verify(jwtService, never()).generarToken(any());
    }

    // ── desactivar ────────────────────────────────────────────────

    @Test
    @DisplayName("Debe desactivar una cuenta cambiando su estado a INACTIVO")
    public void shouldDesactivarCuenta() {
        // Given
        when(this.authUserRepository.findById(1L)).thenReturn(Optional.of(this.cuentaPrueba));
        when(this.authUserRepository.save(any(AuthUser.class))).thenAnswer(inv -> inv.getArgument(0));

        // When
        var result = this.authService.desactivar(1L);

        // Then
        assertThat(result.getEstado()).isEqualTo("INACTIVO");
        verify(authUserRepository, times(1)).save(this.cuentaPrueba);
    }

    @Test
    @DisplayName("Debe lanzar 404 NOT_FOUND al desactivar cuenta inexistente")
    public void shouldThrowNotFoundWhenDesactivarNonExistent() {
        // Given
        when(this.authUserRepository.findById(9999L)).thenReturn(Optional.empty());

        // When + Then
        assertThatThrownBy(() -> this.authService.desactivar(9999L))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("9999");

        verify(authUserRepository, never()).save(any(AuthUser.class));
    }

    // ── updatePassword ────────────────────────────────────────────

    @Test
    @DisplayName("Debe actualizar la password correctamente")
    public void shouldUpdatePassword() {
        // Given
        Faker faker = new Faker(Locale.of("es", "CL"));
        String nuevaPass = faker.internet().password(10, 20, true, true, true);

        when(this.authUserRepository.findById(1L)).thenReturn(Optional.of(this.cuentaPrueba));
        when(this.passwordEncoder.encode(nuevaPass)).thenReturn("$2a$10$nuevo_hash");
        when(this.authUserRepository.save(any(AuthUser.class))).thenAnswer(inv -> inv.getArgument(0));

        // When
        var result = this.authService.updatePassword(1L, nuevaPass);

        // Then: la cuenta se guardó (el hash lo maneja BCrypt internamente)
        assertThat(result).isNotNull();
        verify(passwordEncoder, times(1)).encode(nuevaPass);
        verify(authUserRepository, times(1)).save(this.cuentaPrueba);
    }

    @Test
    @DisplayName("Debe lanzar 400 BAD_REQUEST al actualizar password con valor vacío")
    public void shouldThrowWhenPasswordBlank() {
        // When + Then
        assertThatThrownBy(() -> this.authService.updatePassword(1L, ""))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("newPassword es obligatorio");

        verify(authUserRepository, never()).save(any(AuthUser.class));
    }
}
