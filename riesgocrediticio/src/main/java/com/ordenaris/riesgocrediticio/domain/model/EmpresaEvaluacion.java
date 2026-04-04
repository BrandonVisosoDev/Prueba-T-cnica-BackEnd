package com.ordenaris.riesgocrediticio.domain.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmpresaEvaluacion {

    private String id;
    private String nombre;
    private LocalDate fechaConstitucion;
    private String rfc;
}
