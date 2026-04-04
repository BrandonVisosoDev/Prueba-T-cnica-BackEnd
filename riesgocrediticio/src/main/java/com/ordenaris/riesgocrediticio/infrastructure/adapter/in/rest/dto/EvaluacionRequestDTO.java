package com.ordenaris.riesgocrediticio.infrastructure.adapter.in.rest.dto;

import com.ordenaris.riesgocrediticio.domain.model.enums.ProductoFinanciero;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Solicitud REST para evaluar el riesgo crediticio de una empresa")
public class EvaluacionRequestDTO {

    @NotBlank(message = "El ID de la empresa no puede estar vacio")
    @Schema(example = "EMPRESA001")
    private String empresaId;

    @NotNull(message = "El monto solicitado es obligatorio")
    @DecimalMin(value = "0.01", message = "El monto solicitado debe ser mayor a 0")
    @Schema(example = "150000")
    private BigDecimal montoSolicitado;

    @NotNull(message = "El producto financiero es obligatorio")
    @Schema(example = "LINEA_OPERATIVA")
    private ProductoFinanciero productoFinanciero;

    @NotNull(message = "La fecha de solicitud es obligatoria")
    @Schema(example = "2026-03-24")
    private LocalDate fechaSolicitud;
}
