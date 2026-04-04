package com.ordenaris.riesgocrediticio.infrastructure.config;

import com.ordenaris.riesgocrediticio.infrastructure.adapter.out.persistence.DatosContables;
import com.ordenaris.riesgocrediticio.infrastructure.adapter.out.persistence.DatosContablesRepository;
import com.ordenaris.riesgocrediticio.infrastructure.adapter.out.persistence.Empresa;
import com.ordenaris.riesgocrediticio.infrastructure.adapter.out.persistence.EmpresaRepository;
import com.ordenaris.riesgocrediticio.infrastructure.adapter.out.persistence.HistorialPagos;
import com.ordenaris.riesgocrediticio.infrastructure.adapter.out.persistence.HistorialPagosRepository;
import com.ordenaris.riesgocrediticio.infrastructure.adapter.out.persistence.VerificacionLegal;
import com.ordenaris.riesgocrediticio.infrastructure.adapter.out.persistence.VerificacionLegalRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataLoader implements CommandLineRunner {

    private final EmpresaRepository empresaRepo;
    private final DatosContablesRepository contablesRepo;
    private final HistorialPagosRepository pagosRepo;
    private final VerificacionLegalRepository legalRepo;

    @Override
    public void run(String... args) {
        if (empresaRepo.count() > 0) {
            log.info("Datos semilla ya existen. Se omite recarga.");
            return;
        }

        cargarEmpresaBuena();
        cargarEmpresaRiesgosa();
    }

    private void cargarEmpresaBuena() {
        Empresa empresa = new Empresa();
        empresa.setId("EMPRESA001");
        empresa.setNombre("Tech Solutions S.A. de C.V.");
        empresa.setRfc("TSM123456ABC");
        empresa.setFechaConstitucion(LocalDate.now().minusMonths(24));
        empresaRepo.save(empresa);

        DatosContables contables = new DatosContables();
        contables.setEmpresaId("EMPRESA001");
        contables.setVentasPromedioMensuales(new BigDecimal("150000"));
        contables.setActivos(new BigDecimal("2000000"));
        contables.setPasivos(new BigDecimal("500000"));
        contablesRepo.save(contables);

        HistorialPagos pagos = new HistorialPagos();
        pagos.setEmpresaId("EMPRESA001");
        pagos.setDiasDeudaVencida(0);
        pagos.setPagosEnTiempoUltimos12Meses(true);
        pagos.setTieneRefinanciamiento(false);
        pagosRepo.save(pagos);

        VerificacionLegal legal = new VerificacionLegal();
        legal.setEmpresaId("EMPRESA001");
        legal.setJuicioMercantilEnCurso(false);
        legalRepo.save(legal);
    }

    private void cargarEmpresaRiesgosa() {
        Empresa empresa = new Empresa();
        empresa.setId("EMPRESA002");
        empresa.setNombre("Emprendimientos Riesgosos S.A.");
        empresa.setRfc("BAD987654XYZ");
        empresa.setFechaConstitucion(LocalDate.now().minusMonths(6));
        empresaRepo.save(empresa);

        DatosContables contables = new DatosContables();
        contables.setEmpresaId("EMPRESA002");
        contables.setVentasPromedioMensuales(new BigDecimal("20000"));
        contables.setActivos(new BigDecimal("50000"));
        contables.setPasivos(new BigDecimal("100000"));
        contablesRepo.save(contables);

        HistorialPagos pagos = new HistorialPagos();
        pagos.setEmpresaId("EMPRESA002");
        pagos.setDiasDeudaVencida(45);
        pagos.setPagosEnTiempoUltimos12Meses(false);
        pagos.setTieneRefinanciamiento(true);
        pagosRepo.save(pagos);

        VerificacionLegal legal = new VerificacionLegal();
        legal.setEmpresaId("EMPRESA002");
        legal.setJuicioMercantilEnCurso(true);
        legalRepo.save(legal);
    }
}
