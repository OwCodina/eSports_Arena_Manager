package com.esports.msvc_auth.config;

import com.esports.msvc_auth.models.AuthUser;
import com.esports.msvc_auth.models.Rol;
import com.esports.msvc_auth.repositories.AuthUserRepository;
import com.esports.msvc_auth.repositories.RolRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Set;


@Component
public class DataLoader implements CommandLineRunner {

    private final AuthUserRepository authUserRepository;
    private final RolRepository rolRepository;
    private final PasswordEncoder passwordEncoder;

    public DataLoader(AuthUserRepository authUserRepository, RolRepository rolRepository,
                      PasswordEncoder passwordEncoder) {
        this.authUserRepository = authUserRepository;
        this.rolRepository = rolRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        Rol admin = obtenerOCrearRol("ROLE_ADMINISTRADOR");
        Rol organizador = obtenerOCrearRol("ROLE_ORGANIZADOR");
        Rol jugador = obtenerOCrearRol("ROLE_JUGADOR");

        crearSiNoExiste("admin@esports.cl", "admin123", Set.of(admin, organizador));
        crearSiNoExiste("organizador@esports.cl", "org123", Set.of(organizador));
        crearSiNoExiste("jugador@esports.cl", "jug123", Set.of(jugador));
    }

    private Rol obtenerOCrearRol(String nombre) {
        return rolRepository.findByNombre(nombre).orElseGet(() -> rolRepository.save(new Rol(nombre)));
    }

    private void crearSiNoExiste(String email, String passwordPlano, Set<Rol> roles) {
        if (authUserRepository.existsByEmail(email)) {
            return;
        }
        AuthUser cuenta = new AuthUser();
        cuenta.setEmail(email);
        cuenta.setPasswordHash(passwordEncoder.encode(passwordPlano));
        cuenta.setRoles(roles);
        cuenta.setEstado("ACTIVO");
        authUserRepository.save(cuenta);
    }
}
