package com.ordenaris.riesgocrediticio.infrastructure.adapter.out.persistence;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "datos_contables")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DatosContables {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // Llave primaria autogenerada

    @Column(name = "empresa_id", unique = true)
    private String empresaId; // Para saber a qué empresa pertenecen estos datos

    private BigDecimal ventasPromedioMensuales; // Necesario para la regla de "Alta Solicitud vs Ventas"

    private BigDecimal pasivos;

    private BigDecimal activos;

}