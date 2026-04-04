package com.ordenaris.riesgocrediticio.infrastructure.adapter.in.rest;

import com.ordenaris.riesgocrediticio.domain.model.ResultadoRiesgo;
import com.ordenaris.riesgocrediticio.domain.model.ResultadoRegla;
import com.ordenaris.riesgocrediticio.domain.model.enums.NivelRiesgo;
import com.ordenaris.riesgocrediticio.domain.port.in.EvaluarRiesgoPort;
import com.ordenaris.riesgocrediticio.infrastructure.adapter.in.rest.dto.EvaluacionRequestDTO;
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
import java.util.List;

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
        EvaluacionRequestDTO request = crearRequest();
        ResultadoRiesgo resultado = ResultadoRiesgo.builder()
                .empresaId("EMP-001")
                .nivelRiesgo(NivelRiesgo.BAJO)
                .motivoFinal("Evaluacion completada con exito. Cliente apto.")
                .fechaEvaluacion(LocalDateTime.now())
                .detallesReglas(List.of())
                .build();

        when(riskService.evaluar(any())).thenReturn(resultado);

        ResponseEntity<EvaluacionResponseDTO> response = controller.evaluarRiesgo(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("EMP-001", response.getBody().getEmpresaId());
        assertEquals(NivelRiesgo.BAJO, response.getBody().getNivelRiesgo());
        verify(riskService).evaluar(any());
    }

    @Test
    void evaluarRiesgoDebeMapearDetallesReglas() {
        EvaluacionRequestDTO request = crearRequest();
        ResultadoRiesgo resultado = ResultadoRiesgo.builder()
                .empresaId("EMP-001")
                .nivelRiesgo(NivelRiesgo.ALTO)
                .motivoFinal("Riesgo detectado")
                .fechaEvaluacion(LocalDateTime.now())
                .detallesReglas(List.of(new ResultadoRegla("Regla 1", true, NivelRiesgo.ALTO, null, "ALERTA: detalle")))
                .build();

        when(riskService.evaluar(any())).thenReturn(resultado);

        ResponseEntity<EvaluacionResponseDTO> response = controller.evaluarRiesgo(request);

        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().getDetallesReglas().size());
        assertEquals("Regla 1", response.getBody().getDetallesReglas().get(0).getNombreRegla());
        assertEquals("ALERTA: detalle", response.getBody().getDetallesReglas().get(0).getResultado());
    }

    @Test
    void evaluarRiesgoDebePreservarFechaEvaluacion() {
        EvaluacionRequestDTO request = crearRequest();
        LocalDateTime fechaEvaluacion = LocalDateTime.of(2026, 3, 24, 10, 30, 0);
        ResultadoRiesgo resultado = ResultadoRiesgo.builder()
                .empresaId("EMP-001")
                .nivelRiesgo(NivelRiesgo.MEDIO)
                .motivoFinal("Evaluacion completada")
                .fechaEvaluacion(fechaEvaluacion)
                .detallesReglas(List.of())
                .build();

        when(riskService.evaluar(any())).thenReturn(resultado);

        ResponseEntity<EvaluacionResponseDTO> response = controller.evaluarRiesgo(request);

        assertNotNull(response.getBody());
        assertEquals(fechaEvaluacion, response.getBody().getFechaEvaluacion());
    }

    private EvaluacionRequestDTO crearRequest() {
        return new EvaluacionRequestDTO(
                "EMP-001",
                new BigDecimal("150000"),
                com.ordenaris.riesgocrediticio.domain.model.enums.ProductoFinanciero.LINEA_OPERATIVA,
                LocalDate.of(2026, 3, 24)
        );
    }
}
