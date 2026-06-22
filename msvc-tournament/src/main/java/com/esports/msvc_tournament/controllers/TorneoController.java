package com.esports.msvc_tournament.controllers;


import com.esports.msvc_tournament.models.dtos.TorneoRequestDTO;
import com.esports.msvc_tournament.models.dtos.TorneoResponseDTO;
import com.esports.msvc_tournament.services.TorneoService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/torneos")
@Tag(name = "Torneos V1", description = "Metodos CRUD para la gestion de torneos con HATEOAS")
public class TorneoController {
    @Autowired
    private TorneoService torneoService;

    @GetMapping
    @Operation(
            summary = "Listado de torneos",
            description = "Devuelve una lista de torneos. Permite filtrar por juego y estado."
    )
    @ApiResponse(responseCode = "200", description = "Operacion exitosa")
    public ResponseEntity<List<TorneoResponseDTO>> findAll(
            @Parameter(description = "Id del juego asociado al torneo", example = "1")
            @RequestParam(required=false) Long juegoId,
            @Parameter(description = "Estado del torneo", example = "ABIERTO")
            @RequestParam(required=false) String estado) {
        if (juegoId != null && estado != null)
            return ResponseEntity.ok(torneoService.findByJuegoIdAndEstado(juegoId, estado));
        else if (juegoId != null)
            return ResponseEntity.ok(torneoService.findByJuegoId(juegoId));
        else if (estado != null)
            return ResponseEntity.ok(torneoService.findByEstado(estado));
        return ResponseEntity.ok(torneoService.findAll());
    }
    @GetMapping("/{id}")
    @Operation(
            summary = "Busqueda de un torneo",
            description = "Devuelve un torneo mediante su id, en caso contrario devuelve una excepcion."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Torneo encontrado",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = TorneoResponseDTO.class),
                            examples = @ExampleObject(
                                    name = "Ejemplo torneo",
                                    value = "{\"torneoId\":1,\"nombre\":\"Copa Valorant\",\"juegoId\":1,\"fechaInicio\":\"2026-07-01\",\"fechaFin\":\"2026-07-10\",\"fechaCierreInscripcion\":\"2026-06-25\",\"cupoMaximo\":16,\"estado\":\"ABIERTO\",\"modalidad\":\"ELIMINACION_DIRECTA\",\"createdAt\":\"2026-05-26T03:26:56.797847\",\"updatedAt\":\"2026-05-27T03:26:56.797847\"}"
                            )
                    )
            ),
            @ApiResponse(responseCode = "404", description = "El torneo no se encuentra en la BD")
    })
    public ResponseEntity<TorneoResponseDTO> findById(
            @Parameter(description = "Id del torneo a buscar", required = true, example = "1")
            @PathVariable Long id) {
        return ResponseEntity.ok(torneoService.findById(id));
    }

    @GetMapping("/{id}/esta-abierto")
    @Operation(
            summary = "Confirma si el torneo esta abierto",
            description = "Devuelve true si el torneo se encuentra en estado abierto."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Estado del torneo obtenido"),
            @ApiResponse(responseCode = "404", description = "Torneo no encontrado")
    })
    public ResponseEntity<Map<String,Boolean>> estaAbierto(
            @Parameter(description = "Id del torneo a validar", required = true, example = "1")
            @PathVariable Long id) {
        return ResponseEntity.ok(Map.of("estaAbierto", torneoService.estaAbierto(id)));
    }

    @PostMapping
    @Operation(summary = "Guardado de torneo", description = "Crea un torneo nuevo con un juego valido.")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Torneo a crear",
            required = true,
            content = @Content(schema = @Schema(implementation = TorneoRequestDTO.class))
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Torneo creado"),
            @ApiResponse(responseCode = "400", description = "Datos del torneo invalidos"),
            @ApiResponse(responseCode = "404", description = "No se pudo validar el juego")
    })
    public ResponseEntity<TorneoResponseDTO> save(@Valid @RequestBody TorneoRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(torneoService.save(dto));
    }
    @PutMapping("/{id}")
    @Operation(summary = "Actualizacion de torneo", description = "Actualiza los datos de un torneo existente.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Torneo actualizado"),
            @ApiResponse(responseCode = "400", description = "Datos del torneo invalidos"),
            @ApiResponse(responseCode = "404", description = "Torneo no encontrado")
    })
    public ResponseEntity<TorneoResponseDTO> update(
            @Parameter(description = "Id del torneo a actualizar", required = true, example = "1")
            @PathVariable Long id, @RequestBody TorneoRequestDTO dto) {
        return ResponseEntity.ok(torneoService.updateById(id, dto));
    }

    @PatchMapping("/{id}/abrir")
    @Operation(summary = "Apertura de torneo", description = "Cambia el estado de un torneo a abierto.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Torneo abierto"),
            @ApiResponse(responseCode = "404", description = "Torneo no encontrado")
    })
    public ResponseEntity<TorneoResponseDTO> abrir(
            @Parameter(description = "Id del torneo a abrir", required = true, example = "1")
            @PathVariable Long id) {
        return ResponseEntity.ok(torneoService.abrir(id));
    }

    @PatchMapping("/{id}/cancelar")
    @Operation(summary = "Cancelacion de torneo", description = "Cancela un torneo existente.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Torneo cancelado"),
            @ApiResponse(responseCode = "404", description = "Torneo no encontrado")
    })
    public ResponseEntity<TorneoResponseDTO> cancelar(
            @Parameter(description = "Id del torneo a cancelar", required = true, example = "1")
            @PathVariable Long id) {
        return ResponseEntity.ok(torneoService.cancelar(id));
    }

    @PatchMapping("/{id}/cerrar")
    @Operation(summary = "Cierre de torneo", description = "Cierra un torneo existente.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Torneo cerrado"),
            @ApiResponse(responseCode = "404", description = "Torneo no encontrado")
    })
    public ResponseEntity<TorneoResponseDTO> cerrar(
            @Parameter(description = "Id del torneo a cerrar", required = true, example = "1")
            @PathVariable Long id) {
        return ResponseEntity.ok(torneoService.cerrar(id));
    }
}