package com.esports.msvc_notification.controllers;

import com.esports.msvc_notification.assemblers.NotificacionResponseDTOModelAssembler;
import com.esports.msvc_notification.models.dtos.NotificacionRequestDTO;
import com.esports.msvc_notification.models.dtos.NotificacionResponseDTO;
import com.esports.msvc_notification.services.NotificacionService;
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
@RequestMapping("/api/v2/notificaciones")
@Tag(name = "Notificaciones V2", description = "Metodos CRUD para la gestion de notificaciones con HATEOAS")
public class NotificacionControllerV2 {

    @Autowired
    private NotificacionService notificacionService;

    @Autowired
    private NotificacionResponseDTOModelAssembler notificacionResponseDTOModelAssembler;

    @GetMapping
    @Operation(
            summary = "Listado de notificaciones",
            description = "Devuelve una lista de notificaciones. Permite filtrar por usuario o equipo."
    )
    @ApiResponse(responseCode = "200", description = "Operacion exitosa")
    public ResponseEntity<CollectionModel<EntityModel<NotificacionResponseDTO>>> findAll(
            @Parameter(description = "Id del usuario asociado a la notificacion", example = "1")
            @RequestParam(required = false) Long usuarioId,
            @Parameter(description = "Id del equipo asociado a la notificacion", example = "1")
            @RequestParam(required = false) Long equipoId) {
        List<NotificacionResponseDTO> notificaciones;

        if (usuarioId != null) {
            notificaciones = this.notificacionService.findByUsuarioId(usuarioId);
        } else if (equipoId != null) {
            notificaciones = this.notificacionService.findByEquipoId(equipoId);
        } else {
            notificaciones = this.notificacionService.findAll();
        }

        List<EntityModel<NotificacionResponseDTO>> entityModels = notificaciones
                .stream()
                .map(notificacionResponseDTOModelAssembler::toModel)
                .toList();

        CollectionModel<EntityModel<NotificacionResponseDTO>> collectionModel = CollectionModel.of(
                entityModels,
                linkTo(methodOn(NotificacionControllerV2.class).findAll(usuarioId, equipoId)).withSelfRel()
        );

        return ResponseEntity.status(HttpStatus.OK).body(collectionModel);
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Busqueda de una notificacion",
            description = "Devuelve una notificacion mediante su id, en caso contrario devuelve una excepcion."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Notificacion encontrada",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = NotificacionResponseDTO.class),
                            examples = @ExampleObject(
                                    name = "Ejemplo notificacion",
                                    value = "{\"notificacionId\":1," +
                                            "usuarioId\":1," +
                                            "\"equipoId\":null," +
                                            "\"tipo\":\"SISTEMA\"," +
                                            "\"mensaje\":\"Tienes una nueva invitacion\"," +
                                            "\"leida\":false," +
                                            "\"estado\":\"ACTIVA\"," +
                                            "\"fecha\":\"2026-05-26T03:26:56.797847\"," +
                                            "\"createdAt\":\"2026-05-26T03:26:56.797847\"}"
                            )
                    )
            ),
            @ApiResponse(responseCode = "400", description = "La notificacion no se encuentra en la BD")
    })
    public ResponseEntity<EntityModel<NotificacionResponseDTO>> findById(
            @Parameter(description = "Id de la notificacion a buscar", required = true, example = "1")
            @PathVariable Long id) {
        EntityModel<NotificacionResponseDTO> entityModel = this.notificacionResponseDTOModelAssembler.toModel(
                this.notificacionService.findById(id)
        );
        return ResponseEntity.ok(entityModel);
    }

    @GetMapping("/usuario/{usuarioId}/no-leidas")
    @Operation(
            summary = "Listado de notificaciones no leidas por usuario",
            description = "Devuelve las notificaciones no leidas asociadas a un usuario."
    )
    @ApiResponse(responseCode = "200", description = "Operacion exitosa")
    public ResponseEntity<CollectionModel<EntityModel<NotificacionResponseDTO>>> noLeidasUsuario(
            @Parameter(description = "Id del usuario", required = true, example = "1")
            @PathVariable Long usuarioId) {
        List<EntityModel<NotificacionResponseDTO>> entityModels = this.notificacionService.findNoLeidasByUsuarioId(usuarioId)
                .stream()
                .map(notificacionResponseDTOModelAssembler::toModel)
                .toList();

        CollectionModel<EntityModel<NotificacionResponseDTO>> collectionModel = CollectionModel.of(
                entityModels,
                linkTo(methodOn(NotificacionControllerV2.class).noLeidasUsuario(usuarioId)).withSelfRel()
        );

        return ResponseEntity.ok(collectionModel);
    }

    @GetMapping("/equipo/{equipoId}/no-leidas")
    @Operation(
            summary = "Listado de notificaciones no leidas por equipo",
            description = "Devuelve las notificaciones no leidas asociadas a un equipo."
    )
    @ApiResponse(responseCode = "200", description = "Operacion exitosa")
    public ResponseEntity<CollectionModel<EntityModel<NotificacionResponseDTO>>> noLeidasEquipo(
            @Parameter(description = "Id del equipo", required = true, example = "1")
            @PathVariable Long equipoId) {
        List<EntityModel<NotificacionResponseDTO>> entityModels = this.notificacionService.findNoLeidasByEquipoId(equipoId)
                .stream()
                .map(notificacionResponseDTOModelAssembler::toModel)
                .toList();

        CollectionModel<EntityModel<NotificacionResponseDTO>> collectionModel = CollectionModel.of(
                entityModels,
                linkTo(methodOn(NotificacionControllerV2.class).noLeidasEquipo(equipoId)).withSelfRel()
        );

        return ResponseEntity.ok(collectionModel);
    }

    @PostMapping
    @Operation(summary = "Guardado de notificacion", description = "Crea una notificacion nueva.")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Notificacion a crear",
            required = true,
            content = @Content(schema = @Schema(implementation = NotificacionRequestDTO.class))
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Notificacion creada"),
            @ApiResponse(responseCode = "400", description = "Datos de la notificacion invalidos")
    })
    public ResponseEntity<EntityModel<NotificacionResponseDTO>> save(@Valid @RequestBody NotificacionRequestDTO dto) {
        NotificacionResponseDTO notificacionCreate = this.notificacionService.save(dto);
        EntityModel<NotificacionResponseDTO> entityModel = this.notificacionResponseDTOModelAssembler.toModel(notificacionCreate);
        return ResponseEntity.status(HttpStatus.CREATED).body(entityModel);
    }

    @PatchMapping("/{id}/leer")
    @Operation(summary = "Lectura de notificacion", description = "Marca una notificacion como leida.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Notificacion marcada como leida"),
            @ApiResponse(responseCode = "400", description = "Notificacion no encontrada")
    })
    public ResponseEntity<EntityModel<NotificacionResponseDTO>> leer(
            @Parameter(description = "Id de la notificacion", required = true, example = "1")
            @PathVariable Long id) {
        NotificacionResponseDTO notificacionLeida = this.notificacionService.marcarComoLeida(id);
        EntityModel<NotificacionResponseDTO> entityModel = this.notificacionResponseDTOModelAssembler.toModel(notificacionLeida);
        return ResponseEntity.ok(entityModel);
    }

    @PatchMapping("/usuario/{usuarioId}/leer-todas")
    @Operation(summary = "Lectura de notificaciones por usuario", description = "Marca todas las notificaciones de un usuario como leidas.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Notificaciones actualizadas"),
            @ApiResponse(responseCode = "400", description = "Usuario invalido")
    })
    public ResponseEntity<Map<String, Integer>> leerTodasUsuario(
            @Parameter(description = "Id del usuario", required = true, example = "1")
            @PathVariable Long usuarioId) {
        return ResponseEntity.ok(Map.of("notificacionesActualizadas", this.notificacionService.marcarTodasLeidasByUsuarioId(usuarioId)));
    }

    @PatchMapping("/equipo/{equipoId}/leer-todas")
    @Operation(summary = "Lectura de notificaciones por equipo", description = "Marca todas las notificaciones de un equipo como leidas.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Notificaciones actualizadas"),
            @ApiResponse(responseCode = "400", description = "Equipo invalido")
    })
    public ResponseEntity<Map<String, Integer>> leerTodasEquipo(
            @Parameter(description = "Id del equipo", required = true, example = "1")
            @PathVariable Long equipoId) {
        return ResponseEntity.ok(Map.of("notificacionesActualizadas", this.notificacionService.marcarTodasLeidasByEquipoId(equipoId)));
    }

    @PatchMapping("/{id}/archivar")
    @Operation(summary = "Archivado de notificacion", description = "Archiva una notificacion existente.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Notificacion archivada"),
            @ApiResponse(responseCode = "400", description = "Notificacion no encontrada")
    })
    public ResponseEntity<EntityModel<NotificacionResponseDTO>> archivar(
            @Parameter(description = "Id de la notificacion", required = true, example = "1")
            @PathVariable Long id) {
        NotificacionResponseDTO notificacionArchivada = this.notificacionService.archivar(id);
        EntityModel<NotificacionResponseDTO> entityModel = this.notificacionResponseDTOModelAssembler.toModel(notificacionArchivada);
        return ResponseEntity.ok(entityModel);
    }
}
