package com.ordenaris.riesgocrediticio.infrastructure.adapter.out.persistence;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "verificaciones_legales")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class VerificacionLegal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "empresa_id", unique = true)
    private String empresaId;

    // Para la regla "Demanda Legal Abierta"
    private Boolean juicioMercantilEnCurso;

    // Campos extra basados en la descripción del proveedor en el PDF
    private Integer numeroDemandas;
    private Boolean tieneEmbargos;

}