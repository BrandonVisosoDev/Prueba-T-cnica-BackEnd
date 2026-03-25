package com.ordenaris.riesgocrediticio.application;

import com.ordenaris.riesgocrediticio.domain.engine.OrdenarisRiskEngine;
import com.ordenaris.riesgocrediticio.domain.model.ContextoEvaluacion;
import com.ordenaris.riesgocrediticio.domain.model.EmpresaNotFoundException;
import com.ordenaris.riesgocrediticio.domain.model.EvaluacionResponseDTO;
import com.ordenaris.riesgocrediticio.domain.model.RiesgoEvaluacionException;
import com.ordenaris.riesgocrediticio.domain.model.SolicitudEvaluacion;
import com.ordenaris.riesgocrediticio.domain.port.in.EvaluarRiesgoPort;
import com.ordenaris.riesgocrediticio.domain.port.out.DatosContablesProvider;
import com.ordenaris.riesgocrediticio.domain.port.out.HistorialPagosProvider;
import com.ordenaris.riesgocrediticio.domain.port.out.VerificacionLegalProvider;
import com.ordenaris.riesgocrediticio.infrastructure.adapter.out.persistence.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrdenarisRiskService implements EvaluarRiesgoPort {

    private final EmpresaRepository empresaRepo;
    private final ResultadoEvaluacionRepository resultadoRepo;
    private final DatosContablesProvider datosContablesProvider;
    private final HistorialPagosProvider historialPagosProvider;
    private final VerificacionLegalProvider verificacionLegalProvider;
    private final OrdenarisRiskEngine motorReglas;

    @Override
    public EvaluacionResponseDTO evaluar(SolicitudEvaluacion solicitud) {
        log.info(">> Iniciando evaluación para empresa: {}", solicitud.getEmpresaId());
        try {
            // 1. Buscamos la empresa
            Empresa empresa = empresaRepo.findById(solicitud.getEmpresaId())
                    .orElseThrow(() -> new EmpresaNotFoundException(solicitud.getEmpresaId()));
            log.info(">> Empresa encontrada: {}", empresa.getNombre());

            // 2. Consultamos los proveedores
            DatosContables contables = datosContablesProvider.obtenerDatosContables(solicitud.getEmpresaId());
            HistorialPagos pagos     = historialPagosProvider.obtenerHistorialPagos(solicitud.getEmpresaId());
            VerificacionLegal legal  = verificacionLegalProvider.obtenerVerificacionLegal(solicitud.getEmpresaId());

            // 3. Empaquetamos el contexto
            ContextoEvaluacion contexto = new ContextoEvaluacion(solicitud, empresa, contables, pagos, legal);

            // 4. Ejecutamos el motor
            ResultadoEvaluacion resultadoFinal = motorReglas.evaluarRiesgo(contexto);

            // 5. Guardamos en BD
            ResultadoEvaluacion guardado = resultadoRepo.save(resultadoFinal);
            log.info(">> Evaluación completada. Empresa: {} | Nivel: {}",
                    solicitud.getEmpresaId(), guardado.getNivelRiesgo());

            // 6. Mapeamos a DTO y devolvemos
            return mapearADTO(guardado);

        } catch (EmpresaNotFoundException ex) {
            log.warn(">> Empresa no encontrada: {}", solicitud.getEmpresaId());
            throw ex;
        } catch (Exception ex) {
            log.error(">> Error al evaluar empresa: {} | Causa: {}",
                    solicitud.getEmpresaId(), ex.getMessage(), ex);
            throw new RiesgoEvaluacionException(
                    "Error al evaluar el riesgo de la empresa: " + solicitud.getEmpresaId(), ex);
        }
    }

    // ─── Mapea la entidad JPA al DTO de respuesta ──────────────────────────────
    private EvaluacionResponseDTO mapearADTO(ResultadoEvaluacion entidad) {
        List<EvaluacionResponseDTO.DetalleReglaDTO> detalles = entidad.getDetallesReglas()
                .stream()
                .map(d -> EvaluacionResponseDTO.DetalleReglaDTO.builder()
                        .nombreRegla(d.getNombreRegla())
                        .resultado(d.getResultado())
                        .build())
                .collect(Collectors.toList());

        return EvaluacionResponseDTO.builder()
                .empresaId(entidad.getEmpresaId())
                .nivelRiesgo(entidad.getNivelRiesgo())
                .motivoFinal(entidad.getMotivoFinal())
                .fechaEvaluacion(entidad.getFechaEvaluacion())
                .detallesReglas(detalles)
                .build();
    }
}