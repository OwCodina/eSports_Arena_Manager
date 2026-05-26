package com.esports.msvc_tournament.clients;

import com.esports.msvc_tournament.models.dtos.JuegoDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;


@FeignClient(name = "msvc-game", url = "http://localhost:8010")
public interface GameClient {

    @GetMapping("/api/v1/juegos/{id}")
    JuegoDTO findById(@PathVariable Long id);
}
