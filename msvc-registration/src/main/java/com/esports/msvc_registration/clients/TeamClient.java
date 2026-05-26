package com.esports.msvc_registration.clients;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Map;


@FeignClient(name = "msvc-team", url = "http://localhost:8014")
public interface TeamClient {

    @GetMapping("/api/v1/equipos/{id}/esta-activo")
    Map<String, Boolean> estaActivo(@PathVariable Long id);
}
