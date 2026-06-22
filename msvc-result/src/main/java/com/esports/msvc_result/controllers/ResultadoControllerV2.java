package com.esports.msvc_result.controllers;

import com.esports.msvc_result.assemblers.ResultadoResponseDTOModelAssembler;
import com.esports.msvc_result.models.dtos.ResultadoRequestDTO;
import com.esports.msvc_result.models.dtos.ResultadoResponseDTO;
import com.esports.msvc_result.services.ResultadoService;
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
@RequestMapping("/api/v2/resultados")
@Tag(name = "Resultados V2", description = "Metodos CRUD para la gestion de resultados con HATEOAS")
public class ResultadoControllerV2 {

    @Autowired
    private ResultadoService resultadoService;

    @Autowired
    private ResultadoResponseDTOModelAssembler resultadoResponseDTOModelAssembler;

    @GetMapping
    @Operation(
            summary = "Listado de resultados",
            description = "Devuelve una lista de resultados. Permite filtrar por estado de validacion."
    )
    @ApiResponse(responseCode = "200", description = "Operacion exitosa")
    public ResponseEntity<CollectionModel<EntityModel<ResultadoResponseDTO>>> findAll(
            @Parameter(description = "Estado de validacion del resultado", example = "PENDIENTE")
            @RequestParam(required = false) String estadoValidacion) {
        List<ResultadoResponseDTO> resultados = estadoValidacion != null
                ? this.resultadoService.findByEstadoValidacion(estadoValidacion)
                : this.resultadoService.findAll();

        List<EntityModel<ResultadoResponseDTO>> entityModels = resultados
                .stream()
                .map(resultadoResponseDTOModelAssembler::toModel)
                .toList();

        CollectionModel<EntityModel<ResultadoResponseDTO>> collectionModel = CollectionModel.of(
                entityModels,
                linkTo(methodOn(ResultadoControllerV2.class).findAll(estadoValidacion)).withSelfRel()
        );

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(collectionModel);
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Busqueda de un resultado",
            description = "Devuelve un resultado mediante su id, en caso contrario devuelve una excepcion."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Resultado encontrado",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ResultadoResponseDTO.class),
                            examples = @ExampleObject(
                                    name = "Ejemplo resultado",
                                    value = "{\"resultadoId\":1," +
                                            "\"partidaId\":1," +
                                            "\"ganadorId\":10," +
                                            "\"puntajeA\":13," +
                                            "\"puntajeB\":8," +
                                            "\"estadoValidacion\":\"PENDIENTE\"," +
                                            "\"motivoAnulacion\":null," +
                                            "\"fechaRegistro\":\"2026-05-26T03:26:56.797847\"," +
                                            "\"createdAt\":\"2026-05-26T03:26:56.797847\"}"
                            )
                    )
            ),
            @ApiResponse(responseCode = "400", description = "El resultado no se encuentra en la BD")
    })
    public ResponseEntity<EntityModel<ResultadoResponseDTO>> findById(
            @Parameter(description = "Id del resultado a buscar", required = true, example = "1")
            @PathVariable Long id) {
        EntityModel<ResultadoResponseDTO> entityModel = this.resultadoResponseDTOModelAssembler.toModel(
                this.resultadoService.findById(id)
        );
        return ResponseEntity.ok(entityModel);
    }

    @GetMapping("/partida/{partidaId}")
    @Operation(
            summary = "Busqueda de resultado por partida",
            description = "Devuelve el resultado asociado a una partida."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Resultado encontrado"),
            @ApiResponse(responseCode = "400", description = "Resultado no encontrado")
    })
    public ResponseEntity<EntityModel<ResultadoResponseDTO>> findByPartida(
            @Parameter(description = "Id de la partida", required = true, example = "1")
            @PathVariable Long partidaId) {
        EntityModel<ResultadoResponseDTO> entityModel = this.resultadoResponseDTOModelAssembler.toModel(
                this.resultadoService.findByPartidaId(partidaId)
        );
        return ResponseEntity.ok(entityModel);
    }

    @PostMapping
    @Operation(summary = "Guardado de resultado", description = "Crea un resultado nuevo para una partida.")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Resultado a crear",
            required = true,
            content = @Content(schema = @Schema(implementation = ResultadoRequestDTO.class))
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Resultado creado"),
            @ApiResponse(responseCode = "400", description = "Datos del resultado invalidos")
    })
    public ResponseEntity<EntityModel<ResultadoResponseDTO>> save(@Valid @RequestBody ResultadoRequestDTO dto) {
        ResultadoResponseDTO resultadoCreate = this.resultadoService.save(dto);
        EntityModel<ResultadoResponseDTO> entityModel = this.resultadoResponseDTOModelAssembler.toModel(resultadoCreate);
        return ResponseEntity.status(HttpStatus.CREATED).body(entityModel);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizacion de resultado", description = "Actualiza los datos de un resultado existente.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Resultado actualizado"),
            @ApiResponse(responseCode = "400", description = "Resultado o datos invalidos")
    })
    public ResponseEntity<EntityModel<ResultadoResponseDTO>> update(
            @Parameter(description = "Id del resultado a actualizar", required = true, example = "1")
            @PathVariable Long id,
            @Valid @RequestBody ResultadoRequestDTO dto) {
        ResultadoResponseDTO resultadoUpdate = this.resultadoService.updateById(id, dto);
        EntityModel<ResultadoResponseDTO> entityModel = this.resultadoResponseDTOModelAssembler.toModel(resultadoUpdate);
        return ResponseEntity.ok(entityModel);
    }

    @PatchMapping("/{id}/validar")
    @Operation(summary = "Validacion de resultado", description = "Valida un resultado existente.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Resultado validado"),
            @ApiResponse(responseCode = "400", description = "Resultado no encontrado o no validable")
    })
    public ResponseEntity<EntityModel<ResultadoResponseDTO>> validar(
            @Parameter(description = "Id del resultado a validar", required = true, example = "1")
            @PathVariable Long id) {
        ResultadoResponseDTO resultadoValidado = this.resultadoService.validar(id);
        EntityModel<ResultadoResponseDTO> entityModel = this.resultadoResponseDTOModelAssembler.toModel(resultadoValidado);
        return ResponseEntity.ok(entityModel);
    }

    @PatchMapping("/{id}/anular")
    @Operation(summary = "Anulacion de resultado", description = "Anula un resultado existente indicando un motivo.")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Motivo de anulacion",
            required = true,
            content = @Content(examples = @ExampleObject(value = "{\"motivo\":\"Resultado reportado incorrectamente\"}"))
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Resultado anulado"),
            @ApiResponse(responseCode = "400", description = "Resultado no encontrado o no anulable")
    })
    public ResponseEntity<EntityModel<ResultadoResponseDTO>> anular(
            @Parameter(description = "Id del resultado a anular", required = true, example = "1")
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        ResultadoResponseDTO resultadoAnulado = this.resultadoService.anular(id, body.get("motivo"));
        EntityModel<ResultadoResponseDTO> entityModel = this.resultadoResponseDTOModelAssembler.toModel(resultadoAnulado);
        return ResponseEntity.ok(entityModel);
    }
}
