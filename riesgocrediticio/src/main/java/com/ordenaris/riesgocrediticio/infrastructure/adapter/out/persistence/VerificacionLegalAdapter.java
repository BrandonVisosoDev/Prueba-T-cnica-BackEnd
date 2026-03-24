package com.ordenaris.riesgocrediticio.infrastructure.adapter.out.persistence;

import com.ordenaris.riesgocrediticio.domain.port.out.VerificacionLegalProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class VerificacionLegalAdapter implements VerificacionLegalProvider {

    private final VerificacionLegalRepository repository;

    @Override
    public VerificacionLegal obtenerVerificacionLegal(String empresaId) {
        return repository.findByEmpresaId(empresaId).orElse(null);
    }
}