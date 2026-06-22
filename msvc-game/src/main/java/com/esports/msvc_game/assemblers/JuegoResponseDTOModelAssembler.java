package com.esports.msvc_game.assemblers;

import com.esports.msvc_game.controllers.JuegoControllerV2;
import com.esports.msvc_game.models.dtos.JuegoResponseDTO;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class JuegoResponseDTOModelAssembler implements RepresentationModelAssembler<JuegoResponseDTO, EntityModel<JuegoResponseDTO>> {

    @Override
    public EntityModel<JuegoResponseDTO> toModel(JuegoResponseDTO juegoDTO) {
        return EntityModel.of(
                juegoDTO,
                linkTo(methodOn(JuegoControllerV2.class).findById(juegoDTO.getJuegoId())).withSelfRel(),
                linkTo(methodOn(JuegoControllerV2.class).findAll()).withRel("juegos"),
                linkTo(methodOn(JuegoControllerV2.class).findAllActivos()).withRel("juegos-activos"),
                linkTo(methodOn(JuegoControllerV2.class).desactivar(juegoDTO.getJuegoId())).withRel("desactivar")
        );
    }
}
