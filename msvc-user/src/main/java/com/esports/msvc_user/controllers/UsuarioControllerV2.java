package com.esports.msvc_user.controllers;

import com.esports.msvc_user.assemblers.UsuarioResponseDTOModelAssembler;
import com.esports.msvc_user.models.dtos.UsuarioResponseDTO;
import com.esports.msvc_user.models.dtos.UsuarioRequestDTO;
import com.esports.msvc_user.services.UsuarioService;
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
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@RestController
@RequestMapping("/api/v2/usuarios")
@Tag(name="Usuarios V2", description = "Metodos CRUD para la gestión de usuarios")

public class UsuarioControllerV2 {

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private UsuarioResponseDTOModelAssembler usuarioResponseDTOModelAssembler;

    @GetMapping
    @Operation(
            summary = "Listado de todos los usuarios",
            description = "Se devuelve una lista con los usuarios que se encuentran en la tabla usuarios de la DB"
    )
    @ApiResponse(responseCode = "200", description = "Operacion Exitosa")
    public ResponseEntity<CollectionModel<EntityModel<UsuarioResponseDTO>>> findAll(){
        List<EntityModel<UsuarioResponseDTO>> entityModels = this.usuarioService.findAll()
                .stream()
                .map(usuarioResponseDTOModelAssembler::toModel)
                .toList();
        CollectionModel<EntityModel<UsuarioResponseDTO>> collectionModel = CollectionModel.of(
                entityModels,
                linkTo(methodOn(UsuarioControllerV2.class).findAll()).withSelfRel()
        );
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(collectionModel);

    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Busqueda de un usuario",
            description = "Se devuelve un usuario, en caso contrario se devuelve una excepcion"
    )
    @ApiResponses(value={
            @ApiResponse(
                    responseCode = "200", description = "Usuario encontrado",
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
    public ResponseEntity<EntityModel<UsuarioResponseDTO>> findById(
            @Parameter(description = "Id del usuario a buscar", required = true, example = "1")
            @PathVariable Long id
    ) {
        EntityModel<UsuarioResponseDTO> entityModel = this.usuarioResponseDTOModelAssembler.toModel(
                this.usuarioService.findById(id)
        );
        return ResponseEntity.ok(entityModel);
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
    public ResponseEntity<EntityModel<UsuarioResponseDTO>> findByNickname(
            @Parameter(description = "Nickname del usuario a buscar", required = true, example = "username")
            @PathVariable String nickname) {
        EntityModel<UsuarioResponseDTO> entityModel = this.usuarioResponseDTOModelAssembler.toModel(
                this.usuarioService.findByNickname(nickname)
        );
        return ResponseEntity.ok(entityModel);
    }

    @PostMapping
    @Operation(summary = "Guardado de usuario", description = "Esta es la forma de guardar un usuario")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Usuario a crear", required = true,
            content = @Content(schema = @Schema(implementation = UsuarioRequestDTO.class))
    )
    @ApiResponse(responseCode = "201", description = "Usuario creado")
    public ResponseEntity<EntityModel<UsuarioResponseDTO>> save(@Valid @RequestBody UsuarioRequestDTO dto) {
        UsuarioResponseDTO usuarioCreate = this.usuarioService.save(dto);
        EntityModel<UsuarioResponseDTO> entityModel = this.usuarioResponseDTOModelAssembler.toModel(usuarioCreate);
        return ResponseEntity.status(HttpStatus.CREATED).body(entityModel);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizacion de usuario", description = "Se actualizan los datos de un usuario existente")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Usuario actualizado"),
            @ApiResponse(responseCode = "404", description = "Usuario no se encuentra en la BD")
    })
    public ResponseEntity<EntityModel<UsuarioResponseDTO>> update(
            @Parameter(description = "Id del usuario a actualizar", required = true, example = "1")
            @PathVariable Long id,
            @Valid @RequestBody UsuarioRequestDTO dto) {
        UsuarioResponseDTO usuarioUpdate = this.usuarioService.updateById(id, dto);
        EntityModel<UsuarioResponseDTO> entityModel = this.usuarioResponseDTOModelAssembler.toModel(usuarioUpdate);

        return ResponseEntity.ok(entityModel);
    }

    @PatchMapping("/{id}/desactivar")
    @Operation(summary = "Actualizacion del estado del usuario ", description = "Un usuario existente es desactivado")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Usuario desactivado"),
            @ApiResponse(responseCode = "404", description = "Usuario no se encuentra en la BD")
    })
    public ResponseEntity<EntityModel<UsuarioResponseDTO>> desactivar(
            @Parameter(description = "Id del usuario a desactivar", required = true, example = "1")
            @PathVariable Long id) {
        UsuarioResponseDTO usuarioDesactivado = this.usuarioService.desactivar(id);
        EntityModel<UsuarioResponseDTO> entityModel = this.usuarioResponseDTOModelAssembler.toModel(usuarioDesactivado);
        return ResponseEntity.ok(entityModel);
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
    public ResponseEntity<Map<String, Boolean>> puedeCompetir(
            @Parameter(description = "Id del usuario a buscar", required = true, example = "1")
            @PathVariable Long id) {
        return ResponseEntity.ok(Map.of("puedeCompetir", usuarioService.puedeCompetir(id)));
    }


}

