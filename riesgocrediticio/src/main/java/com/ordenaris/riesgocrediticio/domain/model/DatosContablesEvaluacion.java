package com.ordenaris.riesgocrediticio.domain.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DatosContablesEvaluacion {

    private String empresaId;
    private BigDecimal ventasPromedioMensuales;
    private BigDecimal pasivos;
    private BigDecimal activos;
}
