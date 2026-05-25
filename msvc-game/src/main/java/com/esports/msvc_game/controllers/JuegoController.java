package com.esports.msvc_game.controllers;

import com.esports.msvc_game.models.Juego;
import com.esports.msvc_game.services.JuegoService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;



@RestController
@RequestMapping("/api/v1/juegos")
@Validated
public class JuegoController {

    @Autowired
    private JuegoService juegoService;

    @GetMapping
    public ResponseEntity<List<Juego>> findAll() {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(this.juegoService.findAll());
    }

    @GetMapping("/activos")
    public ResponseEntity<List<Juego>> findAllActivos() {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(this.juegoService.findAllActivos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Juego> findById(@PathVariable Long id) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(this.juegoService.findById(id));
    }

    @PostMapping
    public ResponseEntity<Juego> save(@Valid @RequestBody Juego juego) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(this.juegoService.save(juego));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Juego> update(@PathVariable Long id, @Valid @RequestBody Juego juego) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(this.juegoService.updateById(id, juego));
    }

    @PatchMapping("/{id}/desactivar")
    public ResponseEntity<Juego> desactivar(@PathVariable Long id) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(this.juegoService.desactivar(id));
    }
}
