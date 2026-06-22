package com.esports.msvc_tournament.assemblers;

import com.esports.msvc_tournament.controllers.TorneoControllerV2;
import com.esports.msvc_tournament.models.dtos.TorneoResponseDTO;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class TorneoResponseDTOModelAssembler implements RepresentationModelAssembler<TorneoResponseDTO, EntityModel<TorneoResponseDTO>> {

    @Override
    public EntityModel<TorneoResponseDTO> toModel(TorneoResponseDTO torneoDTO) {
        return EntityModel.of(
                torneoDTO,
                linkTo(methodOn(TorneoControllerV2.class).findById(torneoDTO.getTorneoId())).withSelfRel(),
                linkTo(methodOn(TorneoControllerV2.class).findAll(null, null)).withRel("torneos")
        );
    }
}
