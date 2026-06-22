package com.esports.msvc_match.controllers;
import com.esports.msvc_match.models.dtos.PartidaRequestDTO;
import com.esports.msvc_match.models.dtos.PartidaResponseDTO;
import com.esports.msvc_match.services.PartidaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import java.util.*;
@RestController
@RequestMapping("/api/v1/partidas")
@Tag(name = "Partidas V1", description = "Metodos CRUD para la gestion de partidas con HATEOAS")

public class PartidaController {

    @Autowired private
    PartidaService partidaService;

    @GetMapping
    @Operation(
            summary = "Listado de partidas",
            description = "Devuelve una lista de partidas. Permite filtrar por torneo, ronda o estado."
    )
    @ApiResponse(responseCode = "200", description = "Operacion exitosa")
    public ResponseEntity<List<PartidaResponseDTO>> findAll(
            @Parameter(description = "Id del torneo asociado a la partida", example = "1")
            @RequestParam(required=false) Long torneoId,
            @Parameter(description = "Ronda de la partida", example = "1")
            @RequestParam(required=false) Integer ronda,
            @Parameter(description = "Estado de la partida", example = "PROGRAMADA")
            @RequestParam(required=false) String estado) {
        if (torneoId != null && ronda != null)
            return ResponseEntity.ok(partidaService.findByTorneoIdAndRonda(torneoId, ronda));
        else if (torneoId != null && estado != null)
            return ResponseEntity.ok(partidaService.findByTorneoIdAndEstado(torneoId, estado));
        else if (torneoId != null)
            return ResponseEntity.ok(partidaService.findByTorneoId(torneoId));

        return ResponseEntity.ok(partidaService.findAll());
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Busqueda de una partida",
            description = "Devuelve una partida mediante su id, en caso contrario devuelve una excepcion."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Partida encontrada",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = PartidaResponseDTO.class),
                            examples = @ExampleObject(
                                    name = "Ejemplo partida",
                                    value = "{\"partidaId\":1," +
                                            "\"torneoId\":1," +
                                            "\"participanteAId\":1," +
                                            "\"participanteBId\":5," +
                                            "\"ronda\":1," +
                                            "\"fechaHora\":\"2026-05-26T03:26:56.797847\"," +
                                            "\"estado\":\"PROGRAMADA\"," +
                                            "\"createdAt\":\"2026-05-26T03:26:56.797847\"}"
                            )
                    )
            ),
            @ApiResponse(responseCode = "400", description = "La partida no se encuentra en la BD")
    })
    public ResponseEntity<PartidaResponseDTO> findById(
            @Parameter(description = "Id de la partida a buscar", required = true, example = "1")
            @PathVariable Long id) {
        return ResponseEntity.ok(partidaService.findById(id));
    }

    @PostMapping
    @Operation(summary = "Guardado de partida", description = "Crea una partida nueva para un torneo.")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Partida a crear",
            required = true,
            content = @Content(schema = @Schema(implementation = PartidaRequestDTO.class))
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Partida creada"),
            @ApiResponse(responseCode = "400", description = "Datos de la partida invalidos")
    })
    public ResponseEntity<PartidaResponseDTO> save(@Valid @RequestBody PartidaRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(partidaService.save(dto));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizacion de partida", description = "Actualiza los datos de una partida existente.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Partida actualizada"),
            @ApiResponse(responseCode = "400", description = "Partida o datos invalidos")
    })
    public ResponseEntity<PartidaResponseDTO> update(
            @Parameter(description = "Id de la partida a actualizar", required = true, example = "1")
            @PathVariable Long id, @RequestBody PartidaRequestDTO dto) {
        return ResponseEntity.ok(partidaService.updateById(id, dto));
    }

    @PatchMapping("/{id}/cancelar")
    @Operation(summary = "Cancelacion de partida", description = "Cancela una partida existente.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Partida cancelada"),
            @ApiResponse(responseCode = "400", description = "Partida no encontrada o no cancelable")
    })
    public ResponseEntity<PartidaResponseDTO> cancelar(
            @Parameter(description = "Id de la partida a cancelar", required = true, example = "1")
            @PathVariable Long id) {
        return ResponseEntity.ok(partidaService.cancelar(id));
    }
}