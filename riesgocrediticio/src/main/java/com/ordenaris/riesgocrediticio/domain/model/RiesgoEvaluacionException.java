package com.ordenaris.riesgocrediticio.domain.model;

public class RiesgoEvaluacionException extends RuntimeException {

    public RiesgoEvaluacionException(String mensaje) {
        super(mensaje);
    }

    public RiesgoEvaluacionException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}