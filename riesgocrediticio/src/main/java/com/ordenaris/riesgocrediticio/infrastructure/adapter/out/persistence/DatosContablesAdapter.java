package com.ordenaris.riesgocrediticio.infrastructure.adapter.out.persistence;

import com.ordenaris.riesgocrediticio.domain.model.DatosContablesEvaluacion;
import com.ordenaris.riesgocrediticio.domain.port.out.DatosContablesProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DatosContablesAdapter implements DatosContablesProvider {

    private final DatosContablesRepository repository;

    @Override
    public DatosContablesEvaluacion obtenerDatosContables(String empresaId) {
        return repository.findByEmpresaId(empresaId)
                .map(datos -> new DatosContablesEvaluacion(
                        datos.getEmpresaId(),
                        datos.getVentasPromedioMensuales(),
                        datos.getPasivos(),
                        datos.getActivos()))
                .orElse(null);
    }
}
