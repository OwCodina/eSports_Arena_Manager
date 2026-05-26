package com.esports.msvc_registration.clients;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Map;


@FeignClient(name = "msvc-sanction", url = "http://localhost:8013")
public interface SanctionClient {

    @GetMapping("/api/v1/sanciones/usuario/{usuarioId}/tiene-activa")
    Map<String, Boolean> tieneUsuarioSancionActiva(@PathVariable Long usuarioId);

    @GetMapping("/api/v1/sanciones/equipo/{equipoId}/tiene-activa")
    Map<String, Boolean> tieneEquipoSancionActiva(@PathVariable Long equipoId);
}
