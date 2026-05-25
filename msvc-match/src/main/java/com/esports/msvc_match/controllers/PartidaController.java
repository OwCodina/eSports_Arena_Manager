package com.esports.msvc_match.controllers;
import com.esports.msvc_match.models.dtos.PartidaRequestDTO;
import com.esports.msvc_match.models.dtos.PartidaResponseDTO;
import com.esports.msvc_match.services.PartidaService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import java.util.*;
@RestController @RequestMapping("/api/v1/partidas")
public class PartidaController {
    @Autowired private PartidaService partidaService;
    @GetMapping public ResponseEntity<List<PartidaResponseDTO>> findAll(@RequestParam(required=false) Long torneoId, @RequestParam(required=false) Integer ronda, @RequestParam(required=false) String estado) {
        if (torneoId != null && ronda != null) return ResponseEntity.ok(partidaService.findByTorneoIdAndRonda(torneoId, ronda));
        else if (torneoId != null && estado != null) return ResponseEntity.ok(partidaService.findByTorneoIdAndEstado(torneoId, estado));
        else if (torneoId != null) return ResponseEntity.ok(partidaService.findByTorneoId(torneoId));
        return ResponseEntity.ok(partidaService.findAll());
    }
    @GetMapping("/{id}") public ResponseEntity<PartidaResponseDTO> findById(@PathVariable Long id) { return ResponseEntity.ok(partidaService.findById(id)); }
    @PostMapping public ResponseEntity<PartidaResponseDTO> save(@Valid @RequestBody PartidaRequestDTO dto) { return ResponseEntity.status(HttpStatus.CREATED).body(partidaService.save(dto)); }
    @PutMapping("/{id}") public ResponseEntity<PartidaResponseDTO> update(@PathVariable Long id, @RequestBody PartidaRequestDTO dto) { return ResponseEntity.ok(partidaService.updateById(id, dto)); }
    @PatchMapping("/{id}/cancelar") public ResponseEntity<PartidaResponseDTO> cancelar(@PathVariable Long id) { return ResponseEntity.ok(partidaService.cancelar(id)); }
}