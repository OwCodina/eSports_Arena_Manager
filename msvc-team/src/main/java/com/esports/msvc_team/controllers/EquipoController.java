package com.esports.msvc_team.controllers;

import com.esports.msvc_team.models.MiembroEquipo;
import com.esports.msvc_team.models.dtos.*;
import com.esports.msvc_team.services.EquipoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/v1/equipos")
@Tag(name = "Equipos V1", description = "Metodos CRUD para la gestion de equipos")
public class EquipoController {

    @Autowired
    private EquipoService equipoService;

    @GetMapping
    @Operation(
            summary = "Listado de todos los equipos",
            description = "Devuelve una lista de equipos. Permite filtrar por estado, juego principal o capitan."
    )
    @ApiResponse(responseCode = "200", description = "Operacion exitosa")
    public ResponseEntity<List<EquipoResponseDTO>> findAll(
            @Parameter(description = "Estado del equipo", example = "ACTIVO")
            @RequestParam(required = false) String estado,
            @Parameter(description = "Id del juego principal", example = "1")
            @RequestParam(required = false) Long juegoId,
            @Parameter(description = "Id del capitan del equipo", example = "1")
            @RequestParam(required = false) Long capitanId) {
        if (juegoId != null)
            return ResponseEntity.ok(equipoService.findByJuegoPrincipalId(juegoId));
        else if (capitanId != null)
            return ResponseEntity.ok(equipoService.findByCapitanId(capitanId));
        else if (estado != null)
            return ResponseEntity.ok(equipoService.findByEstado(estado));
        return ResponseEntity.ok(equipoService.findAll());
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Busqueda de un equipo",
            description = "Devuelve un equipo mediante su id. Si no existe, devuelve una excepcion."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Equipo encontrado",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = EquipoResponseDTO.class),
                            examples = @ExampleObject(
                                    name = "Ejemplo equipo",
                                    value = "{\"equipoId\":1," +
                                            "\"nombre\":\"Team Alpha\"," +
                                            "\"capitanId\":1," +
                                            "\"juegoPrincipalId\":1," +
                                            "\"estado\":\"ACTIVO\"," +
                                            "\"createdAt\":\"2026-05-26T03:26:56.797847\"," +
                                            "\"updatedAt\":\"2026-05-27T03:26:56.797847\"}"
                            )
                    )
            ),
            @ApiResponse(responseCode = "404", description = "El equipo no se encuentra en la BD")
    })
    public ResponseEntity<EquipoResponseDTO> findById(
            @Parameter(description = "Id del equipo a buscar", required = true, example = "1")
            @PathVariable Long id) {
        return ResponseEntity.ok(equipoService.findById(id));
    }

    @GetMapping("/{id}/esta-activo")
    @Operation(
            summary = "Confirma si el equipo esta activo",
            description = "Devuelve true si el equipo se encuentra en estado ACTIVO."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Estado del equipo obtenido"),
            @ApiResponse(responseCode = "404", description = "Equipo no encontrado")
    })
    public ResponseEntity<Map<String, Boolean>> estaActivo(
            @Parameter(description = "Id del equipo a validar", required = true, example = "1")
            @PathVariable Long id) {
        return ResponseEntity.ok(Map.of("estaActivo", equipoService.estaActivo(id)));
    }

    @GetMapping("/{id}/miembros")
    @Operation(
            summary = "Listado de miembros de un equipo",
            description = "Devuelve los miembros asociados a un equipo."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Miembros encontrados"),
            @ApiResponse(responseCode = "404", description = "Equipo no encontrado")
    })
    public ResponseEntity<List<MiembroEquipo>> findMiembros(
            @Parameter(description = "Id del equipo", required = true, example = "1")
            @PathVariable Long id) {
        return ResponseEntity.ok(equipoService.findMiembrosByEquipoId(id));
    }

    @PostMapping
    @Operation(summary = "Guardado de equipo", description = "Crea un equipo nuevo con un capitan y juego principal validos.")
    @RequestBody(
            description = "Equipo a crear",
            required = true,
            content = @Content(schema = @Schema(implementation = EquipoRequestDTO.class))
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Equipo creado"),
            @ApiResponse(responseCode = "400", description = "Datos del equipo invalidos"),
            @ApiResponse(responseCode = "404", description = "No se pudo validar capitan o juego principal")
    })
    public ResponseEntity<EquipoResponseDTO> save(@Valid @org.springframework.web.bind.annotation.RequestBody EquipoRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(equipoService.save(dto));
    }

    @PostMapping("/{id}/miembros")
    @Operation(summary = "Agrega un miembro al equipo", description = "Asocia un usuario activo como miembro de un equipo activo.")
    @RequestBody(
            description = "Miembro a agregar",
            required = true,
            content = @Content(schema = @Schema(implementation = MiembroRequestDTO.class))
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Miembro agregado"),
            @ApiResponse(responseCode = "400", description = "Datos del miembro invalidos"),
            @ApiResponse(responseCode = "404", description = "Equipo o usuario no encontrado")
    })
    public ResponseEntity<MiembroEquipo> agregarMiembro(
            @Parameter(description = "Id del equipo", required = true, example = "1")
            @PathVariable Long id,
            @Valid @org.springframework.web.bind.annotation.RequestBody MiembroRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(equipoService.agregarMiembro(id, dto));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizacion de equipo", description = "Actualiza los datos de un equipo existente.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Equipo actualizado"),
            @ApiResponse(responseCode = "404", description = "Equipo no encontrado")
    })
    public ResponseEntity<EquipoResponseDTO> update(
            @Parameter(description = "Id del equipo a actualizar", required = true, example = "1")
            @PathVariable Long id,
            @Valid @org.springframework.web.bind.annotation.RequestBody EquipoRequestDTO dto) {
        return ResponseEntity.ok(equipoService.updateById(id, dto));
    }

    @PatchMapping("/{id}/desactivar")
    @Operation(summary = "Actualizacion del estado del equipo", description = "Desactiva un equipo existente.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Equipo desactivado"),
            @ApiResponse(responseCode = "404", description = "Equipo no encontrado")
    })
    public ResponseEntity<EquipoResponseDTO> desactivar(
            @Parameter(description = "Id del equipo a desactivar", required = true, example = "1")
            @PathVariable Long id) {
        return ResponseEntity.ok(equipoService.desactivar(id));
    }

    @DeleteMapping("/{id}/miembros/{miembroId}")
    @Operation(summary = "Eliminacion de miembro", description = "Elimina un miembro de un equipo.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Miembro eliminado"),
            @ApiResponse(responseCode = "404", description = "Miembro no encontrado")
    })
    public ResponseEntity<Void> eliminarMiembro(
            @Parameter(description = "Id del equipo", required = true, example = "1")
            @PathVariable Long id,
            @Parameter(description = "Id del miembro a eliminar", required = true, example = "1")
            @PathVariable Long miembroId) {
        equipoService.eliminarMiembro(miembroId);
        return ResponseEntity.noContent().build();
    }
}
