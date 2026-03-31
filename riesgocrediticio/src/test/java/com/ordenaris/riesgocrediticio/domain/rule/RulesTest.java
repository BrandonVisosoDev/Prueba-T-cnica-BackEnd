package com.ordenaris.riesgocrediticio.domain.rule;

import com.ordenaris.riesgocrediticio.domain.model.ContextoEvaluacion;
import com.ordenaris.riesgocrediticio.domain.model.ResultadoRegla;
import com.ordenaris.riesgocrediticio.domain.model.SolicitudEvaluacion;
import com.ordenaris.riesgocrediticio.domain.model.enums.NivelRiesgo;
import com.ordenaris.riesgocrediticio.domain.model.enums.ProductoFinanciero;
import com.ordenaris.riesgocrediticio.infrastructure.adapter.out.persistence.DatosContables;
import com.ordenaris.riesgocrediticio.infrastructure.adapter.out.persistence.Empresa;
import com.ordenaris.riesgocrediticio.infrastructure.adapter.out.persistence.HistorialPagos;
import com.ordenaris.riesgocrediticio.infrastructure.adapter.out.persistence.VerificacionLegal;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ReglaDeudaActivaTest {

    @Test
    void evaluarConDeudaMayorA90DiasDebeAplicar() {
        ReglaDeudaActiva regla = new ReglaDeudaActiva();
        HistorialPagos historial = new HistorialPagos(1L, "EMP-001", 120, true, false);
        ContextoEvaluacion contexto = crearContextoConHistorial(historial);

        ResultadoRegla resultado = regla.evaluar(contexto);

        assertTrue(resultado.isAplico());
        assertEquals(NivelRiesgo.RECHAZADO, resultado.getNivelRiesgoPropuesto());
        assertTrue(resultado.getDetalle().contains("120 días"));
    }

    @Test
    void evaluarConDeudaMenorA90DiasNoDebeAplicar() {
        ReglaDeudaActiva regla = new ReglaDeudaActiva();
        HistorialPagos historial = new HistorialPagos(1L, "EMP-001", 30, true, false);
        ContextoEvaluacion contexto = crearContextoConHistorial(historial);

        ResultadoRegla resultado = regla.evaluar(contexto);

        assertFalse(resultado.isAplico());
        assertNull(resultado.getNivelRiesgoPropuesto());
    }

    @Test
    void evaluarConDeudaNulaNoDebeAplicar() {
        ReglaDeudaActiva regla = new ReglaDeudaActiva();
        HistorialPagos historial = new HistorialPagos(1L, "EMP-001", null, true, false);
        ContextoEvaluacion contexto = crearContextoConHistorial(historial);

        ResultadoRegla resultado = regla.evaluar(contexto);

        assertFalse(resultado.isAplico());
    }

    @Test
    void evaluarConHistorialNuloNoDebeAplicar() {
        ReglaDeudaActiva regla = new ReglaDeudaActiva();
        ContextoEvaluacion contexto = crearContextoConHistorial(null);

        ResultadoRegla resultado = regla.evaluar(contexto);

        assertFalse(resultado.isAplico());
        assertEquals("Deuda Activa", resultado.getNombreRegla());
    }

    private ContextoEvaluacion crearContextoConHistorial(HistorialPagos historial) {
        SolicitudEvaluacion solicitud = new SolicitudEvaluacion(
                "EMP-001", new BigDecimal("100000"), ProductoFinanciero.LINEA_OPERATIVA, LocalDate.now());
        Empresa empresa = new Empresa("EMP-001", "Test", LocalDate.of(2020, 1, 1), "RFC");
        DatosContables contables = new DatosContables(1L, "EMP-001", new BigDecimal("500000"), new BigDecimal("100000"), new BigDecimal("900000"));
        VerificacionLegal legal = new VerificacionLegal(1L, "EMP-001", false, 0, false);
        return new ContextoEvaluacion(solicitud, empresa, contables, historial, legal);
    }
}

class ReglaHistorialExelenteTest {

    @Test
    void evaluarConHistorialExcelenteDebeAplicarYModificarNegativo() {
        ReglaHistorialExcelente regla = new ReglaHistorialExcelente();
        HistorialPagos historial = new HistorialPagos(1L, "EMP-001", 0, true, false);
        ContextoEvaluacion contexto = crearContextoConHistorial(historial);

        ResultadoRegla resultado = regla.evaluar(contexto);

        assertTrue(resultado.isAplico());
        assertNull(resultado.getNivelRiesgoPropuesto());
        assertEquals(-1, resultado.getModificadorPuntos());
    }

    @Test
    void evaluarConRefinanciamientoNoDebeAplicar() {
        ReglaHistorialExcelente regla = new ReglaHistorialExcelente();
        HistorialPagos historial = new HistorialPagos(1L, "EMP-001", 0, true, true);
        ContextoEvaluacion contexto = crearContextoConHistorial(historial);

        ResultadoRegla resultado = regla.evaluar(contexto);

        assertFalse(resultado.isAplico());
    }

    @Test
    void evaluarConAtrasoNoDebeAplicar() {
        ReglaHistorialExcelente regla = new ReglaHistorialExcelente();
        HistorialPagos historial = new HistorialPagos(1L, "EMP-001", 0, false, false);
        ContextoEvaluacion contexto = crearContextoConHistorial(historial);

        ResultadoRegla resultado = regla.evaluar(contexto);

        assertFalse(resultado.isAplico());
    }

    @Test
    void evaluarConHistorialNuloNoDebeAplicar() {
        ReglaHistorialExcelente regla = new ReglaHistorialExcelente();
        ContextoEvaluacion contexto = crearContextoConHistorial(null);

        ResultadoRegla resultado = regla.evaluar(contexto);

        assertFalse(resultado.isAplico());
    }

    private ContextoEvaluacion crearContextoConHistorial(HistorialPagos historial) {
        SolicitudEvaluacion solicitud = new SolicitudEvaluacion(
                "EMP-001", new BigDecimal("100000"), ProductoFinanciero.LINEA_OPERATIVA, LocalDate.now());
        Empresa empresa = new Empresa("EMP-001", "Test", LocalDate.of(2020, 1, 1), "RFC");
        DatosContables contables = new DatosContables(1L, "EMP-001", new BigDecimal("500000"), new BigDecimal("100000"), new BigDecimal("900000"));
        VerificacionLegal legal = new VerificacionLegal(1L, "EMP-001", false, 0, false);
        return new ContextoEvaluacion(solicitud, empresa, contables, historial, legal);
    }
}

class ReglaEmpresaNuevaTest {

    @Test
    void evaluarConEmpresaDeMenosDe18MesesDebeAplicar() {
        ReglaEmpresaNueva regla = new ReglaEmpresaNueva();
        SolicitudEvaluacion solicitud = new SolicitudEvaluacion(
                "EMP-001", new BigDecimal("100000"), ProductoFinanciero.LINEA_OPERATIVA, LocalDate.of(2026, 3, 24));
        Empresa empresa = new Empresa("EMP-001", "Test", LocalDate.of(2025, 1, 1), "RFC");
        DatosContables contables = new DatosContables(1L, "EMP-001", new BigDecimal("500000"), new BigDecimal("100000"), new BigDecimal("900000"));
        HistorialPagos historial = new HistorialPagos(1L, "EMP-001", 0, true, false);
        VerificacionLegal legal = new VerificacionLegal(1L, "EMP-001", false, 0, false);
        ContextoEvaluacion contexto = new ContextoEvaluacion(solicitud, empresa, contables, historial, legal);

        ResultadoRegla resultado = regla.evaluar(contexto);

        assertTrue(resultado.isAplico());
        assertEquals(NivelRiesgo.MEDIO, resultado.getNivelRiesgoPropuesto());
    }

    @Test
    void evaluarConEmpresaDeMasDe18MesesNoDebeAplicar() {
        ReglaEmpresaNueva regla = new ReglaEmpresaNueva();
        SolicitudEvaluacion solicitud = new SolicitudEvaluacion(
                "EMP-001", new BigDecimal("100000"), ProductoFinanciero.LINEA_OPERATIVA, LocalDate.of(2026, 3, 24));
        Empresa empresa = new Empresa("EMP-001", "Test", LocalDate.of(2020, 1, 1), "RFC");
        DatosContables contables = new DatosContables(1L, "EMP-001", new BigDecimal("500000"), new BigDecimal("100000"), new BigDecimal("900000"));
        HistorialPagos historial = new HistorialPagos(1L, "EMP-001", 0, true, false);
        VerificacionLegal legal = new VerificacionLegal(1L, "EMP-001", false, 0, false);
        ContextoEvaluacion contexto = new ContextoEvaluacion(solicitud, empresa, contables, historial, legal);

        ResultadoRegla resultado = regla.evaluar(contexto);

        assertFalse(resultado.isAplico());
    }
}

class ReglaDemandaLegalAbiertaTest {

    @Test
    void evaluarConJuicioEnCursoDebeAplicar() {
        ReglaDemandaLegalAbierta regla = new ReglaDemandaLegalAbierta();
        VerificacionLegal legal = new VerificacionLegal(1L, "EMP-001", true, 0, false);
        SolicitudEvaluacion solicitud = new SolicitudEvaluacion(
                "EMP-001", new BigDecimal("100000"), ProductoFinanciero.LINEA_OPERATIVA, LocalDate.now());
        Empresa empresa = new Empresa("EMP-001", "Test", LocalDate.of(2020, 1, 1), "RFC");
        DatosContables contables = new DatosContables(1L, "EMP-001", new BigDecimal("500000"), new BigDecimal("100000"), new BigDecimal("900000"));
        HistorialPagos historial = new HistorialPagos(1L, "EMP-001", 0, true, false);
        ContextoEvaluacion contexto = new ContextoEvaluacion(solicitud, empresa, contables, historial, legal);

        ResultadoRegla resultado = regla.evaluar(contexto);

        assertTrue(resultado.isAplico());
        assertEquals(NivelRiesgo.ALTO, resultado.getNivelRiesgoPropuesto());
    }

    @Test
    void evaluarSinJuicioNoDebeAplicar() {
        ReglaDemandaLegalAbierta regla = new ReglaDemandaLegalAbierta();
        VerificacionLegal legal = new VerificacionLegal(1L, "EMP-001", false, 0, false);
        SolicitudEvaluacion solicitud = new SolicitudEvaluacion(
                "EMP-001", new BigDecimal("100000"), ProductoFinanciero.LINEA_OPERATIVA, LocalDate.now());
        Empresa empresa = new Empresa("EMP-001", "Test", LocalDate.of(2020, 1, 1), "RFC");
        DatosContables contables = new DatosContables(1L, "EMP-001", new BigDecimal("500000"), new BigDecimal("100000"), new BigDecimal("900000"));
        HistorialPagos historial = new HistorialPagos(1L, "EMP-001", 0, true, false);
        ContextoEvaluacion contexto = new ContextoEvaluacion(solicitud, empresa, contables, historial, legal);

        ResultadoRegla resultado = regla.evaluar(contexto);

        assertFalse(resultado.isAplico());
    }
}

class ReglaProductoEstrictoTest {

    @Test
    void evaluarConArrendamientoFinancieroDebeAplicarModificador() {
        ReglaProductoEstricto regla = new ReglaProductoEstricto();
        SolicitudEvaluacion solicitud = new SolicitudEvaluacion(
                "EMP-001", new BigDecimal("100000"), ProductoFinanciero.ARRENDAMIENTO_FINANCIERO, LocalDate.now());
        Empresa empresa = new Empresa("EMP-001", "Test", LocalDate.of(2020, 1, 1), "RFC");
        DatosContables contables = new DatosContables(1L, "EMP-001", new BigDecimal("500000"), new BigDecimal("100000"), new BigDecimal("900000"));
        HistorialPagos historial = new HistorialPagos(1L, "EMP-001", 0, true, false);
        VerificacionLegal legal = new VerificacionLegal(1L, "EMP-001", false, 0, false);
        ContextoEvaluacion contexto = new ContextoEvaluacion(solicitud, empresa, contables, historial, legal);

        ResultadoRegla resultado = regla.evaluar(contexto);

        assertTrue(resultado.isAplico());
        assertEquals(1, resultado.getModificadorPuntos());
    }

    @Test
    void evaluarConOtroProductoNoDebeAplicar() {
        ReglaProductoEstricto regla = new ReglaProductoEstricto();
        SolicitudEvaluacion solicitud = new SolicitudEvaluacion(
                "EMP-001", new BigDecimal("100000"), ProductoFinanciero.LINEA_OPERATIVA, LocalDate.now());
        Empresa empresa = new Empresa("EMP-001", "Test", LocalDate.of(2020, 1, 1), "RFC");
        DatosContables contables = new DatosContables(1L, "EMP-001", new BigDecimal("500000"), new BigDecimal("100000"), new BigDecimal("900000"));
        HistorialPagos historial = new HistorialPagos(1L, "EMP-001", 0, true, false);
        VerificacionLegal legal = new VerificacionLegal(1L, "EMP-001", false, 0, false);
        ContextoEvaluacion contexto = new ContextoEvaluacion(solicitud, empresa, contables, historial, legal);

        ResultadoRegla resultado = regla.evaluar(contexto);

        assertFalse(resultado.isAplico());
    }
}

class ReglaAltaSolicitudVsVentasTest {

    @Test
    void evaluarConMontoMayorA8VecesVentasDebeAplicar() {
        ReglaAltaSolicitudVsVentas regla = new ReglaAltaSolicitudVsVentas();
        SolicitudEvaluacion solicitud = new SolicitudEvaluacion(
                "EMP-001", new BigDecimal("900000"), ProductoFinanciero.LINEA_OPERATIVA, LocalDate.now());
        Empresa empresa = new Empresa("EMP-001", "Test", LocalDate.of(2020, 1, 1), "RFC");
        DatosContables contables = new DatosContables(1L, "EMP-001", new BigDecimal("100000"), new BigDecimal("100000"), new BigDecimal("900000"));
        HistorialPagos historial = new HistorialPagos(1L, "EMP-001", 0, true, false);
        VerificacionLegal legal = new VerificacionLegal(1L, "EMP-001", false, 0, false);
        ContextoEvaluacion contexto = new ContextoEvaluacion(solicitud, empresa, contables, historial, legal);

        ResultadoRegla resultado = regla.evaluar(contexto);

        assertTrue(resultado.isAplico());
        assertEquals(NivelRiesgo.ALTO, resultado.getNivelRiesgoPropuesto());
    }

    @Test
    void evaluarConMontoMenorA8VecesVentasNoDebeAplicar() {
        ReglaAltaSolicitudVsVentas regla = new ReglaAltaSolicitudVsVentas();
        SolicitudEvaluacion solicitud = new SolicitudEvaluacion(
                "EMP-001", new BigDecimal("100000"), ProductoFinanciero.LINEA_OPERATIVA, LocalDate.now());
        Empresa empresa = new Empresa("EMP-001", "Test", LocalDate.of(2020, 1, 1), "RFC");
        DatosContables contables = new DatosContables(1L, "EMP-001", new BigDecimal("100000"), new BigDecimal("100000"), new BigDecimal("900000"));
        HistorialPagos historial = new HistorialPagos(1L, "EMP-001", 0, true, false);
        VerificacionLegal legal = new VerificacionLegal(1L, "EMP-001", false, 0, false);
        ContextoEvaluacion contexto = new ContextoEvaluacion(solicitud, empresa, contables, historial, legal);

        ResultadoRegla resultado = regla.evaluar(contexto);

        assertFalse(resultado.isAplico());
    }

    @Test
    void evaluarConDatosNulosNoDebeAplicar() {
        ReglaAltaSolicitudVsVentas regla = new ReglaAltaSolicitudVsVentas();
        SolicitudEvaluacion solicitud = new SolicitudEvaluacion(
                "EMP-001", null, ProductoFinanciero.LINEA_OPERATIVA, LocalDate.now());
        Empresa empresa = new Empresa("EMP-001", "Test", LocalDate.of(2020, 1, 1), "RFC");
        DatosContables contables = new DatosContables(1L, "EMP-001", null, new BigDecimal("100000"), new BigDecimal("900000"));
        HistorialPagos historial = new HistorialPagos(1L, "EMP-001", 0, true, false);
        VerificacionLegal legal = new VerificacionLegal(1L, "EMP-001", false, 0, false);
        ContextoEvaluacion contexto = new ContextoEvaluacion(solicitud, empresa, contables, historial, legal);

        ResultadoRegla resultado = regla.evaluar(contexto);

        assertFalse(resultado.isAplico());
    }
}

