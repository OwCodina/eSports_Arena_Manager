package com.esports.msvc_user.controllers;

import com.esports.msvc_user.models.dtos.UsuarioRequestDTO;
import com.esports.msvc_user.models.dtos.UsuarioResponseDTO;
import com.esports.msvc_user.services.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/usuarios")
@Tag(name="Usuarios V1", description = "Metodos CRUD para la gestión de usuarios")
public class UsuarioController {

    @Autowired
    private UsuarioService usuarioService;


    @GetMapping
    @Operation(
            summary = "Listado de todos los usuarios",
            description = "Se devuelve una lista con los usuarios que se encuentran en la tabla usuarios de la DB"
    )
    @ApiResponse(responseCode = "200", description = "Operacion Exitosa")
    public ResponseEntity<List<UsuarioResponseDTO>> findAll(
            @RequestParam(required = false) String rol,
            @RequestParam(required = false) String estado) {
        if (rol != null && estado != null)
            return ResponseEntity.ok(usuarioService.findByRolAndEstado(rol, estado));
        else if (rol != null)
            return ResponseEntity.ok(usuarioService.findByRol(rol));
        else if (estado != null)
            return ResponseEntity.ok(usuarioService.findByEstado(estado));
        return ResponseEntity.ok(usuarioService.findAll());
    }


    @GetMapping("/{id}")
    @Operation(
            summary = "Busqueda de un usuario",
            description = "Se devuelve un usuario, en caso contrario se devuelve una excepcion"
    )
    @ApiResponses(value={
            @ApiResponse(
                    responseCode = "200", description = "usuario encontrado",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = UsuarioResponseDTO.class),
                            examples = {
                                    @ExampleObject(
                                            name = "Ejemplo usuario",
                                            value = "{\"usuarioId\": \"1\"," +
                                                    "\"nombre\": \"Juan\"," +
                                                    "\"nickname\": \"usuarin\"," +
                                                    "\"email\": \"correo@gmail.com\"," +
                                                    "\"rol\": \"Jugador\"," +
                                                    "\"estado\": \"Activo\"," +
                                                    "\"fechaRegistro\": \"2026-05-26\"," +
                                                    "\"CreatedAt\": \"2026-05-26T03:26:56.797847\"," +
                                                    "\"UpdatedAt\": \"2026-05-27T03:26:56.797847\"}"
                                    )
                            }
                    )),
            @ApiResponse(responseCode = "404", description = "El usuario no se encuentra en la BD")
    })
    public ResponseEntity<UsuarioResponseDTO> findById(
            @Parameter(description = "Id del usuario a buscar", required = true, example = "1")
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(usuarioService.findById(id));
    }


    @GetMapping("/nickname/{nickname}")
    @Operation(
            summary = "Busqueda de usuario mediante nickname",
            description = "Se devuelve un usuario, en caso contrario se devuelve una excepcion"
    )
    @ApiResponses(value={
            @ApiResponse(
                    responseCode = "200", description = "usuario encontrado",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = UsuarioResponseDTO.class)
                    )),
            @ApiResponse(responseCode = "404", description = "El usuario no se encuentra en la BD")
    })
    public ResponseEntity<UsuarioResponseDTO> findByNickname(@PathVariable String nickname) {
        return ResponseEntity.ok(usuarioService.findByNickname(nickname));
    }

    @GetMapping("/{id}/puede-competir")
    @Operation(
            summary = "Confirma si el usuario puede competir",
            description = "Se devuelve el estado del usuario, en caso contrario se devuelve una excepcion"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Activo / Inactivo "),
            @ApiResponse(responseCode = "404", description = "Usuario no encontrado")
    })
    public ResponseEntity<Map<String, Boolean>> puedeCompetir(@PathVariable Long id) {
        return ResponseEntity.ok(Map.of("puedeCompetir", usuarioService.puedeCompetir(id)));
    }

    @PostMapping
    @Operation(summary = "Guardado de usuario", description = "Esta es la forma de guardar un usuario")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Usuario a crear", required = true,
            content = @Content(schema = @Schema(implementation = UsuarioRequestDTO.class))
    )
    @ApiResponse(responseCode = "201", description = "Usuario creado")
    public ResponseEntity<UsuarioResponseDTO> save(@Valid @RequestBody UsuarioRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(usuarioService.save(dto));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizacion de usuario", description = "Se actualizan los datos de un usuario existente")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Usuario actualizado"),
            @ApiResponse(responseCode = "404", description = "Usuario no se encuentra en la BD")
    })
    public ResponseEntity<UsuarioResponseDTO> update(
            @Parameter(description = "Id del usuario a actualizar", required = true, example = "1")
            @PathVariable Long id,
            @Valid @RequestBody UsuarioRequestDTO dto) {
        return ResponseEntity.ok(usuarioService.updateById(id, dto));
    }

    @PatchMapping("/{id}/desactivar")
    @Operation(summary = "Actualizacion del estado del usuario ", description = "Un usuario existente es desactivado")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Usuario desactivado"),
            @ApiResponse(responseCode = "404", description = "Usuario no se encuentra en la BD")
    })
    public ResponseEntity<UsuarioResponseDTO> desactivar(
            @Parameter(description = "Id del usuario a desactivar", required = true, example = "1")
            @PathVariable Long id) {
        return ResponseEntity.ok(usuarioService.desactivar(id));
    }
}
