package com.ordenaris.riesgocrediticio.infrastructure.adapter.in.rest;

import com.ordenaris.riesgocrediticio.domain.model.ResultadoRiesgo;
import com.ordenaris.riesgocrediticio.domain.model.SolicitudEvaluacion;
import com.ordenaris.riesgocrediticio.domain.port.in.EvaluarRiesgoPort;
import com.ordenaris.riesgocrediticio.infrastructure.adapter.in.rest.dto.EvaluacionRequestDTO;
import com.ordenaris.riesgocrediticio.infrastructure.adapter.in.rest.dto.EvaluacionResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@Tag(name = "Riesgo Crediticio", description = "API para evaluar el riesgo crediticio de empresas")
@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/v1/riesgo")
@RequiredArgsConstructor
public class OrdenarisRiskController {

    private final EvaluarRiesgoPort riskService;

    @Operation(
            summary = "Evaluar riesgo crediticio",
            description = "Procesa una solicitud de evaluacion y responde con un DTO de salida desacoplado de JPA.")
    @ApiResponse(responseCode = "200", description = "Evaluacion completada",
            content = @Content(schema = @Schema(implementation = EvaluacionResponseDTO.class)))
    @ApiResponse(responseCode = "400", description = "Solicitud invalida")
    @ApiResponse(responseCode = "404", description = "Empresa no encontrada")
    @ApiResponse(responseCode = "422", description = "Error de negocio durante la evaluacion")
    @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    @PostMapping("/evaluar")
    public ResponseEntity<EvaluacionResponseDTO> evaluarRiesgo(
            @Valid @RequestBody EvaluacionRequestDTO request) {

        log.info("Solicitud recibida en controller. empresaId={}", request.getEmpresaId());

        ResultadoRiesgo resultado = riskService.evaluar(mapearASolicitud(request));
        EvaluacionResponseDTO respuesta = mapearADTO(resultado);

        log.info("Respuesta lista en controller. empresaId={}, nivel={}",
                respuesta.getEmpresaId(), respuesta.getNivelRiesgo());

        return ResponseEntity.ok(respuesta);
    }

    private SolicitudEvaluacion mapearASolicitud(EvaluacionRequestDTO request) {
        return new SolicitudEvaluacion(
                request.getEmpresaId(),
                request.getMontoSolicitado(),
                request.getProductoFinanciero(),
                request.getFechaSolicitud());
    }

    private EvaluacionResponseDTO mapearADTO(ResultadoRiesgo resultado) {
        List<EvaluacionResponseDTO.DetalleReglaDTO> detalles = resultado.getDetallesReglas().stream()
                .map(d -> EvaluacionResponseDTO.DetalleReglaDTO.builder()
                        .nombreRegla(d.getNombreRegla())
                        .resultado(d.getDetalle())
                        .build())
                .toList();

        return EvaluacionResponseDTO.builder()
                .empresaId(resultado.getEmpresaId())
                .nivelRiesgo(resultado.getNivelRiesgo())
                .motivoFinal(resultado.getMotivoFinal())
                .fechaEvaluacion(resultado.getFechaEvaluacion())
                .detallesReglas(detalles)
                .build();
    }
}
