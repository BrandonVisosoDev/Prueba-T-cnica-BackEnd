package com.ordenaris.riesgocrediticio.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "historial_pagos")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class HistorialPagos {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "empresa_id", unique = true)
    private String empresaId;

    // Para la regla "Deuda Activa"
    private Integer diasDeudaVencida;

    // Para la regla "Historial Excelente"
    private Boolean pagosEnTiempoUltimos12Meses;

    // Para la regla "Historial Excelente"
    private Boolean tieneRefinanciamiento;

}