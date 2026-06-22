package com.esports.msvc_prize.assemblers;

import com.esports.msvc_prize.controllers.PremioControllerV2;
import com.esports.msvc_prize.models.dtos.PremioResponseDTO;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class PremioResponseDTOModelAssembler implements RepresentationModelAssembler<PremioResponseDTO, EntityModel<PremioResponseDTO>> {

    @Override
    public EntityModel<PremioResponseDTO> toModel(PremioResponseDTO premioDTO) {
        return EntityModel.of(
                premioDTO,
                linkTo(methodOn(PremioControllerV2.class).findById(premioDTO.getPremioId())).withSelfRel(),
                linkTo(methodOn(PremioControllerV2.class).findAll(null)).withRel("premios")
        );
    }
}
