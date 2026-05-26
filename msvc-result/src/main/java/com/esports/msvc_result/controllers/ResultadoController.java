package com.esports.msvc_result.controllers;
import com.esports.msvc_result.models.dtos.ResultadoRequestDTO;
import com.esports.msvc_result.models.dtos.ResultadoResponseDTO;
import com.esports.msvc_result.services.ResultadoService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import java.util.*;
@RestController @RequestMapping("/api/v1/resultados")
public class ResultadoController {
    @Autowired
    private ResultadoService resultadoService;

    @GetMapping
    public ResponseEntity<List<ResultadoResponseDTO>> findAll(@RequestParam(required=false) String estadoValidacion) {
        return estadoValidacion != null ?
                ResponseEntity.ok(resultadoService.findByEstadoValidacion(estadoValidacion)) :
                ResponseEntity.ok(resultadoService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ResultadoResponseDTO> findById(@PathVariable Long id) {
        return ResponseEntity.ok(resultadoService.findById(id));
    }

    @GetMapping("/partida/{partidaId}")
    public ResponseEntity<ResultadoResponseDTO> findByPartida(@PathVariable Long partidaId) {
        return ResponseEntity.ok(resultadoService.findByPartidaId(partidaId));
    }

    @PostMapping
    public ResponseEntity<ResultadoResponseDTO> save(@Valid @RequestBody ResultadoRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(resultadoService.save(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ResultadoResponseDTO> update(@PathVariable Long id, @RequestBody ResultadoRequestDTO dto) {
        return ResponseEntity.ok(resultadoService.updateById(id, dto));
    }

    @PatchMapping("/{id}/validar")
    public ResponseEntity<ResultadoResponseDTO> validar(@PathVariable Long id) {
        return ResponseEntity.ok(resultadoService.validar(id));
    }

    @PatchMapping("/{id}/anular")
    public ResponseEntity<ResultadoResponseDTO> anular(@PathVariable Long id, @RequestBody Map<String,String> body) {
        return ResponseEntity.ok(resultadoService.anular(id, body.get("motivo")));
    }
}