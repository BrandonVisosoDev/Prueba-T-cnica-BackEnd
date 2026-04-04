package com.ordenaris.riesgocrediticio.domain.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HistorialPagosEvaluacion {

    private String empresaId;
    private Integer diasDeudaVencida;
    private Boolean pagosEnTiempoUltimos12Meses;
    private Boolean tieneRefinanciamiento;
}
