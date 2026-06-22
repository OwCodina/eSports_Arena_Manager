package com.esports.msvc_team.assemblers;

import com.esports.msvc_team.controllers.EquipoControllerV2;
import com.esports.msvc_team.models.dtos.EquipoResponseDTO;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class EquipoResponseDTOModelAssembler implements RepresentationModelAssembler<EquipoResponseDTO, EntityModel<EquipoResponseDTO>> {

    @Override
    public EntityModel<EquipoResponseDTO> toModel(EquipoResponseDTO equipoDTO) {
        return EntityModel.of(
                equipoDTO,
                linkTo(methodOn(EquipoControllerV2.class).findById(equipoDTO.getEquipoId())).withSelfRel(),
                linkTo(methodOn(EquipoControllerV2.class).findAll(null, null, null)).withRel("equipos")
        );
    }
}
