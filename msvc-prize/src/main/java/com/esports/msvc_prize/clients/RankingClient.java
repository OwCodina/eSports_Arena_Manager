package com.esports.msvc_prize.clients;

import com.esports.msvc_prize.models.dtos.RankingDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;


@FeignClient(name = "msvc-ranking", url = "http://localhost:8018")
public interface RankingClient {

    @GetMapping("/api/v1/rankings/torneo/{torneoId}")
    List<RankingDTO> findByTorneoId(@PathVariable Long torneoId);
}
