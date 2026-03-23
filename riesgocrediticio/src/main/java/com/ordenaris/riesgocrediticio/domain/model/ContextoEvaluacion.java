package com.ordenaris.riesgocrediticio.domain.model;

import com.ordenaris.riesgocrediticio.infrastructure.adapter.out.persistence.DatosContables;
import com.ordenaris.riesgocrediticio.infrastructure.adapter.out.persistence.Empresa;
import com.ordenaris.riesgocrediticio.infrastructure.adapter.out.persistence.HistorialPagos;
import com.ordenaris.riesgocrediticio.infrastructure.adapter.out.persistence.VerificacionLegal;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ContextoEvaluacion {
    private SolicitudEvaluacion solicitud;
    private Empresa empresa;
    private DatosContables datosContables;
    private HistorialPagos historialPagos;
    private VerificacionLegal verificacionLegal;
}