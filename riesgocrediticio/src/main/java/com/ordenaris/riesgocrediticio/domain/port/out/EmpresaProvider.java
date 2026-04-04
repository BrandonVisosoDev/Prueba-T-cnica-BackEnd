package com.ordenaris.riesgocrediticio.domain.port.out;

import com.ordenaris.riesgocrediticio.domain.model.EmpresaEvaluacion;

import java.util.Optional;

public interface EmpresaProvider {

    Optional<EmpresaEvaluacion> obtenerEmpresaPorId(String empresaId);
}
