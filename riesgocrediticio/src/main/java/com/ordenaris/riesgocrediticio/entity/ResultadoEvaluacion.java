package com.ordenaris.riesgocrediticio.entity;

import com.ordenaris.riesgocrediticio.enums.NivelRiesgo;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "resultados_evaluacion")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResultadoEvaluacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "empresa_id")
    private String empresaId; // A quién evaluamos

    @Enumerated(EnumType.STRING) // Guarda el nombre del Enum como texto en la DB
    private NivelRiesgo nivelRiesgo;

    @Column(length = 500) // Le damos más espacio por si el motivo es largo
    private String motivoFinal;

    private LocalDateTime fechaEvaluacion;

    // Relación Uno a Muchos con los detalles de las reglas
    @OneToMany(mappedBy = "resultadoEvaluacion", cascade = CascadeType.ALL)
    private List<DetalleReglaEvaluada> detallesReglas;

}