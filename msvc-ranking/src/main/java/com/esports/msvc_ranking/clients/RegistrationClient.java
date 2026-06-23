package com.esports.msvc_ranking.clients;

import com.esports.msvc_ranking.models.dtos.InscripcionDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;



@FeignClient(name = "msvc-registration", url = "${msvc.registration.url}")
public interface RegistrationClient {

    @GetMapping("/api/v1/inscripciones")
    List<InscripcionDTO> findByTorneoId(@RequestParam Long torneoId);
}
