package com.ordenaris.riesgocrediticio.rule;

import com.ordenaris.riesgocrediticio.dto.ContextoEvaluacion;
import com.ordenaris.riesgocrediticio.dto.ResultadoRegla;
import com.ordenaris.riesgocrediticio.enums.ProductoFinanciero;
import org.springframework.stereotype.Component;

@Component
public class ReglaProductoEstricto implements ReglaEvaluacion {

    @Override
    public ResultadoRegla evaluar(ContextoEvaluacion contexto) {
        var solicitud = contexto.getSolicitud();

        if (solicitud != null
                && ProductoFinanciero.ARRENDAMIENTO_FINANCIERO.equals(solicitud.getProductoFinanciero())) {

            return new ResultadoRegla(
                    "Producto Estricto",
                    true,
                    null,
                    +1, // ← Sube un nivel de riesgo
                    "El producto solicitado es ARRENDAMIENTO_FINANCIERO, el cual aplica un nivel adicional de riesgo."
            );
        }

        return new ResultadoRegla(
                "Producto Estricto",
                false,
                null,
                0,
                "El producto solicitado no aplica restricción adicional de riesgo."
        );
    }
}