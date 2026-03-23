package com.ordenaris.riesgocrediticio.domain.engine;

import com.ordenaris.riesgocrediticio.domain.model.ContextoEvaluacion;
import com.ordenaris.riesgocrediticio.domain.model.ResultadoRegla;
import com.ordenaris.riesgocrediticio.infrastructure.adapter.out.persistence.DetalleReglaEvaluada;
import com.ordenaris.riesgocrediticio.infrastructure.adapter.out.persistence.ResultadoEvaluacion;
import com.ordenaris.riesgocrediticio.domain.model.enums.NivelRiesgo;
import com.ordenaris.riesgocrediticio.domain.rule.ReglaEvaluacion;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Component
public class OrdenarisRiskEngine {

    // Spring inyecta automáticamente todas las clases @Component que implementen ReglaEvaluacion
    private final List<ReglaEvaluacion> reglas;

    public OrdenarisRiskEngine(List<ReglaEvaluacion> reglas) {
        this.reglas = reglas;
    }

    public ResultadoEvaluacion evaluarRiesgo(ContextoEvaluacion contexto) {
        List<ResultadoRegla> resultadosParciales = new ArrayList<>();
        int contadorAltos = 0;
        int modificadorTotal = 0;
        boolean rechazadoInmediato = false;
        boolean nivelMinimoMedio = false;
        String motivoPrincipal = "Evaluación completada con éxito. Cliente apto.";

        // 1. Pasamos la empresa por todas las reglas
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
                    nivelMinimoMedio = true; // ReglaEmpresaNueva activa esto
                }

                if (res.getModificadorPuntos() != null) {
                    modificadorTotal += res.getModificadorPuntos();
                }
            }
        }

        // 2. Determinamos el nivel base
        NivelRiesgo nivelFinal;

        if (rechazadoInmediato) {
            nivelFinal = NivelRiesgo.RECHAZADO;
        } else if (contadorAltos >= 2) {
            nivelFinal = NivelRiesgo.RECHAZADO;
            motivoPrincipal = "Rechazado por acumulación de " + contadorAltos + " alertas de nivel ALTO.";
        } else if (contadorAltos == 1) {
            nivelFinal = NivelRiesgo.ALTO;
            motivoPrincipal = "Riesgo ALTO por alerta importante detectada.";
        } else {
            nivelFinal = NivelRiesgo.BAJO;
        }

        // 3. Aplicamos modificadores (+1 Producto Estricto / -1 Historial Excelente)
        if (nivelFinal != NivelRiesgo.RECHAZADO) {
            nivelFinal = aplicarModificador(nivelFinal, modificadorTotal);
            if (modificadorTotal > 0) {
                motivoPrincipal = "Nivel aumentado por producto ARRENDAMIENTO_FINANCIERO.";
            } else if (modificadorTotal < 0) {
                motivoPrincipal = "Riesgo reducido un nivel gracias a historial de pagos excelente.";
            }
        }

        // 4. Garantizamos el mínimo MEDIO para empresa nueva (Regla 3)
        if (nivelFinal == NivelRiesgo.BAJO && nivelMinimoMedio) {
            nivelFinal = NivelRiesgo.MEDIO;
            motivoPrincipal = "Empresa con menos de 18 meses: nivel mínimo es MEDIO.";
        }

        // 5. Construimos y devolvemos el resultado final
        return armarResultado(contexto, resultadosParciales, nivelFinal, motivoPrincipal);
    }

    // ─── Sube o baja el nivel según el modificador acumulado ───────────────────
    private NivelRiesgo aplicarModificador(NivelRiesgo nivel, int modificador) {
        // Usamos Arrays.asList (compatible Java 8+) en lugar de List.of (Java 9+)
        List<NivelRiesgo> escala = Arrays.asList(NivelRiesgo.BAJO, NivelRiesgo.MEDIO, NivelRiesgo.ALTO);
        int indice = escala.indexOf(nivel);
        int nuevoIndice = Math.max(0, Math.min(escala.size() - 1, indice + modificador));
        return escala.get(nuevoIndice);
    }

    // ─── Arma el objeto ResultadoEvaluacion con todos sus detalles ─────────────
    private ResultadoEvaluacion armarResultado(ContextoEvaluacion contexto,
                                               List<ResultadoRegla> parciales,
                                               NivelRiesgo nivel,
                                               String motivo) {
        ResultadoEvaluacion resultadoFinal = new ResultadoEvaluacion();
        resultadoFinal.setEmpresaId(contexto.getSolicitud().getEmpresaId());
        resultadoFinal.setNivelRiesgo(nivel);
        resultadoFinal.setMotivoFinal(motivo);
        resultadoFinal.setFechaEvaluacion(LocalDateTime.now());

        List<DetalleReglaEvaluada> detallesBD = new ArrayList<>();
        for (ResultadoRegla rr : parciales) {
            DetalleReglaEvaluada det = new DetalleReglaEvaluada();
            det.setResultadoEvaluacion(resultadoFinal);
            det.setNombreRegla(rr.getNombreRegla());
            det.setResultado(rr.isAplico() ? "ALERTA: " + rr.getDetalle() : "OK");
            detallesBD.add(det);
        }
        resultadoFinal.setDetallesReglas(detallesBD);

        return resultadoFinal;
    }
}