package com.ordenaris.riesgocrediticio.infrastructure.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface VerificacionLegalRepository extends JpaRepository<VerificacionLegal, Long> {
    Optional<VerificacionLegal> findByEmpresaId(String empresaId);
}