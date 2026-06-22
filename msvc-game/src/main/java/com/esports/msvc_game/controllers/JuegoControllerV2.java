package com.esports.msvc_game.controllers;

import com.esports.msvc_game.assemblers.JuegoResponseDTOModelAssembler;
import com.esports.msvc_game.models.dtos.JuegoRequestDTO;
import com.esports.msvc_game.models.dtos.JuegoResponseDTO;
import com.esports.msvc_game.services.JuegoService;
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
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping("/api/v2/juegos")
@Tag(name = "Juegos V2", description = "Metodos CRUD para la gestion de juegos con HATEOAS")
public class JuegoControllerV2 {

    @Autowired
    private JuegoService juegoService;

    @Autowired
    private JuegoResponseDTOModelAssembler juegoResponseDTOModelAssembler;

    @GetMapping
    @Operation(
            summary = "Listado de todos los juegos",
            description = "Devuelve una lista con todos los juegos registrados en la base de datos."
    )
    @ApiResponse(responseCode = "200", description = "Operacion exitosa")
    public ResponseEntity<CollectionModel<EntityModel<JuegoResponseDTO>>> findAll() {
        List<EntityModel<JuegoResponseDTO>> entityModels = this.juegoService.findAll()
                .stream()
                .map(juegoResponseDTOModelAssembler::toModel)
                .toList();

        CollectionModel<EntityModel<JuegoResponseDTO>> collectionModel = CollectionModel.of(
                entityModels,
                linkTo(methodOn(JuegoControllerV2.class).findAll()).withSelfRel(),
                linkTo(methodOn(JuegoControllerV2.class).findAllActivos()).withRel("juegos-activos")
        );

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(collectionModel);
    }

    @GetMapping("/activos")
    @Operation(
            summary = "Listado de juegos activos",
            description = "Devuelve una lista con los juegos que se encuentran activos."
    )
    @ApiResponse(responseCode = "200", description = "Operacion exitosa")
    public ResponseEntity<CollectionModel<EntityModel<JuegoResponseDTO>>> findAllActivos() {
        List<EntityModel<JuegoResponseDTO>> entityModels = this.juegoService.findAllActivos()
                .stream()
                .map(juegoResponseDTOModelAssembler::toModel)
                .toList();

        CollectionModel<EntityModel<JuegoResponseDTO>> collectionModel = CollectionModel.of(
                entityModels,
                linkTo(methodOn(JuegoControllerV2.class).findAllActivos()).withSelfRel(),
                linkTo(methodOn(JuegoControllerV2.class).findAll()).withRel("juegos")
        );

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(collectionModel);
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Busqueda de un juego",
            description = "Devuelve un juego mediante su id, en caso contrario devuelve una excepcion."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Juego encontrado",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = JuegoResponseDTO.class),
                            examples = @ExampleObject(
                                    name = "Ejemplo juego",
                                    value = "{\"juegoId\":1,\"nombre\":\"Valorant\",\"genero\":\"FPS\",\"modalidad\":\"5v5\",\"jugadoresPorEquipo\":5,\"estado\":\"ACTIVO\",\"createdAt\":\"2026-05-26T03:26:56.797847\",\"updatedAt\":\"2026-05-27T03:26:56.797847\"}"
                            )
                    )
            ),
            @ApiResponse(responseCode = "404", description = "El juego no se encuentra en la BD")
    })
    public ResponseEntity<EntityModel<JuegoResponseDTO>> findById(
            @Parameter(description = "Id del juego a buscar", required = true, example = "1")
            @PathVariable Long id) {
        EntityModel<JuegoResponseDTO> entityModel = this.juegoResponseDTOModelAssembler.toModel(
                this.juegoService.findById(id)
        );
        return ResponseEntity.ok(entityModel);
    }

    @PostMapping
    @Operation(summary = "Guardado de juego", description = "Crea un juego nuevo.")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Juego a crear",
            required = true,
            content = @Content(schema = @Schema(implementation = JuegoRequestDTO.class))
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Juego creado"),
            @ApiResponse(responseCode = "400", description = "Datos del juego invalidos")
    })
    public ResponseEntity<EntityModel<JuegoResponseDTO>> save(@Valid @RequestBody JuegoRequestDTO dto) {
        JuegoResponseDTO juegoCreate = this.juegoService.save(dto);
        EntityModel<JuegoResponseDTO> entityModel = this.juegoResponseDTOModelAssembler.toModel(juegoCreate);
        return ResponseEntity.status(HttpStatus.CREATED).body(entityModel);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizacion de juego", description = "Actualiza los datos de un juego existente.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Juego actualizado"),
            @ApiResponse(responseCode = "400", description = "Datos del juego invalidos"),
            @ApiResponse(responseCode = "404", description = "Juego no encontrado")
    })
    public ResponseEntity<EntityModel<JuegoResponseDTO>> update(
            @Parameter(description = "Id del juego a actualizar", required = true, example = "1")
            @PathVariable Long id,
            @Valid @RequestBody JuegoRequestDTO dto) {
        JuegoResponseDTO juegoUpdate = this.juegoService.updateById(id, dto);
        EntityModel<JuegoResponseDTO> entityModel = this.juegoResponseDTOModelAssembler.toModel(juegoUpdate);
        return ResponseEntity.ok(entityModel);
    }

    @PatchMapping("/{id}/desactivar")
    @Operation(summary = "Actualizacion del estado del juego", description = "Desactiva un juego existente.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Juego desactivado"),
            @ApiResponse(responseCode = "404", description = "Juego no encontrado")
    })
    public ResponseEntity<EntityModel<JuegoResponseDTO>> desactivar(
            @Parameter(description = "Id del juego a desactivar", required = true, example = "1")
            @PathVariable Long id) {
        JuegoResponseDTO juegoDesactivado = this.juegoService.desactivar(id);
        EntityModel<JuegoResponseDTO> entityModel = this.juegoResponseDTOModelAssembler.toModel(juegoDesactivado);
        return ResponseEntity.ok(entityModel);
    }
}
