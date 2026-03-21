package com.ordenaris.riesgocrediticio.rule;

import com.ordenaris.riesgocrediticio.dto.ContextoEvaluacion;
import com.ordenaris.riesgocrediticio.dto.ResultadoRegla;
import com.ordenaris.riesgocrediticio.enums.NivelRiesgo;
import org.springframework.stereotype.Component;

@Component
public class ReglaDemandaLegalAbierta implements ReglaEvaluacion {

    @Override
    public ResultadoRegla evaluar(ContextoEvaluacion contexto) {
        var legal = contexto.getVerificacionLegal();

        // Usamos Boolean.TRUE.equals para evitar errores si el valor es null
        if (legal != null && Boolean.TRUE.equals(legal.getJuicioMercantilEnCurso())) {

            // Si la regla APLICA (hay demanda)
            return new ResultadoRegla(
                    "Demanda Legal Abierta",
                    true,
                    NivelRiesgo.RECHAZADO,
                    null,
                    "Alerta legal: La empresa tiene un juicio mercantil en curso."
            );
        }

        // Si la regla NO APLICA (está limpia)
        return new ResultadoRegla(
                "Demanda Legal Abierta",
                false,
                null,
                null,
                "No se encontraron juicios mercantiles en curso."
        );
    }
}