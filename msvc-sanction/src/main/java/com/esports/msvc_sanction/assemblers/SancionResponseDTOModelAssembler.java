package com.esports.msvc_sanction.assemblers;

import com.esports.msvc_sanction.controllers.SancionControllerV2;
import com.esports.msvc_sanction.models.dtos.SancionResponseDTO;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class SancionResponseDTOModelAssembler implements RepresentationModelAssembler<SancionResponseDTO, EntityModel<SancionResponseDTO>> {

    @Override
    public EntityModel<SancionResponseDTO> toModel(SancionResponseDTO sancionDTO) {
        return EntityModel.of(
                sancionDTO,
                linkTo(methodOn(SancionControllerV2.class).findById(sancionDTO.getSancionId())).withSelfRel(),
                linkTo(methodOn(SancionControllerV2.class).findAll(null, null, null)).withRel("sanciones")
        );
    }
}
