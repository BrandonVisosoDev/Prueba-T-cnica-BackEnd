package com.ordenaris.riesgocrediticio.domain.model;

import com.ordenaris.riesgocrediticio.domain.model.enums.NivelRiesgo;
import com.ordenaris.riesgocrediticio.domain.model.enums.ProductoFinanciero;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SolicitudEvaluacionTest {

    @Test
    void constructorDebeAsignarTodosLosValores() {
        LocalDate fecha = LocalDate.of(2026, 3, 24);
        BigDecimal monto = new BigDecimal("150000");

        SolicitudEvaluacion solicitud = new SolicitudEvaluacion(
                "EMP-001",
                monto,
                ProductoFinanciero.LINEA_OPERATIVA,
                fecha
        );

        assertEquals("EMP-001", solicitud.getEmpresaId());
        assertEquals(monto, solicitud.getMontoSolicitado());
        assertEquals(ProductoFinanciero.LINEA_OPERATIVA, solicitud.getProductoFinanciero());
        assertEquals(fecha, solicitud.getFechaSolicitud());
    }
}

class ResultadoRiesgoTest {

    @Test
    void constructorConBuilderDebeAsignarValores() {
        ResultadoRiesgo resultado = ResultadoRiesgo.builder()
                .empresaId("EMP-001")
                .nivelRiesgo(NivelRiesgo.BAJO)
                .motivoFinal("Cliente apto")
                .detallesReglas(null)
                .build();

        assertEquals("EMP-001", resultado.getEmpresaId());
        assertEquals(NivelRiesgo.BAJO, resultado.getNivelRiesgo());
        assertEquals("Cliente apto", resultado.getMotivoFinal());
    }
}

class ResultadoReglaTest {

    @Test
    void constructorDebeAsignarTodosLosValores() {
        ResultadoRegla resultado = new ResultadoRegla("Regla Test", true, NivelRiesgo.ALTO, 2, "Detalle test");

        assertEquals("Regla Test", resultado.getNombreRegla());
        assertTrue(resultado.isAplico());
        assertEquals(NivelRiesgo.ALTO, resultado.getNivelRiesgoPropuesto());
        assertEquals(2, resultado.getModificadorPuntos());
        assertEquals("Detalle test", resultado.getDetalle());
    }

    @Test
    void constructorSinArgumentosDebeCrearObjeto() {
        ResultadoRegla resultado = new ResultadoRegla();

        assertNull(resultado.getNombreRegla());
        assertNull(resultado.getNivelRiesgoPropuesto());
    }
}

class ContextoEvaluacionTest {

    @Test
    void constructorDebeAsignarValores() {
        SolicitudEvaluacion solicitud = new SolicitudEvaluacion(
                "EMP-001", new BigDecimal("100000"), ProductoFinanciero.LINEA_OPERATIVA, LocalDate.now());
        EmpresaEvaluacion empresa = new EmpresaEvaluacion("EMP-001", "Test", LocalDate.of(2020, 1, 1), "RFC");

        ContextoEvaluacion contexto = new ContextoEvaluacion(solicitud, empresa, null, null, null);

        assertNotNull(contexto.getSolicitud());
        assertNotNull(contexto.getEmpresa());
        assertNull(contexto.getDatosContables());
        assertNull(contexto.getHistorialPagos());
        assertNull(contexto.getVerificacionLegal());
    }
}

class EmpresaNotFoundExceptionTest {

    @Test
    void exceptionDebeTenerMensajeDinamico() {
        EmpresaNotFoundException exception = new EmpresaNotFoundException("EMP-999");

        assertTrue(exception.getMessage().contains("EMP-999"));
        assertTrue(exception.getMessage().contains("no"));
    }
}

class RiesgoEvaluacionExceptionTest {

    @Test
    void exceptionConMensajeYCausaDebe() {
        IllegalStateException causa = new IllegalStateException("Causa raiz");
        RiesgoEvaluacionException exception = new RiesgoEvaluacionException("Error envuelto", causa);

        assertEquals("Error envuelto", exception.getMessage());
        assertEquals(causa, exception.getCause());
    }
}
