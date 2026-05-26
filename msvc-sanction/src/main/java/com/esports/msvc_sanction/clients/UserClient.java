package com.esports.msvc_sanction.clients;

import com.esports.msvc_sanction.models.dtos.UsuarioDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "msvc-user", url = "http://localhost:8011")
public interface UserClient {

    @GetMapping("/api/v1/usuarios/{id}")
    UsuarioDTO findById(@PathVariable Long id);
}
