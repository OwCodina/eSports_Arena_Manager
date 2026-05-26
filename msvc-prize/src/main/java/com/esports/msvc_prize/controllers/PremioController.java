package com.esports.msvc_prize.controllers;
import com.esports.msvc_prize.models.PremioAsignado;
import com.esports.msvc_prize.models.dtos.PremioRequestDTO;
import com.esports.msvc_prize.models.dtos.PremioResponseDTO;
import com.esports.msvc_prize.services.PremioService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/api/v1/premios")
public class PremioController {

    @Autowired
    private PremioService premioService;

    @GetMapping
    public ResponseEntity<List<PremioResponseDTO>> findAll(@RequestParam(required=false) Long torneoId) {
        return torneoId != null ?
                ResponseEntity.ok(premioService.findByTorneoId(torneoId)) :
                ResponseEntity.ok(premioService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<PremioResponseDTO> findById(@PathVariable Long id) {
        return ResponseEntity.ok(premioService.findById(id));
    }

    @GetMapping("/asignaciones/participante/{pId}")
    public ResponseEntity<List<PremioAsignado>> findAsignaciones(@PathVariable Long pId) {
        return ResponseEntity.ok(premioService.findAsignacionesByParticipanteId(pId));
    }

    @PostMapping
    public ResponseEntity<PremioResponseDTO> save(@Valid @RequestBody PremioRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(premioService.save(dto));
    }

    @PostMapping("/torneo/{torneoId}/asignar-todos")
    public ResponseEntity<List<PremioAsignado>> asignarTodos(@PathVariable Long torneoId) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(premioService.asignarPremiosTorneo(torneoId));
    }
    @PostMapping("/{id}/asignar")
    public ResponseEntity<PremioAsignado> asignar(@PathVariable Long id, @RequestBody Map<String,Long> body) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(premioService.asignarPremio(id, body.get("participanteId")));
    }
    @PutMapping("/{id}")
    public ResponseEntity<PremioResponseDTO> update(@PathVariable Long id, @RequestBody PremioRequestDTO dto) {
        return ResponseEntity.ok(premioService.updateById(id, dto)); }

    @PatchMapping("/{id}/desactivar")
    public ResponseEntity<PremioResponseDTO> desactivar(@PathVariable Long id) {
        return ResponseEntity.ok(premioService.desactivar(id));
    }
}