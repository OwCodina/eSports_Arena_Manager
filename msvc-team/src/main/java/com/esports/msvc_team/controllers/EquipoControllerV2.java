package com.esports.msvc_team.controllers;

import com.esports.msvc_team.assemblers.EquipoResponseDTOModelAssembler;
import com.esports.msvc_team.models.MiembroEquipo;
import com.esports.msvc_team.models.dtos.EquipoRequestDTO;
import com.esports.msvc_team.models.dtos.EquipoResponseDTO;
import com.esports.msvc_team.models.dtos.MiembroRequestDTO;
import com.esports.msvc_team.services.EquipoService;
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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping("/api/v2/equipos")
@Tag(name = "Equipos V2", description = "Metodos CRUD para la gestion de equipos con HATEOAS")
public class EquipoControllerV2 {

    @Autowired
    private EquipoService equipoService;

    @Autowired
    private EquipoResponseDTOModelAssembler equipoResponseDTOModelAssembler;

    @GetMapping
    @Operation(
            summary = "Listado de todos los equipos",
            description = "Devuelve una lista de equipos con enlaces HATEOAS. Permite filtrar por estado, juego principal o capitan."
    )
    @ApiResponse(responseCode = "200", description = "Operacion exitosa")
    public ResponseEntity<CollectionModel<EntityModel<EquipoResponseDTO>>> findAll(
            @Parameter(description = "Estado del equipo", example = "ACTIVO")
            @RequestParam(required = false) String estado,
            @Parameter(description = "Id del juego principal", example = "1")
            @RequestParam(required = false) Long juegoId,
            @Parameter(description = "Id del capitan del equipo", example = "1")
            @RequestParam(required = false) Long capitanId) {
        List<EquipoResponseDTO> equipos;

        if (juegoId != null) {
            equipos = equipoService.findByJuegoPrincipalId(juegoId);
        } else if (capitanId != null) {
            equipos = equipoService.findByCapitanId(capitanId);
        } else if (estado != null) {
            equipos = equipoService.findByEstado(estado);
        } else {
            equipos = equipoService.findAll();
        }

        List<EntityModel<EquipoResponseDTO>> entityModels = equipos
                .stream()
                .map(equipoResponseDTOModelAssembler::toModel)
                .toList();

        CollectionModel<EntityModel<EquipoResponseDTO>> collectionModel = CollectionModel.of(
                entityModels,
                linkTo(methodOn(EquipoControllerV2.class).findAll(estado, juegoId, capitanId)).withSelfRel()
        );

        return ResponseEntity.status(HttpStatus.OK).body(collectionModel);
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Busqueda de un equipo",
            description = "Devuelve un equipo mediante su id con enlaces HATEOAS."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Equipo encontrado",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = EquipoResponseDTO.class),
                            examples = @ExampleObject(
                                    name = "Ejemplo equipo",
                                    value = "{\"equipoId\":1,\"nombre\":\"Team Alpha\",\"capitanId\":1,\"juegoPrincipalId\":1,\"estado\":\"ACTIVO\",\"createdAt\":\"2026-05-26T03:26:56.797847\",\"updatedAt\":\"2026-05-27T03:26:56.797847\"}"
                            )
                    )
            ),
            @ApiResponse(responseCode = "404", description = "El equipo no se encuentra en la BD")
    })
    public ResponseEntity<EntityModel<EquipoResponseDTO>> findById(
            @Parameter(description = "Id del equipo a buscar", required = true, example = "1")
            @PathVariable Long id) {
        EntityModel<EquipoResponseDTO> entityModel = equipoResponseDTOModelAssembler.toModel(
                equipoService.findById(id)
        );
        return ResponseEntity.ok(entityModel);
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
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Equipo a crear",
            required = true,
            content = @Content(schema = @Schema(implementation = EquipoRequestDTO.class))
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Equipo creado"),
            @ApiResponse(responseCode = "400", description = "Datos del equipo invalidos"),
            @ApiResponse(responseCode = "404", description = "No se pudo validar capitan o juego principal")
    })
    public ResponseEntity<EntityModel<EquipoResponseDTO>> save(
            @Valid @org.springframework.web.bind.annotation.RequestBody EquipoRequestDTO dto) {
        EntityModel<EquipoResponseDTO> entityModel = equipoResponseDTOModelAssembler.toModel(equipoService.save(dto));
        return ResponseEntity.status(HttpStatus.CREATED).body(entityModel);
    }

    @PostMapping("/{id}/miembros")
    @Operation(summary = "Agrega un miembro al equipo", description = "Asocia un usuario activo como miembro de un equipo activo.")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
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
    public ResponseEntity<EntityModel<EquipoResponseDTO>> update(
            @Parameter(description = "Id del equipo a actualizar", required = true, example = "1")
            @PathVariable Long id,
            @Valid @org.springframework.web.bind.annotation.RequestBody EquipoRequestDTO dto) {
        EntityModel<EquipoResponseDTO> entityModel = equipoResponseDTOModelAssembler.toModel(
                equipoService.updateById(id, dto)
        );
        return ResponseEntity.ok(entityModel);
    }

    @PatchMapping("/{id}/desactivar")
    @Operation(summary = "Actualizacion del estado del equipo", description = "Desactiva un equipo existente.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Equipo desactivado"),
            @ApiResponse(responseCode = "404", description = "Equipo no encontrado")
    })
    public ResponseEntity<EntityModel<EquipoResponseDTO>> desactivar(
            @Parameter(description = "Id del equipo a desactivar", required = true, example = "1")
            @PathVariable Long id) {
        EntityModel<EquipoResponseDTO> entityModel = equipoResponseDTOModelAssembler.toModel(
                equipoService.desactivar(id)
        );
        return ResponseEntity.ok(entityModel);
    }
}
