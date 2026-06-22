package com.esports.msvc_auth.repositories;

import com.esports.msvc_auth.models.Rol;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repositorio de roles. Hereda los metodos CRUD de JpaRepository.
 */
@Repository
public interface RolRepository extends JpaRepository<Rol, Long> {

    /** Busca un rol por su nombre (ej: "ROLE_ADMINISTRADOR"). Lo usan register y el seed. */
    Optional<Rol> findByNombre(String nombre);

}
