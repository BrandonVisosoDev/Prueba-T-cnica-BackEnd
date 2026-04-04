package com.ordenaris.riesgocrediticio.domain.engine;

import com.ordenaris.riesgocrediticio.domain.model.ContextoEvaluacion;
import com.ordenaris.riesgocrediticio.domain.model.ResultadoRegla;
import com.ordenaris.riesgocrediticio.domain.model.ResultadoRiesgo;
import com.ordenaris.riesgocrediticio.domain.model.enums.NivelRiesgo;
import com.ordenaris.riesgocrediticio.domain.rule.ReglaEvaluacion;
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

    public ResultadoRiesgo evaluarRiesgo(ContextoEvaluacion contexto) {
        log.info("Motor de riesgo evaluando {} reglas para empresa={}",
                reglas.size(), contexto.getSolicitud().getEmpresaId());

        List<ResultadoRegla> resultadosParciales = evaluarTodasLasReglas(contexto);
        Acumulador acumulador = procesarResultados(resultadosParciales);
        NivelRiesgo nivelFinal = determinarNivelFinal(acumulador);
        String motivo = determinarMotivo(acumulador, nivelFinal);

        return ResultadoRiesgo.builder()
                .empresaId(contexto.getSolicitud().getEmpresaId())
                .nivelRiesgo(nivelFinal)
                .motivoFinal(motivo)
                .fechaEvaluacion(LocalDateTime.now())
                .detallesReglas(resultadosParciales)
                .build();
    }

    private List<ResultadoRegla> evaluarTodasLasReglas(ContextoEvaluacion contexto) {
        List<ResultadoRegla> resultados = new ArrayList<>();
        for (ReglaEvaluacion regla : reglas) {
            ResultadoRegla resultado = regla.evaluar(contexto);
            if (resultado.isAplico()) {
                log.warn("Regla activada. nombre={}, nivel={}, detalle={}",
                        resultado.getNombreRegla(), resultado.getNivelRiesgoPropuesto(), resultado.getDetalle());
            } else {
                log.info("Regla sin alerta. nombre={}", resultado.getNombreRegla());
            }
            resultados.add(resultado);
        }
        return resultados;
    }

    private Acumulador procesarResultados(List<ResultadoRegla> resultados) {
        Acumulador acumulador = new Acumulador();
        for (ResultadoRegla res : resultados) {
            if (res.isAplico()) {
                acumulador.procesar(res);
            }
        }
        return acumulador;
    }

    private NivelRiesgo determinarNivelFinal(Acumulador acumulador) {
        NivelRiesgo nivel = calcularNivelBase(acumulador);
        nivel = aplicarModificador(nivel, acumulador.modificadorTotal);
        return aplicarMinimoMedio(nivel, acumulador.nivelMinimoMedio);
    }

    private NivelRiesgo calcularNivelBase(Acumulador acumulador) {
        if (acumulador.rechazadoInmediato) {
            return NivelRiesgo.RECHAZADO;
        }
        if (acumulador.contadorAltos >= 2) {
            return NivelRiesgo.RECHAZADO;
        }
        if (acumulador.contadorAltos == 1) {
            return NivelRiesgo.ALTO;
        }
        return NivelRiesgo.BAJO;
    }

    private NivelRiesgo aplicarModificador(NivelRiesgo nivel, int modificador) {
        if (nivel == NivelRiesgo.RECHAZADO) {
            return nivel;
        }
        List<NivelRiesgo> escala = Arrays.asList(NivelRiesgo.BAJO, NivelRiesgo.MEDIO, NivelRiesgo.ALTO);
        int indice = escala.indexOf(nivel);
        int nuevoIndice = Math.max(0, Math.min(escala.size() - 1, indice + modificador));
        return escala.get(nuevoIndice);
    }

    private NivelRiesgo aplicarMinimoMedio(NivelRiesgo nivel, boolean nivelMinimoMedio) {
        if (nivelMinimoMedio && nivel == NivelRiesgo.BAJO) {
            return NivelRiesgo.MEDIO;
        }
        return nivel;
    }

    private String determinarMotivo(Acumulador acumulador, NivelRiesgo nivelFinal) {
        if (acumulador.rechazadoInmediato) {
            return acumulador.motivoPrincipal;
        }
        if (acumulador.contadorAltos >= 2) {
            return "Rechazado por acumulacion de " + acumulador.contadorAltos + " alertas de nivel ALTO.";
        }
        if (nivelFinal == NivelRiesgo.MEDIO && acumulador.nivelMinimoMedio) {
            return "Empresa con menos de 18 meses: nivel minimo es MEDIO.";
        }
        if (acumulador.modificadorTotal > 0) {
            return "Nivel aumentado por producto ARRENDAMIENTO_FINANCIERO.";
        }
        if (acumulador.modificadorTotal < 0) {
            return "Riesgo reducido un nivel gracias a historial de pagos excelente.";
        }
        if (acumulador.contadorAltos == 1) {
            return "Riesgo ALTO por alerta importante detectada.";
        }
        return "Evaluacion completada con exito. Cliente apto.";
    }

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
