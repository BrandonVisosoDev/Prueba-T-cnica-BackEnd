package com.ordenaris.riesgocrediticio.dto;

import com.ordenaris.riesgocrediticio.entity.DatosContables;
import com.ordenaris.riesgocrediticio.entity.Empresa;
import com.ordenaris.riesgocrediticio.entity.HistorialPagos;
import com.ordenaris.riesgocrediticio.entity.VerificacionLegal;
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