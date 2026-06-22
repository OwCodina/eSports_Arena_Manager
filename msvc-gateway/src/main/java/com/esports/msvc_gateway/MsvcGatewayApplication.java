package com.esports.msvc_gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * API Gateway — eSports Arena Manager.
 *
 * Punto de entrada único para todos los clientes externos (Postman, frontend).
 * Las rutas se definen en application.yml y reenvían las peticiones a cada
 * microservicio usando el nombre registrado en Eureka (lb://msvc-game, etc.)
 * en lugar de URLs estáticas con puertos.
 *
 * Acceso: http://localhost:8080
 * Swagger agregado: http://localhost:8080/docs/swagger-ui.html
 * Panel Eureka: http://localhost:8761
 */
@SpringBootApplication
public class MsvcGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(MsvcGatewayApplication.class, args);
    }
}
