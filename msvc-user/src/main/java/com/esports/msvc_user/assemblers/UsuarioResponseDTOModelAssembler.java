package com.esports.msvc_user.assemblers;

import com.esports.msvc_user.controllers.UsuarioControllerV2;
import com.esports.msvc_user.models.dtos.UsuarioResponseDTO;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@Component
public class UsuarioResponseDTOModelAssembler implements RepresentationModelAssembler<UsuarioResponseDTO, EntityModel<UsuarioResponseDTO>> {

    @Override
    public EntityModel<UsuarioResponseDTO> toModel(UsuarioResponseDTO usuarioDTO) {
        return EntityModel.of(
                usuarioDTO,
                linkTo(methodOn(UsuarioControllerV2.class).findById(usuarioDTO.getUsuarioId())).withSelfRel(),
                linkTo(methodOn(UsuarioControllerV2.class).findAll()).withRel("usuarios"));
    }
}