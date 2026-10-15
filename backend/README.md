# SGA Backend - Sistema de Gestión Aduanera

API Backend desarrollada con **Spring Boot 4 / Java 21**, persistencia en **MySQL** (`SGAdb`), autenticación desacoplada **JWT**, y motor de reportes dinámicos **JasperReports 7.0.8**.

---

## 1. Alcance Implementado (Hasta Módulo 1)

En estricto apego a las directivas del proyecto:
- **Base de Datos Separada**:
  - DDL (Estructura de tablas e índices): [SGA/database/schema.sql](../database/schema.sql).
  - DML (Datos iniciales y semillas): [SGA/database/data.sql](../database/data.sql).
  - Tablas:
    - `rol` (con `estado_activo` para soporte de eliminación lógica por estado).
    - `sede` (con campo `estado`).
    - `usuario` (con campo `estado_activo`).
    - `catalogo_puerto` (con campo `estado`).
    - `catalogo_tipo_carga` (con campo `estado`).
- **Persistencia Directa**: Ningún dato relevante existe solo en frontend; cada atributo se mapea 1:1 con la base de datos MySQL.
- **Acceso Exclusivo por API**: Todas las operaciones de lectura, creación, edición, desactivación lógica y reportes están desacopladas en controladores REST.
- **Sin Eliminación de Código Previo**: Los archivos legados (`Sigad_legacy`) se conservaron intactos.
- **Sin Ejecución de Pruebas**: No se ejecutan pruebas de integración ni arranque contra MySQL para permitir que realices la vinculación limpia a tu nueva base de datos.

---

## 2. Estructura del Proyecto (`SGA`)

```text
SGA/
├── database/
│   ├── schema.sql                # Estructura DDL de tablas e índices para SGAdb
│   ├── data.sql                  # Datos iniciales y semillas (roles, admin, sedes, catálogos)
│   └── database-rewrite.sql      # Script de referencia original
│
└── backend/
    ├── api-test.http             # Colección de peticiones para VS Code / IntelliJ REST Client
    ├── pom.xml                   # Maven POM con Spring Boot, MySQL, JJWT y JasperReports
    ├── src/main/java/pe/sga/
    │   ├── SgaApplication.java   # Punto de entrada Spring Boot
    │   ├── config/               # Seguridad, JWT, CORS, inicializador de datos
    │   │   ├── CorsConfig.java
    │   │   ├── DataInitializer.java
    │   │   ├── JwtAuthenticationFilter.java
    │   │   ├── JwtTokenProvider.java
    │   │   └── SecurityConfig.java
    │   ├── controller/           # Endpoints RESTful
    │   │   ├── AuthController.java
    │   │   ├── CatalogoPuertoController.java
    │   │   ├── CatalogoTipoCargaController.java
    │   │   ├── HealthController.java
    │   │   ├── RolController.java
    │   │   ├── SedeController.java
    │   │   └── UsuarioController.java
    │   ├── dto/                  # Requests, Responses y Reportes
    │   │   ├── reporte/
    │   │   ├── request/
    │   │   └── response/
    │   ├── entity/               # Entidades JPA (Módulo 1 y Catálogos)
    │   │   ├── enums/RolUsuario.java
    │   │   ├── CatalogoPuerto.java
    │   │   ├── CatalogoTipoCarga.java
    │   │   ├── Rol.java
    │   │   ├── Sede.java
    │   │   └── Usuario.java
    │   ├── exception/            # Manejador global de excepciones HTTP
    │   │   ├── BadRequestException.java
    │   │   ├── GlobalExceptionHandler.java
    │   │   └── ResourceNotFoundException.java
    │   ├── repository/           # Repositorios Spring Data JPA
    │   │   ├── CatalogoPuertoRepository.java
    │   │   ├── CatalogoTipoCargaRepository.java
    │   │   ├── RolRepository.java
    │   │   ├── SedeRepository.java
    │   │   └── UsuarioRepository.java
    │   └── service/              # Lógica de negocio y JasperReports
    │       ├── ICatalogoPuertoService.java
    │       ├── ICatalogoTipoCargaService.java
    │       ├── IReporteJasperService.java
    │       ├── IRolService.java
    │       ├── ISedeService.java
    │       ├── IUsuarioService.java
    │       └── impl/
    │           ├── CatalogoPuertoServiceImpl.java
    │           ├── CatalogoTipoCargaServiceImpl.java
    │           ├── ReporteJasperServiceImpl.java
    │           ├── RolServiceImpl.java
    │           ├── SedeServiceImpl.java
    │           └── UsuarioServiceImpl.java
    └── src/main/resources/
        └── application.properties # Configuración de base de datos MySQL SGAdb y JWT
```

---

## 3. Endpoints Principales de la API

| Módulo | Método | Endpoint | Descripción |
|---|---|---|---|
| **Salud** | `GET` | `/api/v1/health` | Diagnóstico del backend y versión de SGA |
| **Auth** | `POST` | `/api/v1/auth/login` | Login con credenciales (`admin` / `Password123!`) |
| **Auth** | `GET` | `/api/v1/auth/me` | Obtiene el usuario autenticado |
| **Usuarios** | `GET` | `/api/v1/usuarios` | Listar usuarios (`?soloActivos=true`) |
| **Usuarios** | `POST` | `/api/v1/usuarios` | Crear nuevo usuario |
| **Usuarios** | `PUT` | `/api/v1/usuarios/{id}` | Actualizar datos del usuario |
| **Usuarios** | `DELETE` | `/api/v1/usuarios/{id}` | Eliminación lógica por estado (`estado_activo = false`) |
| **Usuarios** | `PATCH` | `/api/v1/usuarios/{id}/reactivar` | Reactivación lógica del usuario |
| **Usuarios** | `GET` | `/api/v1/usuarios/exportar-pdf` | Exportación a PDF con JasperReports |
| **Sedes** | `GET` | `/api/v1/sedes` | Listar sedes (`?soloOperativas=true`) |
| **Sedes** | `POST` | `/api/v1/sedes` | Crear sede |
| **Sedes** | `PUT` | `/api/v1/sedes/{id}` | Actualizar sede |
| **Sedes** | `DELETE` | `/api/v1/sedes/{id}` | Eliminación lógica por estado (`estado = 'INACTIVO'`) |
| **Sedes** | `PATCH` | `/api/v1/sedes/{id}/reactivar` | Reactivación de sede (`estado = 'OPERATIVO'`) |
| **Sedes** | `GET` | `/api/v1/sedes/exportar-pdf` | Exportación de sedes a PDF con JasperReports |
| **Roles** | `GET` | `/api/v1/roles` | Listar roles |
| **Roles** | `DELETE` | `/api/v1/roles/{id}` | Eliminación lógica por estado (`estado_activo = false`) |
| **Catálogos** | `GET` | `/api/v1/catalogos/puertos` | Listar puertos aduaneros |
| **Catálogos** | `GET` | `/api/v1/catalogos/tipos-carga` | Listar tipos de carga |

---

## 4. Instrucciones para vincular y ejecutar

> 📖 **Guía Completa de Base de Datos:** Consulta la [**Guía de Configuración y Conexión de Base de Datos**](../docs/DATABASE_SETUP.md) para detalles sobre Podman, Docker y solución de problemas comunes.

1. **Configurar credenciales locales (sin subirlas a Git):**
   ```bash
   cp application-local.properties.example application-local.properties
   ```
   Ajusta tu contraseña en `application-local.properties`.

2. **Crear y poblar la base de datos en MySQL:**
   ```bash
   # Paso A: Crear estructura de tablas
   mysql -u root -p SGAdb < ../database/schema.sql

   # Paso B: Cargar datos iniciales
   mysql -u root -p SGAdb < ../database/data.sql
   ```
   *(O permite que Hibernate y `DataInitializer` la inicialicen automáticamente al arrancar).*

3. **Iniciar el backend:**
   ```bash
   ./mvnw spring-boot:run
   ```
   *El backend se ejecutará en `http://localhost:8085`. Mantén esta terminal abierta.*
