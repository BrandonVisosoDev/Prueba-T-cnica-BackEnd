package com.ordenaris.riesgocrediticio.domain.port.out;

import com.ordenaris.riesgocrediticio.infrastructure.adapter.out.persistence.DatosContables;

public interface DatosContablesProvider {
    /**
     * Obtiene ventas promedio, pasivos y activos de una empresa.
     */
    DatosContables obtenerDatosContables(String empresaId);
}