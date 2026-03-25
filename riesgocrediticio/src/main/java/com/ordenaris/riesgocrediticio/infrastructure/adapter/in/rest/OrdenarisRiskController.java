package com.ordenaris.riesgocrediticio.infrastructure.adapter.in.rest;

import com.ordenaris.riesgocrediticio.domain.model.EvaluacionResponseDTO;
import com.ordenaris.riesgocrediticio.domain.model.SolicitudEvaluacion;
import com.ordenaris.riesgocrediticio.domain.port.in.EvaluarRiesgoPort;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/v1/riesgo")
@RequiredArgsConstructor
public class OrdenarisRiskController {

    private final EvaluarRiesgoPort riskService;

    @PostMapping("/evaluar")
    public ResponseEntity<EvaluacionResponseDTO> evaluarRiesgo(
            @Valid @RequestBody SolicitudEvaluacion solicitud) {

        EvaluacionResponseDTO resultado = riskService.evaluar(solicitud);
        return ResponseEntity.ok(resultado);
    }
}