package com.ordenaris.riesgocrediticio.domain.port.out;

import com.ordenaris.riesgocrediticio.domain.model.ResultadoRiesgo;

public interface ResultadoEvaluacionProvider {

    ResultadoRiesgo guardar(ResultadoRiesgo resultadoRiesgo);
}
