package com.ordenaris.riesgocrediticio.infrastructure.config;

import com.ordenaris.riesgocrediticio.infrastructure.adapter.out.persistence.*;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;

@Component
@RequiredArgsConstructor
public class DataLoader implements CommandLineRunner {

    private final EmpresaRepository empresaRepo;
    private final DatosContablesRepository contablesRepo;
    private final HistorialPagosRepository pagosRepo;
    private final VerificacionLegalRepository legalRepo;

    @Override
    public void run(String... args) throws Exception {

        // ─── EMPRESA 001 — La buena (debe PASAR) ───────────────────────
        Empresa empresa = new Empresa();
        empresa.setId("EMPRESA001");
        empresa.setNombre("Tech Solutions S.A. de C.V.");
        empresa.setRfc("TSM123456ABC");
        empresa.setFechaConstitucion(LocalDate.now().minusMonths(24)); // 24 meses → pasa Regla 3
        empresaRepo.save(empresa);

        DatosContables contables = new DatosContables();
        contables.setEmpresaId("EMPRESA001");
        contables.setVentasPromedioMensuales(new BigDecimal("150000"));
        contables.setActivos(new BigDecimal("2000000"));
        contables.setPasivos(new BigDecimal("500000"));
        contablesRepo.save(contables);

        HistorialPagos pagos = new HistorialPagos();
        pagos.setEmpresaId("EMPRESA001");
        pagos.setDiasDeudaVencida(0);               // sin deuda → pasa Regla 1
        pagos.setPagosEnTiempoUltimos12Meses(true); // baja nivel → Regla 5
        pagos.setTieneRefinanciamiento(false);
        pagosRepo.save(pagos);

        VerificacionLegal legal = new VerificacionLegal();
        legal.setEmpresaId("EMPRESA001");
        legal.setJuicioMercantilEnCurso(false);     // sin juicio → pasa Regla 4
        legalRepo.save(legal);

        System.out.println(">> [OK] EMPRESA001 cargada — esperado: BAJO (historial excelente baja nivel)");

        // ─── EMPRESA 002 — La mala (debe FALLAR) ───────────────────────
        Empresa empresaMala = new Empresa();
        empresaMala.setId("EMPRESA002");
        empresaMala.setNombre("Emprendimientos Riesgosos S.A.");
        empresaMala.setRfc("BAD987654XYZ");
        empresaMala.setFechaConstitucion(LocalDate.now().minusMonths(6)); // 6 meses → Regla 3 activa
        empresaRepo.save(empresaMala);

        DatosContables contablesMalos = new DatosContables();
        contablesMalos.setEmpresaId("EMPRESA002");
        contablesMalos.setVentasPromedioMensuales(new BigDecimal("20000"));
        contablesMalos.setActivos(new BigDecimal("50000"));
        contablesMalos.setPasivos(new BigDecimal("100000"));
        contablesRepo.save(contablesMalos);

        HistorialPagos pagosMalos = new HistorialPagos();
        pagosMalos.setEmpresaId("EMPRESA002");
        pagosMalos.setDiasDeudaVencida(45);          // < 90 días, no activa Regla 1
        pagosMalos.setPagosEnTiempoUltimos12Meses(false);
        pagosMalos.setTieneRefinanciamiento(true);
        pagosRepo.save(pagosMalos);

        VerificacionLegal legalMalo = new VerificacionLegal();
        legalMalo.setEmpresaId("EMPRESA002");
        legalMalo.setJuicioMercantilEnCurso(true);   // juicio activo → RECHAZADO (Regla 4)
        legalRepo.save(legalMalo);

        System.out.println(">> [OK] EMPRESA002 cargada — esperado: RECHAZADO (juicio mercantil)");

    } // fin run()

} // fin DataLoader