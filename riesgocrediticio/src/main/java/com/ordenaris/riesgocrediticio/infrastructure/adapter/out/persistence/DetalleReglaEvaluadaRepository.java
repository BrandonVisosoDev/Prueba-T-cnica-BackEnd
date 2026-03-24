package com.ordenaris.riesgocrediticio.infrastructure.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DetalleReglaEvaluadaRepository extends JpaRepository<DetalleReglaEvaluada, Long> {
}