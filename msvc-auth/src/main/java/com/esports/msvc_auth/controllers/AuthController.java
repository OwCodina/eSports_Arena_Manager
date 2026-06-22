package com.esports.msvc_auth.controllers;

import com.esports.msvc_auth.models.dtos.AuthResponseDTO;
import com.esports.msvc_auth.models.dtos.LoginRequestDTO;
import com.esports.msvc_auth.models.dtos.RegisterRequestDTO;
import com.esports.msvc_auth.services.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Autenticacion", description = "Registro y login. Devuelve el JWT que usa el resto del sistema.")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    @Operation(summary = "Registrar cuenta de acceso",
            description = "Crea una cuenta (email + password + rol) en msvc-auth y el perfil de usuario en msvc-user. Devuelve un JWT. Roles validos: ADMINISTRADOR, ORGANIZADOR, JUGADOR.")
    public ResponseEntity<AuthResponseDTO> register(@Valid @RequestBody RegisterRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(request));
    }

    @PostMapping("/login")
    @Operation(summary = "Iniciar sesion",
            description = "Valida email y password (BCrypt) y devuelve el token JWT.")
    public ResponseEntity<AuthResponseDTO> login(@Valid @RequestBody LoginRequestDTO request) {
        return ResponseEntity.ok(authService.login(request));
    }
}
