package com.ordenaris.riesgocrediticio.domain.port.out;

import com.ordenaris.riesgocrediticio.infrastructure.adapter.out.persistence.HistorialPagos;

public interface HistorialPagosProvider {
    /**
     * Obtiene el comportamiento histórico de pagos a Ordenaris y otros acreedores.
     */
    HistorialPagos obtenerHistorialPagos(String empresaId);
}