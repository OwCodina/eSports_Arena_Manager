package com.esports.msvc_ranking.controllers;

import com.esports.msvc_ranking.models.dtos.ActualizarRankingRequestDTO;
import com.esports.msvc_ranking.models.dtos.RankingResponseDTO;
import com.esports.msvc_ranking.services.RankingService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/rankings")
public class RankingController {

    @Autowired
    private RankingService rankingService;

    @GetMapping("/torneo/{torneoId}")
    public ResponseEntity<List<RankingResponseDTO>> findByTorneo(@PathVariable Long torneoId) {
        return ResponseEntity.ok(rankingService.findByTorneoId(torneoId));
    }

    @GetMapping("/torneo/{torneoId}/participante/{participanteId}")
    public ResponseEntity<RankingResponseDTO> findByParticipante(@PathVariable Long torneoId, @PathVariable Long participanteId) {
        return ResponseEntity.ok(rankingService.findByTorneoIdAndParticipanteId(torneoId, participanteId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<RankingResponseDTO> findById(@PathVariable Long id) {
        return ResponseEntity.ok(rankingService.findById(id));
    }

    @PostMapping("/torneo/{torneoId}/inicializar")
    public ResponseEntity<List<RankingResponseDTO>> inicializar(@PathVariable Long torneoId) {
        return ResponseEntity.status(HttpStatus.CREATED).body(rankingService.inicializarRanking(torneoId));
    }

    @PostMapping("/torneo/{torneoId}/actualizar")
    public ResponseEntity<RankingResponseDTO> actualizar(
            @PathVariable Long torneoId,
            @Valid @RequestBody ActualizarRankingRequestDTO dto) {
        return ResponseEntity.ok(rankingService.actualizarConResultado(
                torneoId, dto.getGanadorId(), dto.getPuntajeGanador(),
                dto.getPuntajePerdedor(), dto.getPerdedorId()));
    }

    @PostMapping("/torneo/{torneoId}/recalcular")
    public ResponseEntity<Void> recalcular(@PathVariable Long torneoId) {
        rankingService.recalcularPosiciones(torneoId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/torneo/{torneoId}/cerrar")
    public ResponseEntity<Void> cerrar(@PathVariable Long torneoId) {
        rankingService.cerrarRanking(torneoId);
        return ResponseEntity.ok().build();
    }
}
