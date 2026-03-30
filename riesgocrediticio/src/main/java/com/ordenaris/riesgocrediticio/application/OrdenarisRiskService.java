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
        log.info(">> [INICIO] Evaluación de riesgo | EmpresaId: {} | Producto: {} | Monto: {}",
                solicitud.getEmpresaId(), solicitud.getProductoFinanciero(), solicitud.getMontoSolicitado());
        try {
            // 1. Buscamos la empresa
            log.info(">> [PASO 1] Buscando empresa en repositorio | EmpresaId: {}", solicitud.getEmpresaId());
            Empresa empresa = empresaRepo.findById(solicitud.getEmpresaId())
                    .orElseThrow(() -> new EmpresaNotFoundException(solicitud.getEmpresaId()));
            log.info(">> [PASO 1] Empresa encontrada | Nombre: {}", empresa.getNombre());

            // 2. Consultamos los proveedores externos
            log.info(">> [PASO 2] Consultando proveedores externos | EmpresaId: {}", solicitud.getEmpresaId());
            DatosContables contables = datosContablesProvider.obtenerDatosContables(solicitud.getEmpresaId());
            log.info(">> [PASO 2] Datos contables obtenidos correctamente");
            HistorialPagos pagos = historialPagosProvider.obtenerHistorialPagos(solicitud.getEmpresaId());
            log.info(">> [PASO 2] Historial de pagos obtenido correctamente");
            VerificacionLegal legal = verificacionLegalProvider.obtenerVerificacionLegal(solicitud.getEmpresaId());
            log.info(">> [PASO 2] Verificación legal obtenida correctamente");

            // 3. Empaquetamos el contexto
            log.info(">> [PASO 3] Construyendo contexto de evaluación");
            ContextoEvaluacion contexto = new ContextoEvaluacion(solicitud, empresa, contables, pagos, legal);
            log.info(">> [PASO 3] Contexto construido correctamente");

            // 4. Ejecutamos el motor de reglas
            log.info(">> [PASO 4] Enviando contexto al motor de reglas");
            ResultadoEvaluacion resultadoFinal = motorReglas.evaluarRiesgo(contexto);
            log.info(">> [PASO 4] Motor de reglas ejecutado | Nivel resultante: {}", resultadoFinal.getNivelRiesgo());

            // 5. Guardamos en BD
            log.info(">> [PASO 5] Persistiendo resultado en base de datos");
            ResultadoEvaluacion guardado = resultadoRepo.save(resultadoFinal);
            log.info(">> [PASO 5] Resultado persistido correctamente | Id: {}", guardado.getId());

            // 6. Mapeamos a DTO y devolvemos
            log.info(">> [PASO 6] Mapeando resultado a DTO de respuesta");
            EvaluacionResponseDTO respuesta = mapearADTO(guardado);
            log.info(">> [FIN] Evaluación finalizada exitosamente | EmpresaId: {} | Nivel: {}",
                    solicitud.getEmpresaId(), respuesta.getNivelRiesgo());
            return respuesta;

        } catch (EmpresaNotFoundException ex) {
            log.warn(">> [ERROR] Empresa no encontrada | EmpresaId: {} | Causa: {}",
                    solicitud.getEmpresaId(), ex.getMessage(), ex);
            throw ex;
        } catch (Exception ex) {
            log.error(">> [ERROR] Fallo inesperado durante la evaluación | EmpresaId: {} | Causa: {}",
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
                        .build()).toList();


        return EvaluacionResponseDTO.builder()
                .empresaId(entidad.getEmpresaId())
                .nivelRiesgo(entidad.getNivelRiesgo())
                .motivoFinal(entidad.getMotivoFinal())
                .fechaEvaluacion(entidad.getFechaEvaluacion())
                .detallesReglas(detalles)
                .build();
    }
}