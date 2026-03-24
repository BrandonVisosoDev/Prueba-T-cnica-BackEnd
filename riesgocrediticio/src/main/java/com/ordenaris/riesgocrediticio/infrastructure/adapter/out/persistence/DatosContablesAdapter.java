package com.ordenaris.riesgocrediticio.infrastructure.adapter.out.persistence;

import com.ordenaris.riesgocrediticio.domain.port.out.DatosContablesProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DatosContablesAdapter implements DatosContablesProvider {

    private final DatosContablesRepository repository;

    @Override
    public DatosContables obtenerDatosContables(String empresaId) {
        return repository.findByEmpresaId(empresaId).orElse(null);
    }
}