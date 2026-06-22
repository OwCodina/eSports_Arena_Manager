package com.esports.msvc_user.config;

import com.esports.msvc_user.models.Usuario;
import com.esports.msvc_user.repositories.UsuarioRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDate;


@Component
public class DataLoader implements CommandLineRunner {

    private final UsuarioRepository usuarioRepository;

    public DataLoader(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    public void run(String... args) {
        crearSiNoExiste("Admin Demo", "admin", "admin@esports.cl", "ADMINISTRADOR");
        crearSiNoExiste("Organizador Demo", "organizador", "organizador@esports.cl", "ORGANIZADOR");
        crearSiNoExiste("Jugador Demo", "jugador1", "jugador@esports.cl", "JUGADOR");
    }

    private void crearSiNoExiste(String nombre, String nickname, String email, String rol) {
        if (usuarioRepository.findByNickname(nickname).isPresent() || usuarioRepository.findByEmail(email).isPresent()) {
            return;
        }
        Usuario u = new Usuario();
        u.setNombre(nombre);
        u.setNickname(nickname);
        u.setEmail(email);
        u.setRol(rol);
        u.setEstado("ACTIVO");
        u.setFechaRegistro(LocalDate.now());
        usuarioRepository.save(u);
    }
}
