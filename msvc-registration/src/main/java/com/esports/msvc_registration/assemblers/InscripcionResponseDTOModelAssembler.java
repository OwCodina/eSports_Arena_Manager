package com.esports.msvc_registration.assemblers;

import com.esports.msvc_registration.controllers.InscripcionControllerV2;
import com.esports.msvc_registration.models.dtos.InscripcionResponseDTO;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class InscripcionResponseDTOModelAssembler implements RepresentationModelAssembler<InscripcionResponseDTO, EntityModel<InscripcionResponseDTO>> {

    @Override
    public EntityModel<InscripcionResponseDTO> toModel(InscripcionResponseDTO inscripcionDTO) {
        return EntityModel.of(
                inscripcionDTO,
                linkTo(methodOn(InscripcionControllerV2.class).findById(inscripcionDTO.getInscripcionId())).withSelfRel(),
                linkTo(methodOn(InscripcionControllerV2.class).findAll(null, null, null)).withRel("inscripciones")
        );
    }
}
