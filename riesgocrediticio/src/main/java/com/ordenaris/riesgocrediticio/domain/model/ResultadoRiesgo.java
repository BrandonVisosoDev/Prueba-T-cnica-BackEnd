package com.ordenaris.riesgocrediticio.domain.model;

import com.ordenaris.riesgocrediticio.domain.model.enums.NivelRiesgo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Objeto de dominio puro que representa el resultado de una evaluación de riesgo.
 * No tiene anotaciones JPA ni dependencias de infraestructura.
 * Es lo que el dominio conoce y maneja internamente.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResultadoRiesgo {

    private String empresaId;
    private NivelRiesgo nivelRiesgo;
    private String motivoFinal;
    private LocalDateTime fechaEvaluacion;
    private List<ResultadoRegla> detallesReglas;
}
