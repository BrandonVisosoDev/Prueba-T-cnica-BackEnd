package com.ordenaris.riesgocrediticio.repository;

import com.ordenaris.riesgocrediticio.entity.DatosContables;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DatosContablesRepository extends JpaRepository<DatosContables, Long> {

}