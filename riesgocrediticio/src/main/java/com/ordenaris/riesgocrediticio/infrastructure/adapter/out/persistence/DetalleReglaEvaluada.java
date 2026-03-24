package com.ordenaris.riesgocrediticio.infrastructure.adapter.out.persistence;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "detalles_reglas_evaluadas")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DetalleReglaEvaluada {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Relación de vuelta al Resultado General
    @ManyToOne
    @JoinColumn(name = "resultado_evaluacion_id")
    @JsonIgnore
    private ResultadoEvaluacion resultadoEvaluacion;

    private String nombreRegla; // Ej: "Deuda Activa", "Empresa Nueva"

    private String resultado; // Ej: "Aplicó (Rechazo)", "No Aplicó", "Aumentó 1 nivel"

}