package com.esports.msvc_match.controllers;

import com.esports.msvc_match.assemblers.PartidaResponseDTOModelAssembler;
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
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping("/api/v2/partidas")
@Tag(name = "Partidas V2", description = "Metodos CRUD para la gestion de partidas con HATEOAS")
public class PartidaControllerV2 {

    @Autowired
    private PartidaService partidaService;

    @Autowired
    private PartidaResponseDTOModelAssembler partidaResponseDTOModelAssembler;

    @GetMapping
    @Operation(
            summary = "Listado de partidas",
            description = "Devuelve una lista de partidas. Permite filtrar por torneo, ronda o estado."
    )
    @ApiResponse(responseCode = "200", description = "Operacion exitosa")
    public ResponseEntity<CollectionModel<EntityModel<PartidaResponseDTO>>> findAll(
            @Parameter(description = "Id del torneo asociado a la partida", example = "1")
            @RequestParam(required = false) Long torneoId,
            @Parameter(description = "Ronda de la partida", example = "1")
            @RequestParam(required = false) Integer ronda,
            @Parameter(description = "Estado de la partida", example = "PROGRAMADA")
            @RequestParam(required = false) String estado) {
        List<PartidaResponseDTO> partidas;

        if (torneoId != null && ronda != null) {
            partidas = this.partidaService.findByTorneoIdAndRonda(torneoId, ronda);
        } else if (torneoId != null && estado != null) {
            partidas = this.partidaService.findByTorneoIdAndEstado(torneoId, estado);
        } else if (torneoId != null) {
            partidas = this.partidaService.findByTorneoId(torneoId);
        } else {
            partidas = this.partidaService.findAll();
        }

        List<EntityModel<PartidaResponseDTO>> entityModels = partidas
                .stream()
                .map(partidaResponseDTOModelAssembler::toModel)
                .toList();

        CollectionModel<EntityModel<PartidaResponseDTO>> collectionModel = CollectionModel.of(
                entityModels,
                linkTo(methodOn(PartidaControllerV2.class).findAll(torneoId, ronda, estado)).withSelfRel()
        );

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(collectionModel);
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
    public ResponseEntity<EntityModel<PartidaResponseDTO>> findById(
            @Parameter(description = "Id de la partida a buscar", required = true, example = "1")
            @PathVariable Long id) {
        EntityModel<PartidaResponseDTO> entityModel = this.partidaResponseDTOModelAssembler.toModel(
                this.partidaService.findById(id)
        );
        return ResponseEntity.ok(entityModel);
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
    public ResponseEntity<EntityModel<PartidaResponseDTO>> save(@Valid @RequestBody PartidaRequestDTO dto) {
        PartidaResponseDTO partidaCreate = this.partidaService.save(dto);
        EntityModel<PartidaResponseDTO> entityModel = this.partidaResponseDTOModelAssembler.toModel(partidaCreate);
        return ResponseEntity.status(HttpStatus.CREATED).body(entityModel);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizacion de partida", description = "Actualiza los datos de una partida existente.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Partida actualizada"),
            @ApiResponse(responseCode = "400", description = "Partida o datos invalidos")
    })
    public ResponseEntity<EntityModel<PartidaResponseDTO>> update(
            @Parameter(description = "Id de la partida a actualizar", required = true, example = "1")
            @PathVariable Long id,
            @Valid @RequestBody PartidaRequestDTO dto) {
        PartidaResponseDTO partidaUpdate = this.partidaService.updateById(id, dto);
        EntityModel<PartidaResponseDTO> entityModel = this.partidaResponseDTOModelAssembler.toModel(partidaUpdate);
        return ResponseEntity.ok(entityModel);
    }

    @PatchMapping("/{id}/cancelar")
    @Operation(summary = "Cancelacion de partida", description = "Cancela una partida existente.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Partida cancelada"),
            @ApiResponse(responseCode = "400", description = "Partida no encontrada o no cancelable")
    })
    public ResponseEntity<EntityModel<PartidaResponseDTO>> cancelar(
            @Parameter(description = "Id de la partida a cancelar", required = true, example = "1")
            @PathVariable Long id) {
        PartidaResponseDTO partidaCancelada = this.partidaService.cancelar(id);
        EntityModel<PartidaResponseDTO> entityModel = this.partidaResponseDTOModelAssembler.toModel(partidaCancelada);
        return ResponseEntity.ok(entityModel);
    }
}
