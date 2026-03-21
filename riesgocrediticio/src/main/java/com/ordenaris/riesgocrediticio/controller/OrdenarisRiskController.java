package com.ordenaris.riesgocrediticio.controller;

import com.ordenaris.riesgocrediticio.dto.SolicitudEvaluacion;
import com.ordenaris.riesgocrediticio.entity.ResultadoEvaluacion;
import com.ordenaris.riesgocrediticio.service.OrdenarisRiskService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/riesgo")
@RequiredArgsConstructor
public class OrdenarisRiskController {

    private final OrdenarisRiskService riskService;

    @PostMapping("/evaluar")
    public ResponseEntity<ResultadoEvaluacion> evaluarRiesgo(@Valid @RequestBody SolicitudEvaluacion solicitud) {

        // El controlador solo recibe la petición y se la pasa al Service.
        // Nada de lógica de negocio aquí (¡Otra buena práctica para tu entrevista!)
        ResultadoEvaluacion resultado = riskService.procesarSolicitud(solicitud);

        return ResponseEntity.ok(resultado);
    }
}