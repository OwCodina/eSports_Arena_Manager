package com.esports.msvc_ranking.clients;

import com.esports.msvc_ranking.models.dtos.TorneoDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;


@FeignClient(name = "msvc-tournament", url = "${msvc.tournament.url}")
public interface TournamentClient {

    @GetMapping("/api/v1/torneos/{id}")
    TorneoDTO findById(@PathVariable Long id);
}
