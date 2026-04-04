package com.ordenaris.riesgocrediticio.infrastructure.adapter.out.persistence;

import com.ordenaris.riesgocrediticio.domain.model.EmpresaEvaluacion;
import com.ordenaris.riesgocrediticio.domain.port.out.EmpresaProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class EmpresaPersistenceAdapter implements EmpresaProvider {

    private final EmpresaRepository empresaRepository;

    @Override
    public Optional<EmpresaEvaluacion> obtenerEmpresaPorId(String empresaId) {
        return empresaRepository.findById(empresaId)
                .map(empresa -> new EmpresaEvaluacion(
                        empresa.getId(),
                        empresa.getNombre(),
                        empresa.getFechaConstitucion(),
                        empresa.getRfc()));
    }
}
