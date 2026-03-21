package com.ordenaris.riesgocrediticio.repository;

import com.ordenaris.riesgocrediticio.entity.HistorialPagos;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface HistorialPagosRepository extends JpaRepository<HistorialPagos, Long> {
    Optional<HistorialPagos> findByEmpresaId(String empresaId);
}