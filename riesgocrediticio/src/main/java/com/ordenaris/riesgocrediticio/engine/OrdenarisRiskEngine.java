package com.ordenaris.riesgocrediticio.engine;

import com.ordenaris.riesgocrediticio.dto.ContextoEvaluacion;
import com.ordenaris.riesgocrediticio.dto.ResultadoRegla;
import com.ordenaris.riesgocrediticio.entity.DetalleReglaEvaluada;
import com.ordenaris.riesgocrediticio.entity.ResultadoEvaluacion;
import com.ordenaris.riesgocrediticio.enums.NivelRiesgo;
import com.ordenaris.riesgocrediticio.rule.ReglaEvaluacion;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Component
public class OrdenarisRiskEngine {

    // Spring buscará automáticamente todas las clases con @Component que implementen ReglaEvaluacion
    private final List<ReglaEvaluacion> reglas;

    public OrdenarisRiskEngine(List<ReglaEvaluacion> reglas) {
        this.reglas = reglas;
    }

    public ResultadoEvaluacion evaluarRiesgo(ContextoEvaluacion contexto) {
        List<ResultadoRegla> resultadosParciales = new ArrayList<>();
        int contadorAltos = 0;
        int modificadorTotal = 0;
        boolean rechazadoInmediato = false;
        String motivoPrincipal = "Evaluación completada con éxito. Cliente apto.";

        // 1. La línea de ensamblaje: Pasamos la empresa por todas las reglas
        for (ReglaEvaluacion regla : reglas) {
            ResultadoRegla res = regla.evaluar(contexto);
            resultadosParciales.add(res);

            if (res.isAplico()) {
                if (res.getNivelRiesgoPropuesto() == NivelRiesgo.RECHAZADO) {
                    rechazadoInmediato = true;
                    motivoPrincipal = res.getDetalle();
                } else if (res.getNivelRiesgoPropuesto() == NivelRiesgo.ALTO) {
                    contadorAltos++; // Vamos contando las alertas
                }

                if (res.getModificadorPuntos() != null) {
                    modificadorTotal += res.getModificadorPuntos(); // Acumulamos los premios o castigos
                }
            }
        }

        // 2. Aplicamos la Regla 6: Suma de Factores de Riesgo
        NivelRiesgo nivelFinal = NivelRiesgo.BAJO; // Empezamos asumiendo que es un buen cliente

        if (rechazadoInmediato) {
            nivelFinal = NivelRiesgo.RECHAZADO;
        } else if (contadorAltos >= 2) {
            nivelFinal = NivelRiesgo.RECHAZADO;
            motivoPrincipal = "Rechazado por Suma de Factores: Acumuló " + contadorAltos + " alertas de nivel ALTO.";
        } else if (contadorAltos == 1) {
            nivelFinal = NivelRiesgo.ALTO;
            motivoPrincipal = "Riesgo ALTO debido a una alerta importante detectada.";
        }

        // 3. Aplicamos el modificador (Historial Excelente)
        if (nivelFinal == NivelRiesgo.ALTO && modificadorTotal < 0) {
            nivelFinal = NivelRiesgo.MEDIO;
            motivoPrincipal = "El riesgo inicial era ALTO, pero disminuyó a MEDIO gracias a su excelente historial de pagos.";
        }

        // 4. Construimos el documento final para guardar en la base de datos
        return armarResultado(contexto, resultadosParciales, nivelFinal, motivoPrincipal);
    }

    // Metodo de apoyo
    private ResultadoEvaluacion armarResultado(ContextoEvaluacion contexto, List<ResultadoRegla> parciales, NivelRiesgo nivel, String motivo) {
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