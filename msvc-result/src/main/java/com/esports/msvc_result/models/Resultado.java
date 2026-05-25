package com.esports.msvc_result.models;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "resultados", uniqueConstraints = @UniqueConstraint(name = "uk_partida_resultado", columnNames = "partida_id"))
@Getter
@Setter
@ToString
@NoArgsConstructor

public class Resultado {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "resultado_id")
    private Long resultadoId;

    @Column(name = "partida_id", nullable = false, unique = true)
    private Long partidaId;

    @Column(name = "ganador_id", nullable = false)
    private Long ganadorId;

    @Column(name = "puntaje_a", nullable = false)
    private Integer puntajeA;

    @Column(name = "puntaje_b", nullable = false)
    private Integer puntajeB;

    @Column(name = "estado_validacion", nullable = false)
    private String estadoValidacion;

    @Column(name = "motivo_anulacion")
    private String motivoAnulacion;

    @Column(name = "fecha_registro", nullable = false)
    private LocalDateTime fechaRegistro;

    @Embedded
    private Audit audit = new Audit();
}