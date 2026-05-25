package com.esports.msvc_user.repositories;

import com.esports.msvc_user.models.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByNickname(String nickname);

    Optional<Usuario> findByEmail(String email);

    List<Usuario> findByRol(String rol);

    List<Usuario> findByEstado(String estado);

    List<Usuario> findByRolAndEstado(String rol, String estado);
}
