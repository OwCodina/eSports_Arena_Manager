package com.esports.msvc_result.clients;

import com.esports.msvc_result.models.dtos.PartidaDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;


@FeignClient(name = "msvc-match", url = "${msvc.match.url}")
public interface MatchClient {

    @GetMapping("/api/v1/partidas/{id}")
    PartidaDTO findById(@PathVariable Long id);
}
