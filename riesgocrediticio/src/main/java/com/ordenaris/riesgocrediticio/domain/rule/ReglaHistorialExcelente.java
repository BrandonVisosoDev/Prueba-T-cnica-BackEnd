package com.ordenaris.riesgocrediticio.domain.rule;

import com.ordenaris.riesgocrediticio.domain.model.ContextoEvaluacion;
import com.ordenaris.riesgocrediticio.domain.model.ResultadoRegla;
import org.springframework.stereotype.Component;

@Component
public class ReglaHistorialExcelente implements ReglaEvaluacion {

    @Override
    public ResultadoRegla evaluar(ContextoEvaluacion contexto) {
        var historial = contexto.getHistorialPagos();

        if (historial != null &&
                Boolean.TRUE.equals(historial.getPagosEnTiempoUltimos12Meses()) &&
                Boolean.FALSE.equals(historial.getTieneRefinanciamiento())) {

            // Si la regla APLICA (es un cliente estrella)
            // Ojo aquí: nivelRiesgoPropuesto es null, pero mandamos -1 en el modificador
            return new ResultadoRegla(
                    "Historial Excelente",
                    true,
                    null,
                    -1, // Resta 1 al nivel de riesgo final
                    "El cliente tiene un historial de pagos impecable (12 meses sin atrasos ni refinanciamientos). El riesgo disminuye un nivel."
            );
        }

        // Si la regla NO APLICA (tiene atrasos o refinanciamientos)
        return new ResultadoRegla(
                "Historial Excelente",
                false,
                null,
                0, // No modifica nada
                "No cumple con los criterios de historial excelente continuo."
        );
    }
}