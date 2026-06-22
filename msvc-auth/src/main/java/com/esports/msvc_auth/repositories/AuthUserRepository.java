package com.esports.msvc_auth.repositories;

import com.esports.msvc_auth.models.AuthUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


@Repository
public interface AuthUserRepository extends JpaRepository<AuthUser, Long> {

    Optional<AuthUser> findByEmail(String email);

    boolean existsByEmail(String email);

    List<AuthUser> findByEstado(String estado);

    @Query("SELECT DISTINCT u FROM AuthUser u JOIN u.roles r WHERE r.nombre = :rol")
    List<AuthUser> findByRolNombre(@Param("rol") String rol);


    @Query("SELECT DISTINCT u FROM AuthUser u JOIN u.roles r WHERE r.nombre = :rol AND u.estado = :estado")
    List<AuthUser> findByRolNombreAndEstado(@Param("rol") String rol, @Param("estado") String estado);
}
