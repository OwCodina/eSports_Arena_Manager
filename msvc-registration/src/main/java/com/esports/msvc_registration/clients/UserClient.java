package com.esports.msvc_registration.clients;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Map;


@FeignClient(name = "msvc-user", url = "${msvc.user.url}")
public interface UserClient {

    @GetMapping("/api/v1/usuarios/{id}/puede-competir")
    Map<String, Boolean> puedeCompetitr(@PathVariable Long id);
}
