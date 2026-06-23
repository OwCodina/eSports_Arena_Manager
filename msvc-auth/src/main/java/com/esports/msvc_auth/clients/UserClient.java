package com.esports.msvc_auth.clients;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;


@FeignClient(name = "msvc-user", url = "${msvc.user.url}")
public interface UserClient {

    @PostMapping
    void crearPerfil(@RequestBody Map<String, Object> perfil);
}
