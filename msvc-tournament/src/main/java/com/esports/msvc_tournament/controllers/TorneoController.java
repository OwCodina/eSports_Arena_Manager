package com.esports.msvc_tournament.controllers;
import com.esports.msvc_tournament.models.dtos.TorneoRequestDTO;
import com.esports.msvc_tournament.models.dtos.TorneoResponseDTO;
import com.esports.msvc_tournament.services.TorneoService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/api/v1/torneos")

public class TorneoController {
    @Autowired
    private TorneoService torneoService;

    @GetMapping
    public ResponseEntity<List<TorneoResponseDTO>> findAll(@RequestParam(required=false) Long juegoId, @RequestParam(required=false) String estado) {
        if (juegoId != null && estado != null)
            return ResponseEntity.ok(torneoService.findByJuegoIdAndEstado(juegoId, estado));
        else if (juegoId != null)
            return ResponseEntity.ok(torneoService.findByJuegoId(juegoId));
        else if (estado != null)
            return ResponseEntity.ok(torneoService.findByEstado(estado));
        return ResponseEntity.ok(torneoService.findAll());
    }
    @GetMapping("/{id}")
    public ResponseEntity<TorneoResponseDTO> findById(@PathVariable Long id) {
        return ResponseEntity.ok(torneoService.findById(id));
    }

    @GetMapping("/{id}/esta-abierto")
    public ResponseEntity<Map<String,Boolean>> estaAbierto(@PathVariable Long id) {
        return ResponseEntity.ok(Map.of("estaAbierto", torneoService.estaAbierto(id)));
    }

    @PostMapping
    public ResponseEntity<TorneoResponseDTO> save(@Valid @RequestBody TorneoRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(torneoService.save(dto));
    }
    @PutMapping("/{id}")
    public ResponseEntity<TorneoResponseDTO> update(@PathVariable Long id, @RequestBody TorneoRequestDTO dto) {
        return ResponseEntity.ok(torneoService.updateById(id, dto));
    }

    @PatchMapping("/{id}/cancelar")
    public ResponseEntity<TorneoResponseDTO> cancelar(@PathVariable Long id) {
        return ResponseEntity.ok(torneoService.cancelar(id));
    }

    @PatchMapping("/{id}/cerrar")
    public ResponseEntity<TorneoResponseDTO> cerrar(@PathVariable Long id) {
        return ResponseEntity.ok(torneoService.cerrar(id));
    }
}