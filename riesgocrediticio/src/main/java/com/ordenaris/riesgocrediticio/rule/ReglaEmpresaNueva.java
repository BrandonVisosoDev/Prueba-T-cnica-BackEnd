package com.ordenaris.riesgocrediticio.rule;

import com.ordenaris.riesgocrediticio.dto.ContextoEvaluacion;
import com.ordenaris.riesgocrediticio.dto.ResultadoRegla;
import com.ordenaris.riesgocrediticio.enums.NivelRiesgo;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class ReglaEmpresaNueva implements ReglaEvaluacion {

    @Override
    public ResultadoRegla evaluar(ContextoEvaluacion contexto) {
        var empresa = contexto.getEmpresa();
        var solicitud = contexto.getSolicitud();

        if (empresa != null && empresa.getFechaConstitucion() != null &&
                solicitud != null && solicitud.getFechaSolicitud() != null) {

            LocalDate fechaConstitucion = empresa.getFechaConstitucion();
            LocalDate fechaSolicitud = solicitud.getFechaSolicitud();

            // Magia de Java 8+: Calculamos los meses exactos entre dos fechas
            long mesesDeOperacion = ChronoUnit.MONTHS.between(fechaConstitucion, fechaSolicitud);

            if (mesesDeOperacion < 18) {
                // Si la regla APLICA (es muy "bebé")
                return new ResultadoRegla(
                        "Empresa Nueva",
                        true,
                        NivelRiesgo.ALTO,
                        null,
                        "Riesgo por madurez: La empresa tiene solo " + mesesDeOperacion + " meses de operación (menor a 18 meses)."
                );
            }
        }

        // Si la regla NO APLICA (ya es madura)
        return new ResultadoRegla(
                "Empresa Nueva",
                false,
                null,
                null,
                "La empresa tiene un tiempo de operación consolidado (mayor a 18 meses)."
        );
    }
}