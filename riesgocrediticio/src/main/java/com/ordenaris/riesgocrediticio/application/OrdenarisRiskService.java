package com.ordenaris.riesgocrediticio.application;

import com.ordenaris.riesgocrediticio.domain.engine.OrdenarisRiskEngine;
import com.ordenaris.riesgocrediticio.domain.model.ContextoEvaluacion;
import com.ordenaris.riesgocrediticio.domain.model.DatosContablesEvaluacion;
import com.ordenaris.riesgocrediticio.domain.model.EmpresaEvaluacion;
import com.ordenaris.riesgocrediticio.domain.model.EmpresaNotFoundException;
import com.ordenaris.riesgocrediticio.domain.model.HistorialPagosEvaluacion;
import com.ordenaris.riesgocrediticio.domain.model.ResultadoRiesgo;
import com.ordenaris.riesgocrediticio.domain.model.RiesgoEvaluacionException;
import com.ordenaris.riesgocrediticio.domain.model.SolicitudEvaluacion;
import com.ordenaris.riesgocrediticio.domain.model.VerificacionLegalEvaluacion;
import com.ordenaris.riesgocrediticio.domain.port.in.EvaluarRiesgoPort;
import com.ordenaris.riesgocrediticio.domain.port.out.DatosContablesProvider;
import com.ordenaris.riesgocrediticio.domain.port.out.EmpresaProvider;
import com.ordenaris.riesgocrediticio.domain.port.out.HistorialPagosProvider;
import com.ordenaris.riesgocrediticio.domain.port.out.ResultadoEvaluacionProvider;
import com.ordenaris.riesgocrediticio.domain.port.out.VerificacionLegalProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrdenarisRiskService implements EvaluarRiesgoPort {

    private final EmpresaProvider empresaProvider;
    private final ResultadoEvaluacionProvider resultadoEvaluacionProvider;
    private final DatosContablesProvider datosContablesProvider;
    private final HistorialPagosProvider historialPagosProvider;
    private final VerificacionLegalProvider verificacionLegalProvider;
    private final OrdenarisRiskEngine motorReglas;

    @Override
    public ResultadoRiesgo evaluar(SolicitudEvaluacion solicitud) {
        log.info("Inicio de evaluacion de riesgo. empresaId={}, producto={}, monto={}",
                solicitud.getEmpresaId(), solicitud.getProductoFinanciero(), solicitud.getMontoSolicitado());
        try {
            EmpresaEvaluacion empresa = empresaProvider.obtenerEmpresaPorId(solicitud.getEmpresaId())
                    .orElseThrow(() -> new EmpresaNotFoundException(solicitud.getEmpresaId()));

            DatosContablesEvaluacion contables = datosContablesProvider.obtenerDatosContables(solicitud.getEmpresaId());
            HistorialPagosEvaluacion pagos = historialPagosProvider.obtenerHistorialPagos(solicitud.getEmpresaId());
            VerificacionLegalEvaluacion legal = verificacionLegalProvider.obtenerVerificacionLegal(solicitud.getEmpresaId());

            ContextoEvaluacion contexto = new ContextoEvaluacion(solicitud, empresa, contables, pagos, legal);
            ResultadoRiesgo resultado = motorReglas.evaluarRiesgo(contexto);
            ResultadoRiesgo persistido = resultadoEvaluacionProvider.guardar(resultado);

            log.info("Evaluacion finalizada. empresaId={}, nivel={}",
                    solicitud.getEmpresaId(), persistido.getNivelRiesgo());
            return persistido;
        } catch (EmpresaNotFoundException ex) {
            log.warn("Empresa no encontrada. empresaId={}", solicitud.getEmpresaId(), ex);
            throw ex;
        } catch (Exception ex) {
            log.error("Error inesperado durante la evaluacion. empresaId={}", solicitud.getEmpresaId(), ex);
            throw new RiesgoEvaluacionException(
                    "Error al evaluar el riesgo de la empresa: " + solicitud.getEmpresaId(), ex);
        }
    }
}
