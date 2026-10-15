# Desglose Arquitectural y Tecnológico del Backend (SGA)

Este documento presenta un análisis exhaustivo y técnico de las tecnologías, librerías, dependencias y patrones de diseño utilizados en el backend del **Sistema de Gestión Aduanera (SGA)** (`sga-backend`).

---

## 1. Núcleo y Runtime

| Componente | Versión / Herramienta | Rol y Justificación |
| :--- | :--- | :--- |
| **Java** | **Java 21 (LTS)** | Plataforma base moderna con soporte a Virtual Threads, mejoras de rendimiento del garbage collector, records y pattern matching. |
| **Spring Boot** | **4.1.1** (Starter Parent) | Framework principal para configuración automática, inyección de dependencias, contenedor de servlets embebido (Apache Tomcat) y arquitectura de microservicios o monolitos modulares. |
| **Apache Maven** | Maven Wrapper (`./mvnw`) | Gestión de ciclo de vida del proyecto, resolución de dependencias, compilación con procesamiento de anotaciones y empaquetado JAR ejecutable. |

---

## 2. Dependencias y Librerías Principales

A continuación se detalla cada una de las librerías declaradas en el [`pom.xml`](file:///home/jiodev/Documents/Proyects/SGA/backend/pom.xml) y su función específica en la solución:

### A. JasperReports (Generación Dinámica de Reportes PDF)
* **Dependencias:**
  * `net.sf.jasperreports:jasperreports` (v7.0.8)
  * `net.sf.jasperreports:jasperreports-pdf` (v7.0.8)
  * `net.sf.jasperreports:jasperreports-jdt` (v7.0.8)
* **¿Qué hace en el sistema?**
  * Es el motor encargado de generar reportes impresos en formato **PDF oficial** para auditoría, logística y catálogos maestros.
  * **Implementación destacada:** En lugar de depender de plantillas estáticas compiladas externamente (`.jrxml`), el servicio [`ReporteJasperServiceImpl.java`](file:///home/jiodev/Documents/Proyects/SGA/backend/src/main/java/pe/sga/service/impl/ReporteJasperServiceImpl.java) construye el diseño programáticamente con la API `JasperDesign`, aplicando caché de reportes compilados (`JasperReport`), inyección de parámetros dinámicos (`FECHA_GENERACION`, `TOTAL_*`) y colecciones de datos en memoria (`JRBeanCollectionDataSource`).
  * **Reportes implementados:**
    * Reporte oficial de usuarios y cuentas de acceso de seguridad TI.
    * Reporte maestro de sedes operativas y agencias aduaneras.
    * Catálogo de puertos y terminales aduaneros.

---

### B. Seguridad, Control de Acceso y JWT (JSON Web Tokens)
* **Dependencias:**
  * `org.springframework.boot:spring-boot-starter-security`
  * `io.jsonwebtoken:jjwt-api` (v0.13.0)
  * `io.jsonwebtoken:jjwt-impl` (v0.13.0, runtime)
  * `io.jsonwebtoken:jjwt-jackson` (v0.13.0, runtime)
* **¿Qué hace en el sistema?**
  * **Spring Security 6+:** Controla el ciclo de vida de autenticación y autorización mediante un pipeline `SecurityFilterChain` completamente *stateless* (sin sesiones en memoria del servidor), deshabilitando CSRF innecesario para APIs REST.
  * **JJWT (Java JWT):** Permite generar y validar tokens de acceso firmados criptográficamente (HMAC-SHA256 con clave secreta configurada en [`application.properties`](file:///home/jiodev/Documents/Proyects/SGA/backend/src/main/resources/application.properties)).
  * **Componentes clave:**
    * [`SecurityConfig.java`](file:///home/jiodev/Documents/Proyects/SGA/backend/src/main/java/pe/sga/config/SecurityConfig.java): Define qué rutas son públicas (login, health, Swagger/Docs si aplica) y cuáles exigen autenticación y roles específicos (`ADMIN`, `OPERADOR`, etc.).
    * [`JwtTokenProvider.java`](file:///home/jiodev/Documents/Proyects/SGA/backend/src/main/java/pe/sga/config/JwtTokenProvider.java): Genera el token con claims (usuario, rol, expiración) y verifica su validez y firma.
    * [`JwtAuthenticationFilter.java`](file:///home/jiodev/Documents/Proyects/SGA/backend/src/main/java/pe/sga/config/JwtAuthenticationFilter.java): Filtro HTTP tipo `OncePerRequestFilter` que extrae el token del encabezado `Authorization: Bearer <token>`, establece el `SecurityContextHolder` y valida permisos por petición.
    * **BCryptPasswordEncoder:** Cifrado unidireccional con sal aleatoria de contraseñas de usuarios.

---

### C. Persistencia y Base de Datos (JPA / Hibernate / MySQL)
* **Dependencias:**
  * `org.springframework.boot:spring-boot-starter-data-jpa`
  * `com.mysql:mysql-connector-j` (Driver oficial JDBC MySQL 8+)
* **¿Qué hace en el sistema?**
  * **Spring Data JPA & Hibernate:** Mapeo Objeto-Relacional (ORM). Convierte entidades Java a tablas relacionales en MySQL con soporte de transacciones (`@Transactional`), consultas JPQL y generación automática o validación de esquema (`spring.jpa.hibernate.ddl-auto=update`).
  * **Pool de Conexiones HikariCP:** Viene incluido por defecto en Spring Boot para un pooling eficiente y de alta concurrencia con la base de datos `SGAdb`.
  * **Inicializador de Datos:** [`DataInitializer.java`](file:///home/jiodev/Documents/Proyects/SGA/backend/src/main/java/pe/sga/config/DataInitializer.java) siembra automáticamente los roles del sistema, el superusuario `admin` con contraseña cifrada y los datos base de prueba si la base de datos está vacía.

---

### D. Capa Web MVC & REST
* **Dependencias:**
  * `org.springframework.boot:spring-boot-starter-webmvc`
* **¿Qué hace en el sistema?**
  * Expone los servicios de la aplicación a través de controladores REST (`@RestController`, `@RequestMapping("/api/v1/...")`).
  * Integra **Jackson (JSON)** para serialización y deserialización automática de objetos Java a formato JSON y viceversa.
  * Permite streaming directo de archivos binarios (ej. `ResponseEntity<byte[]>` o `InputStreamResource`) para descarga instantánea de reportes PDF.

---

### E. Validación de Datos (Bean Validation / Jakarta)
* **Dependencias:**
  * `org.springframework.boot:spring-boot-starter-validation`
* **¿Qué hace en el sistema?**
  * Valida en el backend las entradas enviadas por el frontend antes de que lleguen a la lógica de negocio.
  * Anotaciones utilizadas en DTOs: `@NotNull`, `@NotBlank`, `@Size`, `@Email`, `@Min`, `@Max`, `@Pattern`.
  * Si los datos no cumplen los requisitos, se intercepta automáticamente en el manejador global.

---

### F. Productividad y Reducción de Boilerplate (Lombok)
* **Dependencias:**
  * `org.projectlombok:lombok`
* **¿Qué hace en el sistema?**
  * Elimina código repetitivo mediante anotaciones procesadas en tiempo de compilación:
    * `@Getter` y `@Setter`: Generación de métodos de acceso.
    * `@Builder`: Implementación limpia del patrón Builder para instanciar objetos complejos.
    * `@NoArgsConstructor` y `@AllArgsConstructor`: Constructores vacíos y completos requeridos por JPA y serializadores.
    * `@RequiredArgsConstructor`: Inyección de dependencias mediante constructor (`final fields`), recomendado por Spring en lugar de `@Autowired`.
    * `@Slf4j`: Provee directamente la variable `log` para registro de trazas y eventos sin configurar loggers manualmente.

---

### G. Manejo Global de Errores y Excepciones
* **Componentes:**
  * [`GlobalExceptionHandler.java`](file:///home/jiodev/Documents/Proyects/SGA/backend/src/main/java/pe/sga/exception/GlobalExceptionHandler.java) con `@RestControllerAdvice`.
  * [`ResourceNotFoundException.java`](file:///home/jiodev/Documents/Proyects/SGA/backend/src/main/java/pe/sga/exception/ResourceNotFoundException.java) (HTTP 404).
  * [`BadRequestException.java`](file:///home/jiodev/Documents/Proyects/SGA/backend/src/main/java/pe/sga/exception/BadRequestException.java) (HTTP 400).
* **¿Qué hace en el sistema?**
  * Estandariza las respuestas de error en un formato JSON consistente (`timestamp`, `status`, `error`, `message`, `path`, lista de campos inválidos), evitando que se filtren excepciones crudas o trazas de pila (stacktraces) al cliente.

---

### H. Configuración Cross-Origin (CORS)
* **Componentes:**
  * [`CorsConfig.java`](file:///home/jiodev/Documents/Proyects/SGA/backend/src/main/java/pe/sga/config/CorsConfig.java)
* **¿Qué hace en el sistema?**
  * Permite la comunicación segura y sin bloqueos entre el frontend (Vite / React / Angular en `http://localhost:5173`, etc.) y el backend en el puerto `8085`, configurando cabeceras permitidas (`Authorization`, `Content-Type`), métodos HTTP (`GET`, `POST`, `PUT`, `DELETE`, `OPTIONS`) y credenciales.

---

## 3. Módulos de Negocio Implementados en el Backend

El backend organiza la operativa del Sistema de Gestión Aduanera en los siguientes dominios:

1. **Seguridad y Usuarios:** Autenticación JWT, gestión de roles (`ADMIN`, `OPERADOR_ADUANAS`, `SUPERVISOR`, `LIQUIDADOR`, `CLIENTE`), usuarios y sedes.
2. **Catálogos Maestros:** Puertos aduaneros (`CatalogoPuerto`), tipos de mercancías (`CatalogoMercancia`) y tipos de carga (`CatalogoTipoCarga`).
3. **Comercial:** Gestión de clientes (`Cliente`) y emisión/seguimiento de cotizaciones (`Cotizacion`).
4. **Operaciones y Trámites Aduaneros:** Gestión integral de expedientes aduaneros (`Expediente`), asignación de canales SUNAT (Verde, Naranja, Rojo) y regímenes aduaneros (Importación, Exportación, Tránsito).
5. **Logística y Transporte:** Registro de conductores (`Conductor`), unidades y flotas de transporte (`UnidadTransporte`) y despachos en tránsito (`DespachoTransporte`).
6. **Almacenamiento y Depósito:** Control de inventario en almacenes aduaneros (`ControlAlmacen`).
7. **Finanzas y Tributación:** Liquidación de derechos arancelarios, impuestos SUNAT (IGV, IPM, Percepciones) y facturación (`LiquidacionFinanciera`).
8. **Reportes:** Exportación a PDF con JasperReports de usuarios, sedes y puertos.

---

## 4. Estructura de Directorios del Código Fuente

```text
backend/
├── pom.xml                               # Declaración de dependencias Maven
└── src/main/
    ├── resources/
    │   ├── application.properties        # Configuración de puerto (8085), MySQL, JWT y JPA
    │   └── initial-data.json             # Semilla inicial para datos de catálogos y pruebas
    └── java/pe/sga/
        ├── SgaApplication.java           # Clase principal (Spring Boot Main)
        ├── config/                       # Seguridad, JWT, CORS e Inicializador de datos
        ├── controller/                   # Endpoints REST (16 controladores)
        ├── dto/                          # Objetos de transferencia de datos y DTOs de reportes
        ├── entity/                       # Entidades JPA y enums de negocio
        ├── exception/                    # Manejador global de excepciones HTTP
        ├── repository/                   # Repositorios JPA (Spring Data)
        └── service/                      # Interfaces y servicios de lógica de negocio + JasperReports
```

---

## 5. Resumen de Puertos y Variables de Entorno

| Variable | Valor por Defecto | Propósito |
| :--- | :--- | :--- |
| `SERVER_PORT` | `8085` | Puerto HTTP donde corre el servidor de Spring Boot. |
| `DB_URL` | `jdbc:mysql://localhost:3306/SGAdb...` | Cadena de conexión al servidor MySQL. |
| `DB_USERNAME` | `root` | Usuario de base de datos. |
| `DB_PASSWORD` | *(vacío)* | Contraseña de base de datos. |
| `JWT_SECRET` | *(clave predeterminada en config)* | Llave secreta para firma criptográfica de tokens JWT. |
| `JWT_EXPIRATION_MS`| `86400000` (24 horas) | Tiempo de vida de la sesión del token. |
