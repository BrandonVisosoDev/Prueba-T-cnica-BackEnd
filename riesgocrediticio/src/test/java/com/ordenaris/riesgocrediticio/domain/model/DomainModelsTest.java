package com.ordenaris.riesgocrediticio.domain.model;

import com.ordenaris.riesgocrediticio.domain.model.enums.NivelRiesgo;
import com.ordenaris.riesgocrediticio.domain.model.enums.ProductoFinanciero;
import com.ordenaris.riesgocrediticio.infrastructure.adapter.out.persistence.Empresa;
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

    @Test
    void settersDebenModificarValores() {
        SolicitudEvaluacion solicitud = new SolicitudEvaluacion();
        solicitud.setEmpresaId("EMP-002");
        solicitud.setMontoSolicitado(new BigDecimal("200000"));
        solicitud.setProductoFinanciero(ProductoFinanciero.ARRENDAMIENTO_FINANCIERO);
        solicitud.setFechaSolicitud(LocalDate.of(2026, 3, 25));

        assertEquals("EMP-002", solicitud.getEmpresaId());
        assertEquals(new BigDecimal("200000"), solicitud.getMontoSolicitado());
        assertEquals(ProductoFinanciero.ARRENDAMIENTO_FINANCIERO, solicitud.getProductoFinanciero());
        assertEquals(LocalDate.of(2026, 3, 25), solicitud.getFechaSolicitud());
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

    @Test
    void settersDebenModificarValores() {
        ResultadoRiesgo resultado = new ResultadoRiesgo();
        resultado.setEmpresaId("EMP-002");
        resultado.setNivelRiesgo(NivelRiesgo.ALTO);
        resultado.setMotivoFinal("Empresa rechazada");

        assertEquals("EMP-002", resultado.getEmpresaId());
        assertEquals(NivelRiesgo.ALTO, resultado.getNivelRiesgo());
        assertEquals("Empresa rechazada", resultado.getMotivoFinal());
    }
}

class ResultadoReglaTest {

    @Test
    void constructorDebeAsignarTodosLosValores() {
        ResultadoRegla resultado = new ResultadoRegla(
                "Regla Test",
                true,
                NivelRiesgo.ALTO,
                2,
                "Detalle test"
        );

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
        Empresa empresa = new Empresa("EMP-001", "Test", LocalDate.of(2020, 1, 1), "RFC");

        ContextoEvaluacion contexto = new ContextoEvaluacion(
                solicitud,
                empresa,
                null,
                null,
                null
        );

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
        assertTrue(exception.getMessage().contains("no está registrada"));
    }

    @Test
    void exceptionDebeSerRuntimeException() {
        EmpresaNotFoundException exception = new EmpresaNotFoundException("EMP-001");

        assertTrue(exception instanceof RuntimeException);
    }
}

class RiesgoEvaluacionExceptionTest {

    @Test
    void exceptionConMensajeDebe() {
        RiesgoEvaluacionException exception = new RiesgoEvaluacionException("Error de prueba");

        assertEquals("Error de prueba", exception.getMessage());
    }

    @Test
    void exceptionConMensajeYCausaDebe() {
        IllegalStateException causa = new IllegalStateException("Causa raiz");
        RiesgoEvaluacionException exception = new RiesgoEvaluacionException("Error envuelto", causa);

        assertEquals("Error envuelto", exception.getMessage());
        assertEquals(causa, exception.getCause());
    }

    @Test
    void exceptionDebeSerRuntimeException() {
        RiesgoEvaluacionException exception = new RiesgoEvaluacionException("Test");

        assertTrue(exception instanceof RuntimeException);
    }
}

