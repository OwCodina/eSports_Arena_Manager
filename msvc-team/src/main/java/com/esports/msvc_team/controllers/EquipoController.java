package com.esports.msvc_team.controllers;
import com.esports.msvc_team.models.MiembroEquipo;
import com.esports.msvc_team.models.dtos.*;
import com.esports.msvc_team.services.EquipoService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import java.util.*;
@RestController @RequestMapping("/api/v1/equipos")
public class EquipoController {

    @Autowired private EquipoService equipoService;
    @GetMapping public ResponseEntity<List<EquipoResponseDTO>> findAll(@RequestParam(required=false) String estado, @RequestParam(required=false) Long juegoId, @RequestParam(required=false) Long capitanId) {
        if (juegoId != null) return ResponseEntity.ok(equipoService.findByJuegoPrincipalId(juegoId));
        else if (capitanId != null) return ResponseEntity.ok(equipoService.findByCapitanId(capitanId));
        else if (estado != null) return ResponseEntity.ok(equipoService.findByEstado(estado));
        return ResponseEntity.ok(equipoService.findAll());
    }
    @GetMapping("/{id}") public ResponseEntity<EquipoResponseDTO> findById(@PathVariable Long id) { return ResponseEntity.ok(equipoService.findById(id)); }
    @GetMapping("/{id}/esta-activo") public ResponseEntity<Map<String,Boolean>> estaActivo(@PathVariable Long id) { return ResponseEntity.ok(Map.of("estaActivo", equipoService.estaActivo(id))); }
    @GetMapping("/{id}/miembros") public ResponseEntity<List<MiembroEquipo>> findMiembros(@PathVariable Long id) { return ResponseEntity.ok(equipoService.findMiembrosByEquipoId(id)); }
    @PostMapping public ResponseEntity<EquipoResponseDTO> save(@Valid @RequestBody EquipoRequestDTO dto) { return ResponseEntity.status(HttpStatus.CREATED).body(equipoService.save(dto)); }
    @PostMapping("/{id}/miembros") public ResponseEntity<MiembroEquipo> agregarMiembro(@PathVariable Long id, @Valid @RequestBody MiembroRequestDTO dto) { return ResponseEntity.status(HttpStatus.CREATED).body(equipoService.agregarMiembro(id, dto)); }
    @PutMapping("/{id}") public ResponseEntity<EquipoResponseDTO> update(@PathVariable Long id, @RequestBody EquipoRequestDTO dto) { return ResponseEntity.ok(equipoService.updateById(id, dto)); }
    @PatchMapping("/{id}/desactivar") public ResponseEntity<EquipoResponseDTO> desactivar(@PathVariable Long id) { return ResponseEntity.ok(equipoService.desactivar(id)); }
    @DeleteMapping("/{id}/miembros/{miembroId}") public ResponseEntity<Void> eliminarMiembro(@PathVariable Long id, @PathVariable Long miembroId) { equipoService.eliminarMiembro(miembroId); return ResponseEntity.noContent().build(); }
}