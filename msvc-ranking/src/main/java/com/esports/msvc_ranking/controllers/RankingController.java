package com.esports.msvc_ranking.controllers;

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

@RestController
@RequestMapping("/api/v1/rankings")
@Tag(name = "Rankings V1", description = "Metodos para la gestion de rankings con HATEOAS")
public class RankingController {

    @Autowired
    private RankingService rankingService;

    @GetMapping("/torneo/{torneoId}")
    @Operation(
            summary = "Listado de rankings por torneo",
            description = "Devuelve el ranking completo de un torneo."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Operacion exitosa"),
            @ApiResponse(responseCode = "400", description = "Torneo o ranking invalido")
    })
    public ResponseEntity<List<RankingResponseDTO>> findByTorneo(
            @Parameter(description = "Id del torneo", required = true, example = "1")
            @PathVariable Long torneoId) {
        return ResponseEntity.ok(rankingService.findByTorneoId(torneoId));
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
    public ResponseEntity<RankingResponseDTO> findByParticipante(
            @Parameter(description = "Id del torneo", required = true, example = "1")
            @PathVariable Long torneoId,
            @Parameter(description = "Id del participante", required = true, example = "10")
            @PathVariable Long participanteId){
        return ResponseEntity.ok(rankingService.findByTorneoIdAndParticipanteId(torneoId, participanteId));
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
    public ResponseEntity<RankingResponseDTO> findById(
            @Parameter(description = "Id del ranking a buscar", required = true, example = "1")
            @PathVariable Long id) {
        return ResponseEntity.ok(rankingService.findById(id));
    }

    @PostMapping("/torneo/{torneoId}/inicializar")
    @Operation(summary = "Inicializacion de ranking", description = "Inicializa el ranking de un torneo.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Ranking inicializado"),
            @ApiResponse(responseCode = "400", description = "No se pudo inicializar el ranking")
    })
    public ResponseEntity<List<RankingResponseDTO>> inicializar(
            @Parameter(description = "Id del torneo", required = true, example = "1")
            @PathVariable Long torneoId) {
        return ResponseEntity.status(HttpStatus.CREATED).body(rankingService.inicializarRanking(torneoId));
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
    public ResponseEntity<RankingResponseDTO> actualizar(
            @Parameter(description = "Id del torneo", required = true, example = "1")
            @PathVariable Long torneoId,
            @Valid @RequestBody ActualizarRankingRequestDTO dto) {
        return ResponseEntity.ok(rankingService.actualizarConResultado(
                torneoId, dto.getGanadorId(), dto.getPuntajeGanador(),
                dto.getPuntajePerdedor(), dto.getPerdedorId()));
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
        rankingService.recalcularPosiciones(torneoId);
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
        rankingService.cerrarRanking(torneoId);
        return ResponseEntity.ok().build();
    }
}
