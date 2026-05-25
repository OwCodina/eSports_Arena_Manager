package com.esports.msvc_notification.controllers;
import com.esports.msvc_notification.models.dtos.NotificacionRequestDTO;
import com.esports.msvc_notification.models.dtos.NotificacionResponseDTO;
import com.esports.msvc_notification.services.NotificacionService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/api/v1/notificaciones")
public class NotificacionController {
    @Autowired private NotificacionService notificacionService;
    @GetMapping public ResponseEntity<List<NotificacionResponseDTO>> findAll(@RequestParam(required=false) Long usuarioId, @RequestParam(required=false) Long equipoId) {
        if (usuarioId != null) return ResponseEntity.ok(notificacionService.findByUsuarioId(usuarioId));
        else if (equipoId != null) return ResponseEntity.ok(notificacionService.findByEquipoId(equipoId));
        return ResponseEntity.ok(notificacionService.findAll());
    }
    @GetMapping("/{id}")
    public ResponseEntity<NotificacionResponseDTO> findById(@PathVariable Long id) { return ResponseEntity.ok(notificacionService.findById(id)); }
    @GetMapping("/usuario/{usuarioId}/no-leidas")
    public ResponseEntity<List<NotificacionResponseDTO>> noLeidasUsuario(@PathVariable Long usuarioId) { return ResponseEntity.ok(notificacionService.findNoLeidasByUsuarioId(usuarioId)); }
    @GetMapping("/equipo/{equipoId}/no-leidas")
    public ResponseEntity<List<NotificacionResponseDTO>> noLeidasEquipo(@PathVariable Long equipoId) { return ResponseEntity.ok(notificacionService.findNoLeidasByEquipoId(equipoId)); }
    @PostMapping
    public ResponseEntity<NotificacionResponseDTO> save(@Valid @RequestBody NotificacionRequestDTO dto) { return ResponseEntity.status(HttpStatus.CREATED).body(notificacionService.save(dto)); }
    @PatchMapping("/{id}/leer")
    public ResponseEntity<NotificacionResponseDTO> leer(@PathVariable Long id) { return ResponseEntity.ok(notificacionService.marcarComoLeida(id)); }
    @PatchMapping("/usuario/{usuarioId}/leer-todas")
    public ResponseEntity<Map<String,Integer>> leerTodasUsuario(@PathVariable Long usuarioId) { return ResponseEntity.ok(Map.of("notificacionesActualizadas", notificacionService.marcarTodasLeidasByUsuarioId(usuarioId))); }
    @PatchMapping("/equipo/{equipoId}/leer-todas")
    public ResponseEntity<Map<String,Integer>> leerTodasEquipo(@PathVariable Long equipoId) { return ResponseEntity.ok(Map.of("notificacionesActualizadas", notificacionService.marcarTodasLeidasByEquipoId(equipoId))); }
    @PatchMapping("/{id}/archivar")
    public ResponseEntity<NotificacionResponseDTO> archivar(@PathVariable Long id) { return ResponseEntity.ok(notificacionService.archivar(id)); }
}