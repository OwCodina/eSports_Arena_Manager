package com.esports.msvc_game.controllers;

import com.esports.msvc_game.models.dtos.JuegoRequestDTO;
import com.esports.msvc_game.models.dtos.JuegoResponseDTO;
import com.esports.msvc_game.services.JuegoService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/juegos")
public class JuegoController {

    @Autowired
    private JuegoService juegoService;

    @GetMapping
    public ResponseEntity<List<JuegoResponseDTO>> findAll() {
        return ResponseEntity.ok(juegoService.findAll());
    }

    @GetMapping("/activos")
    public ResponseEntity<List<JuegoResponseDTO>> findAllActivos() {
        return ResponseEntity.ok(juegoService.findAllActivos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<JuegoResponseDTO> findById(@PathVariable Long id) {
        return ResponseEntity.ok(juegoService.findById(id));
    }

    @PostMapping
    public ResponseEntity<JuegoResponseDTO> save(@Valid @RequestBody JuegoRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(juegoService.save(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<JuegoResponseDTO> update(@PathVariable Long id,
                                                    @Valid @RequestBody JuegoRequestDTO dto) {
        return ResponseEntity.ok(juegoService.updateById(id, dto));
    }

    @PatchMapping("/{id}/desactivar")
    public ResponseEntity<JuegoResponseDTO> desactivar(@PathVariable Long id) {
        return ResponseEntity.ok(juegoService.desactivar(id));
    }
}
