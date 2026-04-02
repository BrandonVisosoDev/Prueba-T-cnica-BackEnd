package com.ordenaris.riesgocrediticio.infrastructure.adapter.in.rest;

import com.ordenaris.riesgocrediticio.domain.model.ResultadoRiesgo;
import com.ordenaris.riesgocrediticio.domain.model.SolicitudEvaluacion;
import com.ordenaris.riesgocrediticio.domain.port.in.EvaluarRiesgoPort;
import com.ordenaris.riesgocrediticio.infrastructure.adapter.in.rest.dto.EvaluacionResponseDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/v1/riesgo")
@RequiredArgsConstructor
public class OrdenarisRiskController {

    private final EvaluarRiesgoPort riskService;

    @PostMapping("/evaluar")
    public ResponseEntity<EvaluacionResponseDTO> evaluarRiesgo(
            @Valid @RequestBody SolicitudEvaluacion solicitud) {

        log.info(">> [CONTROLLER] Solicitud recibida | EmpresaId: {}", solicitud.getEmpresaId());

        // 1. Llamamos al dominio — nos devuelve un objeto de dominio puro
        ResultadoRiesgo resultado = riskService.evaluar(solicitud);

        // 2. Mapeamos al DTO de presentación — aquí vive esta responsabilidad
        EvaluacionResponseDTO respuesta = mapearADTO(resultado);

        log.info(">> [CONTROLLER] Respuesta lista | EmpresaId: {} | Nivel: {}",
                solicitud.getEmpresaId(), respuesta.getNivelRiesgo());

        return ResponseEntity.ok(respuesta);
    }

    //  Mapea el objeto de dominio al DTO que ve el cliente
    private EvaluacionResponseDTO mapearADTO(ResultadoRiesgo resultado) {
        List<EvaluacionResponseDTO.DetalleReglaDTO> detalles = resultado.getDetallesReglas()
                .stream()
                .map(d -> EvaluacionResponseDTO.DetalleReglaDTO.builder()
                        .nombreRegla(d.getNombreRegla())
                        .resultado(d.getDetalle())
                        .build())
                .collect(Collectors.toList());

        return EvaluacionResponseDTO.builder()
                .empresaId(resultado.getEmpresaId())
                .nivelRiesgo(resultado.getNivelRiesgo())
                .motivoFinal(resultado.getMotivoFinal())
                .fechaEvaluacion(resultado.getFechaEvaluacion())
                .detallesReglas(detalles)
                .build();
    }
}