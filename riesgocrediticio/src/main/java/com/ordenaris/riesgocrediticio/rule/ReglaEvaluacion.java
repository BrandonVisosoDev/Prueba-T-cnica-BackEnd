package com.ordenaris.riesgocrediticio.rule;

import com.ordenaris.riesgocrediticio.dto.ContextoEvaluacion;
import com.ordenaris.riesgocrediticio.dto.ResultadoRegla;

public interface ReglaEvaluacion {
    /**
     * Evalúa una regla de negocio específica basándose en el contexto de la empresa.
     */
    ResultadoRegla evaluar(ContextoEvaluacion contexto);
}