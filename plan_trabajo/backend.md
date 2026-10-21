# Guía Maestra de Recreación del Backend
**Transaduanas Platform (Powered by SGA Core)**  
*Arquitectura, Paso a Paso de Desarrollo y Secuencia de Commits Git*

---

## 1. Visión General y Arquitectura de Software

El backend de **Transaduanas Platform** está construido sobre el ecosistema empresarial **Java 21** y **Spring Boot 4.1.1**, diseñado bajo un patrón en capas estricto (**Layered Clean Architecture**) que garantiza el desacoplamiento total entre la lógica de negocio, la exposición HTTP REST y la persistencia de datos.

```
┌────────────────────────────────────────────────────────────────────────┐
│                        CLIENTE HTTP / FRONTEND                         │
│                    (Peticiones JSON + Bearer Token)                    │
└───────────────────────────────────┬────────────────────────────────────┘
                                    │
                                    ▼
┌────────────────────────────────────────────────────────────────────────┐
│                     CAPA DE FILTROS & SEGURIDAD                        │
│   CorsConfig ──► JwtAuthenticationFilter ──► SecurityRoles (SpEL)      │
└───────────────────────────────────┬────────────────────────────────────┘
                                    │
                                    ▼
┌────────────────────────────────────────────────────────────────────────┐
│                        CAPA DE CONTROLADORES                           │
│  19 @RestController · 126 Endpoints REST · @PreAuthorize · @Valid     │
│  Retorno unificado mediante: ResponseEntity<ApiResponseDTO<T>>         │
└───────────────────────────────────┬────────────────────────────────────┘
                                    │
                                    ▼
┌────────────────────────────────────────────────────────────────────────┐
│                         CAPA DE SERVICIOS                              │
│  Interfaces (pe.sga.service.*) + Implementaciones (*ServiceImpl)       │
│  @Transactional · Lógica de Negocio · Cálculos · Compuertas de Flujo   │
│  IStorageService (Archivos) · IReporteJasperService (PDFs en caliente) │
└───────────────────┬────────────────────────────────┬───────────────────┘
                    │                                │
                    ▼                                ▼
┌──────────────────────────────────────┐  ┌──────────────────────────────┐
│         CAPA DE PERSISTENCIA         │  │   SISTEMA DE ARCHIVOS / OS   │
│   16 Repositorios Spring Data JPA    │  │   Almacenamiento Local Físico│
│   Consultas derivadas y JPQL         │  │   Uploads / Documentos DAM   │
└───────────────────┬──────────────────┘  └──────────────────────────────┘
                    │
                    ▼
┌────────────────────────────────────────────────────────────────────────┐
│                      BASE DE DATOS RELACIONAL                          │
│               MySQL 8.4 InnoDB · 16 Tablas Normalizadas                │
│                 Borrado Lógico transversal (estado_activo)             │
└────────────────────────────────────────────────────────────────────────┘
```

### 1.1 Principios Arquitectónicos Fundamentales

1. **Desacoplamiento Estricto Entity-DTO:**  
   Ninguna entidad JPA (`pe.sga.entity`) es expuesta directamente en las firmas de los controladores ni consumida desde el exterior. Se emplean Data Transfer Objects especializados divididos en:
   * `dto.request`: Objetos de entrada decorados con validaciones Jakarta (`@NotBlank`, `@NotNull`, `@Size`, `@Min`).
   * `dto.response`: Proyecciones limpias de salida para serialización JSON segura.
   * `dto.reporte`: Modelos planos optimizados para fuentes de datos JRBeanCollectionDataSource de JasperReports.
2. **Contrato de Respuesta Unificado (`ApiResponseDTO<T>`):**  
   Todas las respuestas JSON (exitosas y errores controlados) comparten la misma firma estándar:
   ```json
   {
     "status": 200,
     "message": "Operación ejecutada exitosamente",
     "data": { ... },
     "timestamp": "2026-10-21T09:00:00"
   }
   ```
3. **Seguridad Stateless JWT con SpEL:**  
   No se crean sesiones HTTP en servidor (`SessionCreationPolicy.STATELESS`). Cada petición es validada por `JwtAuthenticationFilter`. Las autorizaciones se declaran a nivel de método con `@PreAuthorize(SecurityRoles.NOMBRE_CONSTANTE)`, centralizadas en `pe.sga.config.SecurityRoles`.
4. **Manejo Centralizado de Excepciones:**  
   `GlobalExceptionHandler` (`@RestControllerAdvice`) intercepta `MethodArgumentNotValidException`, `ResourceNotFoundException`, `BadRequestException`, `AccessDeniedException` y fallos genéricos, retornando códigos HTTP semánticos (400, 401, 403, 404, 500) en el formato unificado `ApiResponseDTO`.
5. **Borrado Lógico y Auditoría:**  
   Todas las entidades de negocio implementan la columna `estado_activo` (boolean/tinyint), previniendo la eliminación física destructiva y manteniendo la integridad referencial histórica aduanera.
6. **Compuerta de Negocio Pre-Expediente (Regla de Oro):**  
   Un expediente aduanero (`Expediente`) no puede ser aperturado a partir de una cotización a menos que dicha cotización cuente con `tienePagoAprobado = true`, validado y aprobado previamente por el rol `CONTADOR` en `CotizacionPagoServiceImpl`.

---

### 1.2 Mapa Completo de Paquetes y Estructura de Directorios

```
backend/
├── pom.xml                                    # Descriptor de dependencias Maven
├── mvnw / mvnw.cmd / .mvn/                    # Wrapper de Maven 3.9+
└── src/
    └── main/
        ├── resources/
        │   ├── application.properties         # Configuración del servidor y base de datos
        │   ├── logback-spring.xml             # Formato y política de rotación de logs
        │   └── initial-data.json              # Datos de catálogo base
        └── java/
            └── pe/sga/
                ├── SgaApplication.java        # Punto de entrada (@SpringBootApplication)
                │
                ├── config/                    # Configuración transversal
                │   ├── CorsConfig.java        # Filtro CORS para Vue 3 (localhost:5180)
                │   ├── DataInitializer.java   # Sembrado inicial de roles y usuario admin
                │   ├── JwtAuthenticationFilter.java  # Extracción e inyección de token
                │   ├── JwtTokenProvider.java  # Cifrado/firma JJWT 0.13.0
                │   ├── SecurityConfig.java    # SecurityFilterChain y BCrypt
                │   └── SecurityRoles.java     # Constantes SpEL de autorización
                │
                ├── controller/                # 19 Controladores REST (126 Endpoints)
                │   ├── AdminLogsController.java
                │   ├── AuthController.java
                │   ├── CatalogoMercanciaController.java
                │   ├── CatalogoPuertoController.java
                │   ├── CatalogoTipoCargaController.java
                │   ├── ClienteController.java
                │   ├── ConductorController.java
                │   ├── ControlAlmacenController.java
                │   ├── CotizacionController.java
                │   ├── CotizacionPagoController.java
                │   ├── DespachoTransporteController.java
                │   ├── DocumentoController.java
                │   ├── ExpedienteController.java
                │   ├── HealthController.java
                │   ├── LiquidacionFinancieraController.java
                │   ├── RolController.java
                │   ├── SedeController.java
                │   ├── UnidadTransporteController.java
                │   └── UsuarioController.java
                │
                ├── dto/                       # 52 Data Transfer Objects
                │   ├── reporte/               # 3 DTOs para JasperReports
                │   │   ├── PuertoReporteItemDTO.java
                │   │   ├── SedeReporteItemDTO.java
                │   │   └── UsuarioReporteItemDTO.java
                │   ├── request/               # 29 DTOs de entrada con @Valid
                │   │   ├── CatalogoMercanciaRequestDTO.java
                │   │   ├── CatalogoPuertoDTO.java
                │   │   ├── CatalogoTipoCargaDTO.java
                │   │   ├── ClienteRequestDTO.java
                │   │   ├── ConductorRequestDTO.java
                │   │   ├── ControlAlmacenRequestDTO.java
                │   │   ├── ControlAlmacenSalidaDTO.java
                │   │   ├── CotizacionEstadoDTO.java
                │   │   ├── CotizacionPagoRegistrarDTO.java
                │   │   ├── CotizacionPagoValidarDTO.java
                │   │   ├── CotizacionRequestDTO.java
                │   │   ├── CotizacionRevisionDTO.java
                │   │   ├── DespachoAsignarRecursosDTO.java
                │   │   ├── DespachoEntregaDTO.java
                │   │   ├── DespachoRutaDTO.java
                │   │   ├── DespachoTransporteCrearDTO.java
                │   │   ├── ExpedienteActualizarDTO.java
                │   │   ├── ExpedienteCanalDTO.java
                │   │   ├── ExpedienteCrearDTO.java
                │   │   ├── ExpedienteLevanteDTO.java
                │   │   ├── LiquidacionFinancieraRequestDTO.java
                │   │   ├── LiquidacionPagoDTO.java
                │   │   ├── LoginRequestDTO.java
                │   │   ├── RolDTO.java
                │   │   ├── SedeCreateDTO.java
                │   │   ├── SedeUpdateDTO.java
                │   │   ├── UnidadTransporteRequestDTO.java
                │   │   ├── UsuarioCreateDTO.java
                │   │   └── UsuarioUpdateDTO.java
                │   └── response/              # 20 DTOs de salida serializables
                │       ├── ApiResponseDTO.java
                │       ├── AuthResponseDTO.java
                │       ├── CatalogoMercanciaResponseDTO.java
                │       ├── CatalogoPuertoResponseDTO.java
                │       ├── CatalogoTipoCargaResponseDTO.java
                │       ├── ClienteResponseDTO.java
                │       ├── ConductorResponseDTO.java
                │       ├── ControlAlmacenResponseDTO.java
                │       ├── CotizacionPagoResponseDTO.java
                │       ├── CotizacionResponseDTO.java
                │       ├── DespachoTransporteResponseDTO.java
                │       ├── DocumentoResponseDTO.java
                │       ├── ExpedienteResponseDTO.java
                │       ├── FinanzasMetricasResponseDTO.java
                │       ├── LiquidacionEstimadoResponseDTO.java
                │       ├── LiquidacionFinancieraResponseDTO.java
                │       ├── RolResponseDTO.java
                │       ├── SedeResponseDTO.java
                │       ├── UnidadTransporteResponseDTO.java
                │       └── UsuarioResponseDTO.java
                │
                ├── entity/                    # 16 Entidades JPA Relacionales
                │   ├── CatalogoMercancia.java
                │   ├── CatalogoPuerto.java
                │   ├── CatalogoTipoCarga.java
                │   ├── Cliente.java
                │   ├── Conductor.java
                │   ├── ControlAlmacen.java
                │   ├── Cotizacion.java
                │   ├── CotizacionPago.java
                │   ├── DespachoTransporte.java
                │   ├── DocumentoAdjunto.java
                │   ├── Expediente.java
                │   ├── LiquidacionFinanciera.java
                │   ├── Rol.java
                │   ├── Sede.java
                │   ├── UnidadTransporte.java
                │   ├── Usuario.java
                │   └── enums/                 # 8 Enumeraciones de Estado y Dominio
                │       ├── CanalSunat.java
                │       ├── CategoriaDocumento.java
                │       ├── EntidadDocumento.java
                │       ├── EstadoCotizacion.java
                │       ├── EstadoExpediente.java
                │       ├── EstadoPagoCotizacion.java
                │       ├── RolUsuario.java
                │       └── TipoAbonoCotizacion.java
                │
                ├── exception/                 # Gestión Centralizada de Errores
                │   ├── BadRequestException.java
                │   ├── GlobalExceptionHandler.java
                │   └── ResourceNotFoundException.java
                │
                ├── repository/                # 16 Repositorios Spring Data JPA
                │   ├── CatalogoMercanciaRepository.java
                │   ├── CatalogoPuertoRepository.java
                │   ├── CatalogoTipoCargaRepository.java
                │   ├── ClienteRepository.java
                │   ├── ConductorRepository.java
                │   ├── ControlAlmacenRepository.java
                │   ├── CotizacionPagoRepository.java
                │   ├── CotizacionRepository.java
                │   ├── DespachoTransporteRepository.java
                │   ├── ExpedienteRepository.java
                │   ├── IDocumentoAdjuntoRepository.java
                │   ├── LiquidacionFinancieraRepository.java
                │   ├── RolRepository.java
                │   ├── SedeRepository.java
                │   ├── UnidadTransporteRepository.java
                │   └── UsuarioRepository.java
                │
                └── service/                   # Lógica de Negocio Transaccional
                    ├── IAdminLogsService.java
                    ├── ICatalogoMercanciaService.java
                    ├── ICatalogoPuertoService.java
                    ├── ICatalogoTipoCargaService.java
                    ├── IClienteService.java
                    ├── IConductorService.java
                    ├── IControlAlmacenService.java
                    ├── ICotizacionPagoService.java
                    ├── ICotizacionService.java
                    ├── IDespachoTransporteService.java
                    ├── IDocumentoService.java
                    ├── IExpedienteService.java
                    ├── ILiquidacionFinancieraService.java
                    ├── IReporteJasperService.java
                    ├── IRolService.java
                    ├── ISedeService.java
                    ├── IStorageService.java
                    ├── IUnidadTransporteService.java
                    ├── IUsuarioService.java
                    └── impl/                  # 19 Implementaciones de Servicio
                        ├── AdminLogsServiceImpl.java
                        ├── CatalogoMercanciaServiceImpl.java
                        ├── CatalogoPuertoServiceImpl.java
                        ├── CatalogoTipoCargaServiceImpl.java
                        ├── ClienteServiceImpl.java
                        ├── ConductorServiceImpl.java
                        ├── ControlAlmacenServiceImpl.java
                        ├── CotizacionPagoServiceImpl.java
                        ├── CotizacionServiceImpl.java
                        ├── DespachoTransporteServiceImpl.java
                        ├── DocumentoServiceImpl.java
                        ├── ExpedienteServiceImpl.java
                        ├── LiquidacionFinancieraServiceImpl.java
                        ├── LocalStorageServiceImpl.java
                        ├── ReporteJasperServiceImpl.java
                        ├── RolServiceImpl.java
                        ├── SedeServiceImpl.java
                        ├── UnidadTransporteServiceImpl.java
                        └── UsuarioServiceImpl.java
```

---

## 2. Metodología y Convención de Ramas Git

El desarrollo del Backend Completo (DEV 2) se ejecuta de forma ordenada y atómica mediante una rama de especialidad dedicada o sub-ramas funcionales:

```
[develop]
   │
   └──► [backend/feature-complete]
           │
           ├── Commit 1: Inicialización Maven y Entorno
           ├── Commit 2: Núcleo de Respuestas y Excepciones
           ├── Commit 3: Seguridad JWT y Matriz SpEL
           ├── Commit 4: Identidad, Login y Administración TI
           ├── Commit 5: Catálogos y Motor JasperReports
           ├── Commit 6: Almacenamiento Polimórfico de Archivos
           ├── Commit 7: Comercial, Costos y Compuerta Pre-Pago
           ├── Commit 8: Aduanas, DAM y Canales SUNAT
           ├── Commit 9: Almacén VGM y Transporte de Flota
           ├── Commit 10: Finanzas, Facturación y SPOT
           └── Commit 11: Endurecimiento y Verificación Integral
```

### Reglas de Calidad Obligatorias por Commit:
1. **Compilación Limpia:** Antes de cada commit se debe ejecutar `./mvnw test-compile -DskipTests`. La compilación debe dar `BUILD SUCCESS` con 0 errores.
2. **Convención de Mensajes:** Se utiliza la especificación **Conventional Commits** (`feat(...)`, `chore(...)`, `test(...)`).
3. **Copia Segura:** En cada paso se detalla la ruta de origen en este repositorio y la ruta destino a recrear.

---

## 3. Recreación Paso a Paso con Commits

### Commit 1: Inicialización del Proyecto Maven y Configuración de Entorno

**Objetivo:** Crear el andamiaje del proyecto Spring Boot con Java 21, resolviendo dependencias de base de datos, seguridad, validación y utilidades.

#### 1. Crear el archivo `backend/pom.xml`:
Copiar de: `backend/pom.xml`  
*Asegurar que incluya:*
* `spring-boot-starter-parent` v4.1.1
* `spring-boot-starter-data-jpa`, `spring-boot-starter-security`, `spring-boot-starter-validation`, `spring-boot-starter-webmvc`
* `mysql-connector-j` (scope runtime)
* `lombok` con su correspondiente `annotationProcessorPaths` en `maven-compiler-plugin`
* `jasperreports`, `jasperreports-pdf`, `jasperreports-jdt` (v7.0.8)
* `jjwt-api`, `jjwt-impl`, `jjwt-jackson` (v0.13.0)

#### 2. Crear las carpetas base del proyecto:
```bash
mkdir -p backend/src/main/resources
mkdir -p backend/src/main/java/pe/sga
```

#### 3. Crear el archivo de configuración `backend/src/main/resources/application.properties`:
Copiar de: `backend/src/main/resources/application.properties`  
*Parámetros clave:*
```properties
spring.application.name=sga-backend
server.port=${SERVER_PORT:8085}

spring.datasource.url=${DB_URL:jdbc:mysql://localhost:3306/SGAdb?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true}
spring.datasource.username=${DB_USERNAME:root}
spring.datasource.password=${DB_PASSWORD:}
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

spring.jpa.hibernate.ddl-auto=${JPA_DDL_AUTO:update}
spring.jpa.show-sql=${JPA_SHOW_SQL:true}
spring.jpa.properties.hibernate.format_sql=true

jwt.secret=${JWT_SECRET:sga_platform_secret_key_super_segura_2026_jwt_token_auth}
jwt.expiration-ms=${JWT_EXPIRATION_MS:86400000}

spring.servlet.multipart.enabled=true
spring.servlet.multipart.max-file-size=25MB
spring.servlet.multipart.max-request-size=100MB
app.storage.upload-dir=${STORAGE_DIR:./uploads/documentos}
```

#### 4. Crear `backend/src/main/resources/logback-spring.xml` y `initial-data.json`:
Copiar de:
* `backend/src/main/resources/logback-spring.xml`
* `backend/src/main/resources/initial-data.json`

#### 5. Crear la clase principal `backend/src/main/java/pe/sga/SgaApplication.java`:
Copiar de: `backend/src/main/java/pe/sga/SgaApplication.java`
```java
package pe.sga;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class SgaApplication {
    public static void main(String[] args) {
        SpringApplication.run(SgaApplication.class, args);
    }
}
```

#### 6. Comando Git:
```bash
./mvnw clean compile -DskipTests
git add backend/pom.xml backend/src/main/resources/ backend/src/main/java/pe/sga/SgaApplication.java
git commit -m "chore(backend): inicializar proyecto maven con spring boot 4.1.1, java 21 y dependencias core"
```

---

### Commit 2: Núcleo de Respuestas HTTP y Manejo Centralizado de Excepciones

**Objetivo:** Establecer el contrato estándar de comunicación REST y los interceptores de fallos de la aplicación.

#### 1. Crear carpetas:
```bash
mkdir -p backend/src/main/java/pe/sga/dto/response
mkdir -p backend/src/main/java/pe/sga/exception
```

#### 2. Copiar archivos de respuesta y excepción:
* `ApiResponseDTO.java`: Copiar a `backend/src/main/java/pe/sga/dto/response/ApiResponseDTO.java`
  * Contiene los métodos estáticos `success(data, message)` y `error(status, message)`.
* `BadRequestException.java`: Copiar a `backend/src/main/java/pe/sga/exception/BadRequestException.java`
* `ResourceNotFoundException.java`: Copiar a `backend/src/main/java/pe/sga/exception/ResourceNotFoundException.java`
* `GlobalExceptionHandler.java`: Copiar a `backend/src/main/java/pe/sga/exception/GlobalExceptionHandler.java`
  * Maneja `MethodArgumentNotValidException` (concatenando errores de validación de campos), `AccessDeniedException` (403), `BadCredentialsException` (401), y excepciones no controladas (500).

#### 3. Comando Git:
```bash
./mvnw test-compile -DskipTests
git add backend/src/main/java/pe/sga/dto/response/ApiResponseDTO.java backend/src/main/java/pe/sga/exception/
git commit -m "feat(core): implementar ApiResponseDTO unificado y GlobalExceptionHandler para errores REST"
```

---

### Commit 3: Seguridad Stateless JWT, Filtros y Matriz SpEL de Autorización

**Objetivo:** Levantar la infraestructura de autenticación desacoplada por tokens firmados y control de acceso basado en roles.

#### 1. Crear carpetas:
```bash
mkdir -p backend/src/main/java/pe/sga/entity/enums
mkdir -p backend/src/main/java/pe/sga/config
```

#### 2. Copiar archivos:
* Enumeración de roles:
  * Copiar `backend/src/main/java/pe/sga/entity/enums/RolUsuario.java`
  * Contiene los 8 roles institucionales: `ADMINISTRADOR_TI`, `AGENTE_ADUANAS`, `OPERADOR_LOGISTICO`, `SUPERVISOR_OPERACIONES`, `GERENTE_GENERAL`, `COORDINADOR_TRANSPORTE`, `EJECUTIVO_COMERCIAL`, `CONTADOR`.
* Proveedor de Tokens JWT:
  * Copiar `backend/src/main/java/pe/sga/config/JwtTokenProvider.java`
  * Implementa generación de tokens con HMAC-SHA256, expiración configurable y extracción de subject y claims (`roles`).
* Filtro de Autenticación:
  * Copiar `backend/src/main/java/pe/sga/config/JwtAuthenticationFilter.java`
  * Intercepta la cabecera `Authorization: Bearer <token>`, valida el token e inyecta `UsernamePasswordAuthenticationToken` en el `SecurityContextHolder`.
* Matriz de Expresiones SpEL:
  * Copiar `backend/src/main/java/pe/sga/config/SecurityRoles.java`
  * Declara las constantes de acceso para `@PreAuthorize`:
    * `ACCESS_EXPEDIENTE_ALMACEN_WRITE`: Operador, Aduanas, Supervisor y Admin.
    * `ACCESS_TRANSPORTE_WRITE`: Transporte, Supervisor y Admin.
    * `ACCESS_COTIZACION_WRITE`: Comercial, Supervisor y Admin.
    * `ACCESS_FINANZAS_WRITE`: Contador, Supervisor, Gerente y Admin.
    * `ACCESS_ADMIN_TI`: Exclusivo Admin.
* Configuración de CORS:
  * Copiar `backend/src/main/java/pe/sga/config/CorsConfig.java`
  * Habilita `http://localhost:5180` y `http://localhost:3000` con métodos `GET, POST, PUT, DELETE, PATCH, OPTIONS`.
* Cadena de Seguridad Principal:
  * Copiar `backend/src/main/java/pe/sga/config/SecurityConfig.java`
  * Configura `SecurityFilterChain`, `BCryptPasswordEncoder`, endpoints públicos (`/api/v1/auth/**`, `/api/v1/health`), deshabilita CSRF y sessions, e inyecta el `JwtAuthenticationFilter`.

#### 3. Comando Git:
```bash
./mvnw test-compile -DskipTests
git add backend/src/main/java/pe/sga/entity/enums/RolUsuario.java backend/src/main/java/pe/sga/config/
git commit -m "feat(security): implementar seguridad stateless JWT, filtros y matriz de roles con SpEL"
```

---

### Commit 4: Entidades Base de Identidad, Repositorios, Semillas y Autenticación

**Objetivo:** Permitir el arranque completo del servidor, la creación de roles y usuario administrador en base de datos, y el inicio de sesión vía JWT.

#### 1. Crear carpetas:
```bash
mkdir -p backend/src/main/java/pe/sga/entity
mkdir -p backend/src/main/java/pe/sga/repository
mkdir -p backend/src/main/java/pe/sga/dto/request
mkdir -p backend/src/main/java/pe/sga/service/impl
mkdir -p backend/src/main/java/pe/sga/controller
```

#### 2. Copiar archivos del dominio de Identidad:
* **Entidades JPA:**
  * `Rol.java`: Copiar a `backend/src/main/java/pe/sga/entity/Rol.java`
  * `Sede.java`: Copiar a `backend/src/main/java/pe/sga/entity/Sede.java`
  * `Usuario.java`: Copiar a `backend/src/main/java/pe/sga/entity/Usuario.java`
* **Repositorios Spring Data JPA:**
  * `RolRepository.java`: Copiar a `backend/src/main/java/pe/sga/repository/RolRepository.java`
  * `SedeRepository.java`: Copiar a `backend/src/main/java/pe/sga/repository/SedeRepository.java`
  * `UsuarioRepository.java`: Copiar a `backend/src/main/java/pe/sga/repository/UsuarioRepository.java`
* **DTOs de Entrada y Salida:**
  * `LoginRequestDTO.java`, `AuthResponseDTO.java`
  * `RolDTO.java`, `RolResponseDTO.java`
  * `SedeCreateDTO.java`, `SedeUpdateDTO.java`, `SedeResponseDTO.java`
  * `UsuarioCreateDTO.java`, `UsuarioUpdateDTO.java`, `UsuarioResponseDTO.java`
* **Servicios de Identidad:**
  * `IRolService.java` y `RolServiceImpl.java`
  * `ISedeService.java` y `SedeServiceImpl.java`
  * `IUsuarioService.java` y `UsuarioServiceImpl.java` (implementa autenticación, encriptación con BCrypt y mapeo de roles)
  * `IAdminLogsService.java` y `AdminLogsServiceImpl.java`
* **Sembrado Automático en Arranque:**
  * Copiar `backend/src/main/java/pe/sga/config/DataInitializer.java`
  * *Comportamiento:* Si la tabla de roles está vacía, inserta los 8 roles normativos, una sede por defecto y el usuario `admin` con contraseña `Password123!` encriptada.
* **Controladores REST:**
  * `HealthController.java`: Endpoint `/api/v1/health` para sondas de liveness.
  * `AuthController.java`: Endpoint `/api/v1/auth/login` y verificación de token.
  * `RolController.java`, `SedeController.java`, `UsuarioController.java`, `AdminLogsController.java`.

#### 3. Validación y Comando Git:
```bash
./mvnw test-compile -DskipTests
git add backend/src/main/java/pe/sga/
git commit -m "feat(auth-admin): implementar entidades de identidad, login JWT, seed automatico y modulo TI"
```

*Prueba de Verificación:* Iniciar el backend con `./mvnw spring-boot:run` y probar login:
```bash
curl -X POST http://localhost:8085/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"Password123!"}'
```

---

### Commit 5: Catálogos Maestros y Motor Dinámico de JasperReports

**Objetivo:** Habilitar los maestros portuarios y aduaneros (puertos, tipos de carga, mercancías) y la generación de reportes oficiales en formato PDF.

#### 1. Crear carpetas:
```bash
mkdir -p backend/src/main/java/pe/sga/dto/reporte
```

#### 2. Copiar archivos:
* **Entidades:**
  * `CatalogoPuerto.java`, `CatalogoTipoCarga.java`, `CatalogoMercancia.java`
* **Repositorios:**
  * `CatalogoPuertoRepository.java`, `CatalogoTipoCargaRepository.java`, `CatalogoMercanciaRepository.java`
* **DTOs:**
  * Request: `CatalogoPuertoDTO.java`, `CatalogoTipoCargaDTO.java`, `CatalogoMercanciaRequestDTO.java`
  * Response: `CatalogoPuertoResponseDTO.java`, `CatalogoTipoCargaResponseDTO.java`, `CatalogoMercanciaResponseDTO.java`
  * Reporte: `PuertoReporteItemDTO.java`, `SedeReporteItemDTO.java`, `UsuarioReporteItemDTO.java`
* **Servicios e Implementaciones:**
  * `ICatalogoPuertoService.java` y `CatalogoPuertoServiceImpl.java`
  * `ICatalogoTipoCargaService.java` y `CatalogoTipoCargaServiceImpl.java`
  * `ICatalogoMercanciaService.java` y `CatalogoMercanciaServiceImpl.java`
  * `IReporteJasperService.java` y `ReporteJasperServiceImpl.java`
    * Genera reportes PDF compilando diseños JasperReports en memoria y volcando bytes con `JasperExportManager.exportReportToPdf(jasperPrint)`.
* **Controladores:**
  * `CatalogoPuertoController.java`, `CatalogoTipoCargaController.java`, `CatalogoMercanciaController.java`
  * Cada uno incluye endpoints para descarga directa de PDF institucional (ej. `GET /api/v1/catalogo-puertos/reporte/pdf`).

#### 3. Comando Git:
```bash
./mvnw test-compile -DskipTests
git add backend/src/main/java/pe/sga/
git commit -m "feat(catalogos-jasper): implementar catalogos maestros y motor dinamico de reportes JasperReports PDF"
```

---

### Commit 6: Servicio de Almacenamiento Desacoplado y Gestión Documental Polimórfica

**Objetivo:** Permitir la subida de archivos físicos (PDFs de DUA, guías de remisión, tickets de pesaje, comprobantes de pago) asociados a cualquier entidad del sistema.

#### 1. Copiar archivos:
* **Enums:**
  * `CategoriaDocumento.java` (FACTURA, GUIA_REMISION, DUA_DAM, TICKET_PESAJE, COMPROBANTE_PAGO, OTRO)
  * `EntidadDocumento.java` (COTIZACION, EXPEDIENTE, DESPACHO, ALMACEN, LIQUIDACION)
* **Entidad:**
  * `DocumentoAdjunto.java`: Relaciona `entidadTipo`, `entidadId`, nombre original, ruta física de almacenamiento, tamaño y mimeType.
* **Repositorio:**
  * `IDocumentoAdjuntoRepository.java`
* **DTOs:**
  * `DocumentoResponseDTO.java`
* **Servicios:**
  * `IStorageService.java` y `LocalStorageServiceImpl.java` (gestión de carpetas en disco, resolución de nombres únicos con UUID y lectura binaria segura).
  * `IDocumentoService.java` y `DocumentoServiceImpl.java`.
* **Controlador:**
  * `DocumentoController.java`:
    * `POST /api/v1/documentos/upload`: Recepción multipart (`MultipartFile`).
    * `GET /api/v1/documentos/{id}/descargar`: Descarga de archivo con `Content-Disposition: attachment`.
    * `GET /api/v1/documentos/entidad/{tipo}/{id}`: Listado de adjuntos por entidad.
    * `DELETE /api/v1/documentos/{id}`: Eliminación física y lógica.

#### 2. Comando Git:
```bash
./mvnw test-compile -DskipTests
git add backend/src/main/java/pe/sga/
git commit -m "feat(storage): implementar servicio desacoplado de almacenamiento y gestion documental polimorfica"
```

---

### Commit 7: Módulo Comercial y Compuerta Financiera de Pre-Pago

**Objetivo:** Gestión de clientes, cotizaciones con cálculo de costos logísticos y registro/validación de abonos obligatorios.

#### 1. Copiar archivos:
* **Enums:**
  * `EstadoCotizacion.java` (BORRADOR, EN_REVISION, APROBADA, RECHAZADA)
  * `EstadoPagoCotizacion.java` (PENDIENTE, OBSERVADO, APROBADO)
  * `TipoAbonoCotizacion.java` (ADELANTO, TOTAL)
* **Entidades:**
  * `Cliente.java` (RUC/DNI, razón social, dirección, contacto, estado_activo).
  * `Cotizacion.java` (código `COT-YYYY-XXXX`, desglose de flete marítimo, flete terrestre, agenciamiento, seguro, IGV y total).
  * `CotizacionPago.java` (monto abonado, número de operación bancaria, comprobante adjunto, estado de validación).
* **Repositorios:**
  * `ClienteRepository.java`, `CotizacionRepository.java`, `CotizacionPagoRepository.java`
* **DTOs:**
  * Request: `ClienteRequestDTO.java`, `CotizacionRequestDTO.java`, `CotizacionRevisionDTO.java`, `CotizacionEstadoDTO.java`, `CotizacionPagoRegistrarDTO.java`, `CotizacionPagoValidarDTO.java`.
  * Response: `ClienteResponseDTO.java`, `CotizacionResponseDTO.java`, `CotizacionPagoResponseDTO.java`.
* **Servicios:**
  * `IClienteService.java` y `ClienteServiceImpl.java`.
  * `ICotizacionService.java` y `CotizacionServiceImpl.java` (incluye cálculo automático de costos y transición de estados).
  * `ICotizacionPagoService.java` y `CotizacionPagoServiceImpl.java` (valida el abono por parte del `CONTADOR` y actualiza la bandera `tienePagoAprobado`).
* **Controladores:**
  * `ClienteController.java`, `CotizacionController.java`, `CotizacionPagoController.java`.

#### 2. Comando Git:
```bash
./mvnw test-compile -DskipTests
git add backend/src/main/java/pe/sga/
git commit -m "feat(comercial-pagos): crear gestion de clientes, cotizaciones con calculo de flete y compuerta de validacion de pago"
```

---

### Commit 8: Operaciones Aduaneras (Gestión de Expedientes y DAM)

**Objetivo:** Control aduanero estricto de expedientes de importación/exportación, asignación de canal SUNAT y validación de levante.

#### 1. Copiar archivos:
* **Enums:**
  * `EstadoExpediente.java` (REGISTRADO, EN_TRAMITE, LEVANTE_AUTORIZADO, CONCLUIDO)
  * `CanalSunat.java` (VERDE, NARANJA, ROJO)
* **Entidad:**
  * `Expediente.java` (código `EXP-YYYY-XXXX`, número DAM/DUA de 18 dígitos, aduana de ingreso, canal SUNAT, fecha de numeración y levante).
* **Repositorio:**
  * `ExpedienteRepository.java`
* **DTOs:**
  * `ExpedienteCrearDTO.java`, `ExpedienteActualizarDTO.java`, `ExpedienteCanalDTO.java`, `ExpedienteLevanteDTO.java`, `ExpedienteResponseDTO.java`.
* **Servicio:**
  * `IExpedienteService.java` y `ExpedienteServiceImpl.java`.
  * *Regla de Negocio Crítica:* Al invocar `crear(ExpedienteCrearDTO dto)`, el servicio valida:
    ```java
    if (!cotizacion.isTienePagoAprobado()) {
        throw new BadRequestException("No se puede aperturar el expediente aduanero: La cotización asociada no cuenta con pago aprobado por Finanzas.");
    }
    ```
* **Controlador:**
  * `ExpedienteController.java` (endpoints para numerar DAM, asignar canal verde/naranja/rojo, registrar levante aduanero y exportar PDF).

#### 2. Comando Git:
```bash
./mvnw test-compile -DskipTests
git add backend/src/main/java/pe/sga/
git commit -m "feat(aduanas): implementar gestion de expedientes DAM, compuerta estricta de pago y control de canales SUNAT"
```

---

### Commit 9: Operaciones de Almacén y Logística de Transporte Terrestre

**Objetivo:** Recepción y pesaje VGM de carga en balanza de almacén, y programación de flota pesada con conductores MTC y unidades de transporte.

#### 1. Copiar archivos:
* **Entidades:**
  * `ControlAlmacen.java` (número de bultos, peso bruto, peso tara, peso neto VGM, ubicación física de estiba, estado de carga).
  * `Conductor.java` (licencia MTC, récord de conductor, teléfono, estado activo).
  * `UnidadTransporte.java` (placa tracto, placa carreta, configuración vehicular MTC, carga útil máxima).
  * `DespachoTransporte.java` (código de despacho, ruta origen-destino, estado: PROGRAMADO, EN_RUTA, ENTREGADO).
* **Repositorios:**
  * `ControlAlmacenRepository.java`, `ConductorRepository.java`, `UnidadTransporteRepository.java`, `DespachoTransporteRepository.java`.
* **DTOs:**
  * Almacén: `ControlAlmacenRequestDTO.java`, `ControlAlmacenSalidaDTO.java`, `ControlAlmacenResponseDTO.java`.
  * Flota: `ConductorRequestDTO.java`, `ConductorResponseDTO.java`, `UnidadTransporteRequestDTO.java`, `UnidadTransporteResponseDTO.java`.
  * Despacho: `DespachoTransporteCrearDTO.java`, `DespachoAsignarRecursosDTO.java`, `DespachoRutaDTO.java`, `DespachoEntregaDTO.java`, `DespachoTransporteResponseDTO.java`.
* **Servicios:**
  * `IControlAlmacenService.java` y `ControlAlmacenServiceImpl.java`.
  * `IConductorService.java` y `ConductorServiceImpl.java`.
  * `IUnidadTransporteService.java` y `UnidadTransporteServiceImpl.java`.
  * `IDespachoTransporteService.java` y `DespachoTransporteServiceImpl.java` (asigna camión y chofer, valida que no tengan servicios en ruta concurrentes, genera eventos de monitoreo).
* **Controladores:**
  * `ControlAlmacenController.java`, `ConductorController.java`, `UnidadTransporteController.java`, `DespachoTransporteController.java`.

#### 2. Comando Git:
```bash
./mvnw test-compile -DskipTests
git add backend/src/main/java/pe/sga/
git commit -m "feat(almacen-transporte): integrar pesaje VGM en almacen y despacho de flota terrestre con monitoreo en ruta"
```

---

### Commit 10: Liquidación Financiera, Facturación y Reportes Contables

**Objetivo:** Cierre contable de la operación, cálculo de tributos aduaneros, liquidación de gastos portuarios, cálculo de detracciones SPOT Banco de la Nación y emisión de comprobantes.

#### 1. Copiar archivos:
* **Entidad:**
  * `LiquidacionFinanciera.java` (gastos operativos, adelantos recibidos, saldo por cobrar, comprobante SUNAT, estado de pago).
* **Repositorio:**
  * `LiquidacionFinancieraRepository.java`.
* **DTOs:**
  * Request: `LiquidacionFinancieraRequestDTO.java`, `LiquidacionPagoDTO.java`.
  * Response: `LiquidacionFinancieraResponseDTO.java`, `LiquidacionEstimadoResponseDTO.java`, `FinanzasMetricasResponseDTO.java`.
* **Servicio:**
  * `ILiquidacionFinancieraService.java` y `LiquidacionFinancieraServiceImpl.java` (calcula impuestos, IGV 18%, detracción bancaria SPOT y métricas de rentabilidad).
* **Controlador:**
  * `LiquidacionFinancieraController.java` (cálculo de estimado previo, liquidación formal, registro de pago final y reporte PDF contable).

#### 2. Comando Git:
```bash
./mvnw test-compile -DskipTests
git add backend/src/main/java/pe/sga/
git commit -m "feat(finanzas): implementar liquidacion financiera final, calculos tributarios SPOT y reporte contable"
```

---

### Commit 11: Endurecimiento, Perfiles y Verificación Integral de los 126 Endpoints

**Objetivo:** Auditoría de compilación, aseguramiento de que el 100% de los endpoints respondan adecuadamente y congelamiento de versión backend.

#### 1. Comprobación y Empaquetado:
```bash
# Compilación completa y generación del archivo JAR ejecutable
./mvnw clean package -DskipTests
```
*Resultado esperado:*  
`BUILD SUCCESS` con generación de `target/sga-backend-0.0.1-SNAPSHOT.jar`.

#### 2. Validación de Arranque en Vivo:
```bash
java -jar target/sga-backend-0.0.1-SNAPSHOT.jar
```
*Comprobar logs:*
* Inicialización de Hibernate dialect MySQL 8.
* Ejecución de `DataInitializer` sembrando roles y usuario administrador.
* Mensaje: `Tomcat started on port 8085 (http) with context path '/'`.

#### 3. Comando Git Final:
```bash
git add .
git commit -m "test(hardening): validar suite completa de 126 endpoints, paquete JAR y backend listo para integracion"
```

---

## 4. Guía de Ejecución y Pruebas Rápidas con cURL

A continuación se presentan los comandos cURL esenciales para validar que la réplica del backend funciona al 100%:

### 1. Health Check Público
```bash
curl -i -X GET http://localhost:8085/api/v1/health
```
*Respuesta esperada:* HTTP 200 con `{ "status": "UP", "database": "CONNECTED" }`.

### 2. Autenticación y Obtención de Token JWT
```bash
curl -i -X POST http://localhost:8085/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "password": "Password123!"
  }'
```
*Copiar el campo `token` del JSON devuelto para las siguientes peticiones:*
```bash
export TOKEN="<pegar_aqui_el_jwt_token>"
```

### 3. Listar Sedes (Ruta Protegida)
```bash
curl -i -X GET http://localhost:8085/api/v1/sedes \
  -H "Authorization: Bearer $TOKEN"
```

### 4. Generar y Descargar Reporte PDF de Puertos en Caliente
```bash
curl -i -X GET http://localhost:8085/api/v1/catalogo-puertos/reporte/pdf \
  -H "Authorization: Bearer $TOKEN" \
  --output reporte_puertos.pdf
```
*Verificación:* Abrir el archivo `reporte_puertos.pdf` generado para comprobar la cabecera, tabla de registros y pie de página de JasperReports.

### 5. Probar Compuerta de Pago Pre-Expediente
Intentar aperturar un expediente sobre una cotización sin pago aprobado debe retornar HTTP 400:
```bash
curl -i -X POST http://localhost:8085/api/v1/expedientes \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "cotizacionId": 1,
    "numeroDam": "118-2026-10-000001",
    "aduana": "CALLAO"
  }'
```
*Respuesta esperada:* HTTP 400 con mensaje:  
`"No se puede aperturar el expediente aduanero: La cotización asociada no cuenta con pago aprobado por Finanzas."`

---

## 5. Resumen de Entregables del Backend

Al concluir los 11 commits de esta guía, el repositorio alternativo contará con:
* **16 Tablas JPA mapeadas en MySQL InnoDB:** Normalizadas en 3NF con borrado lógico transversal.
* **8 Enumeraciones normativas:** Control de flujo de cotizaciones, expedientes, pagos, aduanas y roles.
* **16 Repositorios Spring Data JPA:** Con soporte de consultas derivadas y paginación.
* **52 DTOs segregados:** Validación rigurosa de entradas y proyecciones de salida seguras.
* **19 Controladores REST:** Exponiendo 126 endpoints RESTful listos para el cliente Vue 3.
* **Seguridad Declarativa:** Matriz de roles SpEL sin fugas de acceso ni endpoints desprotegidos.
* **Motor JasperReports 7:** Emisión de reportes PDF oficiales para auditoría aduanera.
