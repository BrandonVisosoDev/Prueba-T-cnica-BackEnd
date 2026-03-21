package com.ordenaris.riesgocrediticio.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "empresas")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Empresa {

    @Id
    private String id; // Este será el empresaId (String) que menciona el PDF

    private String nombre;

    private LocalDate fechaConstitucion; // Lo usaremos para saber si tiene menos de 18 meses

}