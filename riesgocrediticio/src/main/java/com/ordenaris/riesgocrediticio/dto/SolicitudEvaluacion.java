package com.ordenaris.riesgocrediticio.dto;

import com.ordenaris.riesgocrediticio.enums.ProductoFinanciero;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SolicitudEvaluacion {

    @NotBlank(message = "El ID de la empresa no puede estar vacío")
    private String empresaId;

    @NotNull(message = "El monto solicitado es obligatorio")
    @DecimalMin(value = "0.01", message = "El monto solicitado debe ser mayor a 0")
    private BigDecimal montoSolicitado;

    @NotNull(message = "El producto financiero es obligatorio")
    private ProductoFinanciero productoFinanciero;

    @NotNull(message = "La fecha de solicitud es obligatoria")
    private LocalDate fechaSolicitud;
}