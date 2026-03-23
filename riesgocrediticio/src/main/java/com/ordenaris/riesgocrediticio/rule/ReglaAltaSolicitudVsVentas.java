package com.ordenaris.riesgocrediticio.rule;

import com.ordenaris.riesgocrediticio.dto.ContextoEvaluacion;
import com.ordenaris.riesgocrediticio.dto.ResultadoRegla;
import com.ordenaris.riesgocrediticio.enums.NivelRiesgo;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
@Component
public class ReglaAltaSolicitudVsVentas implements ReglaEvaluacion {

    @Override
    public ResultadoRegla evaluar(ContextoEvaluacion contexto) {
        var solicitud = contexto.getSolicitud();
        var datosContables = contexto.getDatosContables();

        // Verificamos que tengamos la información necesaria para no tener errores
        if (solicitud != null && solicitud.getMontoSolicitado() != null &&
                datosContables != null && datosContables.getVentasPromedioMensuales() != null) {

            BigDecimal montoSolicitado = solicitud.getMontoSolicitado();
            BigDecimal ventasMensuales = datosContables.getVentasPromedioMensuales();

            // Multiplicamos las ventas x 8 usando BigDecimal
            BigDecimal limiteSano = ventasMensuales.multiply(new BigDecimal("8"));

            // compareTo devuelve 1 si el monto solicitado es mayor al límite sano
            if (montoSolicitado.compareTo(limiteSano) > 0) {
                return new ResultadoRegla(
                        "Alta Solicitud vs Ventas",
                        true,
                        NivelRiesgo.ALTO,
                        null,
                        "El monto solicitado de $" + montoSolicitado + " supera 8 veces las ventas promedio ($" + ventasMensuales + ")."
                );
            }
        }

        // Si la regla NO APLICA (pidió algo razonable)
        return new ResultadoRegla(
                "Alta Solicitud vs Ventas",
                false,
                null,
                null,
                "La proporción del monto solicitado respecto a las ventas es sana."
        );
    }
}