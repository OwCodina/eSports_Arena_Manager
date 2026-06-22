package com.esports.msvc_registration.controllers;

import com.esports.msvc_registration.assemblers.InscripcionResponseDTOModelAssembler;
import com.esports.msvc_registration.models.dtos.InscripcionRequestDTO;
import com.esports.msvc_registration.models.dtos.InscripcionResponseDTO;
import com.esports.msvc_registration.services.InscripcionService;
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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping("/api/v2/inscripciones")
@Tag(name = "Inscripciones V2", description = "Metodos CRUD para la gestion de inscripciones con HATEOAS")
public class InscripcionControllerV2 {

    @Autowired
    private InscripcionService inscripcionService;

    @Autowired
    private InscripcionResponseDTOModelAssembler inscripcionResponseDTOModelAssembler;

    @GetMapping
    @Operation(
            summary = "Listado de inscripciones",
            description = "Devuelve una lista de inscripciones. Permite filtrar por torneo, equipo o jugador."
    )
    @ApiResponse(responseCode = "200", description = "Operacion exitosa")
    public ResponseEntity<CollectionModel<EntityModel<InscripcionResponseDTO>>> findAll(
            @Parameter(description = "Id del torneo asociado a la inscripcion", example = "1")
            @RequestParam(required = false) Long torneoId,
            @Parameter(description = "Id del equipo inscrito", example = "1")
            @RequestParam(required = false) Long equipoId,
            @Parameter(description = "Id del jugador inscrito", example = "1")
            @RequestParam(required = false) Long jugadorId) {
        List<InscripcionResponseDTO> inscripciones;

        if (torneoId != null) {
            inscripciones = this.inscripcionService.findByTorneoId(torneoId);
        } else if (equipoId != null) {
            inscripciones = this.inscripcionService.findByEquipoId(equipoId);
        } else if (jugadorId != null) {
            inscripciones = this.inscripcionService.findByJugadorId(jugadorId);
        } else {
            inscripciones = this.inscripcionService.findAll();
        }

        List<EntityModel<InscripcionResponseDTO>> entityModels = inscripciones
                .stream()
                .map(inscripcionResponseDTOModelAssembler::toModel)
                .toList();

        CollectionModel<EntityModel<InscripcionResponseDTO>> collectionModel = CollectionModel.of(
                entityModels,
                linkTo(methodOn(InscripcionControllerV2.class).findAll(torneoId, equipoId, jugadorId)).withSelfRel()
        );

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(collectionModel);
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Busqueda de una inscripcion",
            description = "Devuelve una inscripcion mediante su id, en caso contrario devuelve una excepcion."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Inscripcion encontrada",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = InscripcionResponseDTO.class),
                            examples = @ExampleObject(
                                    name = "Ejemplo inscripcion",
                                    value = "{\"inscripcionId\":1," +
                                            "\"torneoId\":1," +
                                            "\"equipoId\":1," +
                                            "\"jugadorId\":1," +
                                            "\"tipoParticipante\":\"EQUIPO\"," +
                                            "\"estado\":\"PENDIENTE\"," +
                                            "\"fechaInscripcion\":\"2026-05-26T03:26:56.797847\"," +
                                            "\"createdAt\":\"2026-05-26T03:26:56.797847\"}"
                            )
                    )
            ),
            @ApiResponse(responseCode = "400", description = "La inscripcion no se encuentra en la BD")
    })
    public ResponseEntity<EntityModel<InscripcionResponseDTO>> findById(
            @Parameter(description = "Id de la inscripcion a buscar", required = true, example = "1")
            @PathVariable Long id) {
        EntityModel<InscripcionResponseDTO> entityModel = this.inscripcionResponseDTOModelAssembler.toModel(
                this.inscripcionService.findById(id)
        );
        return ResponseEntity.ok(entityModel);
    }

    @PostMapping
    @Operation(summary = "Guardado de inscripcion", description = "Crea una inscripcion nueva para un torneo.")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Inscripcion a crear",
            required = true,
            content = @Content(schema = @Schema(implementation = InscripcionRequestDTO.class))
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Inscripcion creada"),
            @ApiResponse(responseCode = "400", description = "Datos de la inscripcion invalidos")
    })
    public ResponseEntity<EntityModel<InscripcionResponseDTO>> save(@Valid @RequestBody InscripcionRequestDTO dto) {
        InscripcionResponseDTO inscripcionCreate = this.inscripcionService.save(dto);
        EntityModel<InscripcionResponseDTO> entityModel = this.inscripcionResponseDTOModelAssembler.toModel(inscripcionCreate);
        return ResponseEntity.status(HttpStatus.CREATED).body(entityModel);
    }

    @PatchMapping("/{id}/estado")
    @Operation(summary = "Actualizacion del estado de la inscripcion", description = "Actualiza el estado de una inscripcion existente.")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Nuevo estado de la inscripcion",
            required = true,
            content = @Content(examples = @ExampleObject(value = "{\"estado\":\"APROBADA\"}"))
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Estado actualizado"),
            @ApiResponse(responseCode = "400", description = "Inscripcion o estado invalido")
    })
    public ResponseEntity<EntityModel<InscripcionResponseDTO>> updateEstado(
            @Parameter(description = "Id de la inscripcion a actualizar", required = true, example = "1")
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        InscripcionResponseDTO inscripcionUpdate = this.inscripcionService.updateEstado(id, body.get("estado"));
        EntityModel<InscripcionResponseDTO> entityModel = this.inscripcionResponseDTOModelAssembler.toModel(inscripcionUpdate);
        return ResponseEntity.ok(entityModel);
    }

    @PatchMapping("/{id}/cancelar")
    @Operation(summary = "Cancelacion de inscripcion", description = "Cancela una inscripcion existente.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Inscripcion cancelada"),
            @ApiResponse(responseCode = "400", description = "Inscripcion no encontrada o no cancelable")
    })
    public ResponseEntity<EntityModel<InscripcionResponseDTO>> cancelar(
            @Parameter(description = "Id de la inscripcion a cancelar", required = true, example = "1")
            @PathVariable Long id) {
        InscripcionResponseDTO inscripcionCancelada = this.inscripcionService.cancelar(id);
        EntityModel<InscripcionResponseDTO> entityModel = this.inscripcionResponseDTOModelAssembler.toModel(inscripcionCancelada);
        return ResponseEntity.ok(entityModel);
    }
}
