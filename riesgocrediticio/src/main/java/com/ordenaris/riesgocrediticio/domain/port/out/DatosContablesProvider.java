package com.ordenaris.riesgocrediticio.domain.port.out;

import com.ordenaris.riesgocrediticio.domain.model.DatosContablesEvaluacion;

public interface DatosContablesProvider {

    DatosContablesEvaluacion obtenerDatosContables(String empresaId);
}
