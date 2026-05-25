package com.esports.msvc_sanction.clients;

import com.esports.msvc_sanction.models.dtos.UsuarioDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * Cliente Feign que consume msvc-user.
 * Usado para validar que el usuario exista antes de aplicarle una sanción.
 */
@FeignClient(name = "msvc-user", url = "${msvc.user.url}")
public interface UserClient {

    @GetMapping("/api/v1/usuarios/{id}")
    UsuarioDTO findById(@PathVariable Long id);
}
