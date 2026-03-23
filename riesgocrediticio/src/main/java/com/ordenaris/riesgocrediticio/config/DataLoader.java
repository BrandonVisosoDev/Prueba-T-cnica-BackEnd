package com.ordenaris.riesgocrediticio.config;

import com.ordenaris.riesgocrediticio.entity.*;
import com.ordenaris.riesgocrediticio.repository.*;
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
        // 1. Creamos la Empresa base
        Empresa empresa = new Empresa();
        empresa.setId("EMPRESA001");
        empresa.setNombre("Tech Solutions S.A. de C.V.");
        empresa.setRfc("TSM123456ABC");
        // Le damos 2 años de antigüedad para que pase la regla de 'Empresa Nueva'
        empresa.setFechaConstitucion(LocalDate.now().minusMonths(24));
        empresaRepo.save(empresa);

        // 2. Le asignamos sus Datos Contables
        DatosContables contables = new DatosContables();
        contables.setEmpresa(empresa);
        contables.setVentasPromedioMensuales(new BigDecimal("150000")); // Vende 150k al mes
        contables.setActivosTotales(new BigDecimal("2000000"));
        contables.setPasivosTotales(new BigDecimal("500000"));
        contablesRepo.save(contables);

        // 3. Su Historial de Pagos (Cliente impecable para probar el premio)
        HistorialPagos pagos = new HistorialPagos();
        pagos.setEmpresa(empresa);
        pagos.setDiasDeudaVencida(0);
        pagos.setPagosEnTiempoUltimos12Meses(true);
        pagos.setTieneRefinanciamiento(false);
        pagosRepo.save(pagos);

        // 4. Verificación Legal (Limpio, sin juicios)
        VerificacionLegal legal = new VerificacionLegal();
        legal.setEmpresa(empresa);
        legal.setJuicioMercantilEnCurso(false);
        legalRepo.save(legal);

        System.out.println(">> [SISTEMA] Datos de prueba (EMPRESA001) cargados exitosamente.");

        // ---------------------------------------------------------
        // 2. CREAMOS LA EMPRESA DE RIESGO (EMPRESA002)
        // ---------------------------------------------------------
        Empresa empresaMala = new Empresa();
        empresaMala.setId("EMPRESA002");
        empresaMala.setNombre("Emprendimientos Riesgosos S.A.");
        empresaMala.setRfc("BAD987654XYZ");
        // Regla 4: Empresa nueva (solo 6 meses de vida) -> Riesgo ALTO
        empresaMala.setFechaConstitucion(LocalDate.now().minusMonths(6));
        empresaRepo.save(empresaMala);

        // Datos Contables: Vende poquito (20k)
        DatosContables contablesMalos = new DatosContables();
        contablesMalos.setEmpresa(empresaMala);
        contablesMalos.setVentasPromedioMensuales(new BigDecimal("20000"));
        contablesMalos.setActivosTotales(new BigDecimal("50000"));
        contablesMalos.setPasivosTotales(new BigDecimal("100000"));
        contablesRepo.save(contablesMalos);

        // Historial de Pagos: Tiene deuda de 45 días -> Riesgo ALTO
        HistorialPagos pagosMalos = new HistorialPagos();
        pagosMalos.setEmpresa(empresaMala);
        pagosMalos.setDiasDeudaVencida(45); // Regla 1 activa
        pagosMalos.setPagosEnTiempoUltimos12Meses(false);
        pagosMalos.setTieneRefinanciamiento(true);
        pagosRepo.save(pagosMalos);

        // Legal: Tiene un juicio activo -> RECHAZO INMEDIATO
        VerificacionLegal legalMalo = new VerificacionLegal();
        legalMalo.setEmpresa(empresaMala);
        legalMalo.setJuicioMercantilEnCurso(true); // Regla 3 activa
        legalRepo.save(legalMalo);

        System.out.println(">> [SISTEMA] Empresa 'Mala' (EMPRESA002) cargada. Lista para fallar.");

    }
}