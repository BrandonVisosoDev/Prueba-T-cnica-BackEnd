package com.ordenaris.riesgocrediticio.application;

import com.ordenaris.riesgocrediticio.domain.engine.OrdenarisRiskEngine;
import com.ordenaris.riesgocrediticio.domain.model.DatosContablesEvaluacion;
import com.ordenaris.riesgocrediticio.domain.model.EmpresaEvaluacion;
import com.ordenaris.riesgocrediticio.domain.model.EmpresaNotFoundException;
import com.ordenaris.riesgocrediticio.domain.model.HistorialPagosEvaluacion;
import com.ordenaris.riesgocrediticio.domain.model.ResultadoRiesgo;
import com.ordenaris.riesgocrediticio.domain.model.RiesgoEvaluacionException;
import com.ordenaris.riesgocrediticio.domain.model.SolicitudEvaluacion;
import com.ordenaris.riesgocrediticio.domain.model.VerificacionLegalEvaluacion;
import com.ordenaris.riesgocrediticio.domain.model.enums.NivelRiesgo;
import com.ordenaris.riesgocrediticio.domain.model.enums.ProductoFinanciero;
import com.ordenaris.riesgocrediticio.domain.port.out.DatosContablesProvider;
import com.ordenaris.riesgocrediticio.domain.port.out.EmpresaProvider;
import com.ordenaris.riesgocrediticio.domain.port.out.HistorialPagosProvider;
import com.ordenaris.riesgocrediticio.domain.port.out.ResultadoEvaluacionProvider;
import com.ordenaris.riesgocrediticio.domain.port.out.VerificacionLegalProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrdenarisRiskServiceTest {

    @Mock
    private EmpresaProvider empresaProvider;

    @Mock
    private ResultadoEvaluacionProvider resultadoEvaluacionProvider;

    @Mock
    private DatosContablesProvider datosContablesProvider;

    @Mock
    private HistorialPagosProvider historialPagosProvider;

    @Mock
    private VerificacionLegalProvider verificacionLegalProvider;

    @Mock
    private OrdenarisRiskEngine motorReglas;

    @InjectMocks
    private OrdenarisRiskService service;

    @Test
    void evaluarDebeGuardarYRetornarResultadoCuandoTodoSaleBien() {
        SolicitudEvaluacion solicitud = crearSolicitud();
        EmpresaEvaluacion empresa = new EmpresaEvaluacion("EMP-001", "Ordenaris", LocalDate.of(2020, 1, 15), "ORD010101AA1");
        DatosContablesEvaluacion contables = new DatosContablesEvaluacion("EMP-001", new BigDecimal("500000"), new BigDecimal("100000"), new BigDecimal("900000"));
        HistorialPagosEvaluacion pagos = new HistorialPagosEvaluacion("EMP-001", 0, true, false);
        VerificacionLegalEvaluacion legal = new VerificacionLegalEvaluacion("EMP-001", false, 0, false);

        ResultadoRiesgo resultadoMotor = ResultadoRiesgo.builder()
                .empresaId("EMP-001")
                .nivelRiesgo(NivelRiesgo.BAJO)
                .motivoFinal("Evaluacion completada con exito. Cliente apto.")
                .fechaEvaluacion(LocalDateTime.now())
                .detallesReglas(Collections.emptyList())
                .build();

        when(empresaProvider.obtenerEmpresaPorId("EMP-001")).thenReturn(Optional.of(empresa));
        when(datosContablesProvider.obtenerDatosContables("EMP-001")).thenReturn(contables);
        when(historialPagosProvider.obtenerHistorialPagos("EMP-001")).thenReturn(pagos);
        when(verificacionLegalProvider.obtenerVerificacionLegal("EMP-001")).thenReturn(legal);
        when(motorReglas.evaluarRiesgo(any())).thenReturn(resultadoMotor);
        when(resultadoEvaluacionProvider.guardar(resultadoMotor)).thenReturn(resultadoMotor);

        ResultadoRiesgo resultado = service.evaluar(solicitud);

        assertNotNull(resultado);
        assertEquals("EMP-001", resultado.getEmpresaId());
        assertEquals(NivelRiesgo.BAJO, resultado.getNivelRiesgo());
        verify(empresaProvider).obtenerEmpresaPorId("EMP-001");
        verify(datosContablesProvider).obtenerDatosContables("EMP-001");
        verify(historialPagosProvider).obtenerHistorialPagos("EMP-001");
        verify(verificacionLegalProvider).obtenerVerificacionLegal("EMP-001");
        verify(motorReglas).evaluarRiesgo(any());
        verify(resultadoEvaluacionProvider).guardar(resultadoMotor);
    }

    @Test
    void evaluarDebePropagarEmpresaNotFoundExceptionCuandoLaEmpresaNoExiste() {
        SolicitudEvaluacion solicitud = crearSolicitud();
        when(empresaProvider.obtenerEmpresaPorId("EMP-001")).thenReturn(Optional.empty());

        EmpresaNotFoundException exception = assertThrows(EmpresaNotFoundException.class, () -> service.evaluar(solicitud));

        assertTrue(exception.getMessage().contains("EMP-001"));
        verify(empresaProvider).obtenerEmpresaPorId("EMP-001");
        verify(datosContablesProvider, never()).obtenerDatosContables(any());
        verify(historialPagosProvider, never()).obtenerHistorialPagos(any());
        verify(verificacionLegalProvider, never()).obtenerVerificacionLegal(any());
        verify(motorReglas, never()).evaluarRiesgo(any());
        verify(resultadoEvaluacionProvider, never()).guardar(any());
    }

    @Test
    void evaluarDebeEnvolverExcepcionesGenericasEnRiesgoEvaluacionException() {
        SolicitudEvaluacion solicitud = crearSolicitud();
        EmpresaEvaluacion empresa = new EmpresaEvaluacion("EMP-001", "Ordenaris", LocalDate.of(2020, 1, 15), "ORD010101AA1");

        when(empresaProvider.obtenerEmpresaPorId("EMP-001")).thenReturn(Optional.of(empresa));
        when(datosContablesProvider.obtenerDatosContables("EMP-001")).thenThrow(new IllegalStateException("Proveedor no disponible"));

        RiesgoEvaluacionException exception = assertThrows(RiesgoEvaluacionException.class, () -> service.evaluar(solicitud));

        assertTrue(exception.getMessage().contains("EMP-001"));
        assertNotNull(exception.getCause());
        assertEquals(IllegalStateException.class, exception.getCause().getClass());
        verify(empresaProvider).obtenerEmpresaPorId("EMP-001");
        verify(datosContablesProvider).obtenerDatosContables("EMP-001");
        verify(historialPagosProvider, never()).obtenerHistorialPagos(any());
        verify(verificacionLegalProvider, never()).obtenerVerificacionLegal(any());
        verify(motorReglas, never()).evaluarRiesgo(any());
        verify(resultadoEvaluacionProvider, never()).guardar(any());
    }

    private SolicitudEvaluacion crearSolicitud() {
        return new SolicitudEvaluacion(
                "EMP-001",
                new BigDecimal("150000"),
                ProductoFinanciero.LINEA_OPERATIVA,
                LocalDate.of(2026, 3, 24)
        );
    }
}
