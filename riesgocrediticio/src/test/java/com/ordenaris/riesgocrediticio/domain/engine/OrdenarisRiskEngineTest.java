package com.ordenaris.riesgocrediticio.domain.engine;

import com.ordenaris.riesgocrediticio.domain.model.ContextoEvaluacion;
import com.ordenaris.riesgocrediticio.domain.model.ResultadoRegla;
import com.ordenaris.riesgocrediticio.domain.model.SolicitudEvaluacion;
import com.ordenaris.riesgocrediticio.domain.model.enums.NivelRiesgo;
import com.ordenaris.riesgocrediticio.domain.model.enums.ProductoFinanciero;
import com.ordenaris.riesgocrediticio.domain.rule.ReglaEvaluacion;
import com.ordenaris.riesgocrediticio.infrastructure.adapter.out.persistence.DatosContables;
import com.ordenaris.riesgocrediticio.infrastructure.adapter.out.persistence.Empresa;
import com.ordenaris.riesgocrediticio.infrastructure.adapter.out.persistence.HistorialPagos;
import com.ordenaris.riesgocrediticio.infrastructure.adapter.out.persistence.ResultadoEvaluacion;
import com.ordenaris.riesgocrediticio.infrastructure.adapter.out.persistence.VerificacionLegal;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OrdenarisRiskEngineTest {

    @Test
    void evaluarRiesgoConTodasLasReglasValiendoDeberiaDarNivelBajo() {
        List<ReglaEvaluacion> reglas = Arrays.asList(
                crearReglaQuePasaSinAplico("Regla 1"),
                crearReglaQuePasaSinAplico("Regla 2"),
                crearReglaQuePasaSinAplico("Regla 3")
        );
        OrdenarisRiskEngine motor = new OrdenarisRiskEngine(reglas);
        ContextoEvaluacion contexto = crearContextoEvaluacion();

        ResultadoEvaluacion resultado = motor.evaluarRiesgo(contexto);

        assertNotNull(resultado);
        assertEquals(NivelRiesgo.BAJO, resultado.getNivelRiesgo());
        assertEquals("Evaluación completada con éxito. Cliente apto.", resultado.getMotivoFinal());
        assertEquals(3, resultado.getDetallesReglas().size());
    }

    @Test
    void evaluarRiesgoConUnaReglaAltoDeberiaDarNivelAlto() {
        List<ReglaEvaluacion> reglas = Arrays.asList(
                crearReglaQuePasaSinAplico("Regla 1"),
                crearReglaConNivelAlto("Regla Peligrosa"),
                crearReglaQuePasaSinAplico("Regla 3")
        );
        OrdenarisRiskEngine motor = new OrdenarisRiskEngine(reglas);
        ContextoEvaluacion contexto = crearContextoEvaluacion();

        ResultadoEvaluacion resultado = motor.evaluarRiesgo(contexto);

        assertNotNull(resultado);
        assertEquals(NivelRiesgo.ALTO, resultado.getNivelRiesgo());
        assertTrue(resultado.getMotivoFinal().contains("ALTO"));
    }

    @Test
    void evaluarRiesgoConDosReglasAltoDeberiaDarRechazo() {
        List<ReglaEvaluacion> reglas = Arrays.asList(
                crearReglaConNivelAlto("Regla 1"),
                crearReglaConNivelAlto("Regla 2"),
                crearReglaQuePasaSinAplico("Regla 3")
        );
        OrdenarisRiskEngine motor = new OrdenarisRiskEngine(reglas);
        ContextoEvaluacion contexto = crearContextoEvaluacion();

        ResultadoEvaluacion resultado = motor.evaluarRiesgo(contexto);

        assertNotNull(resultado);
        assertEquals(NivelRiesgo.RECHAZADO, resultado.getNivelRiesgo());
        assertTrue(resultado.getMotivoFinal().contains("acumulación"));
    }

    @Test
    void evaluarRiesgoConReglaRechazoInmediatoDebeRechazarInmediatamente() {
        List<ReglaEvaluacion> reglas = Arrays.asList(
                crearReglaConRechazo("Regla Fatal"),
                crearReglaConNivelAlto("Regla 2")
        );
        OrdenarisRiskEngine motor = new OrdenarisRiskEngine(reglas);
        ContextoEvaluacion contexto = crearContextoEvaluacion();

        ResultadoEvaluacion resultado = motor.evaluarRiesgo(contexto);

        assertNotNull(resultado);
        assertEquals(NivelRiesgo.RECHAZADO, resultado.getNivelRiesgo());
        assertEquals("Empresa descalificada.", resultado.getMotivoFinal());
    }

    @Test
    void evaluarRiesgoConModificadorPositivoDebeSubirNivel() {
        List<ReglaEvaluacion> reglas = Arrays.asList(
                crearReglaQuePasaSinAplico("Regla 1"),
                crearReglaConModificador("Regla Modificadora", 1)
        );
        OrdenarisRiskEngine motor = new OrdenarisRiskEngine(reglas);
        ContextoEvaluacion contexto = crearContextoEvaluacion();

        ResultadoEvaluacion resultado = motor.evaluarRiesgo(contexto);

        assertNotNull(resultado);
        assertEquals(NivelRiesgo.MEDIO, resultado.getNivelRiesgo());
        assertTrue(resultado.getMotivoFinal().contains("aumentado"));
    }

    @Test
    void evaluarRiesgoConModificadorNegativoDebebajarNivel() {
        List<ReglaEvaluacion> reglas = Arrays.asList(
                crearReglaConNivelAlto("Regla Alto"),
                crearReglaConModificador("Regla Mejora", -1)
        );
        OrdenarisRiskEngine motor = new OrdenarisRiskEngine(reglas);
        ContextoEvaluacion contexto = crearContextoEvaluacion();

        ResultadoEvaluacion resultado = motor.evaluarRiesgo(contexto);

        assertNotNull(resultado);
        assertEquals(NivelRiesgo.MEDIO, resultado.getNivelRiesgo());
        assertTrue(resultado.getMotivoFinal().contains("reducido"));
    }

    @Test
    void evaluarRiesgoConNivelMedioDebeAplicarMinimoMedio() {
        List<ReglaEvaluacion> reglas = Arrays.asList(
                crearReglaQuePasaSinAplico("Regla 1"),
                crearReglaConNivelMedio("Regla Media")
        );
        OrdenarisRiskEngine motor = new OrdenarisRiskEngine(reglas);
        ContextoEvaluacion contexto = crearContextoEvaluacion();

        ResultadoEvaluacion resultado = motor.evaluarRiesgo(contexto);

        assertNotNull(resultado);
        assertEquals(NivelRiesgo.MEDIO, resultado.getNivelRiesgo());
    }

    @Test
    void evaluarRiesgoConListaVaciaDebeDarNivelBajo() {
        OrdenarisRiskEngine motor = new OrdenarisRiskEngine(Collections.emptyList());
        ContextoEvaluacion contexto = crearContextoEvaluacion();

        ResultadoEvaluacion resultado = motor.evaluarRiesgo(contexto);

        assertNotNull(resultado);
        assertEquals(NivelRiesgo.BAJO, resultado.getNivelRiesgo());
        assertEquals(0, resultado.getDetallesReglas().size());
    }

    // ========== Métodos auxiliares ==========

    private ReglaEvaluacion crearReglaQuePasaSinAplico(String nombre) {
        return contexto -> new ResultadoRegla(nombre, false, null, null, "Todo OK");
    }

    private ReglaEvaluacion crearReglaConNivelAlto(String nombre) {
        return contexto -> new ResultadoRegla(nombre, true, NivelRiesgo.ALTO, null, "Alerta detectada");
    }

    private ReglaEvaluacion crearReglaConNivelMedio(String nombre) {
        return contexto -> new ResultadoRegla(nombre, true, NivelRiesgo.MEDIO, null, "Riesgo medio");
    }

    private ReglaEvaluacion crearReglaConRechazo(String nombre) {
        return contexto -> new ResultadoRegla(nombre, true, NivelRiesgo.RECHAZADO, null, "Empresa descalificada.");
    }

    private ReglaEvaluacion crearReglaConModificador(String nombre, int modificador) {
        return contexto -> new ResultadoRegla(nombre, true, null, modificador, "Aplicando modificador");
    }

    private ContextoEvaluacion crearContextoEvaluacion() {
        SolicitudEvaluacion solicitud = new SolicitudEvaluacion(
                "EMP-001",
                new BigDecimal("100000"),
                ProductoFinanciero.LINEA_OPERATIVA,
                LocalDate.of(2026, 3, 24)
        );
        Empresa empresa = new Empresa("EMP-001", "Empresa Test", LocalDate.of(2020, 1, 1), "RFC001");
        DatosContables contables = new DatosContables(1L, "EMP-001", new BigDecimal("500000"), new BigDecimal("100000"), new BigDecimal("900000"));
        HistorialPagos pagos = new HistorialPagos(1L, "EMP-001", 0, true, false);
        VerificacionLegal legal = new VerificacionLegal(1L, "EMP-001", false, 0, false);
        return new ContextoEvaluacion(solicitud, empresa, contables, pagos, legal);
    }
}

