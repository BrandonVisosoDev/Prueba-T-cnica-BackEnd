package com.ordenaris.riesgocrediticio.repository;

import com.ordenaris.riesgocrediticio.entity.Empresa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EmpresaRepository extends JpaRepository<Empresa, String> {
}