package com.esports.msvc_registration.controllers;
import com.esports.msvc_registration.models.dtos.InscripcionRequestDTO;
import com.esports.msvc_registration.models.dtos.InscripcionResponseDTO;
import com.esports.msvc_registration.services.InscripcionService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import java.util.*;
@RestController
@RequestMapping("/api/v1/inscripciones")
public class InscripcionController {

    @Autowired
    private InscripcionService inscripcionService;

    @GetMapping
    public ResponseEntity<List<InscripcionResponseDTO>> findAll(@RequestParam(required=false) Long torneoId,
                                                                @RequestParam(required=false) Long equipoId,
                                                                @RequestParam(required=false) Long jugadorId) {
        if (torneoId != null)
            return ResponseEntity.ok(inscripcionService.findByTorneoId(torneoId));
        else if (equipoId != null)
            return ResponseEntity.ok(inscripcionService.findByEquipoId(equipoId));
        else if (jugadorId != null)
            return ResponseEntity.ok(inscripcionService.findByJugadorId(jugadorId));
        return ResponseEntity.ok(inscripcionService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<InscripcionResponseDTO> findById(@PathVariable Long id) {
        return ResponseEntity.ok(inscripcionService.findById(id));
    }

    @PostMapping
    public ResponseEntity<InscripcionResponseDTO> save(@Valid @RequestBody InscripcionRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(inscripcionService.save(dto));
    }
    @PatchMapping("/{id}/estado")
    public ResponseEntity<InscripcionResponseDTO> updateEstado(@PathVariable Long id, @RequestBody Map<String,String> body) {
        return ResponseEntity.ok(inscripcionService.updateEstado(id, body.get("estado")));
    }

    @PatchMapping("/{id}/cancelar")
    public ResponseEntity<InscripcionResponseDTO> cancelar(@PathVariable Long id) {
        return ResponseEntity.ok(inscripcionService.cancelar(id));
    }
}