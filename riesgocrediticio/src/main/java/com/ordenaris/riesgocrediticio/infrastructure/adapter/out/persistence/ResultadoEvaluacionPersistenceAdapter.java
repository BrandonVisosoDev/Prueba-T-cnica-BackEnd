package com.ordenaris.riesgocrediticio.infrastructure.adapter.out.persistence;

import com.ordenaris.riesgocrediticio.domain.model.ResultadoRegla;
import com.ordenaris.riesgocrediticio.domain.model.ResultadoRiesgo;
import com.ordenaris.riesgocrediticio.domain.port.out.ResultadoEvaluacionProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class ResultadoEvaluacionPersistenceAdapter implements ResultadoEvaluacionProvider {

    private final ResultadoEvaluacionRepository resultadoEvaluacionRepository;

    @Override
    public ResultadoRiesgo guardar(ResultadoRiesgo resultadoRiesgo) {
        ResultadoEvaluacion entidad = new ResultadoEvaluacion();
        entidad.setEmpresaId(resultadoRiesgo.getEmpresaId());
        entidad.setNivelRiesgo(resultadoRiesgo.getNivelRiesgo());
        entidad.setMotivoFinal(resultadoRiesgo.getMotivoFinal());
        entidad.setFechaEvaluacion(resultadoRiesgo.getFechaEvaluacion());
        entidad.setDetallesReglas(crearDetalles(resultadoRiesgo, entidad));

        ResultadoEvaluacion guardado = resultadoEvaluacionRepository.save(entidad);
        return mapearADominio(guardado);
    }

    private List<DetalleReglaEvaluada> crearDetalles(ResultadoRiesgo resultadoRiesgo, ResultadoEvaluacion entidad) {
        return resultadoRiesgo.getDetallesReglas().stream()
                .map(regla -> {
                    DetalleReglaEvaluada detalle = new DetalleReglaEvaluada();
                    detalle.setResultadoEvaluacion(entidad);
                    detalle.setNombreRegla(regla.getNombreRegla());
                    detalle.setResultado(regla.isAplico() ? "ALERTA: " + regla.getDetalle() : "OK");
                    return detalle;
                })
                .toList();
    }

    private ResultadoRiesgo mapearADominio(ResultadoEvaluacion entidad) {
        List<ResultadoRegla> detalles = entidad.getDetallesReglas().stream()
                .map(detalle -> new ResultadoRegla(
                        detalle.getNombreRegla(),
                        detalle.getResultado() != null && detalle.getResultado().startsWith("ALERTA:"),
                        null,
                        null,
                        detalle.getResultado()))
                .toList();

        return ResultadoRiesgo.builder()
                .empresaId(entidad.getEmpresaId())
                .nivelRiesgo(entidad.getNivelRiesgo())
                .motivoFinal(entidad.getMotivoFinal())
                .fechaEvaluacion(entidad.getFechaEvaluacion())
                .detallesReglas(detalles)
                .build();
    }
}
