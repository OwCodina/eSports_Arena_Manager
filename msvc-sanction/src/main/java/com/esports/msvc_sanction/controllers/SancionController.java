package com.esports.msvc_sanction.controllers;
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
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/api/v1/sanciones")
@Tag(name = "Sanciones V1", description = "Metodos CRUD para la gestion de sanciones con HATEOAS")

public class SancionController {
    @Autowired
    private SancionService sancionService;

    @GetMapping
    @Operation(
            summary = "Listado de sanciones",
            description = "Devuelve una lista de sanciones. Permite filtrar por usuario, equipo o estado."
    )
    @ApiResponse(responseCode = "200", description = "Operacion exitosa")
    public ResponseEntity<List<SancionResponseDTO>> findAll(
            @Parameter(description = "Id del usuario sancionado", example = "1")
            @RequestParam(required = false) Long usuarioId,
            @Parameter(description = "Id del equipo sancionado", example = "1")
            @RequestParam(required = false) Long equipoId,
            @Parameter(description = "Estado de la sancion", example = "ACTIVA")
            @RequestParam(required = false) String estado) {
        {
            if (usuarioId != null)
                return ResponseEntity.ok(sancionService.findByUsuarioId(usuarioId));
            else if (equipoId != null)
                return ResponseEntity.ok(sancionService.findByEquipoId(equipoId));
            else if (estado != null)
                return ResponseEntity.ok(sancionService.findByEstado(estado));
            return ResponseEntity.ok(sancionService.findAll());
        }
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
                                    value = "{\"sancionId\":1," +
                                            "\"usuarioId\":1," +
                                            "\"equipoId\":null," +
                                            "\"motivo\":\"Conducta antideportiva\"," +
                                            "\"fechaInicio\":\"2026-06-01\"," +
                                            "\"fechaFin\":\"2026-06-15\"," +
                                            "\"estado\":\"ACTIVA\"," +
                                            "\"severidad\":\"MEDIA\"," +
                                            "\"createdAt\":\"2026-05-26T03:26:56.797847\"," +
                                            "\"updatedAt\":\"2026-05-27T03:26:56.797847\"}"
                            )
                    )
            ),
            @ApiResponse(responseCode = "404", description = "La sancion no se encuentra en la BD")
    })

    public ResponseEntity<SancionResponseDTO> findById(
            @Parameter(description = "Id de la sancion a buscar", required = true, example = "1")
            @PathVariable Long id) {
        return ResponseEntity.ok(sancionService.findById(id));
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
    public ResponseEntity<Map<String,Boolean>> tieneUsuario(
            @Parameter(description = "Id del usuario", required = true, example = "1")
            @PathVariable Long usuarioId) {
        return ResponseEntity.ok(Map.of("tieneSancionActiva",
                sancionService.tieneUsuarioSancionActiva(usuarioId)));
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
    public ResponseEntity<Map<String,Boolean>> tieneEquipo(
            @Parameter(description = "Id del equipo", required = true, example = "1")
            @PathVariable Long equipoId) {
        return ResponseEntity.ok(Map.of("tieneSancionActiva",
                sancionService.tieneEquipoSancionActiva(equipoId)));
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
    public ResponseEntity<SancionResponseDTO> save(@Valid @RequestBody SancionRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(sancionService.save(dto));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizacion de sancion", description = "Actualiza los datos de una sancion existente.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Sancion actualizada"),
            @ApiResponse(responseCode = "400", description = "Datos de la sancion invalidos"),
            @ApiResponse(responseCode = "404", description = "Sancion no encontrada")
    })
    public ResponseEntity<SancionResponseDTO> update(
            @Parameter(description = "Id de la sancion a actualizar", required = true, example = "1")
            @PathVariable Long id, @RequestBody SancionRequestDTO dto) {
        return ResponseEntity.ok(sancionService.updateById(id, dto));
    }

    @PatchMapping("/{id}/cerrar")
    @Operation(summary = "Cierre de sancion", description = "Cierra una sancion existente.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Sancion cerrada"),
            @ApiResponse(responseCode = "404", description = "Sancion no encontrada")
    })
    public ResponseEntity<SancionResponseDTO> cerrar(
            @Parameter(description = "Id de la sancion a cerrar", required = true, example = "1")
            @PathVariable Long id) {
        return ResponseEntity.ok(sancionService.cerrar(id));
    }
}