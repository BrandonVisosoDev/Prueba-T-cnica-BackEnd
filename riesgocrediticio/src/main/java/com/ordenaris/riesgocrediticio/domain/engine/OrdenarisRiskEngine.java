package com.ordenaris.riesgocrediticio.domain.engine;

import com.ordenaris.riesgocrediticio.domain.model.ResultadoRegla;
import com.ordenaris.riesgocrediticio.domain.model.ContextoEvaluacion;
import com.ordenaris.riesgocrediticio.domain.rule.ReglaEvaluacion;
import com.ordenaris.riesgocrediticio.domain.model.enums.NivelRiesgo;
import com.ordenaris.riesgocrediticio.infrastructure.adapter.out.persistence.DetalleReglaEvaluada;
import com.ordenaris.riesgocrediticio.infrastructure.adapter.out.persistence.ResultadoEvaluacion;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Slf4j

@Component
public class OrdenarisRiskEngine {

    private final List<ReglaEvaluacion> reglas;

    public OrdenarisRiskEngine(List<ReglaEvaluacion> reglas) {
        this.reglas = reglas;
    }

    // ─── Metodo principal — Cognitive Complexity reducida ─────────────
    public ResultadoEvaluacion evaluarRiesgo(ContextoEvaluacion contexto) {
        log.info(">> Motor iniciado — evaluando {} reglas", reglas.size());
        List<ResultadoRegla> resultadosParciales = evaluarTodasLasReglas(contexto);
        Acumulador acumulador = procesarResultados(resultadosParciales);
        NivelRiesgo nivelFinal = determinarNivelFinal(acumulador);
        String motivo = determinarMotivo(acumulador, nivelFinal);
        log.info(">> Motor finalizado — Nivel: {} | Motivo: {}", nivelFinal, motivo); // ← agrega
        return armarResultado(contexto, resultadosParciales, nivelFinal, motivo);
    }

    // ─── Paso 1: Evalúa todas las reglas y devuelve los resultados ─────────────
    private List<ResultadoRegla> evaluarTodasLasReglas(ContextoEvaluacion contexto) {
        List<ResultadoRegla> resultados = new ArrayList<>();
        for (ReglaEvaluacion regla : reglas) {
            resultados.add(regla.evaluar(contexto));
        }
        return resultados;
    }

    // ─── Paso 2: Procesa los resultados y acumula contadores ───────────────────
    private Acumulador procesarResultados(List<ResultadoRegla> resultados) {
        Acumulador acumulador = new Acumulador();
        for (ResultadoRegla res : resultados) {
            if (res.isAplico()) {
                acumulador.procesar(res);
            }
        }
        return acumulador;
    }

    // ─── Paso 3: Determina el nivel base según los contadores ──────────────────
    private NivelRiesgo determinarNivelFinal(Acumulador acumulador) {
        NivelRiesgo nivel = calcularNivelBase(acumulador);
        nivel = aplicarModificador(nivel, acumulador.modificadorTotal);
        return aplicarMinimoMedio(nivel, acumulador.nivelMinimoMedio);
    }

    private NivelRiesgo calcularNivelBase(Acumulador acumulador) {
        if (acumulador.rechazadoInmediato) return NivelRiesgo.RECHAZADO;
        if (acumulador.contadorAltos >= 2)  return NivelRiesgo.RECHAZADO;
        if (acumulador.contadorAltos == 1)  return NivelRiesgo.ALTO;
        return NivelRiesgo.BAJO;
    }

    private NivelRiesgo aplicarModificador(NivelRiesgo nivel, int modificador) {
        if (nivel == NivelRiesgo.RECHAZADO) return nivel;
        List<NivelRiesgo> escala = Arrays.asList(NivelRiesgo.BAJO, NivelRiesgo.MEDIO, NivelRiesgo.ALTO);
        int indice = escala.indexOf(nivel);
        int nuevoIndice = Math.max(0, Math.min(escala.size() - 1, indice + modificador));
        return escala.get(nuevoIndice);
    }

    private NivelRiesgo aplicarMinimoMedio(NivelRiesgo nivel, boolean nivelMinimoMedio) {
        if (nivelMinimoMedio && nivel == NivelRiesgo.BAJO) return NivelRiesgo.MEDIO;
        return nivel;
    }

    // ─── Paso 4: Determina el motivo final ─────────────────────────────────────
    private String determinarMotivo(Acumulador acumulador, NivelRiesgo nivelFinal) {
        if (acumulador.rechazadoInmediato)                                  return acumulador.motivoPrincipal;
        if (acumulador.contadorAltos >= 2)                                  return "Rechazado por acumulación de " + acumulador.contadorAltos + " alertas de nivel ALTO.";
        if (nivelFinal == NivelRiesgo.MEDIO && acumulador.nivelMinimoMedio) return "Empresa con menos de 18 meses: nivel mínimo es MEDIO.";
        if (acumulador.modificadorTotal > 0)                                return "Nivel aumentado por producto ARRENDAMIENTO_FINANCIERO.";
        if (acumulador.modificadorTotal < 0)                                return "Riesgo reducido un nivel gracias a historial de pagos excelente.";
        if (acumulador.contadorAltos == 1)                                  return "Riesgo ALTO por alerta importante detectada.";
        return "Evaluación completada con éxito. Cliente apto.";
    }

    // ─── Paso 5: Arma el ResultadoEvaluacion final ─────────────────────────────
    private ResultadoEvaluacion armarResultado(ContextoEvaluacion contexto,
                                               List<ResultadoRegla> parciales,
                                               NivelRiesgo nivel,
                                               String motivo) {
        ResultadoEvaluacion resultadoFinal = new ResultadoEvaluacion();
        resultadoFinal.setEmpresaId(contexto.getSolicitud().getEmpresaId());
        resultadoFinal.setNivelRiesgo(nivel);
        resultadoFinal.setMotivoFinal(motivo);
        resultadoFinal.setFechaEvaluacion(LocalDateTime.now());
        resultadoFinal.setDetallesReglas(construirDetalles(parciales, resultadoFinal));
        return resultadoFinal;
    }

    private List<DetalleReglaEvaluada> construirDetalles(List<ResultadoRegla> parciales,
                                                         ResultadoEvaluacion resultadoFinal) {
        List<DetalleReglaEvaluada> detalles = new ArrayList<>();
        for (ResultadoRegla rr : parciales) {
            DetalleReglaEvaluada det = new DetalleReglaEvaluada();
            det.setResultadoEvaluacion(resultadoFinal);
            det.setNombreRegla(rr.getNombreRegla());
            det.setResultado(rr.isAplico() ? "ALERTA: " + rr.getDetalle() : "OK");
            detalles.add(det);
        }
        return detalles;
    }

    // ─── Clase interna para acumular resultados ────────────────────────────────
    private static class Acumulador {
        int contadorAltos = 0;
        int modificadorTotal = 0;
        boolean rechazadoInmediato = false;
        boolean nivelMinimoMedio = false;
        String motivoPrincipal = "";

        void procesar(ResultadoRegla res) {
            NivelRiesgo propuesto = res.getNivelRiesgoPropuesto();
            if (propuesto == NivelRiesgo.RECHAZADO) {
                rechazadoInmediato = true;
                motivoPrincipal = res.getDetalle();
            } else if (propuesto == NivelRiesgo.ALTO) {
                contadorAltos++;
            } else if (propuesto == NivelRiesgo.MEDIO) {
                nivelMinimoMedio = true;
            }
            if (res.getModificadorPuntos() != null) {
                modificadorTotal += res.getModificadorPuntos();
            }
        }
    }
}