package com.ordenaris.riesgocrediticio.infrastructure.adapter.out.persistence;

import com.ordenaris.riesgocrediticio.domain.model.HistorialPagosEvaluacion;
import com.ordenaris.riesgocrediticio.domain.port.out.HistorialPagosProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class HistorialPagosAdapter implements HistorialPagosProvider {

    private final HistorialPagosRepository repository;

    @Override
    public HistorialPagosEvaluacion obtenerHistorialPagos(String empresaId) {
        return repository.findByEmpresaId(empresaId)
                .map(historial -> new HistorialPagosEvaluacion(
                        historial.getEmpresaId(),
                        historial.getDiasDeudaVencida(),
                        historial.getPagosEnTiempoUltimos12Meses(),
                        historial.getTieneRefinanciamiento()))
                .orElse(null);
    }
}
