package com.esports.msvc_prize.models;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Table(name = "premios", uniqueConstraints = @UniqueConstraint(name = "uk_torneo_posicion", columnNames = {"torneo_id","posicion"}))
@Getter
@Setter
@ToString
@NoArgsConstructor
public class Premio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "premio_id")
    private Long premioId;

    @Column(name = "torneo_id", nullable = false)
    private Long torneoId;

    @Column(nullable = false)
    private Integer posicion;

    @Column(nullable = false)
    private String descripcion;

    @Column(precision = 12, scale = 2)
    private BigDecimal valor;

    @Column(nullable = false)
    private String estado;

    @Embedded
    private Audit audit = new Audit();

}