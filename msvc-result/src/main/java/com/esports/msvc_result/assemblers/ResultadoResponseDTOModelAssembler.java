package com.esports.msvc_result.assemblers;

import com.esports.msvc_result.controllers.ResultadoControllerV2;
import com.esports.msvc_result.models.dtos.ResultadoResponseDTO;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class ResultadoResponseDTOModelAssembler implements RepresentationModelAssembler<ResultadoResponseDTO, EntityModel<ResultadoResponseDTO>> {

    @Override
    public EntityModel<ResultadoResponseDTO> toModel(ResultadoResponseDTO resultadoDTO) {
        return EntityModel.of(
                resultadoDTO,
                linkTo(methodOn(ResultadoControllerV2.class).findById(resultadoDTO.getResultadoId())).withSelfRel(),
                linkTo(methodOn(ResultadoControllerV2.class).findAll(null)).withRel("resultados")
        );
    }
}
