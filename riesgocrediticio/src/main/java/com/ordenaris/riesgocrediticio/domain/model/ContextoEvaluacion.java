package com.ordenaris.riesgocrediticio.domain.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ContextoEvaluacion {
    private SolicitudEvaluacion solicitud;
    private EmpresaEvaluacion empresa;
    private DatosContablesEvaluacion datosContables;
    private HistorialPagosEvaluacion historialPagos;
    private VerificacionLegalEvaluacion verificacionLegal;
}
