package com.esports.msvc_notification.services;

import com.esports.msvc_notification.exceptions.NotificationException;
import com.esports.msvc_notification.models.Notificacion;
import com.esports.msvc_notification.models.dtos.NotificacionRequestDTO;
import com.esports.msvc_notification.models.dtos.NotificacionResponseDTO;
import com.esports.msvc_notification.repositories.NotificacionRepository;
import net.datafaker.Faker;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Pruebas unitarias de {@link NotificacionServiceImpl}.
 * msvc-notification no tiene Feign: solo se mockea el repositorio.
 */
@ExtendWith(MockitoExtension.class)
public class NotificacionServiceTest {

    @Mock
    private NotificacionRepository notificacionRepository;

    @InjectMocks
    private NotificacionServiceImpl notificacionService;

    private Notificacion notifPrueba;

    @BeforeEach
    public void setUp() {
        this.notifPrueba = new Notificacion();
        this.notifPrueba.setNotificacionId(1L);
        this.notifPrueba.setUsuarioId(10L);
        this.notifPrueba.setTipo("INSCRIPCION_ACEPTADA");
        this.notifPrueba.setMensaje("Tu equipo fue inscrito correctamente.");
        this.notifPrueba.setLeida(false);
        this.notifPrueba.setEstado("ACTIVA");
        this.notifPrueba.setFecha(LocalDateTime.now());
    }



    @Test
    @DisplayName("Debe listar todas las notificaciones")
    public void shouldListAllNotificaciones() {
        // Given
        Faker faker = new Faker(Locale.of("es", "CL"));
        List<Notificacion> lista = new ArrayList<>();
        for (int i = 0; i < 30; i++) {
            Notificacion n = new Notificacion();
            n.setNotificacionId((long)(i+2));
            n.setMensaje(faker.lorem().sentence());
            n.setLeida(false); n.setEstado("ACTIVA");
            lista.add(n);
        }
        lista.add(this.notifPrueba);
        when(this.notificacionRepository.findAll()).thenReturn(lista);
        // When
        List<NotificacionResponseDTO> result = this.notificacionService.findAll();
        // Then
        assertThat(result).hasSize(31);
        verify(notificacionRepository, times(1)).findAll();
    }



    @Test
    @DisplayName("Debe encontrar una notificacion por su ID")
    public void shouldFindNotificacionById() {
        // Given
        when(this.notificacionRepository.findById(1L)).thenReturn(Optional.of(this.notifPrueba));
        // When
        NotificacionResponseDTO result = this.notificacionService.findById(1L);
        // Then
        assertThat(result.getNotificacionId()).isEqualTo(1L);
        assertThat(result.getLeida()).isFalse();
        assertThat(result.getEstado()).isEqualTo("ACTIVA");
    }

    @Test
    @DisplayName("Debe lanzar NotificationException al buscar ID inexistente")
    public void shouldThrowWhenNotificacionNotFound() {
        // Given
        when(this.notificacionRepository.findById(9999L)).thenReturn(Optional.empty());
        // When + Then
        assertThatThrownBy(() -> this.notificacionService.findById(9999L))
                .isInstanceOf(NotificationException.class)
                .hasMessage("Notificacion con id: 9999 no encontrada");
    }


    @Test
    @DisplayName("Debe guardar notificacion a usuario con leida=false y estado ACTIVA")
    public void shouldSaveNotificacionToUsuario() {
        // Given
        NotificacionRequestDTO dto = new NotificacionRequestDTO();
        dto.setUsuarioId(10L);
        dto.setTipo("PARTIDA_PROGRAMADA");
        dto.setMensaje("Tu partida es el 03/08 a las 15:00.");

        when(this.notificacionRepository.save(any(Notificacion.class))).thenReturn(this.notifPrueba);

        // When
        NotificacionResponseDTO result = this.notificacionService.save(dto);

        // Then
        assertThat(result.getLeida()).isFalse();
        assertThat(result.getEstado()).isEqualTo("ACTIVA");
        verify(notificacionRepository, times(1)).save(any(Notificacion.class));
    }

    @Test
    @DisplayName("Debe guardar notificacion a equipo")
    public void shouldSaveNotificacionToEquipo() {
        // Given
        NotificacionRequestDTO dto = new NotificacionRequestDTO();
        dto.setEquipoId(5L);
        dto.setTipo("RESULTADO_REGISTRADO");
        dto.setMensaje("Se registro el resultado de la partida.");

        Notificacion n = new Notificacion();
        n.setNotificacionId(2L); n.setEquipoId(5L);
        n.setLeida(false); n.setEstado("ACTIVA");
        when(this.notificacionRepository.save(any(Notificacion.class))).thenReturn(n);

        // When
        NotificacionResponseDTO result = this.notificacionService.save(dto);

        // Then
        assertThat(result.getEstado()).isEqualTo("ACTIVA");
        verify(notificacionRepository, times(1)).save(any(Notificacion.class));
    }

    @Test
    @DisplayName("Debe lanzar NotificationException cuando no hay destinatario")
    public void shouldThrowWhenNoDestinatario() {
        // Given: ni usuarioId ni equipoId
        NotificacionRequestDTO dto = new NotificacionRequestDTO();
        dto.setTipo("GENERAL");
        dto.setMensaje("Sin destino");
        // When + Then
        assertThatThrownBy(() -> this.notificacionService.save(dto))
                .isInstanceOf(NotificationException.class)
                .hasMessageContaining("usuarioId o equipoId");
        verify(notificacionRepository, never()).save(any(Notificacion.class));
    }



    @Test
    @DisplayName("Debe marcar notificacion como leida")
    public void shouldMarcarComoLeida() {
        // Given
        when(this.notificacionRepository.findById(1L)).thenReturn(Optional.of(this.notifPrueba));
        when(this.notificacionRepository.save(any(Notificacion.class))).thenAnswer(inv -> inv.getArgument(0));
        // When
        NotificacionResponseDTO result = this.notificacionService.marcarComoLeida(1L);
        // Then
        assertThat(result.getLeida()).isTrue();
        verify(notificacionRepository, times(1)).save(this.notifPrueba);
    }

    @Test
    @DisplayName("No debe guardar si la notificacion ya estaba leida (idempotente)")
    public void shouldBeIdempotentWhenAlreadyRead() {
        // Given: ya está leída
        this.notifPrueba.setLeida(true);
        when(this.notificacionRepository.findById(1L)).thenReturn(Optional.of(this.notifPrueba));
        // When
        NotificacionResponseDTO result = this.notificacionService.marcarComoLeida(1L);
        // Then: retorna el DTO pero NO llama a save
        assertThat(result.getLeida()).isTrue();
        verify(notificacionRepository, never()).save(any(Notificacion.class));
    }



    @Test
    @DisplayName("Debe archivar notificacion cambiando estado a ARCHIVADA")
    public void shouldArchivarNotificacion() {
        // Given
        when(this.notificacionRepository.findById(1L)).thenReturn(Optional.of(this.notifPrueba));
        when(this.notificacionRepository.save(any(Notificacion.class))).thenAnswer(inv -> inv.getArgument(0));
        // When
        NotificacionResponseDTO result = this.notificacionService.archivar(1L);
        // Then
        assertThat(result.getEstado()).isEqualTo("ARCHIVADA");
    }

    @Test
    @DisplayName("Debe lanzar excepcion al archivar una notificacion ya ARCHIVADA")
    public void shouldThrowWhenAlreadyArchived() {
        // Given
        this.notifPrueba.setEstado("ARCHIVADA");
        when(this.notificacionRepository.findById(1L)).thenReturn(Optional.of(this.notifPrueba));
        // When + Then
        assertThatThrownBy(() -> this.notificacionService.archivar(1L))
                .isInstanceOf(NotificationException.class)
                .hasMessageContaining("ARCHIVADA");
        verify(notificacionRepository, never()).save(any(Notificacion.class));
    }



    @Test
    @DisplayName("Debe marcar todas las notificaciones no leidas de un usuario")
    public void shouldMarcarTodasLeidasByUsuario() {
        // Given: 3 notificaciones no leídas
        List<Notificacion> noLeidas = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            Notificacion n = new Notificacion();
            n.setNotificacionId((long)(i+1));
            n.setUsuarioId(10L); n.setLeida(false);
            noLeidas.add(n);
        }
        when(this.notificacionRepository.findByUsuarioIdAndLeidaOrderByFechaDesc(10L, false))
                .thenReturn(noLeidas);
        when(this.notificacionRepository.saveAll(anyList())).thenReturn(noLeidas);

        // When
        int count = this.notificacionService.marcarTodasLeidasByUsuarioId(10L);

        // Then
        assertThat(count).isEqualTo(3);
        verify(notificacionRepository, times(1)).saveAll(anyList());
    }
}
