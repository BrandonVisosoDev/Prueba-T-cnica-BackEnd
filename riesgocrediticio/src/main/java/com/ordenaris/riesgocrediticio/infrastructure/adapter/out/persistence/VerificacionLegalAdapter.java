package com.ordenaris.riesgocrediticio.infrastructure.adapter.out.persistence;

import com.ordenaris.riesgocrediticio.domain.model.VerificacionLegalEvaluacion;
import com.ordenaris.riesgocrediticio.domain.port.out.VerificacionLegalProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class VerificacionLegalAdapter implements VerificacionLegalProvider {

    private final VerificacionLegalRepository repository;

    @Override
    public VerificacionLegalEvaluacion obtenerVerificacionLegal(String empresaId) {
        return repository.findByEmpresaId(empresaId)
                .map(legal -> new VerificacionLegalEvaluacion(
                        legal.getEmpresaId(),
                        legal.getJuicioMercantilEnCurso(),
                        legal.getNumeroDemandas(),
                        legal.getTieneEmbargos()))
                .orElse(null);
    }
}
