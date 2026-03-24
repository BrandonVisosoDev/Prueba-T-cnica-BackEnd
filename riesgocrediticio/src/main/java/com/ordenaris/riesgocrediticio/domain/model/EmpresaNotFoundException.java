package com.ordenaris.riesgocrediticio.domain.model;

public class EmpresaNotFoundException extends RuntimeException {

    public EmpresaNotFoundException(String empresaId) {
        super("La empresa con ID '" + empresaId + "' no está registrada en el sistema.");
    }
}