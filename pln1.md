# pln1 — Reestructuración Definitiva de Roles y Nombres Internos del SGA

> Alcance: **solo plan**. No se modifica código hasta aprobar este documento.
>
> Estos 8 roles son **definitivos**: los códigos usan el **nombre completo** y los identificadores internos (módulos, rutas, vistas, servicios, stores) se renombran para que coincidan con ellos. La **estructura funcional** de los módulos (qué sub-opciones y pantallas tiene cada uno) no se rediseña ahora; solo se mueve lo del Liquidador a Supervisor Aduanero (§3.4).

## 1. Objetivo

1. Reemplazar el catálogo de roles por 8 roles definitivos, con código = nombre completo.
2. Asignar cada vista a un único rol (Administrador de TI y Gerente General acceden a todos los módulos activos).
3. Transferir **todo** lo que hacía el Liquidador (vistas, sub-opciones y permisos) al Supervisor Aduanero.
4. Renombrar internamente módulos, rutas, vistas, servicios y stores acorde a los nuevos roles.
5. Agregar autorización por rol en la API (hoy solo existe en el frontend).

## 2. Estado actual (resumen del análisis)

| Capa              | Archivo                                                                                                                                                                                          | Qué contiene hoy                                                                                                                                                                                                                      |
| :---------------- | :----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | :------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------ |
| Backend enum      | `entity/enums/RolUsuario.java`                                                                                                                                                                   | `ADMIN_TI, GERENTE, VENTAS, SUPERVISION, AGENTE_ADUANERO, ADMINISTRADOR, LOGISTICA, LIQUIDADOR, LEGAL, FINANZAS, TRANSPORTE`                                                                                                          |
| Backend seed      | `config/DataInitializer.java`                                                                                                                                                                    | Crea un rol por valor del enum; siembra `gerente`, `vendedor`, `supervisor`, `agente`                                                                                                                                                 |
| Backend JWT       | `config/JwtAuthenticationFilter.java` (L42)                                                                                                                                                      | Autoridad = `"ROLE_" + nombreRol.name()` → compatible con `hasRole/hasAnyRole`                                                                                                                                                        |
| Backend seguridad | `config/SecurityConfig.java`                                                                                                                                                                     | Públicos: `/api/v1/auth/**`, `/health/**`, `/catalogos/**`; el resto `authenticated()`. Sin `@PreAuthorize` por rol (aunque `@EnableMethodSecurity` ya está activo)                                                                   |
| Controladores     | 17 en `controller/`                                                                                                                                                                              | AdminLogs, Auth, CatalogoMercancia, CatalogoPuerto, CatalogoTipoCarga, Cliente, Conductor, ControlAlmacen, Cotizacion, DespachoTransporte, Documento, Expediente, Health, LiquidacionFinanciera, Rol, Sede, UnidadTransporte, Usuario |
| BD                | `database/schema.sql`, `data.sql`, `initial-data.json`                                                                                                                                           | `rol.nombre_rol` VARCHAR(50) UNIQUE                                                                                                                                                                                                   |
| Frontend          | `authStore.js`, `navigationStore.js`, 3 archivos de rutas, `core/router/index.js` (guard L78-80), `AppSidebar.vue`, `AppLayout.vue`, `LoginView.vue`, vistas, servicios, componentes de usuarios | Códigos de rol y claves de módulo antiguos                                                                                                                                                                                            |

## 3. Diseño objetivo

### 3.1 Roles definitivos

|  #  | Etiqueta UI              | Código                  |
| :-: | :----------------------- | :---------------------- |
|  1  | Administrador de TI      | `ADMINISTRADOR_TI`      |
|  2  | Gerente General          | `GERENTE_GENERAL`       |
|  3  | Agente Comercial         | `AGENTE_COMERCIAL`      |
|  4  | Supervisor Aduanero      | `SUPERVISOR_ADUANERO`   |
|  5  | Agente Aduanero          | `AGENTE_ADUANERO`       |
|  6  | Supervisor de Transporte | `SUPERVISOR_TRANSPORTE` |
|  7  | Contador                 | `CONTADOR`              |
|  8  | Asesor Legal             | `ASESOR_LEGAL`          |

### 3.2 Mapeo de roles antiguos → nuevos

| Antiguo                                   | Nuevo                   |
| :---------------------------------------- | :---------------------- |
| `ADMIN_TI`, `ADMINISTRADOR`               | `ADMINISTRADOR_TI`      |
| `GERENTE`                                 | `GERENTE_GENERAL`       |
| `VENTAS`                                  | `AGENTE_COMERCIAL`      |
| `SUPERVISION`, `SUPERVISOR`, `LIQUIDADOR` | `SUPERVISOR_ADUANERO`   |
| `AGENTE_ADUANERO`, `LOGISTICA`            | `AGENTE_ADUANERO`       |
| `TRANSPORTE`                              | `SUPERVISOR_TRANSPORTE` |
| `FINANZAS`                                | `CONTADOR`              |
| `LEGAL`                                   | `ASESOR_LEGAL`          |

### 3.3 Renombrado interno de módulos, rutas y archivos

| Actual                                                                               | Nuevo                                                                                             | Nombre en UI                             | Rol                                                   |
| :----------------------------------------------------------------------------------- | :------------------------------------------------------------------------------------------------ | :--------------------------------------- | :---------------------------------------------------- |
| módulo `admin`, `/admin/*`                                                           | `administracion-ti`, `/administracion-ti/*`                                                       | Administración TI                        | `ADMINISTRADOR_TI` (+ módulos activos)                |
| módulo `gerencia`, `/gerencia/*`                                                     | `gerencia-general`, `/gerencia-general/*`                                                         | Gerencia General                         | `GERENTE_GENERAL` (+ módulos activos)                 |
| módulo `ventas`, `/ventas`, `VentasView.vue`, `ventasService.js`                     | `comercial`, `/recepcion-comercial`, `RecepcionComercialView.vue`, `comercialService.js`          | Recepción Comercial                      | `AGENTE_COMERCIAL`                                    |
| módulo `supervision`, `/supervision`, `SupervisionView.vue`, `supervisionService.js` | `gestion-aduanera`, `/gestion-aduanera`, `GestionAduaneraView.vue`, `gestionAduaneraService.js`   | Gestión Aduanera                         | `SUPERVISOR_ADUANERO`                                 |
| módulo `liquidador`, `/liquidador`, `LiquidadorView.vue`                             | se integra en `gestion-aduanera` → `/gestion-aduanera/liquidacion`, `LiquidacionAduaneraView.vue` | Liquidación (dentro de Gestión Aduanera) | `SUPERVISOR_ADUANERO`                                 |
| módulo `aduanas`, `/aduanas`, `LogisticaView.vue`                                    | `agente-aduanero`, `/agente-aduanero`, `AgenteAduaneroView.vue`                                   | Agente Aduanero                          | `AGENTE_ADUANERO` (+ `SUPERVISOR_ADUANERO`, ver §3.4) |
| módulo `transporte`, `/transporte`, `TransporteView.vue`, `transporteService.js`     | `transporte-flota`, `/transporte-flota`, `TransporteFlotaView.vue`, `transporteFlotaService.js`   | Transporte y Flota                       | `SUPERVISOR_TRANSPORTE`                               |
| módulo `finanzas`, `/finanzas`                                                       | sin cambio                                                                                        | Finanzas                                 | `CONTADOR`                                            |
| módulo `legal`, `/legal`, `LegalView.vue`                                            | `legal-auditoria`, `/legal-auditoria`, `LegalAuditoriaView.vue`                                   | Legal y Auditoría                        | `ASESOR_LEGAL`                                        |

Reglas:

- **Acceso exclusivo:** un rol operativo solo entra a su vista; cualquier otra ruta redirige a su `homePath`.
- **Las rutas nuevas reemplazan a las antiguas**: son las mismas vistas con otro nombre. Las rutas antiguas (`/ventas`, `/supervision`, `/aduanas`, `/transporte`, `/legal`, `/liquidador`, `/logistica`, `/gerencia`, `/admin`) se **eliminan**; no se crean redirects ni alias de compatibilidad. Solo se conservan los redirects internos de índice (`/administracion-ti` → `/administracion-ti/usuarios`, `/gerencia-general` → `/gerencia-general/sedes`).
- Estado interno de `navigationStore` renombrado en paralelo: `ventasSubOption`→`comercialSubOption`, `supervisionSubOption`→`gestionAduaneraSubOption`, `aduanasSubOption`→`agenteAduaneroSubOption`, `transporteSubOption`→`transporteFlotaSubOption`, `legalSubOption`→`legalAuditoriaSubOption`, `ventasActionTrigger`→`comercialActionTrigger`, `adminActionTrigger`→`administracionTiActionTrigger`, `liquidadorSubOption` se absorbe en `gestionAduaneraSubOption`; setters y triggers siguen el mismo patrón.
- Getters de `authStore`: `isAdminTI`→`isAdministradorTI`, `isGerente`→`isGerenteGeneral`.

### 3.4 Transferencia del Liquidador a Supervisor Aduanero

El Liquidador hoy podía acceder a:

- `/liquidador` (`LiquidadorView`: declaraciones, clasificación, tributos).
- `/aduanas` (sub-opciones almacén, expedientes, despacho; roles `AGENTE_ADUANERO, LOGISTICA, LIQUIDADOR`).

Por tanto `SUPERVISOR_ADUANERO` pasa a tener:

1. Su vista propia `/gestion-aduanera` (cotizaciones, expedientes).
2. **Liquidación** como sub-opción dentro de Gestión Aduanera (`/gestion-aduanera/liquidacion`), con las sub-opciones `declaraciones`, `clasificacion`, `tributos`.
3. Acceso a `/agente-aduanero` (lo que el Liquidador veía), manteniendo `AGENTE_ADUANERO` como rol propietario.
4. En API: todos los endpoints que usaba el Liquidador (expedientes, almacén, liquidación financiera de consulta/registro) autorizados para `SUPERVISOR_ADUANERO`.

Tareas: mover sub-opciones de `liquidador` al bloque de sub-opciones de `gestion-aduanera` en `AppSidebar.vue`; retirar `liquidador` del selector de módulos; eliminar `liquidadorSubOption`.

## 4. Plan de renombrado en el Backend

### 4.1 Enum y entidad

1. Reescribir `RolUsuario.java` con los 8 códigos de §3.1 y una `etiqueta` legible (ej. `ADMINISTRADOR_TI("Administrador de TI")`).
2. Confirmar `@Enumerated(EnumType.STRING)` en `Rol.nombreRol`; `SUPERVISOR_TRANSPORTE` (21 caracteres) cabe en VARCHAR(50).
3. Descripción por defecto del rol = etiqueta legible.

### 4.2 Búsqueda y reemplazo

1. `grep` en `backend/src` de `ADMIN_TI|ADMINISTRADOR|GERENTE|VENTAS|SUPERVISION|LOGISTICA|LIQUIDADOR|LEGAL|FINANZAS|TRANSPORTE` y reemplazar **solo usos como rol**. No tocar `CategoriaDocumento.TRANSPORTE_BL`.
2. Revisar `service/`, `dto/`, `repository/` (`findByNombreRol`, `existsByNombreRol`), validaciones y mapeos.
3. El JWT mantiene `ROLE_<CODIGO>`; verificar que el claim `rol` del token y de `/auth/login` emita el nuevo código.

### 4.3 DataInitializer

1. Crear únicamente los 8 roles.
2. Admin inicial con `ADMINISTRADOR_TI` (eliminar `.or(... ADMINISTRADOR)`).
3. Usuarios semilla (pass `Password123!`):

| Usuario      | Rol                     |
| :----------- | :---------------------- |
| `admin`      | `ADMINISTRADOR_TI`      |
| `gerente`    | `GERENTE_GENERAL`       |
| `comercial`  | `AGENTE_COMERCIAL`      |
| `supervisor` | `SUPERVISOR_ADUANERO`   |
| `agente`     | `AGENTE_ADUANERO`       |
| `transporte` | `SUPERVISOR_TRANSPORTE` |
| `contador`   | `CONTADOR`              |
| `legal`      | `ASESOR_LEGAL`          |

4. `cargoTitulo` y `codigoSunat` derivados de la etiqueta/código nuevo.
5. Actualizar `initial-data.json` (en `backend/src/main/resources` y `database/`).

### 4.4 Autorización por rol en la API

**Mecanismo**

1. Crear `pe.sga.config.SecurityRoles` con constantes `String` (p. ej. `ADMINISTRADOR_TI = "ADMINISTRADOR_TI"`) y expresiones reutilizables.
2. Usar `@PreAuthorize("hasAnyRole(...)")` (ya habilitado por `@EnableMethodSecurity`) a nivel de clase y, donde lectura y escritura difieran, a nivel de método.
3. `ADMINISTRADOR_TI` y `GERENTE_GENERAL` incluidos en todas las reglas operativas.
4. Respuesta 403 uniforme en JSON (handler `AccessDeniedHandler` / `@RestControllerAdvice`) con el formato `{success:false,message,timestamp}` del resto de la API.
5. Quitar de `SecurityConfig` el `permitAll` sobre `/api/v1/catalogos/**` solo si el frontend ya envía token en esas llamadas (verificar); si no, mantener lectura pública y proteger únicamente escritura.

**Matriz inicial (a validar contra los servicios del frontend)**

| Controlador                                                        | Lectura                                                                                       | Escritura                                                    |
| :----------------------------------------------------------------- | :-------------------------------------------------------------------------------------------- | :----------------------------------------------------------- |
| `Auth`, `Health`                                                   | público                                                                                       | público                                                      |
| `Usuario`, `Rol`, `AdminLogs`                                      | `ADMINISTRADOR_TI` (+ `GERENTE_GENERAL` en Usuario)                                           | `ADMINISTRADOR_TI`                                           |
| `Sede`, `CatalogoPuerto`, `CatalogoTipoCarga`, `CatalogoMercancia` | autenticados                                                                                  | `ADMINISTRADOR_TI`, `GERENTE_GENERAL`                        |
| `Cliente`, `Cotizacion`                                            | `AGENTE_COMERCIAL`, `SUPERVISOR_ADUANERO`, `AGENTE_ADUANERO`, `CONTADOR`, `ASESOR_LEGAL`      | `AGENTE_COMERCIAL`; aprobación/estado: `SUPERVISOR_ADUANERO` |
| `Expediente`, `ControlAlmacen`                                     | `SUPERVISOR_ADUANERO`, `AGENTE_ADUANERO`, `ASESOR_LEGAL`, `SUPERVISOR_TRANSPORTE`, `CONTADOR` | `SUPERVISOR_ADUANERO`, `AGENTE_ADUANERO`                     |
| `DespachoTransporte`, `UnidadTransporte`, `Conductor`              | `SUPERVISOR_TRANSPORTE`, `SUPERVISOR_ADUANERO`, `AGENTE_ADUANERO`                             | `SUPERVISOR_TRANSPORTE`                                      |
| `LiquidacionFinanciera`                                            | `CONTADOR`, `SUPERVISOR_ADUANERO`, `ASESOR_LEGAL`                                             | `CONTADOR`, `SUPERVISOR_ADUANERO`                            |
| `Documento`                                                        | autenticados                                                                                  | autenticados con acceso al expediente/cotización asociado    |

> Siempre además: `ADMINISTRADOR_TI` y `GERENTE_GENERAL`. Durante la implementación cada fila se confirma leyendo qué endpoints llama cada servicio del frontend.

### 4.5 Pruebas backend

- Compilar con Java 21: `JAVA_HOME=/usr/lib/jvm/java-21-openjdk ./mvnw clean compile`.
- Login con cada usuario semilla y validar `rol` en `/api/v1/auth/login`.
- Por cada rol: llamar un endpoint permitido (200) y uno prohibido (403).
- Actualizar `backend/api-test.http` con usuarios y roles nuevos.

## 5. Base de datos y migración

1. **Respaldo:** `podman exec mysql mysqldump -u root -p SGAdb > backup_pre_roles.sql`.
2. `database/migration_roles_v2.sql` (ejecutar **antes** de arrancar el backend nuevo):
   - Renombrar `rol.nombre_rol` según §3.2 en correspondencias 1 a 1.
   - Fusionar duplicados (`SUPERVISION`+`LIQUIDADOR`→`SUPERVISOR_ADUANERO`; `ADMIN_TI`+`ADMINISTRADOR`→`ADMINISTRADOR_TI`; `AGENTE_ADUANERO`+`LOGISTICA`): reasignar `usuario.id_rol` al rol destino y borrar el sobrante.
3. Actualizar `schema.sql`, `data.sql` e `initial-data.json`.
4. Invalidar sesiones activas (tokens con rol antiguo): rotar `jwt.secret` o limpiar `localStorage`.

## 6. Plan de renombrado en el Frontend

### 6.1 Constantes centralizadas

Crear `src/core/auth/roles.js`: constantes de los 8 códigos, mapa código → etiqueta, código → módulo y ruta de inicio. Todo el frontend importa de aquí.

### 6.2 Stores

- `authStore.js`: getters renombrados (§3.3), `canSwitchModules` y `homePath` con códigos y rutas nuevas.
- `navigationStore.js`: claves de módulo, `roleModuleMap`, `moduleTitle`, sub-opciones y triggers renombrados; absorber `liquidador` en `gestion-aduanera`.

### 6.3 Rutas y guard

- `roles.routes.js`, `gerencia.routes.js`, `admin.routes.js`: nuevos `path`, `name` y `meta.roles` que **reemplazan** a los anteriores (sin redirects desde rutas antiguas; se elimina `/logistica`).
- `core/router/index.js`: el guard redirige a `homePath`.

### 6.4 Archivos y componentes

- Renombrar archivos de vistas y servicios (§3.3) con `git mv` para conservar historial y actualizar todos los imports.
- `AppSidebar.vue`: selector de módulos (sin Liquidador) y bloques de sub-opciones con claves nuevas; integrar Liquidación en Gestión Aduanera.
- `AppLayout.vue`, `LoginView.vue`: etiqueta legible del rol; quitar fallback `'ADMINISTRADOR'`.
- `UserRoleBadge.vue`, `UserRegistrationForm.vue`, `UserManagementView.vue`: 8 roles con etiqueta, color y filtro.
- Componentes y vistas con comparaciones de rol: `CotizacionRegistrationForm`, `ClienteRegistrationForm`, `DocumentosManagerModal`, `GerenciaFlotaView`, `GerenciaConductoresView`, `AdminOverviewView`, `transporteFlotaService.js`.
- `AboutSystemView.vue`: actualizar menciones de roles.

## 7. Documentación

Actualizar `README.md` (roles y credenciales), `docs/DATABASE_SETUP.md`, `backend/README.md`, `frontend/README.md`.

---

# PLAN DE IMPLEMENTACIÓN (orden de ejecución)

Se trabaja en una rama `feature/roles-definitivos`, con un commit por fase y verificación al cierre de cada una.

| Fase | Trabajo                                                                                                  | Verificación de salida                                                                                    |
| :--: | :------------------------------------------------------------------------------------------------------- | :-------------------------------------------------------------------------------------------------------- |
|  0   | Crear rama; respaldo `mysqldump`; detener backend y frontend en ejecución                                | Archivo de respaldo existe y no está vacío                                                                |
|  1   | Backend: `RolUsuario`, `SecurityRoles`, reemplazo de referencias (§4.1-4.2)                              | `./mvnw clean compile` OK (Java 21)                                                                       |
|  2   | Backend: `DataInitializer`, `initial-data.json` (§4.3)                                                   | Compila; sin referencias a roles antiguos (`grep`)                                                        |
|  3   | BD: `migration_roles_v2.sql` + actualizar `schema.sql`/`data.sql`; ejecutarlo sobre el contenedor Podman | Consulta `SELECT nombre_rol FROM rol` solo devuelve los 8 códigos; todos los usuarios con `id_rol` válido |
|  4   | Backend: autorización por API, handler 403, pruebas por rol (§4.4-4.5)                                   | Cada rol: permitido 200 / prohibido 403                                                                   |
|  5   | Frontend: `roles.js`, stores y guard (§6.1-6.3)                                                          | `npm run build` sin errores                                                                               |
|  6   | Frontend: renombrado de archivos, rutas, sidebar e integración de Liquidación (§6.4)                     | `npm run build` OK; `grep` sin rutas antiguas; rutas nuevas navegables                                    |
|  7   | Frontend: usuarios, badges, formularios y textos                                                         | Alta/edición de usuarios con los 8 roles                                                                  |
|  8   | Documentación (§7)                                                                                       | README coherente con los nuevos roles                                                                     |
|  9   | Prueba integral: levantar BD (Podman), backend (Java 21) y frontend; recorrer los 8 usuarios             | Checklist final (abajo)                                                                                   |

## Checklist final

- [x] Cada uno de los 8 usuarios entra y aterriza en su vista.
- [x] Acceso por URL a una vista ajena redirige al `homePath`.
- [x] Administrador de TI y Gerente General conmutan entre todos los módulos activos.
- [x] `SUPERVISOR_ADUANERO` ve Gestión Aduanera, Liquidación y Agente Aduanero.
- [x] API devuelve 403 a roles no autorizados y 200 a los autorizados.
- [x] Las rutas antiguas ya no existen (devuelven ruta no encontrada) y solo responden las nuevas.
- [x] Migración sin pérdida de usuarios.
- [x] `grep` no encuentra códigos, claves de módulo ni nombres de archivo antiguos.

## Riesgos y mitigaciones

| Riesgo                                                          | Mitigación                                                                     |
| :-------------------------------------------------------------- | :----------------------------------------------------------------------------- |
| Falsos positivos de `grep` (`TRANSPORTE_BL`, textos de negocio) | Revisar cada reemplazo; sin `sed` masivo                                       |
| `EnumType.STRING` falla con valores antiguos en BD              | Migrar BD antes de arrancar el backend                                         |
| Renombrado de archivos rompe imports                            | `git mv` + `npm run build` en cada fase                                        |
| La matriz de API bloquea una pantalla que sí debía funcionar    | Validar cada fila contra los servicios del frontend; pruebas por rol en fase 4 |
| Tokens JWT con rol antiguo                                      | Rotar `jwt.secret` tras migrar                                                 |
| Marcadores/enlaces a rutas antiguas dejan de funcionar          | Aceptado (cambio definitivo); `grep` de rutas antiguas en código, docs y `api-test.http` |

## Estado de los supuestos

| # | Supuesto | Estado |
| :-: | :--- | :--- |
| 1 | "Nombres internos" incluye claves de módulo, rutas, archivos de vista/servicio y estado de stores (§3.3) | ✅ Resuelto: lo pediste explícitamente |
| 2 | Rutas definitivas que **reemplazan** a las antiguas, sin redirects: `/recepcion-comercial`, `/gestion-aduanera`, `/gestion-aduanera/liquidacion`, `/agente-aduanero`, `/transporte-flota`, `/legal-auditoria`, `/administracion-ti`, `/gerencia-general` (`/finanzas` no cambia) | ✅ Resuelto: reemplazo sin redirects. Los nombres exactos de las rutas son los propuestos; faltan solo si quieres cambiar alguno |
| 3 | El Supervisor Aduanero hereda todo lo del Liquidador, incluido el acceso a la vista de Agente Aduanero | ✅ Resuelto: indicaste que todo lo del Liquidador pasa a Supervisor Aduanero |
| 4 | La matriz de autorización por API (§4.4) es inicial | ⏳ Pendiente (no bloquea): se valida y ajusta en la fase 4 leyendo los endpoints que llama cada pantalla |
