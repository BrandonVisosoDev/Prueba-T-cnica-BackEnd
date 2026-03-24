package com.ordenaris.riesgocrediticio.infrastructure.adapter.out.persistence;

import com.ordenaris.riesgocrediticio.domain.port.out.HistorialPagosProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class HistorialPagosAdapter implements HistorialPagosProvider {

    private final HistorialPagosRepository repository;

    @Override
    public HistorialPagos obtenerHistorialPagos(String empresaId) {
        return repository.findByEmpresaId(empresaId).orElse(null);
    }
}