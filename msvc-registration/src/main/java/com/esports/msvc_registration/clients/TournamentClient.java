package com.esports.msvc_registration.clients;

import com.esports.msvc_registration.models.dtos.TorneoDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Map;


@FeignClient(name = "msvc-tournament", url = "${msvc.tournament.url}")
public interface TournamentClient {

    @GetMapping("/api/v1/torneos/{id}")
    TorneoDTO findById(@PathVariable Long id);

    @GetMapping("/api/v1/torneos/{id}/esta-abierto")
    Map<String, Boolean> estaAbierto(@PathVariable Long id);
}
