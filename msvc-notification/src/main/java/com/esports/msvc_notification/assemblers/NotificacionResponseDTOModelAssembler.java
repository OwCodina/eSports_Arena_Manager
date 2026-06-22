package com.esports.msvc_notification.assemblers;

import com.esports.msvc_notification.controllers.NotificacionControllerV2;
import com.esports.msvc_notification.models.dtos.NotificacionResponseDTO;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class NotificacionResponseDTOModelAssembler implements RepresentationModelAssembler<NotificacionResponseDTO, EntityModel<NotificacionResponseDTO>> {

    @Override
    public EntityModel<NotificacionResponseDTO> toModel(NotificacionResponseDTO notificacionDTO) {
        return EntityModel.of(
                notificacionDTO,
                linkTo(methodOn(NotificacionControllerV2.class).findById(notificacionDTO.getNotificacionId())).withSelfRel(),
                linkTo(methodOn(NotificacionControllerV2.class).findAll(null, null)).withRel("notificaciones")
        );
    }
}
