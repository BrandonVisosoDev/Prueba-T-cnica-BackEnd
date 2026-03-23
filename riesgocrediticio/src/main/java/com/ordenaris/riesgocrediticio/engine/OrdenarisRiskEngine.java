import com.ordenaris.riesgocrediticio.dto.ContextoEvaluacion;
import com.ordenaris.riesgocrediticio.dto.ResultadoRegla;
import com.ordenaris.riesgocrediticio.entity.ResultadoEvaluacion;
import com.ordenaris.riesgocrediticio.enums.NivelRiesgo;
import com.ordenaris.riesgocrediticio.rule.ReglaEvaluacion;

import java.util.ArrayList;
import java.util.List;

public ResultadoEvaluacion evaluarRiesgo(ContextoEvaluacion contexto) {
    List<ResultadoRegla> resultadosParciales = new ArrayList<>();
    int contadorAltos = 0;
    int modificadorTotal = 0;
    boolean rechazadoInmediato = false;
    boolean nivelMinimoMedio = false;  // ← NUEVO: para ReglaEmpresaNueva
    String motivoPrincipal = "Evaluación completada con éxito. Cliente apto.";

    for (ReglaEvaluacion regla : reglas) {
        ResultadoRegla res = regla.evaluar(contexto);
        resultadosParciales.add(res);

        if (res.isAplico()) {
            NivelRiesgo propuesto = res.getNivelRiesgoPropuesto();

            if (propuesto == NivelRiesgo.RECHAZADO) {
                rechazadoInmediato = true;
                motivoPrincipal = res.getDetalle();
            } else if (propuesto == NivelRiesgo.ALTO) {
                contadorAltos++;
            } else if (propuesto == NivelRiesgo.MEDIO) {
                nivelMinimoMedio = true; // ← ReglaEmpresaNueva activa esto
            }

            if (res.getModificadorPuntos() != null) {
                modificadorTotal += res.getModificadorPuntos();
            }
        }
    }

    // Determinamos el nivel base
    NivelRiesgo nivelFinal;

    if (rechazadoInmediato) {
        nivelFinal = NivelRiesgo.RECHAZADO;
    } else if (contadorAltos >= 2) {
        nivelFinal = NivelRiesgo.RECHAZADO;
        motivoPrincipal = "Rechazado por acumulación de " + contadorAltos + " alertas ALTO.";
    } else if (contadorAltos == 1) {
        nivelFinal = NivelRiesgo.ALTO;
        motivoPrincipal = "Riesgo ALTO por alerta importante detectada.";
    } else {
        nivelFinal = NivelRiesgo.BAJO;
    }

    // Aplicamos modificadores (+1 Producto Estricto, -1 Historial Excelente)
    if (nivelFinal != NivelRiesgo.RECHAZADO) {
        nivelFinal = aplicarModificador(nivelFinal, modificadorTotal, motivoPrincipal);
        // Actualizamos el motivo si cambió
        if (modificadorTotal > 0) {
            motivoPrincipal = "Nivel aumentado por producto ARRENDAMIENTO_FINANCIERO.";
        } else if (modificadorTotal < 0 && nivelFinal != NivelRiesgo.BAJO) {
            motivoPrincipal = "Riesgo reducido un nivel gracias a historial de pagos excelente.";
        }
    }

    // Garantizamos el mínimo MEDIO para empresa nueva (Regla 3)
    if (nivelFinal == NivelRiesgo.BAJO && nivelMinimoMedio) {
        nivelFinal = NivelRiesgo.MEDIO;
        motivoPrincipal = "Empresa con menos de 18 meses: nivel mínimo es MEDIO.";
    }

    return armarResultado(contexto, resultadosParciales, nivelFinal, motivoPrincipal);
}

// Método auxiliar para subir/bajar nivel
private NivelRiesgo aplicarModificador(NivelRiesgo nivel, int modificador, String motivo) {
    List<NivelRiesgo> escala = List.of(NivelRiesgo.BAJO, NivelRiesgo.MEDIO, NivelRiesgo.ALTO);
    int indice = escala.indexOf(nivel);
    int nuevoIndice = Math.max(0, Math.min(escala.size() - 1, indice + modificador));
    return escala.get(nuevoIndice);
}