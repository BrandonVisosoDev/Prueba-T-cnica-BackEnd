package com.ordenaris.riesgocrediticio.application;

import com.ordenaris.riesgocrediticio.domain.engine.OrdenarisRiskEngine;
import com.ordenaris.riesgocrediticio.domain.model.ContextoEvaluacion;
import com.ordenaris.riesgocrediticio.domain.model.EmpresaNotFoundException;
import com.ordenaris.riesgocrediticio.domain.model.RiesgoEvaluacionException;
import com.ordenaris.riesgocrediticio.domain.model.SolicitudEvaluacion;
import com.ordenaris.riesgocrediticio.domain.port.in.EvaluarRiesgoPort;
import com.ordenaris.riesgocrediticio.domain.port.out.DatosContablesProvider;
import com.ordenaris.riesgocrediticio.domain.port.out.HistorialPagosProvider;
import com.ordenaris.riesgocrediticio.domain.port.out.VerificacionLegalProvider;
import com.ordenaris.riesgocrediticio.infrastructure.adapter.out.persistence.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OrdenarisRiskService implements EvaluarRiesgoPort {

    private final EmpresaRepository empresaRepo;
    private final ResultadoEvaluacionRepository resultadoRepo;
    private final DatosContablesProvider datosContablesProvider;
    private final HistorialPagosProvider historialPagosProvider;
    private final VerificacionLegalProvider verificacionLegalProvider;
    private final OrdenarisRiskEngine motorReglas;

    @Override
    public ResultadoEvaluacion evaluar(SolicitudEvaluacion solicitud) {
        try {
            // 1. Buscamos la empresa — lanza excepción propia si no existe
            Empresa empresa = empresaRepo.findById(solicitud.getEmpresaId())
                    .orElseThrow(() -> new EmpresaNotFoundException(solicitud.getEmpresaId()));

            // 2. Consultamos los proveedores de datos
            DatosContables contables = datosContablesProvider.obtenerDatosContables(solicitud.getEmpresaId());
            HistorialPagos pagos = historialPagosProvider.obtenerHistorialPagos(solicitud.getEmpresaId());
            VerificacionLegal legal = verificacionLegalProvider.obtenerVerificacionLegal(solicitud.getEmpresaId());

            // 3. Empaquetamos el contexto
            ContextoEvaluacion contexto = new ContextoEvaluacion(solicitud, empresa, contables, pagos, legal);

            // 4. Ejecutamos el motor
            ResultadoEvaluacion resultadoFinal = motorReglas.evaluarRiesgo(contexto);

            // 5. Guardamos y devolvemos
            return resultadoRepo.save(resultadoFinal);

        } catch (EmpresaNotFoundException ex) {
            throw ex; // La dejamos pasar, el GlobalExceptionHandler la atrapa
        } catch (Exception ex) {
            throw new RiesgoEvaluacionException(
                    "Error al evaluar el riesgo de la empresa: " + solicitud.getEmpresaId(), ex
            );
        }
    }
}