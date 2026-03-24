package com.ordenaris.riesgocrediticio.application;

import com.ordenaris.riesgocrediticio.domain.engine.OrdenarisRiskEngine;
import com.ordenaris.riesgocrediticio.domain.model.EmpresaNotFoundException;
import com.ordenaris.riesgocrediticio.domain.model.RiesgoEvaluacionException;
import com.ordenaris.riesgocrediticio.domain.model.SolicitudEvaluacion;
import com.ordenaris.riesgocrediticio.domain.model.enums.NivelRiesgo;
import com.ordenaris.riesgocrediticio.domain.model.enums.ProductoFinanciero;
import com.ordenaris.riesgocrediticio.domain.port.out.DatosContablesProvider;
import com.ordenaris.riesgocrediticio.domain.port.out.HistorialPagosProvider;
import com.ordenaris.riesgocrediticio.domain.port.out.VerificacionLegalProvider;
import com.ordenaris.riesgocrediticio.infrastructure.adapter.out.persistence.DatosContables;
import com.ordenaris.riesgocrediticio.infrastructure.adapter.out.persistence.Empresa;
import com.ordenaris.riesgocrediticio.infrastructure.adapter.out.persistence.EmpresaRepository;
import com.ordenaris.riesgocrediticio.infrastructure.adapter.out.persistence.HistorialPagos;
import com.ordenaris.riesgocrediticio.infrastructure.adapter.out.persistence.ResultadoEvaluacion;
import com.ordenaris.riesgocrediticio.infrastructure.adapter.out.persistence.ResultadoEvaluacionRepository;
import com.ordenaris.riesgocrediticio.infrastructure.adapter.out.persistence.VerificacionLegal;
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
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrdenarisRiskServiceTest {

    @Mock
    private EmpresaRepository empresaRepo;

    @Mock
    private ResultadoEvaluacionRepository resultadoRepo;

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
        Empresa empresa = new Empresa("EMP-001", "Ordenaris", LocalDate.of(2020, 1, 15), "ORD010101AA1");
        DatosContables contables = new DatosContables(1L, "EMP-001", new BigDecimal("500000"), new BigDecimal("100000"), new BigDecimal("900000"));
        HistorialPagos pagos = new HistorialPagos(1L, "EMP-001", 0, true, false);
        VerificacionLegal legal = new VerificacionLegal(1L, "EMP-001", false, 0, false);

        ResultadoEvaluacion resultadoMotor = new ResultadoEvaluacion();
        resultadoMotor.setEmpresaId("EMP-001");
        resultadoMotor.setNivelRiesgo(NivelRiesgo.BAJO);
        resultadoMotor.setMotivoFinal("Evaluacion completada con exito. Cliente apto.");
        resultadoMotor.setFechaEvaluacion(LocalDateTime.now());
        resultadoMotor.setDetallesReglas(Collections.emptyList());

        ResultadoEvaluacion resultadoGuardado = new ResultadoEvaluacion();
        resultadoGuardado.setId(99L);
        resultadoGuardado.setEmpresaId("EMP-001");
        resultadoGuardado.setNivelRiesgo(NivelRiesgo.BAJO);
        resultadoGuardado.setMotivoFinal(resultadoMotor.getMotivoFinal());
        resultadoGuardado.setFechaEvaluacion(resultadoMotor.getFechaEvaluacion());
        resultadoGuardado.setDetallesReglas(Collections.emptyList());

        when(empresaRepo.findById("EMP-001")).thenReturn(Optional.of(empresa));
        when(datosContablesProvider.obtenerDatosContables("EMP-001")).thenReturn(contables);
        when(historialPagosProvider.obtenerHistorialPagos("EMP-001")).thenReturn(pagos);
        when(verificacionLegalProvider.obtenerVerificacionLegal("EMP-001")).thenReturn(legal);
        when(motorReglas.evaluarRiesgo(any())).thenReturn(resultadoMotor);
        when(resultadoRepo.save(resultadoMotor)).thenReturn(resultadoGuardado);

        ResultadoEvaluacion resultado = service.evaluar(solicitud);

        assertSame(resultadoGuardado, resultado);
        assertEquals(99L, resultado.getId());
        assertEquals(NivelRiesgo.BAJO, resultado.getNivelRiesgo());
        verify(empresaRepo).findById("EMP-001");
        verify(datosContablesProvider).obtenerDatosContables("EMP-001");
        verify(historialPagosProvider).obtenerHistorialPagos("EMP-001");
        verify(verificacionLegalProvider).obtenerVerificacionLegal("EMP-001");
        verify(motorReglas).evaluarRiesgo(any());
        verify(resultadoRepo).save(resultadoMotor);
    }

    @Test
    void evaluarDebePropagarEmpresaNotFoundExceptionCuandoLaEmpresaNoExiste() {
        SolicitudEvaluacion solicitud = crearSolicitud();
        when(empresaRepo.findById("EMP-001")).thenReturn(Optional.empty());

        EmpresaNotFoundException exception = assertThrows(EmpresaNotFoundException.class, () -> service.evaluar(solicitud));

        assertTrue(exception.getMessage().contains("EMP-001"));
        verify(empresaRepo).findById("EMP-001");
        verify(datosContablesProvider, never()).obtenerDatosContables(any());
        verify(historialPagosProvider, never()).obtenerHistorialPagos(any());
        verify(verificacionLegalProvider, never()).obtenerVerificacionLegal(any());
        verify(motorReglas, never()).evaluarRiesgo(any());
        verify(resultadoRepo, never()).save(any());
    }

    @Test
    void evaluarDebeEnvolverExcepcionesGenericasEnRiesgoEvaluacionException() {
        SolicitudEvaluacion solicitud = crearSolicitud();
        Empresa empresa = new Empresa("EMP-001", "Ordenaris", LocalDate.of(2020, 1, 15), "ORD010101AA1");

        when(empresaRepo.findById("EMP-001")).thenReturn(Optional.of(empresa));
        when(datosContablesProvider.obtenerDatosContables("EMP-001")).thenThrow(new IllegalStateException("Proveedor no disponible"));

        RiesgoEvaluacionException exception = assertThrows(RiesgoEvaluacionException.class, () -> service.evaluar(solicitud));

        assertTrue(exception.getMessage().contains("EMP-001"));
        assertNotNull(exception.getCause());
        assertEquals(IllegalStateException.class, exception.getCause().getClass());
        verify(empresaRepo).findById("EMP-001");
        verify(datosContablesProvider).obtenerDatosContables("EMP-001");
        verify(historialPagosProvider, never()).obtenerHistorialPagos(any());
        verify(verificacionLegalProvider, never()).obtenerVerificacionLegal(any());
        verify(motorReglas, never()).evaluarRiesgo(any());
        verify(resultadoRepo, never()).save(any());
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
