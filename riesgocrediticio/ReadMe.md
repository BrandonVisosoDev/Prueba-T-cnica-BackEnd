# Ordenaris Risk Engine 🏦

> Motor de evaluación de riesgo crediticio empresarial desarrollado para **Ordenaris Capital** — una fintech enfocada en otorgar financiamiento a empresas emergentes y consolidadas en América Latina.

---

## Descripción

**OrdenarisRiskEngine** es un componente modular, extensible y desacoplado que, dado un conjunto de datos sobre una empresa solicitante, evalúa su riesgo crediticio aplicando un conjunto de reglas de negocio configurables.

El motor recibe una solicitud con los datos de la empresa, consulta tres fuentes de información (datos contables, historial de pagos y verificación legal), aplica 6 reglas de negocio y devuelve un resultado con el nivel de riesgo, las reglas evaluadas y el motivo determinante.

---

## Cómo ejecutar el motor

### Prerrequisitos

- Java 17
- Maven 3.8+
- IntelliJ IDEA (recomendado) o cualquier IDE compatible

### Pasos

**1. Clonar el repositorio**
```bash
https://github.com/BrandonVisosoDev/Prueba-T-cnica-BackEnd.git
```

**2. Compilar el proyecto**
```bash
mvn clean install
```

**3. Ejecutar la aplicación**
```bash
mvn spring-boot:run
```

**4. Verificar que está corriendo**

La aplicación levanta en `http://localhost:8080`

Al iniciar, el `DataLoader` carga automáticamente 2 empresas de prueba en H2:

| ID | Nombre | Resultado esperado |
|----|--------|--------------------|
| `EMPRESA001` | Tech Solutions S.A. de C.V. | 🟢 BAJO |
| `EMPRESA002` | Emprendimientos Riesgosos S.A. | 🔴 RECHAZADO |

**5. Acceder a la interfaz**

Abre `http://localhost:8080` en tu navegador para usar la interfaz visual del motor.

**6. Consola H2 (opcional)**

Puedes inspeccionar la base de datos en `http://localhost:8080/h2-console`

```
JDBC URL:  jdbc:h2:mem:ordenaris_db
Usuario:   sa
Password:  (vacío)
```

### Endpoint principal

```http
POST /api/v1/riesgo/evaluar
Content-Type: application/json

{
  "empresaId": "EMPRESA001",
  "montoSolicitado": 500000,
  "productoFinanciero": "LINEA_OPERATIVA",
  "fechaSolicitud": "2026-03-24"
}
```

Productos disponibles: `LINEA_OPERATIVA`, `CREDITO_REVOLVENTE`, `ARRENDAMIENTO_FINANCIERO`

---

## Estructura del proyecto

El proyecto implementa **Arquitectura Hexagonal (Ports & Adapters)**, garantizando que el dominio de negocio esté completamente aislado de la infraestructura tecnológica.

```
src/main/java/com/ordenaris/riesgocrediticio/
│
├── domain/                          # Núcleo de negocio — sin Spring, sin JPA
│   ├── engine/
│   │   └── OrdenarisRiskEngine      # Orquestador de reglas
│   ├── model/
│   │   ├── ContextoEvaluacion       # Datos empaquetados para el motor
│   │   ├── ResultadoRegla           # Resultado individual por regla
│   │   ├── ResultadoRiesgo          # Objeto de dominio puro que retorna el puerto
│   │   ├── SolicitudEvaluacion      # DTO de entrada
│   │   ├── EmpresaNotFoundException # Excepción de dominio
│   │   ├── RiesgoEvaluacionException
│   │   └── enums/
│   │       ├── NivelRiesgo          # BAJO, MEDIO, ALTO, RECHAZADO
│   │       └── ProductoFinanciero   # LINEA_OPERATIVA, CREDITO_REVOLVENTE, ARRENDAMIENTO_FINANCIERO
│   ├── port/
│   │   ├── in/
│   │   │   └── EvaluarRiesgoPort    # Puerto de entrada — retorna ResultadoRiesgo
│   │   └── out/
│   │       ├── DatosContablesProvider
│   │       ├── HistorialPagosProvider
│   │       └── VerificacionLegalProvider
│   └── rule/
│       ├── ReglaEvaluacion          # Interfaz base — Strategy Pattern
│       ├── ReglaDeudaActiva
│       ├── ReglaAltaSolicitudVsVentas
│       ├── ReglaEmpresaNueva
│       ├── ReglaDemandaLegalAbierta
│       ├── ReglaHistorialExcelente
│       └── ReglaProductoEstricto
│
├── application/                     # Casos de uso
│   └── OrdenarisRiskService         # Implementa EvaluarRiesgoPort
│
└── infrastructure/                  # Tecnología — Spring Boot, JPA, H2
    ├── adapter/
    │   ├── in/rest/
    │   │   ├── OrdenarisRiskController
    │   │   └── dto/
    │   │       └── EvaluacionResponseDTO  # DTO de presentación al cliente
    │   └── out/persistence/
    │       ├── Empresa
    │       ├── DatosContables
    │       ├── HistorialPagos
    │       ├── VerificacionLegal
    │       ├── ResultadoEvaluacion
    │       ├── DetalleReglaEvaluada
    │       ├── DatosContablesAdapter
    │       ├── HistorialPagosAdapter
    │       ├── VerificacionLegalAdapter
    │       └── *Repository (x6)
    └── config/
        ├── DataLoader               # Carga datos de prueba al iniciar
        ├── CorsConfig               # Configuración CORS
        └── GlobalExceptionHandler   # Manejo global de errores
```

### Principios y patrones aplicados

- **Arquitectura Hexagonal** — dominio completamente desacoplado de la infraestructura
- **DTO Response Pattern** — el dominio expone objetos de dominio puros; la capa web es responsable de mapearlos al DTO de presentación antes de responder al cliente
- **Strategy Pattern** — cada regla es una estrategia intercambiable
- **Chain of Responsibility** — el motor itera las reglas secuencialmente
- **Principios SOLID** — Single Responsibility, Open/Closed, Dependency Inversion

---

## Reglas de negocio

| # | Regla | Condición | Efecto |
|---|-------|-----------|--------|
| 1 | Deuda Activa | Deuda vencida > 90 días | RECHAZADO |
| 2 | Alta Solicitud vs Ventas | Monto > 8x ventas promedio mensuales | ALTO |
| 3 | Empresa Nueva | Menos de 18 meses de operación | Mínimo MEDIO |
| 4 | Demanda Legal Abierta | Juicio mercantil en curso | ALTO |
| 5 | Historial Excelente | 12 pagos en tiempo sin refinanciamiento | Baja 1 nivel |
| 6 | Producto Estricto | Producto = ARRENDAMIENTO_FINANCIERO | Sube 1 nivel |

---

## Supuestos y limitaciones

### Supuestos

- La base de datos H2 es **in-memory** — los datos se pierden al reiniciar la aplicación. Para persistencia real, configurar MySQL en `application.properties`.
- Las tres fuentes de información (`DatosContablesProvider`, `HistorialPagosProvider`, `VerificacionLegalProvider`) son implementadas mediante la base de datos local. En producción, estas interfaces se conectarían a servicios externos o APIs de terceros.
- Si alguna fuente de datos devuelve `null` (empresa sin datos contables, sin historial, etc.), las reglas correspondientes **no aplican** — no generan alerta ni rechazo.
- El `DataLoader` carga las empresas de prueba únicamente si no existen previamente en la base de datos.
- Se asume que `fechaSolicitud` es proporcionada por el cliente — no se usa la fecha del servidor para el cálculo de antigüedad.

### Limitaciones

- No implementa autenticación ni autorización — todos los endpoints son públicos.
- El historial de evaluaciones en la interfaz es **de sesión** — se pierde al recargar la página. Para persistencia en el front se requeriría un endpoint `GET /api/v1/riesgo/historial`.
- El motor no soporta actualmente ponderación dinámica de reglas desde base de datos — las reglas son configuradas en tiempo de compilación.
- No se implementó paginación en el historial de evaluaciones.
- La combinación de 2 alertas `ALTO` resulta en `RECHAZADO` automático — este umbral está hardcodeado en el motor y no es configurable sin modificar el código.

---

## Stack tecnológico

| Tecnología | Versión | Uso |
|------------|---------|-----|
| Java | 17 | Lenguaje principal |
| Spring Boot | 3.4.2 | Framework base |
| Spring Data JPA | 3.4.2 | Persistencia |
| H2 Database | 2.3.232 | Base de datos in-memory |
| Lombok | 1.18.30 | Reducción de boilerplate |
| Tailwind CSS | CDN | Interfaz visual |
| Maven | 3.8+ | Gestión de dependencias |

---

*Desarrollado como prueba técnica para Ordenaris Capital — 2026*
