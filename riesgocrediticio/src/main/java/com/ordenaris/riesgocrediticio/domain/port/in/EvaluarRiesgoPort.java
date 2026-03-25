package com.ordenaris.riesgocrediticio.domain.port.in;

import com.ordenaris.riesgocrediticio.domain.model.EvaluacionResponseDTO;
import com.ordenaris.riesgocrediticio.domain.model.SolicitudEvaluacion;

public interface EvaluarRiesgoPort {
    EvaluacionResponseDTO evaluar(SolicitudEvaluacion solicitud);  // ? cambia el tipo de retorno
}