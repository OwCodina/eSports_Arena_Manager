package com.esports.msvc_sanction.services;

import com.esports.msvc_sanction.clients.UserClient;
import com.esports.msvc_sanction.exceptions.SanctionException;
import com.esports.msvc_sanction.models.Sancion;
import com.esports.msvc_sanction.models.dtos.SancionRequestDTO;
import com.esports.msvc_sanction.models.dtos.SancionResponseDTO;
import com.esports.msvc_sanction.repositories.SancionRepository;
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
 * Pruebas unitarias de {@link SancionServiceImpl}.
 * Mockea repositorio + UserClient (Feign hacia msvc-user).
 */
@ExtendWith(MockitoExtension.class)
public class SancionServiceTest {

    @Mock
    private SancionRepository sancionRepository;

    @Mock
    private UserClient userClient;

    @InjectMocks
    private SancionServiceImpl sancionService;

    private Sancion sancionPrueba;

    @BeforeEach
    public void setUp() {
        this.sancionPrueba = new Sancion();
        this.sancionPrueba.setSancionId(1L);
        this.sancionPrueba.setUsuarioId(10L);
        this.sancionPrueba.setMotivo("Comportamiento antideportivo");
        this.sancionPrueba.setFechaInicio(LocalDate.now());
        this.sancionPrueba.setFechaFin(LocalDate.now().plusDays(15));
        this.sancionPrueba.setSeveridad("SUSPENSION_TEMPORAL");
        this.sancionPrueba.setEstado("ACTIVA");
    }



    @Test
    @DisplayName("Debe listar todas las sanciones")
    public void shouldListAllSanciones() {
        // Given
        Faker faker = new Faker(Locale.of("es", "CL"));
        List<Sancion> lista = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            Sancion s = new Sancion();
            s.setSancionId((long) (i + 2));
            s.setMotivo(faker.lorem().sentence());
            s.setEstado("ACTIVA");
            lista.add(s);
        }
        lista.add(this.sancionPrueba);
        when(this.sancionRepository.findAll()).thenReturn(lista);
        // When
        List<SancionResponseDTO> result = this.sancionService.findAll();
        // Then
        assertThat(result).hasSize(21);
        verify(sancionRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Debe encontrar una sanción por su ID")
    public void shouldFindSancionById() {
        // Given
        when(this.sancionRepository.findById(1L)).thenReturn(Optional.of(this.sancionPrueba));
        // When
        SancionResponseDTO result = this.sancionService.findById(1L);
        // Then
        assertThat(result.getSancionId()).isEqualTo(1L);
        assertThat(result.getEstado()).isEqualTo("ACTIVA");
        verify(sancionRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("Debe lanzar SanctionException al buscar ID inexistente")
    public void shouldThrowWhenSancionNotFound() {
        // Given
        when(this.sancionRepository.findById(9999L)).thenReturn(Optional.empty());
        // When + Then
        assertThatThrownBy(() -> this.sancionService.findById(9999L))
                .isInstanceOf(SanctionException.class)
                .hasMessage("Sanción con id: 9999 no encontrada");
    }



    @Test
    @DisplayName("Debe guardar sanción válida a un usuario con estado ACTIVA")
    public void shouldSaveSancion() {
        // Given
        SancionRequestDTO dto = new SancionRequestDTO();
        dto.setUsuarioId(10L);
        dto.setMotivo("Trampa detectada");
        dto.setFechaInicio(LocalDate.now());
        dto.setFechaFin(LocalDate.now().plusDays(7));
        dto.setSeveridad("ADVERTENCIA");

        when(this.userClient.findById(10L)).thenReturn(null); // Feign verifica que existe
        when(this.sancionRepository.save(any(Sancion.class))).thenReturn(this.sancionPrueba);

        // When
        SancionResponseDTO result = this.sancionService.save(dto);

        // Then
        assertThat(result.getEstado()).isEqualTo("ACTIVA");
        verify(userClient, times(1)).findById(10L);
        verify(sancionRepository, times(1)).save(any(Sancion.class));
    }

    @Test
    @DisplayName("Debe lanzar SanctionException cuando sanción no tiene destinatario")
    public void shouldThrowWhenNoDestinatario() {
        // Given: ni usuarioId ni equipoId
        SancionRequestDTO dto = new SancionRequestDTO();
        dto.setMotivo("Sin destino");
        dto.setFechaInicio(LocalDate.now());
        dto.setFechaFin(LocalDate.now().plusDays(5));
        dto.setSeveridad("ADVERTENCIA");
        // When + Then
        assertThatThrownBy(() -> this.sancionService.save(dto))
                .isInstanceOf(SanctionException.class)
                .hasMessageContaining("usuarioId o equipoId");
        verify(sancionRepository, never()).save(any(Sancion.class));
    }

    @Test
    @DisplayName("Debe lanzar SanctionException cuando fechaFin no es posterior a fechaInicio")
    public void shouldThrowWhenFechasInvalidas() {
        // Given: fechaFin == fechaInicio
        SancionRequestDTO dto = new SancionRequestDTO();
        dto.setUsuarioId(10L);
        dto.setMotivo("Trampa");
        dto.setFechaInicio(LocalDate.now());
        dto.setFechaFin(LocalDate.now()); // igual, no después
        dto.setSeveridad("ADVERTENCIA");
        // When + Then
        assertThatThrownBy(() -> this.sancionService.save(dto))
                .isInstanceOf(SanctionException.class)
                .hasMessageContaining("posterior");
        verify(sancionRepository, never()).save(any(Sancion.class));
    }

    @Test
    @DisplayName("Debe lanzar SanctionException cuando msvc-user no responde")
    public void shouldThrowWhenUserServiceFails() {
        // Given
        SancionRequestDTO dto = new SancionRequestDTO();
        dto.setUsuarioId(999L);
        dto.setMotivo("Trampa");
        dto.setFechaInicio(LocalDate.now());
        dto.setFechaFin(LocalDate.now().plusDays(5));
        dto.setSeveridad("ADVERTENCIA");
        when(this.userClient.findById(999L)).thenThrow(mock(FeignException.class));
        // When + Then
        assertThatThrownBy(() -> this.sancionService.save(dto))
                .isInstanceOf(SanctionException.class)
                .hasMessageContaining("999");
        verify(sancionRepository, never()).save(any(Sancion.class));
    }



    @Test
    @DisplayName("Debe cerrar una sanción activa cambiando estado a CERRADA")
    public void shouldCerrarSancion() {
        // Given
        when(this.sancionRepository.findById(1L)).thenReturn(Optional.of(this.sancionPrueba));
        when(this.sancionRepository.save(any(Sancion.class))).thenAnswer(inv -> inv.getArgument(0));
        // When
        SancionResponseDTO result = this.sancionService.cerrar(1L);
        // Then
        assertThat(result.getEstado()).isEqualTo("CERRADA");
    }

    @Test
    @DisplayName("Debe lanzar SanctionException al cerrar una sanción ya CERRADA")
    public void shouldThrowWhenAlreadyClosed() {
        // Given
        this.sancionPrueba.setEstado("CERRADA");
        when(this.sancionRepository.findById(1L)).thenReturn(Optional.of(this.sancionPrueba));
        // When + Then
        assertThatThrownBy(() -> this.sancionService.cerrar(1L))
                .isInstanceOf(SanctionException.class)
                .hasMessageContaining("ya está CERRADA");
        verify(sancionRepository, never()).save(any(Sancion.class));
    }

    @Test
    @DisplayName("Debe retornar true cuando el usuario tiene sanción activa")
    public void shouldReturnTrueWhenUserHasActiveSanction() {
        // Given
        when(this.sancionRepository.tieneUsuarioSancionActiva(eq(10L), any(LocalDate.class))).thenReturn(true);
        // When + Then
        assertThat(this.sancionService.tieneUsuarioSancionActiva(10L)).isTrue();
    }

    @Test
    @DisplayName("Debe retornar false cuando el usuario NO tiene sanción activa")
    public void shouldReturnFalseWhenNoActiveSanction() {
        // Given
        when(this.sancionRepository.tieneUsuarioSancionActiva(eq(10L), any(LocalDate.class))).thenReturn(false);
        // When + Then
        assertThat(this.sancionService.tieneUsuarioSancionActiva(10L)).isFalse();
    }
}
