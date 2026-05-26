package com.esports.msvc_match.clients;

import com.esports.msvc_match.models.dtos.TorneoDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;


@FeignClient(name = "msvc-tournament", url = "http://localhost:8012")
public interface TournamentClient {

    @GetMapping("/api/v1/torneos/{id}")
    TorneoDTO findById(@PathVariable Long id);
}
