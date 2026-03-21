package com.ordenaris.riesgocrediticio.repository;

import com.ordenaris.riesgocrediticio.entity.VerificacionLegal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface VerificacionLegalRepository extends JpaRepository<VerificacionLegal, Long> {
    Optional<VerificacionLegal> findByEmpresaId(String empresaId);
}