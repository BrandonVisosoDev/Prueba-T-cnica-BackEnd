package com.ordenaris.riesgocrediticio.application;

import com.ordenaris.riesgocrediticio.domain.model.ContextoEvaluacion;
import com.ordenaris.riesgocrediticio.domain.model.SolicitudEvaluacion;
import com.ordenaris.riesgocrediticio.domain.engine.OrdenarisRiskEngine;
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

    // Inyectamos nuestros Repositorios (la conexión a la BD) y nuestro Motor

    private final DatosContablesProvider datosContablesProvider;
    private final HistorialPagosProvider historialPagosProvider;
    private final VerificacionLegalProvider verificacionLegalProvider;
    private final EmpresaRepository empresaRepo;
    private final ResultadoEvaluacionRepository resultadoRepo;
    private final OrdenarisRiskEngine motorReglas;

    @Override
    public ResultadoEvaluacion evaluar(SolicitudEvaluacion solicitud) {

        // 1. Buscamos a la empresa en la base de datos
        // Si no existe, lanzamos un error que detiene el proceso
        Empresa empresa = empresaRepo.findById(solicitud.getEmpresaId())
                .orElseThrow(() -> new RuntimeException("Error: La empresa con ID " + solicitud.getEmpresaId() + " no está registrada en el sistema."));

        // 2. Buscamos los datos de los proveedores (Si no hay, mandamos null, las reglas ya saben manejar nulls)
        DatosContables contables = datosContablesProvider.obtenerDatosContables(solicitud.getEmpresaId());
        HistorialPagos pagos = historialPagosProvider.obtenerHistorialPagos(solicitud.getEmpresaId());
        VerificacionLegal legal = verificacionLegalProvider.obtenerVerificacionLegal(solicitud.getEmpresaId());

        // 3. Empaquetamos to do en el Contexto
        ContextoEvaluacion contexto = new ContextoEvaluacion(solicitud, empresa, contables, pagos, legal);

        // 4. ¡Llamamos al Cerebro! (El motor evalúa todas las reglas)
        ResultadoEvaluacion resultadoFinal = motorReglas.evaluarRiesgo(contexto);

        // 5. Guardamos el resultado en la base de datos para la auditoría (incluyendo los detalles)
        return resultadoRepo.save(resultadoFinal);
    }
}