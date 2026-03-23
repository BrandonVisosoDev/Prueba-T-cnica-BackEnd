package com.ordenaris.riesgocrediticio.domain.port.in;

import com.ordenaris.riesgocrediticio.domain.model.SolicitudEvaluacion;
import com.ordenaris.riesgocrediticio.infrastructure.adapter.out.persistence.ResultadoEvaluacion;

public interface EvaluarRiesgoPort {
    ResultadoEvaluacion evaluar(SolicitudEvaluacion solicitud);
}