package com.ordenaris.riesgocrediticio.domain.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VerificacionLegalEvaluacion {

    private String empresaId;
    private Boolean juicioMercantilEnCurso;
    private Integer numeroDemandasActivas;
    private Boolean embargoActivo;
}
