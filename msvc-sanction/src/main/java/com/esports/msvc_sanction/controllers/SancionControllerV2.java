package com.esports.msvc_sanction.controllers;

import com.esports.msvc_sanction.assemblers.SancionResponseDTOModelAssembler;
import com.esports.msvc_sanction.models.dtos.SancionRequestDTO;
import com.esports.msvc_sanction.models.dtos.SancionResponseDTO;
import com.esports.msvc_sanction.services.SancionService;
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
import java.util.Map;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping("/api/v2/sanciones")
@Tag(name = "Sanciones V2", description = "Metodos CRUD para la gestion de sanciones con HATEOAS")
public class SancionControllerV2 {

    @Autowired
    private SancionService sancionService;

    @Autowired
    private SancionResponseDTOModelAssembler sancionResponseDTOModelAssembler;

    @GetMapping
    @Operation(
            summary = "Listado de sanciones",
            description = "Devuelve una lista de sanciones. Permite filtrar por usuario, equipo o estado."
    )
    @ApiResponse(responseCode = "200", description = "Operacion exitosa")
    public ResponseEntity<CollectionModel<EntityModel<SancionResponseDTO>>> findAll(
            @Parameter(description = "Id del usuario sancionado", example = "1")
            @RequestParam(required = false) Long usuarioId,
            @Parameter(description = "Id del equipo sancionado", example = "1")
            @RequestParam(required = false) Long equipoId,
            @Parameter(description = "Estado de la sancion", example = "ACTIVA")
            @RequestParam(required = false) String estado) {
        List<SancionResponseDTO> sanciones;

        if (usuarioId != null) {
            sanciones = this.sancionService.findByUsuarioId(usuarioId);
        } else if (equipoId != null) {
            sanciones = this.sancionService.findByEquipoId(equipoId);
        } else if (estado != null) {
            sanciones = this.sancionService.findByEstado(estado);
        } else {
            sanciones = this.sancionService.findAll();
        }

        List<EntityModel<SancionResponseDTO>> entityModels = sanciones
                .stream()
                .map(sancionResponseDTOModelAssembler::toModel)
                .toList();

        CollectionModel<EntityModel<SancionResponseDTO>> collectionModel = CollectionModel.of(
                entityModels,
                linkTo(methodOn(SancionControllerV2.class).findAll(usuarioId, equipoId, estado)).withSelfRel()
        );

        return ResponseEntity.status(HttpStatus.OK).body(collectionModel);
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Busqueda de una sancion",
            description = "Devuelve una sancion mediante su id, en caso contrario devuelve una excepcion."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Sancion encontrada",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = SancionResponseDTO.class),
                            examples = @ExampleObject(
                                    name = "Ejemplo sancion",
                                    value = "{\"sancionId\":1,\"usuarioId\":1,\"equipoId\":null,\"motivo\":\"Conducta antideportiva\",\"fechaInicio\":\"2026-06-01\",\"fechaFin\":\"2026-06-15\",\"estado\":\"ACTIVA\",\"severidad\":\"MEDIA\",\"createdAt\":\"2026-05-26T03:26:56.797847\",\"updatedAt\":\"2026-05-27T03:26:56.797847\"}"
                            )
                    )
            ),
            @ApiResponse(responseCode = "404", description = "La sancion no se encuentra en la BD")
    })
    public ResponseEntity<EntityModel<SancionResponseDTO>> findById(
            @Parameter(description = "Id de la sancion a buscar", required = true, example = "1")
            @PathVariable Long id) {
        EntityModel<SancionResponseDTO> entityModel = this.sancionResponseDTOModelAssembler.toModel(
                this.sancionService.findById(id)
        );
        return ResponseEntity.ok(entityModel);
    }

    @GetMapping("/usuario/{usuarioId}/tiene-activa")
    @Operation(
            summary = "Confirma si un usuario tiene sancion activa",
            description = "Devuelve true si el usuario tiene una sancion activa."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Estado obtenido"),
            @ApiResponse(responseCode = "404", description = "Usuario no encontrado")
    })
    public ResponseEntity<Map<String, Boolean>> tieneUsuario(
            @Parameter(description = "Id del usuario", required = true, example = "1")
            @PathVariable Long usuarioId) {
        return ResponseEntity.ok(Map.of("tieneSancionActiva", this.sancionService.tieneUsuarioSancionActiva(usuarioId)));
    }

    @GetMapping("/equipo/{equipoId}/tiene-activa")
    @Operation(
            summary = "Confirma si un equipo tiene sancion activa",
            description = "Devuelve true si el equipo tiene una sancion activa."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Estado obtenido"),
            @ApiResponse(responseCode = "404", description = "Equipo no encontrado")
    })
    public ResponseEntity<Map<String, Boolean>> tieneEquipo(
            @Parameter(description = "Id del equipo", required = true, example = "1")
            @PathVariable Long equipoId) {
        return ResponseEntity.ok(Map.of("tieneSancionActiva", this.sancionService.tieneEquipoSancionActiva(equipoId)));
    }

    @PostMapping
    @Operation(summary = "Guardado de sancion", description = "Crea una sancion nueva.")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Sancion a crear",
            required = true,
            content = @Content(schema = @Schema(implementation = SancionRequestDTO.class))
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Sancion creada"),
            @ApiResponse(responseCode = "400", description = "Datos de la sancion invalidos")
    })
    public ResponseEntity<EntityModel<SancionResponseDTO>> save(@Valid @RequestBody SancionRequestDTO dto) {
        SancionResponseDTO sancionCreate = this.sancionService.save(dto);
        EntityModel<SancionResponseDTO> entityModel = this.sancionResponseDTOModelAssembler.toModel(sancionCreate);
        return ResponseEntity.status(HttpStatus.CREATED).body(entityModel);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizacion de sancion", description = "Actualiza los datos de una sancion existente.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Sancion actualizada"),
            @ApiResponse(responseCode = "400", description = "Datos de la sancion invalidos"),
            @ApiResponse(responseCode = "404", description = "Sancion no encontrada")
    })
    public ResponseEntity<EntityModel<SancionResponseDTO>> update(
            @Parameter(description = "Id de la sancion a actualizar", required = true, example = "1")
            @PathVariable Long id,
            @Valid @RequestBody SancionRequestDTO dto) {
        SancionResponseDTO sancionUpdate = this.sancionService.updateById(id, dto);
        EntityModel<SancionResponseDTO> entityModel = this.sancionResponseDTOModelAssembler.toModel(sancionUpdate);
        return ResponseEntity.ok(entityModel);
    }

    @PatchMapping("/{id}/cerrar")
    @Operation(summary = "Cierre de sancion", description = "Cierra una sancion existente.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Sancion cerrada"),
            @ApiResponse(responseCode = "404", description = "Sancion no encontrada")
    })
    public ResponseEntity<EntityModel<SancionResponseDTO>> cerrar(
            @Parameter(description = "Id de la sancion a cerrar", required = true, example = "1")
            @PathVariable Long id) {
        SancionResponseDTO sancionCerrada = this.sancionService.cerrar(id);
        EntityModel<SancionResponseDTO> entityModel = this.sancionResponseDTOModelAssembler.toModel(sancionCerrada);
        return ResponseEntity.ok(entityModel);
    }
}
