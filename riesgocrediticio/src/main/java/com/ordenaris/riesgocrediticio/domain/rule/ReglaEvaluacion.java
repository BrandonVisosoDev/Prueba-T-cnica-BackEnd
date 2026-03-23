package com.ordenaris.riesgocrediticio.domain.rule;

import com.ordenaris.riesgocrediticio.domain.model.ContextoEvaluacion;
import com.ordenaris.riesgocrediticio.domain.model.ResultadoRegla;

public interface ReglaEvaluacion {
    /**
     * Evalúa una regla de negocio específica basándose en el contexto de la empresa.
     */
    ResultadoRegla evaluar(ContextoEvaluacion contexto);
}