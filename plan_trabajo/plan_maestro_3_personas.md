# Plan Maestro de Construcción para 3 Personas
**Transaduanas Platform (Powered by SGA Core)**  
*Orquestación Integral de Desarrollo: Mantenedor/DBA + Backend Completo + Frontend Completo*

---

## 1. Visión y Estrategia del Equipo de 3 Personas

Para construir **Transaduanas Platform** de manera limpia, profesional, sin colisiones de código y con sincronización perfecta entre capas, el trabajo se divide de forma estricta entre **3 personas con roles especializados**:

```
┌────────────────────────────────────────────────────────────────────────┐
│               PERSONA 1: MANTENEDOR / DEVOPS / DBA (LEAD)              │
│   • Gobernanza del Repositorio Git (Ramas, PRs, Tags)                  │
│   • Base de Datos MySQL 8.4 InnoDB (Esquema 3NF de 16 Tablas)          │
│   • Contenedores Docker/Podman, Variables de Entorno y Semillas        │
│   • Orquestación del Hito de Integración y Release Oficial             │
└──────────────────┬──────────────────────────────────┬──────────────────┘
                   │                                  │
                   ▼                                  ▼
┌──────────────────────────────────────┐  ┌──────────────────────────────┐
│     PERSONA 2: BACKEND DEVELOPER     │  │PERSONA 3: FRONTEND DEVELOPER │
│   • Java 21 / Spring Boot 4.1.1      │  │   • Vue 3 / Vite 8 / Pinia   │
│   • Seguridad Stateless JWT + SpEL   │  │   • PrimeVue 4.5 Warm Editorial│
│   • 16 Entidades JPA + 52 DTOs       │  │   • Shell Maestro Responsive │
│   • 19 Controladores REST (126 APIs) │  │   • 16 Vistas + 14 Modales   │
│   • Compuerta Financiera Pre-Exped.  │  │   • Guardias RBAC en Router  │
│   • Motor JasperReports 7 PDF        │  │   • Consumo de los 126 APIs  │
└──────────────────┬───────────────────┘  └──────────────┬───────────────┘
                   │                                     │
                   └──────────────────┬──────────────────┘
                                      ▼
                      [ HITO DE UNIÓN E INTEGRACIÓN E2E ]
```

### Guías Técnicas Individuales de Consulta:
Cada persona cuenta con su propio manual técnico paso a paso con el código y especificaciones exactas:
* **Persona 1 (DBA / Infraestructura):** Documento [`database.md`](file:///home/jiodev/Documents/Proyects/SGA/alternativo/database.md)
* **Persona 2 (Backend Completo):** Documento [`backend.md`](file:///home/jiodev/Documents/Proyects/SGA/alternativo/backend.md)
* **Persona 3 (Frontend Completo):** Documento [`frontend.md`](file:///home/jiodev/Documents/Proyects/SGA/alternativo/frontend.md)

---

## 2. Estructura Unificada del Repositorio

El repositorio centralizado se organiza en tres carpetas raíz de dominio técnico independiente, permitiendo que cada desarrollador trabaje dentro de su área sin generar conflictos de merge:

```
transaduanas-platform/
│
├── database/                                  # DOMINIO DE PERSONA 1 (DBA / Infra)
│   ├── docker-compose.yml                     # Contenedor MySQL 8.4 InnoDB
│   ├── schema.sql                             # DDL de las 16 tablas relacionales en 3NF
│   ├── data.sql                               # DML con 8 roles, sedes y usuario admin
│   ├── backups/                               # Respaldos SQL y scripts de migración
│   └── README.md                              # Instrucciones de levantamiento de BD
│
├── backend/                                   # DOMINIO DE PERSONA 2 (Backend Java)
│   ├── pom.xml                                # Spring Boot 4.1.1, JJWT, JasperReports
│   ├── mvnw / mvnw.cmd / .mvn/                # Wrapper portable de Maven
│   └── src/
│       ├── main/resources/
│       │   ├── application.properties         # Configuración del servidor y JPA
│       │   ├── logback-spring.xml             # Formato y rotación de logs
│       │   └── initial-data.json              # Semillas de respaldo
│       └── main/java/pe/sga/
│           ├── SgaApplication.java            # Punto de entrada de la aplicación
│           ├── config/                        # Seguridad JWT, SpEL, CORS, Seed
│           ├── controller/                    # 19 Controladores REST (126 Endpoints)
│           ├── dto/                           # 52 DTOs (Request, Response, Reporte)
│           ├── entity/                        # 16 Entidades JPA + 8 Enums
│           ├── exception/                     # GlobalExceptionHandler unificado
│           ├── repository/                    # 16 Repositorios Spring Data JPA
│           └── service/                       # 19 Servicios (Interfaces + Impls)
│
├── frontend/                                  # DOMINIO DE PERSONA 3 (Frontend Vue)
│   ├── package.json                           # Vue 3, Vite, PrimeVue, Pinia, Axios
│   ├── vite.config.js                         # Alias '@/' y Proxy hacia localhost:8085
│   ├── index.html                             # Fuentes Inter y JetBrains Mono
│   └── src/
│       ├── App.vue                            # Contenedor raíz con Toasts globales
│       ├── main.js                            # Inicialización Vue, Pinia y SgaPreset
│       ├── style.css                          # Tokens CSS Warm Editorial y Dark Mode
│       ├── core/                              # Red (apiClient), Roles, Layout, Router
│       ├── features/                          # 8 Módulos de Negocio (Admin, Comercial, etc.)
│       └── shared/                            # Componentes Base, Modales y Composables
│
├── .gitignore                                 # Exclusiones de Git (node_modules, target, .env)
└── README.md                                  # Documentación general de la plataforma
```

---

## 3. Estrategia de Ramas Git Flow y Protocolo de Trabajo

Se establece una jerarquía de ramas clara para aislar los desarrollos y asegurar integraciones predecibles:

```
[main] ─────────────────────────────────────────────────────────────● (Tag Oficial v1.0.0)
  │                                                                 ▲
  ▼                                                                 │
[develop] ───┬──────────────┬──────────────┬──────────────┬─────────┴─ (Rama de Integración)
             │              │              │              │
             ├─ [PERSONA 1] ┼─ [PERSONA 2] ┼─ [PERSONA 3] ┤
             │  infra/*     │  backend/*   │  frontend/*  │
```

### Protocolo de Trabajo:
1. **Persona 1 (Mantenedor):** Inicializa `main` y `develop`. Crea la rama `infra/setup-database-environment`, levanta MySQL, prueba el esquema, hace commit y merge a `develop`.
2. **Persona 2 (Backend):** Hace pull de `develop`, crea la rama `backend/feature-complete`, conecta su Spring Boot a la base de datos levantada por Persona 1 y desarrolla los 11 commits de la API REST.
3. **Persona 3 (Frontend):** Hace pull de `develop`, crea la rama `frontend/feature-complete`, desarrolla la SPA en Vue 3 consumiendo el backend a través del proxy inverso de Vite y realiza sus 12 commits de vistas y componentes.
4. **Hito de Unión:** Una vez que Persona 2 y Persona 3 terminan sus ramas, crean Pull Requests hacia `develop`. Persona 1 revisa, aprueba los merges y todo el equipo ejecuta la prueba integral E2E.

---

## 4. Cronograma Detallado y Secuencia de Commits

A continuación se detalla el orden cronológico exacto de trabajo día por día:

```
 DÍA 1                 DÍAS 2 AL 5                    DÍA 6                DÍA 7
┌──────────────┐      ┌─────────────────────────┐    ┌──────────────┐    ┌──────────────┐
│  PERSONA 1   │ ──►  │ PERSONA 2: Backend      │ ──►│ PRs a        │ ──►│ Hito E2E &   │
│  Repo & BD   │      │ (Commits 2.1 al 2.11)   │    │ develop      │    │ Release      │
│  (Commits    │      ├─────────────────────────┤    │ (Revisión de │    │ v1.0.0       │
│  1.1 al 1.5) │      │ PERSONA 3: Frontend     │    │ Persona 1)   │    │              │
│              │      │ (Commits 3.1 al 3.12)   │    │              │    │              │
└──────────────┘      └─────────────────────────┘    └──────────────┘    └──────────────┘
```

---

### DÍA 1: Cimientos, Base de Datos e Infraestructura (Persona 1)

Persona 1 arranca el proyecto en solitario para preparar la plataforma sobre la que trabajarán los demás:

```bash
# 1. Persona 1 inicializa el repositorio
git init
git checkout -b main
git commit --allow-empty -m "chore: initial commit en main"
git checkout -b develop
git checkout -b infra/setup-database-environment
```

* **Commit 1.1:** `chore(infra): inicializar estructura de repositorio, ramas develop/main y archivo gitignore`  
  *Archivos:* `.gitignore`, estructura de carpetas `database/`, `backend/`, `frontend/`.
* **Commit 1.2:** `feat(infra): configurar docker-compose para mysql 8.4 inno-db y plantilla de conexion local`  
  *Archivos:* `database/docker-compose.yml`, `backend/application-local.properties.example`.
* **Commit 1.3:** `feat(database): implementar esquema ddl relacional de 16 tablas normalizadas en 3nf`  
  *Archivos:* `database/schema.sql` (creación de 16 tablas con claves foráneas e índices).
* **Commit 1.4:** `feat(database): incorporar datos semilla de roles normativos, sedes, catalogos y usuario admin`  
  *Archivos:* `database/data.sql` (8 roles, sedes, tipos de carga, puertos y admin).
* **Commit 1.5:** `test(infra): verificar levantamiento del contenedor mysql y carga exitosa de las 16 tablas`  
  *Acción:* Levantar MySQL, cargar schema, cargar data y verificar con `SHOW TABLES;`.

#### Cierre del Día 1 (Merge de Infraestructura a `develop`):
```bash
git checkout develop
git merge --no-ff infra/setup-database-environment -m "merge: integrar infraestructura base y base de datos relacional en develop"
git push origin develop
```
*A partir de este momento, Persona 2 y Persona 3 clonan el repositorio y comienzan su trabajo en paralelo.*

---

### DÍAS 2 AL 5: Desarrollo Paralelo del Backend y Frontend

#### Trabajo de Persona 2 (Backend Developer en rama `backend/feature-complete`):

Persona 2 levanta la base de datos localmente con `docker compose -f database/docker-compose.yml up -d` y ejecuta sus 11 commits:

```bash
git checkout develop && git pull origin develop
git checkout -b backend/feature-complete
```

* **Commit 2.1:** `chore(backend): inicializar proyecto maven con spring boot 4.1.1, java 21 y dependencias core`  
  *Archivos:* `backend/pom.xml`, `application.properties`, `logback-spring.xml`, `SgaApplication.java`.
* **Commit 2.2:** `feat(core): implementar ApiResponseDTO unificado y GlobalExceptionHandler para errores REST`  
  *Archivos:* `dto/response/ApiResponseDTO.java`, `exception/BadRequestException.java`, `exception/ResourceNotFoundException.java`, `exception/GlobalExceptionHandler.java`.
* **Commit 2.3:** `feat(security): implementar seguridad stateless JWT, filtros y matriz de roles con SpEL`  
  *Archivos:* `entity/enums/RolUsuario.java`, `config/JwtTokenProvider.java`, `config/JwtAuthenticationFilter.java`, `config/SecurityRoles.java`, `config/CorsConfig.java`, `config/SecurityConfig.java`.
* **Commit 2.4:** `feat(auth-admin): implementar entidades de identidad, login JWT, seed automatico y modulo TI`  
  *Archivos:* Entidades `Rol`, `Sede`, `Usuario`, repositorios, DTOs de login, `DataInitializer.java`, `AuthController.java`, `HealthController.java`, `UsuarioController.java`, `SedeController.java`, `RolController.java`, `AdminLogsController.java`.
* **Commit 2.5:** `feat(catalogos-jasper): implementar catalogos maestros y motor dinamico de reportes JasperReports PDF`  
  *Archivos:* Entidades y repositorios de `CatalogoPuerto`, `CatalogoTipoCarga`, `CatalogoMercancia`, DTOs de reporte, `ReporteJasperServiceImpl.java` y controladores.
* **Commit 2.6:** `feat(storage): implementar servicio desacoplado de almacenamiento y gestion documental polimorfica`  
  *Archivos:* `DocumentoAdjunto.java`, `IStorageService.java`, `LocalStorageServiceImpl.java`, `DocumentoServiceImpl.java`, `DocumentoController.java`.
* **Commit 2.7:** `feat(comercial-pagos): crear gestion de clientes, cotizaciones con calculo de flete y compuerta de validacion de pago`  
  *Archivos:* `Cliente.java`, `Cotizacion.java`, `CotizacionPago.java`, servicios de cálculo de costos, `CotizacionPagoServiceImpl.java` (validación del rol `CONTADOR`) y controladores.
* **Commit 2.8:** `feat(aduanas): implementar gestion de expedientes DAM, compuerta estricta de pago y control de canales SUNAT`  
  *Archivos:* `Expediente.java`, `ExpedienteServiceImpl.java` (bloqueo si `tienePagoAprobado != true`), asignación de canal verde/naranja/rojo, levante aduanero y `ExpedienteController.java`.
* **Commit 2.9:** `feat(almacen-transporte): integrar pesaje VGM en almacen y despacho de flota terrestre con monitoreo en ruta`  
  *Archivos:* `ControlAlmacen.java` (balanza VGM), `Conductor.java`, `UnidadTransporte.java`, `DespachoTransporte.java`, servicios transaccionales y controladores.
* **Commit 2.10:** `feat(finanzas): implementar liquidacion financiera final, calculos tributarios SPOT y reporte contable`  
  *Archivos:* `LiquidacionFinanciera.java`, `LiquidacionFinancieraServiceImpl.java` (cálculo de impuestos SUNAT, IGV 18% y SPOT Banco de la Nación), `LiquidacionFinancieraController.java`.
* **Commit 2.11:** `test(hardening): validar suite completa de 126 endpoints, paquete JAR y backend listo para integracion`  
  *Acción:* Compilación completa `./mvnw clean package -DskipTests` y validación de generación del archivo `target/sga-backend-0.0.1-SNAPSHOT.jar`.

---

#### Trabajo de Persona 3 (Frontend Developer en rama `frontend/feature-complete`):

Persona 3 desarrolla la SPA en Vue 3 con el proxy apuntando a `http://localhost:8085` y ejecuta sus 12 commits:

```bash
git checkout develop && git pull origin develop
git checkout -b frontend/feature-complete
```

* **Commit 3.1:** `chore(frontend): inicializar proyecto vite con vue 3, pinia, primevue y dependencias core`  
  *Archivos:* `frontend/package.json`, `vite.config.js` (proxy a 8085), `index.html` (Inter + JetBrains Mono), `App.vue`.
* **Commit 3.2:** `feat(ui): implementar sistema de diseno warm editorial, tokens css y preset sga en primevue`  
  *Archivos:* `style.css` (variables CSS y Dark Theme), `useTheme.js`, `main.js` (`SgaPreset` sobre Aura).
* **Commit 3.3:** `feat(core): configurar cliente axios con interceptores jwt, constantes de roles y utilidades de feedback`  
  *Archivos:* `core/api/apiClient.js` (inyección Bearer y 401 redirect), `core/auth/roles.js` (8 roles y rutas de inicio), `download.js`, `useNotification.js`, `useConfirmation.js`.
* **Commit 3.4:** `feat(shell): implementar stores pinia de autenticacion, navegacion, sidebar reactivo y shell maestro applayout`  
  *Archivos:* `authStore.js`, `navigationStore.js`, `authService.js`, `AppSidebar.vue` (menú filtrado por rol), `AppLayout.vue` (Topbar ejecutiva y selector de contexto).
* **Commit 3.5:** `feat(auth-router): implementar vista de login editorial y guardia de navegacion global con proteccion rbac`  
  *Archivos:* `LoginView.vue`, `auth.routes.js`, `core/router/index.js` (guardia `router.beforeEach` con validación de roles y títulos).
* **Commit 3.6:** `feat(shared-ui): crear biblioteca de componentes compartidos, modales base, paginacion y panel documental`  
  *Archivos:* `BaseModal.vue`, `BasePagination.vue`, `usePagination.js`, `MetricCard.vue`, `HeroBannerCard.vue`, `DocumentosUploadPanel.vue`, `DocumentosManagerModal.vue`, `documentoService.js`.
* **Commit 3.7:** `feat(admin-ti): implementar administracion de usuarios, asignacion de roles, consola de logs y acerca de`  
  *Archivos:* `adminService.js`, `UserManagementView.vue`, `AboutSystemView.vue`, `SystemLogsView.vue`, `admin.routes.js`.
* **Commit 3.8:** `feat(gerencia): implementar gestion de sedes, catalogos portuarios con pdf, padron de flota y choferes mtc`  
  *Archivos:* `SedesManagementView.vue`, `CatalogosManagementView.vue`, `AdminOverviewView.vue`, `GerenciaFlotaView.vue`, `GerenciaConductoresView.vue`, `gerencia.routes.js`.
* **Commit 3.9:** `feat(comercial): implementar registro de clientes, cotizador interactivo de 4 pasos con proforma y bandeja comercial`  
  *Archivos:* `peruUbigeo.js`, `comercialService.js`, `ClienteRegistrationForm.vue`, `CotizacionFormHeader.vue`, `CotizacionStepperNav.vue`, `CotizacionProformaPreview.vue`, `CotizacionRegistrationForm.vue`, `RecepcionComercialView.vue`.
* **Commit 3.10:** `feat(aduanas-legal): implementar gestion de expedientes dam, canales sunat, levante aduanero y auditoria legal`  
  *Archivos:* `expedienteService.js`, `gestionAduaneraService.js`, `AperturaExpedienteModal.vue`, `CanalLevanteModal.vue`, `DetalleExpedienteModal.vue`, `AuditoriaExpedienteModal.vue`, `GestionAduaneraView.vue`, `LiquidacionAduaneraView.vue`, `AgenteAduaneroView.vue`, `LegalAuditoriaView.vue`.
* **Commit 3.11:** `feat(logistica-finanzas): integrar recepcion vgm en almacen, monitoreo de flota en ruta y modulo financiero con pre-pago`  
  *Archivos:* `almacenService.js`, `transporteFlotaService.js`, `finanzasService.js`, `IngresoAlmacenModal.vue`, `CrearDespachoModal.vue`, `AsignarRecursosModal.vue`, `ConfirmarEntregaModal.vue`, `RegistrarPagoModal.vue`, `TransporteFlotaView.vue`, `FinanzasView.vue`.
* **Commit 3.12:** `test(frontend): validar compilacion de produccion, integracion completa de 16 vistas y conexion rest con backend`  
  *Archivos:* `roles.routes.js`, ejecución exitosa de `npm run build` generando carpeta `dist/`.

---

### DÍA 6: Integración y Merge hacia `develop`

Ambos desarrolladores suben sus ramas y notifican a Persona 1 para la revisión y merge:

```bash
# Persona 2 sube su rama backend
git push -u origin backend/feature-complete

# Persona 3 sube su rama frontend
git push -u origin frontend/feature-complete
```

#### Protocolo de Merge ejecutado por Persona 1 (Mantenedor):
```bash
git checkout develop
git pull origin develop

# Integrar Backend
git merge --no-ff backend/feature-complete -m "merge: integrar backend completo de 126 endpoints y servicios transaccionales en develop"

# Integrar Frontend
git merge --no-ff frontend/feature-complete -m "merge: integrar frontend completo con 16 vistas y sistema warm editorial en develop"

git push origin develop
```

---

### DÍA 7: Hito de Integración Extremo a Extremo (E2E) y Release Oficial v1.0.0

Todo el equipo realiza la prueba de validación del ciclo completo de negocio en vivo:

```
┌────────────────────────────────────────────────────────────────────────┐
│                   SECUENCIA DE VALIDACIÓN E2E DEL EQUIPO               │
├────────────────────────────────────────────────────────────────────────┤
│ 1. [Comercial] Crear Cliente con RUC y emitir Cotización ($3,500 USD)  │
│ 2. [Compuerta] Intentar aperturar Expediente DAM ──► Debe dar 400      │
│ 3. [Finanzas] Ingresar como CONTADOR, adjuntar voucher y Aprobar Pago  │
│ 4. [Aduanas] Aperturar Expediente EXP-2026-0001 (DAM 18 dígitos)       │
│ 5. [Aduanas] Asignar Canal VERDE y registrar Levante Aduanero          │
│ 6. [Almacén] Registrar Pesaje Balanza VGM (Bruto, Tara, Neto)          │
│ 7. [Transporte] Programar Despacho, asignar Camión/Chofer y entregar   │
│ 8. [Finanzas] Generar Liquidación Final con SPOT y descargar PDF       │
└────────────────────────────────────────────────────────────────────────┘
```

#### Creación del Release Oficial por Persona 1:
```bash
git checkout -b release/v1.0.0
# Verificaciones finales de logs y puertos
git checkout main
git merge --no-ff release/v1.0.0 -m "release: transaduanas platform version 1.0.0 oficial para produccion"
git tag -a v1.0.0 -m "Release Oficial Transaduanas Platform v1.0.0"
git push origin main --tags

# Sincronizar develop con main
git checkout develop
git merge main
git push origin develop
```

---

## 5. Matriz de Comandos Rápidos por Rol

### Para Persona 1 (Mantenedor / DBA):
```bash
# Iniciar base de datos en segundo plano
podman compose -f database/docker-compose.yml up -d
# Cargar esquema y datos iniciales
mysql -h 127.0.0.1 -P 3306 -u root -proot SGAdb < database/schema.sql
mysql -h 127.0.0.1 -P 3306 -u root -proot SGAdb < database/data.sql
```

### Para Persona 2 (Backend Developer):
```bash
cd backend
# Compilar y validar contratos
./mvnw clean test-compile -DskipTests
# Arrancar servidor en puerto 8085
./mvnw spring-boot:run
# Empaquetar JAR de producción
./mvnw clean package -DskipTests
```

### Para Persona 3 (Frontend Developer):
```bash
cd frontend
# Instalar dependencias
npm install
# Arrancar servidor de desarrollo en puerto 5180
npm run dev
# Compilar paquete de producción
npm run build
```

---

## 6. Resumen de Entregables del Plan

Con este plan de 3 personas ejecutado cronológicamente, el equipo entrega:
* **Infraestructura:** Contenedor MySQL 8.4 InnoDB con 16 tablas relacionales normalizadas en 3NF y datos semilla oficiales.
* **Backend:** Servidor Spring Boot 4.1.1 en Java 21 con 19 controladores, 126 endpoints RESTful, seguridad stateless JWT con SpEL y motor de reportes JasperReports en PDF.
* **Frontend:** SPA en Vue 3 y Vite con 16 vistas, 14 modales, sistema de diseño *Warm Editorial 2.0*, guardias de navegación RBAC y comunicación HTTP reactiva.
* **Gobernanza:** Repositorio limpio con historial de commits atómicos bajo el estándar Conventional Commits, ramas protegidas y tag de producción `v1.0.0`.
