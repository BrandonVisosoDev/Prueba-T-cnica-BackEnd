package com.ordenaris.riesgocrediticio.domain.port.out;

import com.ordenaris.riesgocrediticio.domain.model.VerificacionLegalEvaluacion;

public interface VerificacionLegalProvider {

    VerificacionLegalEvaluacion obtenerVerificacionLegal(String empresaId);
}
