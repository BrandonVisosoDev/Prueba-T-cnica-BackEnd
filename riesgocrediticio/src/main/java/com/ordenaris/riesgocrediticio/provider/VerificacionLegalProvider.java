package com.ordenaris.riesgocrediticio.provider;

import com.ordenaris.riesgocrediticio.entity.VerificacionLegal;

public interface VerificacionLegalProvider {
    /**
     * Obtiene la existencia de procesos judiciales, demandas, embargos, etc.
     */
    VerificacionLegal obtenerVerificacionLegal(String empresaId);
}