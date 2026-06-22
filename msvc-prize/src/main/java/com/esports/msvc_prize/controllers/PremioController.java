package com.esports.msvc_prize.controllers;
import com.esports.msvc_prize.models.PremioAsignado;
import com.esports.msvc_prize.models.dtos.PremioRequestDTO;
import com.esports.msvc_prize.models.dtos.PremioResponseDTO;
import com.esports.msvc_prize.services.PremioService;
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

@RestController
@RequestMapping("/api/v1/premios")
@Tag(name = "Premios V1", description = "Metodos CRUD para la gestion de premios con HATEOAS")
public class PremioController {

    @Autowired
    private PremioService premioService;

    @GetMapping
    @Operation(
            summary = "Listado de premios",
            description = "Devuelve una lista de premios. Permite filtrar por torneo."
    )
    @ApiResponse(responseCode = "200", description = "Operacion exitosa")
    public ResponseEntity<List<PremioResponseDTO>> findAll(
            @Parameter(description = "Id del torneo asociado al premio", example = "1")
            @RequestParam(required=false) Long torneoId) {
        return torneoId != null ?
                ResponseEntity.ok(premioService.findByTorneoId(torneoId)) :
                ResponseEntity.ok(premioService.findAll());
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Busqueda de un premio",
            description = "Devuelve un premio mediante su id, en caso contrario devuelve una excepcion."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Premio encontrado",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = PremioResponseDTO.class),
                            examples = @ExampleObject(
                                    name = "Ejemplo premio",
                                    value = "{\"premioId\":1," +
                                            "\"torneoId\":1," +
                                            "\"posicion\":1," +
                                            "\"descripcion\":\"Primer lugar\"," +
                                            "\"valor\":100000," +
                                            "\"estado\":\"ACTIVO\"," +
                                            "\"createdAt\":\"2026-05-26T03:26:56.797847\"}"
                            )
                    )
            ),
            @ApiResponse(responseCode = "400", description = "El premio no se encuentra en la BD")
    })
    public ResponseEntity<PremioResponseDTO> findById(
            @Parameter(description = "Id del premio a buscar", required = true, example = "1")
            @PathVariable Long id) {
        return ResponseEntity.ok(premioService.findById(id));
    }

    @GetMapping("/asignaciones/participante/{pId}")
    @Operation(
            summary = "Listado de asignaciones por participante",
            description = "Devuelve las asignaciones de premios asociadas a un participante."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Asignaciones encontradas"),
            @ApiResponse(responseCode = "400", description = "Participante invalido")
    })
    public ResponseEntity<List<PremioAsignado>> findAsignaciones(
            @Parameter(description = "Id del participante", required = true, example = "1")
            @PathVariable Long pId) {
        return ResponseEntity.ok(premioService.findAsignacionesByParticipanteId(pId));
    }

    @PostMapping
    @Operation(summary = "Guardado de premio", description = "Crea un premio nuevo para un torneo.")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Premio a crear",
            required = true,
            content = @Content(schema = @Schema(implementation = PremioRequestDTO.class))
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Premio creado"),
            @ApiResponse(responseCode = "400", description = "Datos del premio invalidos")
    })
    public ResponseEntity<PremioResponseDTO> save(@Valid @RequestBody PremioRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(premioService.save(dto));
    }

    @PostMapping("/torneo/{torneoId}/asignar-todos")
    @Operation(summary = "Asignacion de premios del torneo", description = "Asigna todos los premios configurados para un torneo.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Premios asignados"),
            @ApiResponse(responseCode = "400", description = "No se pudieron asignar los premios")
    })
    public ResponseEntity<List<PremioAsignado>> asignarTodos(
            @Parameter(description = "Id del torneo", required = true, example = "1")
            @PathVariable Long torneoId) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(premioService.asignarPremiosTorneo(torneoId));
    }
    @PostMapping("/{id}/asignar")
    @Operation(summary = "Asignacion de premio", description = "Asigna un premio a un participante.")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Participante al que se asigna el premio",
            required = true,
            content = @Content(examples = @ExampleObject(value = "{\"participanteId\":1}"))
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Premio asignado"),
            @ApiResponse(responseCode = "400", description = "No se pudo asignar el premio")
    })
    public ResponseEntity<PremioAsignado> asignar(
            @Parameter(description = "Id del premio a asignar", required = true, example = "1")
            @PathVariable Long id, @RequestBody Map<String,Long> body) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(premioService.asignarPremio(id, body.get("participanteId")));
    }
    @PutMapping("/{id}")
    @Operation(summary = "Actualizacion de premio", description = "Actualiza los datos de un premio existente.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Premio actualizado"),
            @ApiResponse(responseCode = "400", description = "Premio o datos invalidos")
    })
    public ResponseEntity<PremioResponseDTO> update(
            @Parameter(description = "Id del premio a actualizar", required = true, example = "1")
            @PathVariable Long id, @RequestBody PremioRequestDTO dto) {
        return ResponseEntity.ok(premioService.updateById(id, dto)); }

    @PatchMapping("/{id}/desactivar")
    @Operation(summary = "Desactivacion de premio", description = "Desactiva un premio existente.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Premio desactivado"),
            @ApiResponse(responseCode = "400", description = "Premio no encontrado o no desactivable")
    })
    public ResponseEntity<PremioResponseDTO> desactivar(
            @Parameter(description = "Id del premio a desactivar", required = true, example = "1")
            @PathVariable Long id) {
        return ResponseEntity.ok(premioService.desactivar(id));
    }
}