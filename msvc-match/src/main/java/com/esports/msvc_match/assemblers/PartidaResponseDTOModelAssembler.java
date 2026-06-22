package com.esports.msvc_match.assemblers;

import com.esports.msvc_match.controllers.PartidaControllerV2;
import com.esports.msvc_match.models.dtos.PartidaResponseDTO;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class PartidaResponseDTOModelAssembler implements RepresentationModelAssembler<PartidaResponseDTO, EntityModel<PartidaResponseDTO>> {

    @Override
    public EntityModel<PartidaResponseDTO> toModel(PartidaResponseDTO partidaDTO) {
        return EntityModel.of(
                partidaDTO,
                linkTo(methodOn(PartidaControllerV2.class).findById(partidaDTO.getPartidaId())).withSelfRel(),
                linkTo(methodOn(PartidaControllerV2.class).findAll(null, null, null)).withRel("partidas")
        );
    }
}
