package com.ordenaris.riesgocrediticio.rule;

import com.ordenaris.riesgocrediticio.dto.ContextoEvaluacion;
import com.ordenaris.riesgocrediticio.dto.ResultadoRegla;
import com.ordenaris.riesgocrediticio.enums.NivelRiesgo;
import org.springframework.stereotype.Component;

@Component
public class ReglaDeudaActiva implements ReglaEvaluacion {

    private static final int DIAS_LIMITE = 90;

    @Override
    public ResultadoRegla evaluar(ContextoEvaluacion contexto) {
        var historial = contexto.getHistorialPagos();

        if (historial != null && historial.getDiasDeudaVencida() != null
                && historial.getDiasDeudaVencida() > DIAS_LIMITE) {

            return new ResultadoRegla(
                    "Deuda Activa",
                    true,
                    NivelRiesgo.RECHAZADO,
                    null,
                    "La empresa tiene una deuda vencida de " + historial.getDiasDeudaVencida()
                            + " días, superando el límite de " + DIAS_LIMITE + " días."
            );
        }

        return new ResultadoRegla(
                "Deuda Activa",
                false,
                null,
                null,
                "No se detectó deuda vencida mayor a " + DIAS_LIMITE + " días."
        );
    }
}