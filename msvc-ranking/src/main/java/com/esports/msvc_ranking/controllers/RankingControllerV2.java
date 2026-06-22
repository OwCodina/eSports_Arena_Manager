package com.esports.msvc_ranking.controllers;

import com.esports.msvc_ranking.assemblers.RankingResponseDTOModelAssembler;
import com.esports.msvc_ranking.models.dtos.ActualizarRankingRequestDTO;
import com.esports.msvc_ranking.models.dtos.RankingResponseDTO;
import com.esports.msvc_ranking.services.RankingService;
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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping("/api/v2/rankings")
@Tag(name = "Rankings V2", description = "Metodos para la gestion de rankings con HATEOAS")
public class RankingControllerV2 {

    @Autowired
    private RankingService rankingService;

    @Autowired
    private RankingResponseDTOModelAssembler rankingResponseDTOModelAssembler;

    @GetMapping("/torneo/{torneoId}")
    @Operation(
            summary = "Listado de rankings por torneo",
            description = "Devuelve el ranking completo de un torneo."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Operacion exitosa"),
            @ApiResponse(responseCode = "400", description = "Torneo o ranking invalido")
    })
    public ResponseEntity<CollectionModel<EntityModel<RankingResponseDTO>>> findByTorneo(
            @Parameter(description = "Id del torneo", required = true, example = "1")
            @PathVariable Long torneoId) {
        List<EntityModel<RankingResponseDTO>> entityModels = this.rankingService.findByTorneoId(torneoId)
                .stream()
                .map(rankingResponseDTOModelAssembler::toModel)
                .toList();

        CollectionModel<EntityModel<RankingResponseDTO>> collectionModel = CollectionModel.of(
                entityModels,
                linkTo(methodOn(RankingControllerV2.class).findByTorneo(torneoId)).withSelfRel()
        );

        return ResponseEntity.ok(collectionModel);
    }

    @GetMapping("/torneo/{torneoId}/participante/{participanteId}")
    @Operation(
            summary = "Busqueda de ranking por participante",
            description = "Devuelve el ranking de un participante dentro de un torneo."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Ranking encontrado",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = RankingResponseDTO.class),
                            examples = @ExampleObject(
                                    name = "Ejemplo ranking",
                                    value = "{\"rankingId\":1," +
                                            "\"torneoId\":1," +
                                            "\"participanteId\":10," +
                                            "\"puntos\":3,\"victorias\":1," +
                                            "\"derrotas\":0," +
                                            "\"diferencia\":2," +
                                            "\"posicion\":1," +
                                            "\"updatedAt\":\"2026-05-26T03:26:56.797847\"}"
                            )
                    )
            ),
            @ApiResponse(responseCode = "400", description = "Ranking no encontrado")
    })
    public ResponseEntity<EntityModel<RankingResponseDTO>> findByParticipante(
            @Parameter(description = "Id del torneo", required = true, example = "1")
            @PathVariable Long torneoId,
            @Parameter(description = "Id del participante", required = true, example = "10")
            @PathVariable Long participanteId) {
        EntityModel<RankingResponseDTO> entityModel = this.rankingResponseDTOModelAssembler.toModel(
                this.rankingService.findByTorneoIdAndParticipanteId(torneoId, participanteId)
        );
        return ResponseEntity.ok(entityModel);
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Busqueda de ranking",
            description = "Devuelve un ranking mediante su id, en caso contrario devuelve una excepcion."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Ranking encontrado"),
            @ApiResponse(responseCode = "400", description = "Ranking no encontrado")
    })
    public ResponseEntity<EntityModel<RankingResponseDTO>> findById(
            @Parameter(description = "Id del ranking a buscar", required = true, example = "1")
            @PathVariable Long id) {
        EntityModel<RankingResponseDTO> entityModel = this.rankingResponseDTOModelAssembler.toModel(
                this.rankingService.findById(id)
        );
        return ResponseEntity.ok(entityModel);
    }

    @PostMapping("/torneo/{torneoId}/inicializar")
    @Operation(summary = "Inicializacion de ranking", description = "Inicializa el ranking de un torneo.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Ranking inicializado"),
            @ApiResponse(responseCode = "400", description = "No se pudo inicializar el ranking")
    })
    public ResponseEntity<CollectionModel<EntityModel<RankingResponseDTO>>> inicializar(
            @Parameter(description = "Id del torneo", required = true, example = "1")
            @PathVariable Long torneoId) {
        List<EntityModel<RankingResponseDTO>> entityModels = this.rankingService.inicializarRanking(torneoId)
                .stream()
                .map(rankingResponseDTOModelAssembler::toModel)
                .toList();

        CollectionModel<EntityModel<RankingResponseDTO>> collectionModel = CollectionModel.of(
                entityModels,
                linkTo(methodOn(RankingControllerV2.class).findByTorneo(torneoId)).withRel("rankings")
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(collectionModel);
    }

    @PostMapping("/torneo/{torneoId}/actualizar")
    @Operation(summary = "Actualizacion de ranking", description = "Actualiza el ranking de un torneo usando un resultado.")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Datos del resultado para actualizar ranking",
            required = true,
            content = @Content(schema = @Schema(implementation = ActualizarRankingRequestDTO.class))
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Ranking actualizado"),
            @ApiResponse(responseCode = "400", description = "Datos de ranking invalidos")
    })
    public ResponseEntity<EntityModel<RankingResponseDTO>> actualizar(
            @Parameter(description = "Id del torneo", required = true, example = "1")
            @PathVariable Long torneoId,
            @Valid @RequestBody ActualizarRankingRequestDTO dto) {
        RankingResponseDTO rankingUpdate = this.rankingService.actualizarConResultado(
                torneoId,
                dto.getGanadorId(),
                dto.getPuntajeGanador(),
                dto.getPuntajePerdedor(),
                dto.getPerdedorId()
        );
        EntityModel<RankingResponseDTO> entityModel = this.rankingResponseDTOModelAssembler.toModel(rankingUpdate);
        return ResponseEntity.ok(entityModel);
    }

    @PostMapping("/torneo/{torneoId}/recalcular")
    @Operation(summary = "Recalculo de posiciones", description = "Recalcula las posiciones del ranking de un torneo.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Posiciones recalculadas"),
            @ApiResponse(responseCode = "400", description = "No se pudo recalcular el ranking")
    })
    public ResponseEntity<Void> recalcular(
            @Parameter(description = "Id del torneo", required = true, example = "1")
            @PathVariable Long torneoId) {
        this.rankingService.recalcularPosiciones(torneoId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/torneo/{torneoId}/cerrar")
    @Operation(summary = "Cierre de ranking", description = "Cierra el ranking de un torneo.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Ranking cerrado"),
            @ApiResponse(responseCode = "400", description = "No se pudo cerrar el ranking")
    })
    public ResponseEntity<Void> cerrar(
            @Parameter(description = "Id del torneo", required = true, example = "1")
            @PathVariable Long torneoId) {
        this.rankingService.cerrarRanking(torneoId);
        return ResponseEntity.ok().build();
    }
}
