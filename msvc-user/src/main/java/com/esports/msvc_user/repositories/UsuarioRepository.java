package com.esports.msvc_user.repositories;

import com.esports.msvc_user.models.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    /** Busca por nickname para validar unicidad */
    Optional<Usuario> findByNickname(String nickname);

    /** Busca por email para validar unicidad */
    Optional<Usuario> findByEmail(String email);

    /** Lista usuarios por rol (JUGADOR, ORGANIZADOR, ADMINISTRADOR) */
    List<Usuario> findByRol(String rol);

    /** Lista usuarios por estado (ACTIVO, INACTIVO, SANCIONADO) */
    List<Usuario> findByEstado(String estado);

    /** Lista usuarios por rol y estado combinados */
    List<Usuario> findByRolAndEstado(String rol, String estado);
}
