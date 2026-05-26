package com.esports.msvc_ranking.clients;

import com.esports.msvc_ranking.models.dtos.ResultadoDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;


@FeignClient(name = "msvc-result", url = "http://localhost:8017")
public interface ResultClient {

    @GetMapping("/api/v1/resultados")
    List<ResultadoDTO> findAll(@RequestParam(required = false) String estadoValidacion);
}
