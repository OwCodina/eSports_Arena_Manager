package com.esports.msvc_sanction.controllers;
import com.esports.msvc_sanction.models.dtos.SancionRequestDTO;
import com.esports.msvc_sanction.models.dtos.SancionResponseDTO;
import com.esports.msvc_sanction.services.SancionService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import java.util.*;
@RestController @RequestMapping("/api/v1/sanciones")
public class SancionController {
    @Autowired private SancionService sancionService;
    @GetMapping public ResponseEntity<List<SancionResponseDTO>> findAll(@RequestParam(required=false) Long usuarioId, @RequestParam(required=false) Long equipoId, @RequestParam(required=false) String estado) {
        if (usuarioId != null) return ResponseEntity.ok(sancionService.findByUsuarioId(usuarioId));
        else if (equipoId != null) return ResponseEntity.ok(sancionService.findByEquipoId(equipoId));
        else if (estado != null) return ResponseEntity.ok(sancionService.findByEstado(estado));
        return ResponseEntity.ok(sancionService.findAll());
    }
    @GetMapping("/{id}") public ResponseEntity<SancionResponseDTO> findById(@PathVariable Long id) { return ResponseEntity.ok(sancionService.findById(id)); }
    @GetMapping("/usuario/{usuarioId}/tiene-activa") public ResponseEntity<Map<String,Boolean>> tieneUsuario(@PathVariable Long usuarioId) { return ResponseEntity.ok(Map.of("tieneSancionActiva", sancionService.tieneUsuarioSancionActiva(usuarioId))); }
    @GetMapping("/equipo/{equipoId}/tiene-activa") public ResponseEntity<Map<String,Boolean>> tieneEquipo(@PathVariable Long equipoId) { return ResponseEntity.ok(Map.of("tieneSancionActiva", sancionService.tieneEquipoSancionActiva(equipoId))); }
    @PostMapping public ResponseEntity<SancionResponseDTO> save(@Valid @RequestBody SancionRequestDTO dto) { return ResponseEntity.status(HttpStatus.CREATED).body(sancionService.save(dto)); }
    @PutMapping("/{id}") public ResponseEntity<SancionResponseDTO> update(@PathVariable Long id, @RequestBody SancionRequestDTO dto) { return ResponseEntity.ok(sancionService.updateById(id, dto)); }
    @PatchMapping("/{id}/cerrar") public ResponseEntity<SancionResponseDTO> cerrar(@PathVariable Long id) { return ResponseEntity.ok(sancionService.cerrar(id)); }
}