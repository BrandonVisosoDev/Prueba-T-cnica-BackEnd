package com.ordenaris.riesgocrediticio.infrastructure.adapter.in.rest.dto;

import com.ordenaris.riesgocrediticio.domain.model.enums.NivelRiesgo;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "DTO de salida para la evaluacion de riesgo")
public class EvaluacionResponseDTO {

    @Schema(example = "EMPRESA001")
    private String empresaId;

    @Schema(example = "BAJO")
    private NivelRiesgo nivelRiesgo;

    @Schema(example = "Evaluacion completada con exito. Cliente apto.")
    private String motivoFinal;

    @Schema(example = "2026-03-24T10:30:00")
    private LocalDateTime fechaEvaluacion;

    private List<DetalleReglaDTO> detallesReglas;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Detalle de cada regla evaluada")
    public static class DetalleReglaDTO {
        @Schema(example = "Empresa Nueva")
        private String nombreRegla;

        @Schema(example = "OK")
        private String resultado;
    }
}
