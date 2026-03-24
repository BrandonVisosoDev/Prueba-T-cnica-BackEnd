package com.ordenaris.riesgocrediticio.domain.model;

import com.ordenaris.riesgocrediticio.domain.model.enums.NivelRiesgo;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ResultadoRegla {
    private String nombreRegla;
    private boolean aplico; // true si la regla se activó (ej. sí tiene deuda)
    private NivelRiesgo nivelRiesgoPropuesto; // Puede ser null si la regla solo suma puntos
    private Integer modificadorPuntos; // Para las reglas que suben o bajan un nivel (+1 o -1)
    private String detalle; // Un mensaje explicando qué pasó
}