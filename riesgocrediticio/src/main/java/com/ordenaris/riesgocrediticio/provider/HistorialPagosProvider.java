package com.ordenaris.riesgocrediticio.provider;

import com.ordenaris.riesgocrediticio.entity.HistorialPagos;

public interface HistorialPagosProvider {
    /**
     * Obtiene el comportamiento histórico de pagos a Ordenaris y otros acreedores.
     */
    HistorialPagos obtenerHistorialPagos(String empresaId);
}