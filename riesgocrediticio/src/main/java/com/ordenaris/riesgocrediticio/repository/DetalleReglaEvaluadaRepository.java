package com.ordenaris.riesgocrediticio.repository;

import com.ordenaris.riesgocrediticio.entity.DetalleReglaEvaluada;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DetalleReglaEvaluadaRepository extends JpaRepository<DetalleReglaEvaluada, Long> {
}