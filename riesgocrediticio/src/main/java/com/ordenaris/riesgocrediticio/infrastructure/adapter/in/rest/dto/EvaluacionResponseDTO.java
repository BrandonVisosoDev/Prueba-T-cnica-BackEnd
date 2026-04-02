package com.ordenaris.riesgocrediticio.infrastructure.adapter.in.rest.dto;

import com.ordenaris.riesgocrediticio.domain.model.enums.NivelRiesgo;
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
public class EvaluacionResponseDTO {

    private String empresaId;
    private NivelRiesgo nivelRiesgo;
    private String motivoFinal;
    private LocalDateTime fechaEvaluacion;
    private List<DetalleReglaDTO> detallesReglas;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DetalleReglaDTO {
        private String nombreRegla;
        private String resultado;
    }
}
