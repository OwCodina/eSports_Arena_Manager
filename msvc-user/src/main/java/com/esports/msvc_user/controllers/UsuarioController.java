package com.esports.msvc_user.controllers;

import com.esports.msvc_user.models.dtos.UsuarioRequestDTO;
import com.esports.msvc_user.models.dtos.UsuarioResponseDTO;
import com.esports.msvc_user.services.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/usuarios")
public class UsuarioController {

    @Autowired
    private UsuarioService usuarioService;

    @GetMapping
    public ResponseEntity<List<UsuarioResponseDTO>> findAll(
            @RequestParam(required = false) String rol,
            @RequestParam(required = false) String estado) {
        if (rol != null && estado != null) return ResponseEntity.ok(usuarioService.findByRolAndEstado(rol, estado));
        else if (rol != null)    return ResponseEntity.ok(usuarioService.findByRol(rol));
        else if (estado != null) return ResponseEntity.ok(usuarioService.findByEstado(estado));
        return ResponseEntity.ok(usuarioService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<UsuarioResponseDTO> findById(@PathVariable Long id) {
        return ResponseEntity.ok(usuarioService.findById(id));
    }

    @GetMapping("/nickname/{nickname}")
    public ResponseEntity<UsuarioResponseDTO> findByNickname(@PathVariable String nickname) {
        return ResponseEntity.ok(usuarioService.findByNickname(nickname));
    }

    @GetMapping("/{id}/puede-competir")
    public ResponseEntity<Map<String, Boolean>> puedeCompetitr(@PathVariable Long id) {
        return ResponseEntity.ok(Map.of("puedeCompetitr", usuarioService.puedeCompetitr(id)));
    }

    @PostMapping
    public ResponseEntity<UsuarioResponseDTO> save(@Valid @RequestBody UsuarioRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(usuarioService.save(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<UsuarioResponseDTO> update(@PathVariable Long id,
                                                      @Valid @RequestBody UsuarioRequestDTO dto) {
        return ResponseEntity.ok(usuarioService.updateById(id, dto));
    }

    @PatchMapping("/{id}/desactivar")
    public ResponseEntity<UsuarioResponseDTO> desactivar(@PathVariable Long id) {
        return ResponseEntity.ok(usuarioService.desactivar(id));
    }
}
