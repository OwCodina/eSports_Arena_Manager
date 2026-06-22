package com.esports.msvc_auth.controllers;

import com.esports.msvc_auth.models.dtos.AuthAccount;
import com.esports.msvc_auth.services.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/api/v1/cuentas")
@Tag(name = "Cuentas de acceso", description = "CRUD de cuentas de acceso (requiere rol ADMINISTRADOR).")
@SecurityRequirement(name = "bearer-jwt")
@PreAuthorize("hasRole('ADMINISTRADOR')")
public class CuentaAccesoController {

    private final AuthService authService;

    public CuentaAccesoController(AuthService authService) {
        this.authService = authService;
    }

    @GetMapping
    @Operation(summary = "Listar cuentas",
            description = "Lista todas las cuentas o filtra por rol (ej: ROLE_JUGADOR) y/o estado.")
    public ResponseEntity<List<AuthAccount>> findAll(
            @Parameter(description = "Filtrar por rol (ej: ROLE_JUGADOR)") @RequestParam(required = false) String rol,
            @Parameter(description = "Filtrar por estado (ACTIVO/INACTIVO)") @RequestParam(required = false) String estado) {
        return ResponseEntity.ok(authService.findAll(rol, estado));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar cuenta por ID")
    public ResponseEntity<AuthAccount> findById(
            @Parameter(description = "Id de la cuenta", required = true, example = "1") @PathVariable Long id) {
        return ResponseEntity.ok(authService.findById(id));
    }

    @GetMapping("/email/{email}")
    @Operation(summary = "Buscar cuenta por email")
    public ResponseEntity<AuthAccount> findByEmail(
            @Parameter(description = "Email de la cuenta", required = true, example = "admin@esports.cl")
            @PathVariable String email) {
        return ResponseEntity.ok(authService.findByEmail(email));
    }

    @PatchMapping("/{id}/password")
    @Operation(summary = "Actualizar password de la cuenta",
            description = "Body: {\"newPassword\": \"nueva123\"}.")
    public ResponseEntity<AuthAccount> updatePassword(
            @Parameter(description = "Id de la cuenta", required = true, example = "1") @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        return ResponseEntity.ok(authService.updatePassword(id, body.get("newPassword")));
    }

    @PatchMapping("/{id}/roles")
    @Operation(summary = "Actualizar roles de la cuenta",
            description = "Reemplaza TODOS los roles. Body: [\"ADMINISTRADOR\", \"ORGANIZADOR\"]. Se normalizan a ROLE_X.")
    public ResponseEntity<AuthAccount> updateRoles(
            @Parameter(description = "Id de la cuenta", required = true, example = "1") @PathVariable Long id,
            @RequestBody Set<String> nuevosRoles) {
        return ResponseEntity.ok(authService.updateRol(id, nuevosRoles));
    }

    @PatchMapping("/{id}/estado")
    @Operation(summary = "Actualizar estado de la cuenta",
            description = "Body: {\"nuevoEstado\": \"ACTIVO\"}. Valores: ACTIVO / INACTIVO.")
    public ResponseEntity<AuthAccount> updateEstado(
            @Parameter(description = "Id de la cuenta", required = true, example = "1") @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        return ResponseEntity.ok(authService.updateEstado(id, body.get("nuevoEstado")));
    }

    @PatchMapping("/{id}/desactivar")
    @Operation(summary = "Desactivar cuenta",
            description = "Marca la cuenta como INACTIVA sin borrar el registro (mantiene trazabilidad).")
    public ResponseEntity<AuthAccount> desactivar(
            @Parameter(description = "Id de la cuenta", required = true, example = "1") @PathVariable Long id) {
        return ResponseEntity.ok(authService.desactivar(id));
    }
}
