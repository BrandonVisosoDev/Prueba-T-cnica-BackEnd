package com.ordenaris.riesgocrediticio.domain.rule;

import com.ordenaris.riesgocrediticio.domain.model.ContextoEvaluacion;
import com.ordenaris.riesgocrediticio.domain.model.DatosContablesEvaluacion;
import com.ordenaris.riesgocrediticio.domain.model.EmpresaEvaluacion;
import com.ordenaris.riesgocrediticio.domain.model.HistorialPagosEvaluacion;
import com.ordenaris.riesgocrediticio.domain.model.ResultadoRegla;
import com.ordenaris.riesgocrediticio.domain.model.SolicitudEvaluacion;
import com.ordenaris.riesgocrediticio.domain.model.VerificacionLegalEvaluacion;
import com.ordenaris.riesgocrediticio.domain.model.enums.NivelRiesgo;
import com.ordenaris.riesgocrediticio.domain.model.enums.ProductoFinanciero;
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
        ResultadoRegla resultado = regla.evaluar(crearContextoConHistorial(new HistorialPagosEvaluacion("EMP-001", 120, true, false)));

        assertTrue(resultado.isAplico());
        assertEquals(NivelRiesgo.RECHAZADO, resultado.getNivelRiesgoPropuesto());
        assertTrue(resultado.getDetalle().contains("120"));
    }

    @Test
    void evaluarConDeudaMenorA90DiasNoDebeAplicar() {
        ReglaDeudaActiva regla = new ReglaDeudaActiva();
        ResultadoRegla resultado = regla.evaluar(crearContextoConHistorial(new HistorialPagosEvaluacion("EMP-001", 30, true, false)));

        assertFalse(resultado.isAplico());
        assertNull(resultado.getNivelRiesgoPropuesto());
    }

    @Test
    void evaluarConDeudaNulaNoDebeAplicar() {
        ReglaDeudaActiva regla = new ReglaDeudaActiva();
        ResultadoRegla resultado = regla.evaluar(crearContextoConHistorial(new HistorialPagosEvaluacion("EMP-001", null, true, false)));

        assertFalse(resultado.isAplico());
    }

    @Test
    void evaluarConHistorialNuloNoDebeAplicar() {
        ReglaDeudaActiva regla = new ReglaDeudaActiva();
        ResultadoRegla resultado = regla.evaluar(crearContextoConHistorial(null));

        assertFalse(resultado.isAplico());
        assertEquals("Deuda Activa", resultado.getNombreRegla());
    }

    private ContextoEvaluacion crearContextoConHistorial(HistorialPagosEvaluacion historial) {
        return new ContextoEvaluacion(
                new SolicitudEvaluacion("EMP-001", new BigDecimal("100000"), ProductoFinanciero.LINEA_OPERATIVA, LocalDate.now()),
                new EmpresaEvaluacion("EMP-001", "Test", LocalDate.of(2020, 1, 1), "RFC"),
                new DatosContablesEvaluacion("EMP-001", new BigDecimal("500000"), new BigDecimal("100000"), new BigDecimal("900000")),
                historial,
                new VerificacionLegalEvaluacion("EMP-001", false, 0, false));
    }
}

class ReglaHistorialExcelenteTest {

    @Test
    void evaluarConHistorialExcelenteDebeAplicarYModificarNegativo() {
        ReglaHistorialExcelente regla = new ReglaHistorialExcelente();
        ResultadoRegla resultado = regla.evaluar(crearContextoConHistorial(new HistorialPagosEvaluacion("EMP-001", 0, true, false)));

        assertTrue(resultado.isAplico());
        assertNull(resultado.getNivelRiesgoPropuesto());
        assertEquals(-1, resultado.getModificadorPuntos());
    }

    @Test
    void evaluarConRefinanciamientoNoDebeAplicar() {
        ReglaHistorialExcelente regla = new ReglaHistorialExcelente();
        ResultadoRegla resultado = regla.evaluar(crearContextoConHistorial(new HistorialPagosEvaluacion("EMP-001", 0, true, true)));

        assertFalse(resultado.isAplico());
    }

    @Test
    void evaluarConAtrasoNoDebeAplicar() {
        ReglaHistorialExcelente regla = new ReglaHistorialExcelente();
        ResultadoRegla resultado = regla.evaluar(crearContextoConHistorial(new HistorialPagosEvaluacion("EMP-001", 0, false, false)));

        assertFalse(resultado.isAplico());
    }

    @Test
    void evaluarConHistorialNuloNoDebeAplicar() {
        ReglaHistorialExcelente regla = new ReglaHistorialExcelente();
        ResultadoRegla resultado = regla.evaluar(crearContextoConHistorial(null));

        assertFalse(resultado.isAplico());
    }

    private ContextoEvaluacion crearContextoConHistorial(HistorialPagosEvaluacion historial) {
        return new ContextoEvaluacion(
                new SolicitudEvaluacion("EMP-001", new BigDecimal("100000"), ProductoFinanciero.LINEA_OPERATIVA, LocalDate.now()),
                new EmpresaEvaluacion("EMP-001", "Test", LocalDate.of(2020, 1, 1), "RFC"),
                new DatosContablesEvaluacion("EMP-001", new BigDecimal("500000"), new BigDecimal("100000"), new BigDecimal("900000")),
                historial,
                new VerificacionLegalEvaluacion("EMP-001", false, 0, false));
    }
}

class ReglaEmpresaNuevaTest {

    @Test
    void evaluarConEmpresaDeMenosDe18MesesDebeAplicar() {
        ReglaEmpresaNueva regla = new ReglaEmpresaNueva();
        ContextoEvaluacion contexto = new ContextoEvaluacion(
                new SolicitudEvaluacion("EMP-001", new BigDecimal("100000"), ProductoFinanciero.LINEA_OPERATIVA, LocalDate.of(2026, 3, 24)),
                new EmpresaEvaluacion("EMP-001", "Test", LocalDate.of(2025, 1, 1), "RFC"),
                new DatosContablesEvaluacion("EMP-001", new BigDecimal("500000"), new BigDecimal("100000"), new BigDecimal("900000")),
                new HistorialPagosEvaluacion("EMP-001", 0, true, false),
                new VerificacionLegalEvaluacion("EMP-001", false, 0, false));

        ResultadoRegla resultado = regla.evaluar(contexto);

        assertTrue(resultado.isAplico());
        assertEquals(NivelRiesgo.MEDIO, resultado.getNivelRiesgoPropuesto());
    }

    @Test
    void evaluarConEmpresaDeMasDe18MesesNoDebeAplicar() {
        ReglaEmpresaNueva regla = new ReglaEmpresaNueva();
        ContextoEvaluacion contexto = new ContextoEvaluacion(
                new SolicitudEvaluacion("EMP-001", new BigDecimal("100000"), ProductoFinanciero.LINEA_OPERATIVA, LocalDate.of(2026, 3, 24)),
                new EmpresaEvaluacion("EMP-001", "Test", LocalDate.of(2020, 1, 1), "RFC"),
                new DatosContablesEvaluacion("EMP-001", new BigDecimal("500000"), new BigDecimal("100000"), new BigDecimal("900000")),
                new HistorialPagosEvaluacion("EMP-001", 0, true, false),
                new VerificacionLegalEvaluacion("EMP-001", false, 0, false));

        ResultadoRegla resultado = regla.evaluar(contexto);

        assertFalse(resultado.isAplico());
    }
}

class ReglaDemandaLegalAbiertaTest {

    @Test
    void evaluarConJuicioEnCursoDebeAplicar() {
        ReglaDemandaLegalAbierta regla = new ReglaDemandaLegalAbierta();
        ContextoEvaluacion contexto = crearContextoConLegal(new VerificacionLegalEvaluacion("EMP-001", true, 0, false));

        ResultadoRegla resultado = regla.evaluar(contexto);

        assertTrue(resultado.isAplico());
        assertEquals(NivelRiesgo.ALTO, resultado.getNivelRiesgoPropuesto());
    }

    @Test
    void evaluarSinJuicioNoDebeAplicar() {
        ReglaDemandaLegalAbierta regla = new ReglaDemandaLegalAbierta();
        ResultadoRegla resultado = regla.evaluar(crearContextoConLegal(new VerificacionLegalEvaluacion("EMP-001", false, 0, false)));

        assertFalse(resultado.isAplico());
    }

    private ContextoEvaluacion crearContextoConLegal(VerificacionLegalEvaluacion legal) {
        return new ContextoEvaluacion(
                new SolicitudEvaluacion("EMP-001", new BigDecimal("100000"), ProductoFinanciero.LINEA_OPERATIVA, LocalDate.now()),
                new EmpresaEvaluacion("EMP-001", "Test", LocalDate.of(2020, 1, 1), "RFC"),
                new DatosContablesEvaluacion("EMP-001", new BigDecimal("500000"), new BigDecimal("100000"), new BigDecimal("900000")),
                new HistorialPagosEvaluacion("EMP-001", 0, true, false),
                legal);
    }
}

class ReglaProductoEstrictoTest {

    @Test
    void evaluarConArrendamientoFinancieroDebeAplicarModificador() {
        ReglaProductoEstricto regla = new ReglaProductoEstricto();
        ResultadoRegla resultado = regla.evaluar(crearContextoConProducto(ProductoFinanciero.ARRENDAMIENTO_FINANCIERO));

        assertTrue(resultado.isAplico());
        assertEquals(1, resultado.getModificadorPuntos());
    }

    @Test
    void evaluarConOtroProductoNoDebeAplicar() {
        ReglaProductoEstricto regla = new ReglaProductoEstricto();
        ResultadoRegla resultado = regla.evaluar(crearContextoConProducto(ProductoFinanciero.LINEA_OPERATIVA));

        assertFalse(resultado.isAplico());
    }

    private ContextoEvaluacion crearContextoConProducto(ProductoFinanciero producto) {
        return new ContextoEvaluacion(
                new SolicitudEvaluacion("EMP-001", new BigDecimal("100000"), producto, LocalDate.now()),
                new EmpresaEvaluacion("EMP-001", "Test", LocalDate.of(2020, 1, 1), "RFC"),
                new DatosContablesEvaluacion("EMP-001", new BigDecimal("500000"), new BigDecimal("100000"), new BigDecimal("900000")),
                new HistorialPagosEvaluacion("EMP-001", 0, true, false),
                new VerificacionLegalEvaluacion("EMP-001", false, 0, false));
    }
}

class ReglaAltaSolicitudVsVentasTest {

    @Test
    void evaluarConMontoMayorA8VecesVentasDebeAplicar() {
        ReglaAltaSolicitudVsVentas regla = new ReglaAltaSolicitudVsVentas();
        ContextoEvaluacion contexto = new ContextoEvaluacion(
                new SolicitudEvaluacion("EMP-001", new BigDecimal("900000"), ProductoFinanciero.LINEA_OPERATIVA, LocalDate.now()),
                new EmpresaEvaluacion("EMP-001", "Test", LocalDate.of(2020, 1, 1), "RFC"),
                new DatosContablesEvaluacion("EMP-001", new BigDecimal("100000"), new BigDecimal("100000"), new BigDecimal("900000")),
                new HistorialPagosEvaluacion("EMP-001", 0, true, false),
                new VerificacionLegalEvaluacion("EMP-001", false, 0, false));

        ResultadoRegla resultado = regla.evaluar(contexto);

        assertTrue(resultado.isAplico());
        assertEquals(NivelRiesgo.ALTO, resultado.getNivelRiesgoPropuesto());
    }

    @Test
    void evaluarConMontoMenorA8VecesVentasNoDebeAplicar() {
        ReglaAltaSolicitudVsVentas regla = new ReglaAltaSolicitudVsVentas();
        ContextoEvaluacion contexto = new ContextoEvaluacion(
                new SolicitudEvaluacion("EMP-001", new BigDecimal("100000"), ProductoFinanciero.LINEA_OPERATIVA, LocalDate.now()),
                new EmpresaEvaluacion("EMP-001", "Test", LocalDate.of(2020, 1, 1), "RFC"),
                new DatosContablesEvaluacion("EMP-001", new BigDecimal("100000"), new BigDecimal("100000"), new BigDecimal("900000")),
                new HistorialPagosEvaluacion("EMP-001", 0, true, false),
                new VerificacionLegalEvaluacion("EMP-001", false, 0, false));

        ResultadoRegla resultado = regla.evaluar(contexto);

        assertFalse(resultado.isAplico());
    }

    @Test
    void evaluarConDatosNulosNoDebeAplicar() {
        ReglaAltaSolicitudVsVentas regla = new ReglaAltaSolicitudVsVentas();
        ContextoEvaluacion contexto = new ContextoEvaluacion(
                new SolicitudEvaluacion("EMP-001", null, ProductoFinanciero.LINEA_OPERATIVA, LocalDate.now()),
                new EmpresaEvaluacion("EMP-001", "Test", LocalDate.of(2020, 1, 1), "RFC"),
                new DatosContablesEvaluacion("EMP-001", null, new BigDecimal("100000"), new BigDecimal("900000")),
                new HistorialPagosEvaluacion("EMP-001", 0, true, false),
                new VerificacionLegalEvaluacion("EMP-001", false, 0, false));

        ResultadoRegla resultado = regla.evaluar(contexto);

        assertFalse(resultado.isAplico());
    }
}
