package com.esports.msvc_eureka;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

/**
 * Servidor de descubrimiento de microservicios Eureka.
 *
 * Todos los microservicios del sistema se registran aquí al arrancar.
 * El Gateway los busca por nombre (lb://msvc-game, lb://msvc-user, etc.)
 * en lugar de usar URLs estáticas.
 *
 * Panel de administración: http://localhost:8761
 */
@EnableEurekaServer
@SpringBootApplication
public class MsvcEurekaApplication {

    public static void main(String[] args) {
        SpringApplication.run(MsvcEurekaApplication.class, args);
    }
}
