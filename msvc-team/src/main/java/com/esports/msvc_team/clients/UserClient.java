package com.esports.msvc_team.clients;

import com.esports.msvc_team.models.dtos.UsuarioDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "msvc-user", url = "${msvc.user.url}")
public interface UserClient {

    @GetMapping("/api/v1/usuarios/{id}")
    UsuarioDTO findById(@PathVariable Long id);
}
