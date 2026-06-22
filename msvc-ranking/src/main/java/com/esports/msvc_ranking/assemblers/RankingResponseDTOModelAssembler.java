package com.esports.msvc_ranking.assemblers;

import com.esports.msvc_ranking.controllers.RankingControllerV2;
import com.esports.msvc_ranking.models.dtos.RankingResponseDTO;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class RankingResponseDTOModelAssembler implements RepresentationModelAssembler<RankingResponseDTO, EntityModel<RankingResponseDTO>> {

    @Override
    public EntityModel<RankingResponseDTO> toModel(RankingResponseDTO rankingDTO) {
        return EntityModel.of(
                rankingDTO,
                linkTo(methodOn(RankingControllerV2.class).findById(rankingDTO.getRankingId())).withSelfRel(),
                linkTo(methodOn(RankingControllerV2.class).findByTorneo(rankingDTO.getTorneoId())).withRel("rankings")
        );
    }
}
