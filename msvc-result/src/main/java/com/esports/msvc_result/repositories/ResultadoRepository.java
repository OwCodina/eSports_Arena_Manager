package com.esports.msvc_result.repositories;

import com.esports.msvc_result.models.Resultado;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ResultadoRepository extends JpaRepository<Resultado, Long> {


    Optional<Resultado> findByPartidaId(Long partidaId);

    List<Resultado> findByEstadoValidacion(String estadoValidacion);
    List<Resultado> findByPartidaIdIn(List<Long> partidaIds);
}
