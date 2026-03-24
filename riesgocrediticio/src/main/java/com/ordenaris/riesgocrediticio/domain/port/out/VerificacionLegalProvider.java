package com.ordenaris.riesgocrediticio.domain.port.out;

import com.ordenaris.riesgocrediticio.infrastructure.adapter.out.persistence.VerificacionLegal;

public interface VerificacionLegalProvider {
    /**
     * Obtiene la existencia de procesos judiciales, demandas, embargos, etc.
     */
    VerificacionLegal obtenerVerificacionLegal(String empresaId);
}