package com.ordenaris.riesgocrediticio.provider;

import com.ordenaris.riesgocrediticio.entity.DatosContables;

public interface DatosContablesProvider {
    /**
     * Obtiene ventas promedio, pasivos y activos de una empresa.
     */
    DatosContables obtenerDatosContables(String empresaId);
}