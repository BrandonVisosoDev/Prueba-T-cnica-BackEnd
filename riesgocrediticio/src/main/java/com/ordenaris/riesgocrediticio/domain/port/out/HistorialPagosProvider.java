package com.ordenaris.riesgocrediticio.domain.port.out;

import com.ordenaris.riesgocrediticio.domain.model.HistorialPagosEvaluacion;

public interface HistorialPagosProvider {

    HistorialPagosEvaluacion obtenerHistorialPagos(String empresaId);
}
