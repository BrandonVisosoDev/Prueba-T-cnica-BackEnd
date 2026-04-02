package com.ordenaris.riesgocrediticio.infrastructure.adapter.in.rest;

import com.ordenaris.riesgocrediticio.domain.model.ResultadoRiesgo;
import com.ordenaris.riesgocrediticio.domain.model.SolicitudEvaluacion;
import com.ordenaris.riesgocrediticio.domain.model.enums.NivelRiesgo;
import com.ordenaris.riesgocrediticio.domain.model.enums.ProductoFinanciero;
import com.ordenaris.riesgocrediticio.domain.port.in.EvaluarRiesgoPort;
import com.ordenaris.riesgocrediticio.infrastructure.adapter.in.rest.dto.EvaluacionResponseDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrdenarisRiskControllerTest {

    @Mock
    private EvaluarRiesgoPort riskService;

    @InjectMocks
    private OrdenarisRiskController controller;

    @Test
    void evaluarRiesgoDebeRetornarDTOConHttpOk() {
        SolicitudEvaluacion solicitud = new SolicitudEvaluacion(
                "EMP-001",
                new BigDecimal("150000"),
                ProductoFinanciero.LINEA_OPERATIVA,
                LocalDate.of(2026, 3, 24)
        );

        ResultadoRiesgo resultado = ResultadoRiesgo.builder()
                .empresaId("EMP-001")
                .nivelRiesgo(NivelRiesgo.BAJO)
                .motivoFinal("Evaluación completada con éxito. Cliente apto.")
                .fechaEvaluacion(LocalDateTime.now())
                .detallesReglas(new ArrayList<>())
                .build();

        when(riskService.evaluar(any())).thenReturn(resultado);

        ResponseEntity<EvaluacionResponseDTO> response = controller.evaluarRiesgo(solicitud);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("EMP-001", response.getBody().getEmpresaId());
        assertEquals(NivelRiesgo.BAJO, response.getBody().getNivelRiesgo());
        assertEquals("Evaluación completada con éxito. Cliente apto.", response.getBody().getMotivoFinal());
        verify(riskService).evaluar(any());
    }

    @Test
    void evaluarRiesgoDebeMapearDetallesReglas() {
        SolicitudEvaluacion solicitud = new SolicitudEvaluacion(
                "EMP-001",
                new BigDecimal("150000"),
                ProductoFinanciero.LINEA_OPERATIVA,
                LocalDate.of(2026, 3, 24)
        );

        ResultadoRiesgo resultado = ResultadoRiesgo.builder()
                .empresaId("EMP-001")
                .nivelRiesgo(NivelRiesgo.ALTO)
                .motivoFinal("Riesgo detectado")
                .fechaEvaluacion(LocalDateTime.now())
                .detallesReglas(Collections.emptyList())
                .build();

        when(riskService.evaluar(any())).thenReturn(resultado);

        ResponseEntity<EvaluacionResponseDTO> response = controller.evaluarRiesgo(solicitud);

        assertNotNull(response.getBody());
        assertNotNull(response.getBody().getDetallesReglas());
        assertEquals(0, response.getBody().getDetallesReglas().size());
    }

    @Test
    void evaluarRiesgoDebePreservarFechaEvaluacion() {
        SolicitudEvaluacion solicitud = new SolicitudEvaluacion(
                "EMP-001",
                new BigDecimal("150000"),
                ProductoFinanciero.LINEA_OPERATIVA,
                LocalDate.of(2026, 3, 24)
        );

        LocalDateTime fechaEvaluacion = LocalDateTime.of(2026, 3, 24, 10, 30, 0);
        ResultadoRiesgo resultado = ResultadoRiesgo.builder()
                .empresaId("EMP-001")
                .nivelRiesgo(NivelRiesgo.MEDIO)
                .motivoFinal("Evaluación completada")
                .fechaEvaluacion(fechaEvaluacion)
                .detallesReglas(Collections.emptyList())
                .build();

        when(riskService.evaluar(any())).thenReturn(resultado);

        ResponseEntity<EvaluacionResponseDTO> response = controller.evaluarRiesgo(solicitud);

        assertNotNull(response.getBody());
        assertEquals(fechaEvaluacion, response.getBody().getFechaEvaluacion());
    }
}

