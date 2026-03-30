package com.ordenaris.riesgocrediticio.domain.port.in;

import com.ordenaris.riesgocrediticio.domain.model.ResultadoRiesgo;
import com.ordenaris.riesgocrediticio.domain.model.SolicitudEvaluacion;

/**
 * Puerto de entrada (driving port) del dominio.
 * Define el contrato para evaluar el riesgo de una empresa.
 * Retorna un objeto de dominio puro, sin dependencias de infraestructura.
 */
public interface EvaluarRiesgoPort {
    ResultadoRiesgo evaluar(SolicitudEvaluacion solicitud);
}

