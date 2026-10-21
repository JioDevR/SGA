# Guía Maestra de Recreación del Frontend
**Transaduanas Platform (Powered by SGA Core)**  
*Arquitectura, Paso a Paso de Desarrollo y Secuencia de Commits Git*

---

## 1. Visión General y Arquitectura de Software del Frontend

El cliente web de **Transaduanas Platform** está desarrollado como una **Single Page Application (SPA)** de alto rendimiento utilizando **Vue 3 (Composition API con `<script setup>`)**, empaquetada mediante **Vite 8.3**, gestionada en estado por **Pinia 4.0** y estilizada bajo el sistema de diseño corporativo **Warm Editorial 2.0**.

La arquitectura sigue el patrón **Feature-Driven Clean Architecture (Arquitectura Limpia Basada en Dominios y Características)**, organizando el código en tres capas concéntricas con responsabilidades estrictamente delimitadas:

```
┌────────────────────────────────────────────────────────────────────────┐
│                        SHELL MAESTRO (LAYOUT)                          │
│   AppLayout.vue ──► AppSidebar.vue (RBAC) ──► Topbar Ejecutivo         │
└───────────────────────────────────┬────────────────────────────────────┘
                                    │
                                    ▼
┌────────────────────────────────────────────────────────────────────────┐
│                 ROUTER CON GUARDIA DE ACCESO (RBAC)                    │
│   router.beforeEach ──► Validación Token JWT ──► Control por Roles     │
└───────────────────────────────────┬────────────────────────────────────┘
                                    │
                                    ▼
┌────────────────────────────────────────────────────────────────────────┐
│                 VISTAS Y CARACTERÍSTICAS (FEATURES)                    │
│   16 Vistas SPA · 14 Modales Especializados · 8 Módulos de Dominio     │
│   • Administración TI   • Recepción Comercial    • Almacén VGM         │
│   • Gerencia General    • Gestión Aduanera DAM   • Transporte & Flota  │
│   • Agente Aduanero     • Legal & Auditoría      • Finanzas & Pagos    │
└───────────────────┬────────────────────────────────┬───────────────────┘
                    │                                │
                    ▼                                ▼
┌──────────────────────────────────────┐  ┌──────────────────────────────┐
│       ESTADO GLOBAL (PINIA)          │  │    SERVICIOS HTTP AXIOS      │
│  authStore (Sesión, Token, Rol)      │  │  apiClient con Interceptors  │
│  navigationStore (Módulos, Tabs)     │  │  Inyección Bearer + 401 Auto │
└───────────────────┬──────────────────┘  └──────────────┬───────────────┘
                    │                                    │
                    ▼                                    ▼
┌────────────────────────────────────────────────────────────────────────┐
│             SISTEMA DE DISEÑO & BIBLIOTECA COMPARTIDA (SHARED)         │
│   Tokens CSS Warm Editorial (style.css) · Rejilla 4px · Fuentes Dual   │
│   Componentes Base (Modal, Paginador, Tarjetas KPI, Subida de Archivos)│
│   PrimeVue 4.5 con Preset SgaPreset (Modo Claro / Modo Oscuro)         │
└────────────────────────────────────────────────────────────────────────┘
```

---

### 1.1 Principios Arquitectónicos Fundamentales del Frontend

1. **Arquitectura Orientada a Características (Feature-Driven):**  
   Cada dominio de negocio (`auth`, `admin`, `gerencia`, `documentos`, `roles-modules`) contiene de forma autónoma sus propias vistas (`views/`), componentes interactivos (`components/`), servicios HTTP (`services/`), rutas (`routes/`) y esquemas de datos (`data/`), minimizando acoplamientos transversales innecesarios.
2. **Sistema de Diseño *Warm Editorial 2.0*:**  
   Reemplaza los grises fríos genéricos por una paleta cálida basada en tonos arena, marfil, lino y café espresso profundo (`#352f2a`). Utiliza tipografía dual cargada desde Google Fonts:
   * **Inter:** Lectura ejecutiva neutral, menús, etiquetas y formularios.
   * **JetBrains Mono:** Códigos normativos de expedientes (`EXP-2026-0001`), cotizaciones (`COT-2026-0001`), DAMs de 18 dígitos, placas vehiculares y montos en dólares (`$ USD`).
   * **Rejilla de 4px:** Escala espacial matemática estricta (`--space-1` a `--space-16`).
   * **Soporte Bimodal:** Alternancia fluida entre Modo Claro y Modo Oscuro mediante la clase `.dark-theme` gobernada por `useTheme.js`.
3. **Control de Acceso Basado en Roles (RBAC Client-Side):**  
   La matriz institucional de 8 roles se encuentra formalizada en `src/core/auth/roles.js`. El router global valida los permisos de cada ruta en `beforeEach`:
   * Si no hay sesión activa, redirige inmediatamente a `/login`.
   * Si el usuario no posee el rol autorizado, es conducido a su ruta de inicio predeterminada (`ROLE_HOME_PATH`).
   * Los roles `ADMINISTRADOR_TI` y `GERENTE_GENERAL` disponen de supervisión global y un selector dinámico de contexto en el shell.
4. **Capa HTTP Centralizada con Interceptores Inteligentes (`apiClient.js`):**  
   * **Petición:** Inyecta automáticamente el token JWT presente en `localStorage` bajo la cabecera estándar `Authorization: Bearer <token>`.
   * **Respuesta 401:** Si el token expira o es inválido, limpia la sesión y redirige de manera limpia a `/login` sin bucles de recarga.
   * **Respuesta 403:** Identifica accesos denegados a recursos particulares sin desloguear al usuario, permitiendo que la vista notifique mediante Toast visual.
5. **Separación de Responsabilidades en Formularios Complejos:**  
   Los procesos críticos (como el registro de cotizaciones aduaneras con cálculo de flete nacional) se descomponen en sub-componentes especializados: cabecera contextual (`CotizacionFormHeader.vue`), barra de pasos (`CotizacionStepperNav.vue`), formulario interactivo (`CotizacionRegistrationForm.vue`) y previsualización de proforma en tiempo real (`CotizacionProformaPreview.vue`).

---

### 1.2 Estructura y Mapa Completo de Directorios del Frontend

```
frontend/
├── index.html                                 # Punto de entrada HTML con Google Fonts
├── package.json                               # Dependencias Vue 3, Vite, PrimeVue y Pinia
├── vite.config.js                             # Configuración Vite, alias '@/' y proxy /api
└── src/
    ├── App.vue                                # Componente raíz (<router-view /> + Toasts)
    ├── main.js                                # Inicialización Vue, Pinia, PrimeVue SgaPreset
    ├── style.css                              # Tokens CSS Warm Editorial, utilidades y temas
    │
    ├── core/                                  # Núcleo horizontal de la plataforma
    │   ├── api/
    │   │   └── apiClient.js                   # Instancia Axios con Interceptors de Auth
    │   ├── auth/
    │   │   └── roles.js                       # Roles, etiquetas, módulos y rutas por defecto
    │   ├── layouts/
    │   │   └── AppLayout.vue                  # Shell maestro: Topbar, Sidebar, Context Switcher
    │   ├── router/
    │   │   └── index.js                       # Guardia RBAC global y sincronización con Pinia
    │   └── stores/
    │       └── navigationStore.js             # Estado de módulo activo, sub-opciones y breadcrumbs
    │
    ├── features/                              # Módulos verticales por dominio de negocio
    │   ├── admin/                             # Administración de Tecnologías de Información
    │   │   ├── about/views/AboutSystemView.vue
    │   │   ├── catalogos/views/CatalogosManagementView.vue
    │   │   ├── overview/views/AdminOverviewView.vue
    │   │   ├── sedes/views/SedesManagementView.vue
    │   │   ├── services/adminService.js
    │   │   ├── users/
    │   │   │   ├── routes/admin.routes.js
    │   │   │   └── views/UserManagementView.vue
    │   │   └── views/SystemLogsView.vue
    │   │
    │   ├── auth/                              # Autenticación y Control de Sesión
    │   │   ├── routes/auth.routes.js
    │   │   ├── services/authService.js
    │   │   ├── stores/authStore.js
    │   │   └── views/LoginView.vue
    │   │
    │   ├── gerencia/                          # Gerencia General y Fiscalización Operativa
    │   │   ├── routes/gerencia.routes.js
    │   │   └── views/
    │   │       ├── GerenciaConductoresView.vue
    │   │       └── GerenciaFlotaView.vue
    │   │
    │   ├── documentos/                        # Archivo Digital Polimórfico
    │   │   └── services/documentoService.js
    │   │
    │   └── roles-modules/                     # Módulos de Operación Aduanera y Logística
    │       ├── components/                    # 14 Modales y Formularios Especializados
    │       │   ├── AperturaExpedienteModal.vue
    │       │   ├── AsignarRecursosModal.vue
    │       │   ├── AuditoriaExpedienteModal.vue
    │       │   ├── CanalLevanteModal.vue
    │       │   ├── ClienteRegistrationForm.vue
    │       │   ├── ConfirmarEntregaModal.vue
    │       │   ├── CotizacionFormHeader.vue
    │       │   ├── CotizacionProformaPreview.vue
    │       │   ├── CotizacionRegistrationForm.vue
    │       │   ├── CotizacionStepperNav.vue
    │       │   ├── CrearDespachoModal.vue
    │       │   ├── DetalleExpedienteModal.vue
    │       │   ├── IngresoAlmacenModal.vue
    │       │   └── RegistrarPagoModal.vue
    │       ├── data/
    │       │   └── peruUbigeo.js              # Maestro geográfico de rutas y fletes
    │       ├── routes/roles.routes.js
    │       ├── services/                      # Servicios HTTP REST de dominio
    │       │   ├── almacenService.js
    │       │   ├── comercialService.js
    │       │   ├── expedienteService.js
    │       │   ├── finanzasService.js
    │       │   ├── gestionAduaneraService.js
    │       │   └── transporteFlotaService.js
    │       └── views/                         # 7 Vistas de Operaciones
    │           ├── AgenteAduaneroView.vue
    │           ├── FinanzasView.vue
    │           ├── GestionAduaneraView.vue
    │           ├── LegalAuditoriaView.vue
    │           ├── LiquidacionAduaneraView.vue
    │           ├── RecepcionComercialView.vue
    │           └── TransporteFlotaView.vue
    │
    └── shared/                                # Biblioteca Compartida y Reutilizable
        ├── components/
        │   ├── AppSidebar.vue                 # Menú lateral reactivo por rol
        │   ├── BaseModal.vue                  # Ventana modal base con transiciones
        │   ├── BasePagination.vue             # Paginador numérico para tablas
        │   ├── DocumentosManagerModal.vue     # Modal contenedor de archivos
        │   ├── DocumentosUploadPanel.vue      # Subida drag-and-drop de archivos
        │   ├── HeroBannerCard.vue             # Tarjeta resumen de cabecera
        │   └── MetricCard.vue                 # Tarjeta analítica de KPI
        ├── composables/
        │   ├── useConfirmation.js             # Diálogos de confirmación modales
        │   ├── useNotification.js             # Notificaciones Toast reactivas
        │   ├── usePagination.js               # Cálculo reactivo de páginas
        │   └── useTheme.js                    # Alternancia claro/oscuro
        └── utils/
            └── download.js                    # Descargador seguro de blobs PDF y binarios
```

---

## 2. Metodología y Convención de Ramas Git

El desarrollo del Frontend Completo (DEV 3) se estructura de forma atómica y ordenada en su rama de especialidad:

```
[develop]
   │
   └──► [frontend/feature-complete]
           │
           ├── Commit 1: Inicialización Vite, Dependencias y Configuración
           ├── Commit 2: Tokens CSS Warm Editorial, Temas y Preset PrimeVue
           ├── Commit 3: Red Axios, Interceptores y Constantes de Roles
           ├── Commit 4: Stores Pinia, Auth y Shell Maestro AppLayout
           ├── Commit 5: Router con Guardias RBAC y Pantalla de Login
           ├── Commit 6: Biblioteca de Componentes Compartidos (Shared UI)
           ├── Commit 7: Administración TI (Usuarios, Sedes, Catálogos, Logs)
           ├── Commit 8: Gerencia General (Resumen, Flota y Choferes MTC)
           ├── Commit 9: Comercial (Clientes, Cotizador 4 Pasos y Proforma)
           ├── Commit 10: Aduanas (Expedientes DAM, Canales y Auditoría Legal)
           ├── Commit 11: Almacén VGM, Despachos de Flota y Finanzas con Pre-Pago
           └── Commit 12: Endurecimiento, Optimización Build y Conexión E2E
```

### Reglas de Calidad Obligatorias por Commit:
1. **Compilación Limpia:** Antes de cada commit se debe ejecutar `npm run build`. El empaquetador Vite debe finalizar con código 0 y 0 errores de sintaxis en plantillas Vue o imports.
2. **Convención de Commits:** Uso estricto de **Conventional Commits** (`feat(...)`, `chore(...)`, `test(...)`).
3. **Mapeo de Copia:** Se especifica exactamente qué archivo del proyecto actual debe trasladarse al repositorio réplica.

---

## 3. Recreación Paso a Paso con Commits

### Commit 1: Inicialización del Proyecto Vite, Dependencias y Configuración Base

**Objetivo:** Crear el andamiaje inicial del frontend con Node.js, Vite 8, Vue 3 y resolver todas las librerías de interfaz, iconos y gestión de estado.

#### 1. Crear el archivo `frontend/package.json`:
Copiar de: `frontend/package.json`
```json
{
  "name": "frontend",
  "private": true,
  "version": "0.0.0",
  "type": "module",
  "scripts": {
    "dev": "vite",
    "build": "vite build",
    "preview": "vite preview"
  },
  "dependencies": {
    "@primevue/themes": "^4.5.4",
    "axios": "^1.20.0",
    "pinia": "^4.0.3",
    "primeicons": "^8.0.2",
    "primevue": "^4.5.5",
    "vue": "^3.5.42",
    "vue-router": "^4.6.4"
  },
  "devDependencies": {
    "@vitejs/plugin-vue": "^6.0.8",
    "vite": "^8.3.0"
  }
}
```

#### 2. Configurar `frontend/vite.config.js`:
Copiar de: `frontend/vite.config.js`
*Configuración clave:* Define el alias `@/` apuntando a `./src`, configura el puerto local `5180` y establece el proxy inverso `/api` hacia `http://localhost:8085`.

#### 3. Crear `frontend/index.html`:
Copiar de: `frontend/index.html`
*Asegurar que incluya:*
* Preconexión e importación de Google Fonts: `Inter` (300 a 800) y `JetBrains Mono` (400 a 700).
* Título formal: `SGA - Sistema de Gestión Aduanera`.
* Contenedor raíz `<div id="app"></div>` y script de entrada `/src/main.js`.

#### 4. Crear componente raíz `frontend/src/App.vue`:
Copiar de: `frontend/src/App.vue`
*Renderiza `<router-view />`, inicializa el contenedor global de Toasts (`<Toast />`) y confirmaciones (`<ConfirmDialog />`).*

#### 5. Validación y Comando Git:
```bash
npm install
npm run build
git add frontend/package.json frontend/vite.config.js frontend/index.html frontend/src/App.vue
git commit -m "chore(frontend): inicializar proyecto vite con vue 3, pinia, primevue y dependencias core"
```

---

### Commit 2: Tokens CSS Warm Editorial, Temas y Preset PrimeVue

**Objetivo:** Dotar a la aplicación de su identidad visual corporativa *Warm Editorial 2.0*, definiendo paleta cromática bimodal, rejilla de 4px y configuración de PrimeVue.

#### 1. Crear carpetas:
```bash
mkdir -p frontend/src/shared/composables
```

#### 2. Copiar archivos:
* **Hoja de Estilos Maestra:**
  * Copiar `frontend/src/style.css` a `frontend/src/style.css`.
  * *Contenido:* Variables `:root` con la paleta de tonos café espresso (`#352f2a`), lino, arena y marfil; escala espacial `--space-1` (4px) a `--space-16` (64px); tipografías `--font-sans` y `--font-mono`; clases utilitarias de tarjetas, botones editoriales, tablas y badges; reglas de `.dark-theme`.
* **Controlador de Tema (Claro/Oscuro):**
  * Copiar `frontend/src/shared/composables/useTheme.js`.
  * Exporta `initTheme()` (lee la preferencia en `localStorage`), `toggleTheme()` y el estado reactivo `isDark`.
* **Punto de Entrada de la Aplicación:**
  * Copiar `frontend/src/main.js` a `frontend/src/main.js`.
  * Configura el preset personalizado `SgaPreset` sobre Aura de PrimeVue, define la paleta cálida semántica, vincula Pinia, Router, `ToastService` y `ConfirmationService`.

#### 3. Comando Git:
```bash
npm run build
git add frontend/src/style.css frontend/src/shared/composables/useTheme.js frontend/src/main.js
git commit -m "feat(ui): implementar sistema de diseno warm editorial, tokens css y preset sga en primevue"
```

---

### Commit 3: Red Axios, Interceptores y Constantes Institucionales de Roles

**Objetivo:** Establecer la capa de transporte HTTP con inyección automática de Bearer Tokens y el mapa institucional de roles.

#### 1. Crear carpetas:
```bash
mkdir -p frontend/src/core/api
mkdir -p frontend/src/core/auth
mkdir -p frontend/src/shared/utils
```

#### 2. Copiar archivos:
* **Cliente HTTP:**
  * Copiar `frontend/src/core/api/apiClient.js`.
  * Configura `baseURL` (`/api/v1` o `http://localhost:8085/api/v1`), inyecta `Authorization: Bearer <token>` si existe en `localStorage`, intercepta errores 401 para limpiar credenciales y redirigir a `/login`, y procesa 403 sin deslogueo.
* **Constantes de Roles:**
  * Copiar `frontend/src/core/auth/roles.js`.
  * Define `ROLES`, `ROLE_LABELS`, `ROLE_MODULE_MAP`, `ROLE_HOME_PATH` y `ROLE_OPTIONS` para los 8 roles normativos: `ADMINISTRADOR_TI`, `GERENTE_GENERAL`, `AGENTE_COMERCIAL`, `SUPERVISOR_ADUANERO`, `AGENTE_ADUANERO`, `SUPERVISOR_TRANSPORTE`, `CONTADOR` y `ASESOR_LEGAL`.
* **Utilidades y Composables Auxiliares:**
  * Copiar `frontend/src/shared/utils/download.js` (convierte arrays binarios a descargas de archivos con extensión correcta).
  * Copiar `frontend/src/shared/composables/useNotification.js` (envoltorio reactivo sobre PrimeVue Toast con métodos `showSuccess`, `showError`, `showInfo`, `showWarn`).
  * Copiar `frontend/src/shared/composables/useConfirmation.js` (envoltorio reactivo sobre PrimeVue ConfirmDialog).

#### 3. Comando Git:
```bash
npm run build
git add frontend/src/core/api/ frontend/src/core/auth/ frontend/src/shared/utils/ frontend/src/shared/composables/
git commit -m "feat(core): configurar cliente axios con interceptores jwt, constantes de roles y utilidades de feedback"
```

---

### Commit 4: Stores Pinia, Autenticación y Shell Maestro Responsive (AppLayout)

**Objetivo:** Crear el estado global de sesión, el menú de navegación lateral sensible al rol del usuario y el contenedor principal de la aplicación.

#### 1. Crear carpetas:
```bash
mkdir -p frontend/src/core/stores
mkdir -p frontend/src/core/layouts
mkdir -p frontend/src/features/auth/stores
mkdir -p frontend/src/features/auth/services
mkdir -p frontend/src/shared/components
```

#### 2. Copiar archivos:
* **Stores de Pinia:**
  * Copiar `frontend/src/features/auth/stores/authStore.js`: Administra `token`, `user`, getters `isAuthenticated`, `userRole`, `userName`, `homePath`, y acciones `login()`, `logout()`.
  * Copiar `frontend/src/core/stores/navigationStore.js`: Administra `activeModule`, sub-opciones de navegación y breadcrumbs dinámicos.
* **Servicio de Autenticación:**
  * Copiar `frontend/src/features/auth/services/authService.js`: Ejecuta `POST /api/v1/auth/login`.
* **Componentes del Shell:**
  * Copiar `frontend/src/shared/components/AppSidebar.vue`: Barra lateral colapsable, menú reactivo con filtrado de opciones según el rol del usuario, badges de conteo y botón de contracción rápida.
  * Copiar `frontend/src/core/layouts/AppLayout.vue`: Shell maestro responsive que integra la Topbar ejecutiva (logo SGA, indicador de sede, switch de tema claro/oscuro, selector de contexto de módulo para Admin/Gerente, perfil de usuario y botón de cierre de sesión) y el contenedor principal `<router-view />`.

#### 3. Comando Git:
```bash
npm run build
git add frontend/src/core/stores/ frontend/src/features/auth/stores/ frontend/src/features/auth/services/ frontend/src/shared/components/AppSidebar.vue frontend/src/core/layouts/AppLayout.vue
git commit -m "feat(shell): implementar stores pinia de autenticacion, navegacion, sidebar reactivo y shell maestro applayout"
```

---

### Commit 5: Router Global con Guardias RBAC y Pantalla de Login

**Objetivo:** Implementar la seguridad en el cliente mediante guardias de navegación y la pantalla de acceso oficial con validación reactiva.

#### 1. Crear carpetas:
```bash
mkdir -p frontend/src/core/router
mkdir -p frontend/src/features/auth/routes
mkdir -p frontend/src/features/auth/views
```

#### 2. Copiar archivos:
* **Rutas de Autenticación:**
  * Copiar `frontend/src/features/auth/routes/auth.routes.js`: Declara la ruta pública `/login`.
* **Vista de Login:**
  * Copiar `frontend/src/features/auth/views/LoginView.vue`: Formulario de acceso con diseño editorial, validación de credenciales, feedback de errores, credenciales pre-configuradas para pruebas rápidas y redirección automática según el rol asignado.
* **Router Principal:**
  * Copiar `frontend/src/core/router/index.js`:
    * Define las rutas hijas dentro de `AppLayout.vue`.
    * Implementa la guardia global `router.beforeEach`: verifica autenticación, restringe acceso según `meta.roles`, sincroniza `navigationStore` con la URL actual y actualiza el título del documento en el navegador.

#### 3. Validación y Comando Git:
```bash
npm run build
git add frontend/src/core/router/ frontend/src/features/auth/routes/ frontend/src/features/auth/views/
git commit -m "feat(auth-router): implementar vista de login editorial y guardia de navegacion global con proteccion rbac"
```

*Prueba:* Ejecutar `npm run dev` y acceder a `http://localhost:5180`. Debe redirigir automáticamente a `/login`.

---

### Commit 6: Biblioteca de Componentes Compartidos (Shared UI)

**Objetivo:** Construir la suite de componentes visuales reutilizables para modales, paginadores, tarjetas KPI y carga de documentos.

#### 1. Copiar componentes:
* `BaseModal.vue`: Copiar a `frontend/src/shared/components/BaseModal.vue` (modal reutilizable con accesibilidad ESC, bloqueo de scroll de fondo y botones de acción).
* `BasePagination.vue`: Copiar a `frontend/src/shared/components/BasePagination.vue` (paginador compacto para listas de alta densidad).
* `usePagination.js`: Copiar a `frontend/src/shared/composables/usePagination.js`.
* `MetricCard.vue`: Copiar a `frontend/src/shared/components/MetricCard.vue` (tarjeta de métricas analíticas con iconos, tendencias y cifras en formato monoespaciado).
* `HeroBannerCard.vue`: Copiar a `frontend/src/shared/components/HeroBannerCard.vue` (banner informativo contextual para la cabecera de cada módulo).
* `DocumentosUploadPanel.vue`: Copiar a `frontend/src/shared/components/DocumentosUploadPanel.vue` (panel de carga con zona drag-and-drop, validación de tamaño máx. 25MB y formatos PDF/JPG/PNG).
* `DocumentosManagerModal.vue`: Copiar a `frontend/src/shared/components/DocumentosManagerModal.vue`.
* `documentoService.js`: Copiar a `frontend/src/features/documentos/services/documentoService.js` (consumo de `/api/v1/documentos/upload`, listado por entidad y descarga directa).

#### 2. Comando Git:
```bash
npm run build
git add frontend/src/shared/components/ frontend/src/shared/composables/usePagination.js frontend/src/features/documentos/
git commit -m "feat(shared-ui): crear biblioteca de componentes compartidos, modales base, paginacion y panel documental"
```

---

### Commit 7: Módulo de Administración de TI y Seguridad

**Objetivo:** Permitir la gestión de cuentas de usuario, asignación de roles institucionales, supervisión de bitácoras del servidor y ficha técnica del sistema.

#### 1. Crear carpetas:
```bash
mkdir -p frontend/src/features/admin/services
mkdir -p frontend/src/features/admin/users/routes
mkdir -p frontend/src/features/admin/users/views
mkdir -p frontend/src/features/admin/about/views
mkdir -p frontend/src/features/admin/views
```

#### 2. Copiar archivos:
* **Servicio de Administración:**
  * Copiar `frontend/src/features/admin/services/adminService.js`: Métodos para usuarios (`/api/v1/usuarios`), roles (`/api/v1/roles`), sedes (`/api/v1/sedes`), catálogos y bitácoras del sistema (`/api/v1/admin/logs`).
* **Rutas de Administración:**
  * Copiar `frontend/src/features/admin/users/routes/admin.routes.js`.
* **Vistas de Administración:**
  * `UserManagementView.vue`: Copiar a `frontend/src/features/admin/users/views/UserManagementView.vue` (tabla de usuarios, modal para crear/editar usuarios con selector de rol y sede).
  * `AboutSystemView.vue`: Copiar a `frontend/src/features/admin/about/views/AboutSystemView.vue` (ficha de versión del software, stack tecnológico, estado de servicios).
  * `SystemLogsView.vue`: Copiar a `frontend/src/features/admin/views/SystemLogsView.vue` (consola de visualización de eventos de auditoría y logs del backend en tiempo real).

#### 3. Comando Git:
```bash
npm run build
git add frontend/src/features/admin/
git commit -m "feat(admin-ti): implementar administracion de usuarios, asignacion de roles, consola de logs y acerca de"
```

---

### Commit 8: Módulo de Gerencia General (Sedes, Catálogos y Padrón de Flota MTC)

**Objetivo:** Proporcionar al rol de Gerencia General visibilidad y control sobre las sedes portuarias, catálogos auxiliares, resumen ejecutivo y padrón de tractos/conductores.

#### 1. Crear carpetas:
```bash
mkdir -p frontend/src/features/admin/sedes/views
mkdir -p frontend/src/features/admin/catalogos/views
mkdir -p frontend/src/features/admin/overview/views
mkdir -p frontend/src/features/gerencia/routes
mkdir -p frontend/src/features/gerencia/views
```

#### 2. Copiar archivos:
* **Vistas de Sedes y Catálogos:**
  * `SedesManagementView.vue`: Copiar a `frontend/src/features/admin/sedes/views/SedesManagementView.vue` (mantenimiento de sedes Callao, Paita, Matarani).
  * `CatalogosManagementView.vue`: Copiar a `frontend/src/features/admin/catalogos/views/CatalogosManagementView.vue` (gestión de puertos marítimos, tipos de carga y mercancías con descarga de reportes PDF oficiales).
  * `AdminOverviewView.vue`: Copiar a `frontend/src/features/admin/overview/views/AdminOverviewView.vue` (dashboard con indicadores analíticos consolidados).
* **Vistas de Gerencia Flota y Conductores:**
  * `GerenciaFlotaView.vue`: Copiar a `frontend/src/features/gerencia/views/GerenciaFlotaView.vue` (padrón integral de unidades de transporte: placas de tracto y carreta, capacidad de carga útil en toneladas).
  * `GerenciaConductoresView.vue`: Copiar a `frontend/src/features/gerencia/views/GerenciaConductoresView.vue` (padrón de choferes profesionales con validación de categoría de licencia MTC).
* **Rutas de Gerencia:**
  * Copiar `frontend/src/features/gerencia/routes/gerencia.routes.js`.

#### 3. Comando Git:
```bash
npm run build
git add frontend/src/features/admin/sedes/ frontend/src/features/admin/catalogos/ frontend/src/features/admin/overview/ frontend/src/features/gerencia/
git commit -m "feat(gerencia): implementar gestion de sedes, catalogos portuarios con pdf, padron de flota y choferes mtc"
```

---

### Commit 9: Módulo Comercial (Clientes, Cotizador Avanzado de 4 Pasos y Proforma)

**Objetivo:** Implementar la gestión comercial de clientes con clasificación automática y el asistente interactivo de cotización aduanera y cálculo de costos logísticos.

#### 1. Crear carpetas:
```bash
mkdir -p frontend/src/features/roles-modules/data
mkdir -p frontend/src/features/roles-modules/services
mkdir -p frontend/src/features/roles-modules/components
mkdir -p frontend/src/features/roles-modules/views
```

#### 2. Copiar archivos:
* **Maestro Geográfico de Rutas:**
  * Copiar `frontend/src/features/roles-modules/data/peruUbigeo.js` (departamentos, provincias y costos base de flete terrestre nacional por ruta).
* **Servicio Comercial:**
  * Copiar `frontend/src/features/roles-modules/services/comercialService.js`: CRUD de clientes (`/api/v1/clientes`) y cotizaciones (`/api/v1/cotizaciones`).
* **Componentes de Alta Densidad Comercial:**
  * `ClienteRegistrationForm.vue`: Formulario de registro de clientes con validación automática de RUC/DNI y clasificación de riesgo comercial.
  * `CotizacionFormHeader.vue`: Cabecera del cotizador con datos rápidos del cliente.
  * `CotizacionStepperNav.vue`: Barra interactiva de los 4 pasos: *1. Cliente -> 2. Datos de Carga -> 3. Logística y Fletes -> 4. Resumen*.
  * `CotizacionProformaPreview.vue`: Tarjeta flotante con desglose de flete marítimo, flete nacional, agenciamiento, seguro, IGV y total proforma en tiempo real.
  * `CotizacionRegistrationForm.vue`: Componente orquestador del cotizador completo con cálculo automático de costos.
* **Vista Comercial:**
  * Copiar `frontend/src/features/roles-modules/views/RecepcionComercialView.vue`: Bandeja principal del Agente Comercial con tabla de cotizaciones, filtros de estado y acceso rápido a emisión de proformas.

#### 3. Comando Git:
```bash
npm run build
git add frontend/src/features/roles-modules/data/ frontend/src/features/roles-modules/services/comercialService.js frontend/src/features/roles-modules/components/Cotizacion* frontend/src/features/roles-modules/components/Cliente* frontend/src/features/roles-modules/views/RecepcionComercialView.vue
git commit -m "feat(comercial): implementar registro de clientes, cotizador interactivo de 4 pasos con proforma y bandeja comercial"
```

---

### Commit 10: Módulo de Operaciones Aduaneras (Expedientes DAM, Canales SUNAT y Auditoría Legal)

**Objetivo:** Implementar la compuerta de apertura de expedientes aduaneros, asignación de canal de control SUNAT, trámite de levante y auditoría jurídica.

#### 1. Copiar archivos:
* **Servicios de Aduanas:**
  * Copiar `frontend/src/features/roles-modules/services/expedienteService.js` (consumo de `/api/v1/expedientes`).
  * Copiar `frontend/src/features/roles-modules/services/gestionAduaneraService.js`.
* **Modales de Operación Aduanera:**
  * `AperturaExpedienteModal.vue`: Modal para numerar DAM de 18 dígitos y abrir expediente sobre una cotización con pago aprobado.
  * `CanalLevanteModal.vue`: Asignación de canal SUNAT (Verde, Naranja, Rojo) y registro de levante aduanero.
  * `DetalleExpedienteModal.vue`: Línea de tiempo cronológica del trámite aduanero.
  * `AuditoriaExpedienteModal.vue`: Inspección técnica integral del expediente, documentos de soporte y eventos de trazabilidad.
* **Vistas de Operación Aduanera:**
  * `GestionAduaneraView.vue`: Centro de comando del Supervisor Aduanero con filtros por canal SUNAT, búsqueda por número DAM y métricas de despacho.
  * `LiquidacionAduaneraView.vue`: Liquidación técnica de tributos arancelarios.
  * `AgenteAduaneroView.vue`: Bandeja operativa enfocada en trámites de campo y aforo aduanero.
  * `LegalAuditoriaView.vue`: Módulo para el Asesor Legal con auditoría de expedientes y dictámenes normativos.

#### 2. Comando Git:
```bash
npm run build
git add frontend/src/features/roles-modules/services/expedienteService.js frontend/src/features/roles-modules/services/gestionAduaneraService.js frontend/src/features/roles-modules/components/AperturaExpedienteModal.vue frontend/src/features/roles-modules/components/CanalLevanteModal.vue frontend/src/features/roles-modules/components/DetalleExpedienteModal.vue frontend/src/features/roles-modules/components/AuditoriaExpedienteModal.vue frontend/src/features/roles-modules/views/GestionAduaneraView.vue frontend/src/features/roles-modules/views/LiquidacionAduaneraView.vue frontend/src/features/roles-modules/views/AgenteAduaneroView.vue frontend/src/features/roles-modules/views/LegalAuditoriaView.vue
git commit -m "feat(aduanas-legal): implementar gestion de expedientes dam, canales sunat, levante aduanero y auditoria legal"
```

---

### Commit 11: Módulos de Operaciones Logísticas (Almacén VGM, Flota en Ruta) y Finanzas Integral

**Objetivo:** Completar la logística operativa (recepción en balanza de almacén, programación de tractos en ruta) y la gestión financiera de cobros y liquidaciones.

#### 1. Copiar archivos:
* **Servicios:**
  * `almacenService.js`: Copiar a `frontend/src/features/roles-modules/services/almacenService.js` (pesaje VGM, control de estiba, salida física).
  * `transporteFlotaService.js`: Copiar a `frontend/src/features/roles-modules/services/transporteFlotaService.js` (despachos de flota, asignación de chofer/unidad, eventos de ruta).
  * `finanzasService.js`: Copiar a `frontend/src/features/roles-modules/services/finanzasService.js` (pre-pagos de cotización, liquidaciones contables, detracciones SPOT).
* **Modales Logísticos y Financieros:**
  * `IngresoAlmacenModal.vue`: Registro de pesaje VGM balanza (peso bruto, tara, neto) y ubicación física de estiba.
  * `CrearDespachoModal.vue`: Programación de flete terrestre vinculado al expediente aduanero.
  * `AsignarRecursosModal.vue`: Asignación de tracto, carreta y chofer MTC disponible.
  * `ConfirmarEntregaModal.vue`: Cierre de despacho con firma de guía de remisión conforme.
  * `RegistrarPagoModal.vue`: Registro del comprobante de abono bancario y validación por parte del `CONTADOR`.
* **Vistas:**
  * `TransporteFlotaView.vue`: Consola de control del Supervisor de Transporte con monitoreo de unidades en ruta.
  * `FinanzasView.vue`: Módulo integral del Contador con panel de pre-pagos por aprobar, cálculo de liquidaciones finales, emisión de facturas y detracciones tributarias SPOT Banco de la Nación.

#### 2. Comando Git:
```bash
npm run build
git add frontend/src/features/roles-modules/services/almacenService.js frontend/src/features/roles-modules/services/transporteFlotaService.js frontend/src/features/roles-modules/services/finanzasService.js frontend/src/features/roles-modules/components/IngresoAlmacenModal.vue frontend/src/features/roles-modules/components/CrearDespachoModal.vue frontend/src/features/roles-modules/components/AsignarRecursosModal.vue frontend/src/features/roles-modules/components/ConfirmarEntregaModal.vue frontend/src/features/roles-modules/components/RegistrarPagoModal.vue frontend/src/features/roles-modules/views/TransporteFlotaView.vue frontend/src/features/roles-modules/views/FinanzasView.vue
git commit -m "feat(logistica-finanzas): integrar recepcion vgm en almacen, monitoreo de flota en ruta y modulo financiero con pre-pago"
```

---

### Commit 12: Endurecimiento, Optimización Build y Conexión E2E

**Objetivo:** Vincular todas las rutas operativas en el enrutador maestro, validar la compilación de producción con Vite y certificar la comunicación fluida con la API REST.

#### 1. Configurar `frontend/src/features/roles-modules/routes/roles.routes.js`:
Copiar de: `frontend/src/features/roles-modules/routes/roles.routes.js`
*Registra las 7 rutas operativas protegidas con lazy loading:*
* `/recepcion-comercial`
* `/gestion-aduanera`
* `/gestion-aduanera/liquidacion`
* `/agente-aduanero`
* `/transporte-flota`
* `/finanzas`
* `/legal-auditoria`

#### 2. Compilación de Producción:
```bash
npm run build
```
*Resultado esperado:*
```
vite v8.3.0 building for production...
transforming...
✓ 142 modules transformed.
rendering chunks...
computing gzip size...
dist/index.html                   0.82 kB │ gzip:  0.45 kB
dist/assets/index-*.css          29.74 kB │ gzip:  5.82 kB
dist/assets/index-*.js          382.15 kB │ gzip: 114.28 kB
✓ built in 420ms
```

#### 3. Comando Git Final:
```bash
git add frontend/src/features/roles-modules/routes/roles.routes.js
git commit -m "test(frontend): validar compilacion de produccion, integracion completa de 16 vistas y conexion rest con backend"
```

---

## 4. Guía de Ejecución y Pruebas Rápidas de Navegación

### 4.1 Arrancar el Servidor de Desarrollo
```bash
cd frontend
npm run dev
```
La aplicación cliente estará disponible inmediatamente en: **`http://localhost:5180`**.

### 4.2 Matriz de Pruebas de Acceso por Rol
Para validar el comportamiento del router y la guardia RBAC, inicie sesión con cualquiera de los 8 perfiles institucionales:

| Rol Institucional | Usuario | Contraseña | Ruta de Aterrizaje Inicial | Módulos Autorizados |
| :--- | :---: | :---: | :--- | :--- |
| **Administrador de TI** | `admin` | `Password123!` | `/administracion-ti/usuarios` | Acceso y supervisión global total |
| **Gerente General** | `gerente` | `Password123!` | `/gerencia-general/sedes` | Sedes, Catálogos, Flota y Supervisión |
| **Agente Comercial** | `comercial` | `Password123!` | `/recepcion-comercial` | Clientes y Emisión de Cotizaciones |
| **Supervisor Aduanero** | `supervisor` | `Password123!` | `/gestion-aduanera` | Apertura de DAM y Canales SUNAT |
| **Agente Aduanero** | `agente` | `Password123!` | `/agente-aduanero` | Aforo y Trámites Operativos |
| **Supervisor de Transporte** | `transporte` | `Password123!` | `/transporte-flota` | Asignación de Flota y Monitoreo en Ruta |
| **Contador** | `contador` | `Password123!` | `/finanzas` | Aprobación de Pre-Pagos y Liquidación |
| **Asesor Legal** | `legal` | `Password123!` | `/legal-auditoria` | Auditoría Técnica y Cumplimiento |

### 4.3 Verificación del Flujo de Negocio Extremo a Extremo (E2E)
1. **Comercial:** Ingresar como `comercial`, dar de alta un cliente RUC y generar una cotización de importación por $3,500 USD (cálculo automático de flete Callao-Lima + seguro + IGV).
2. **Finanzas (Compuerta Pre-Expediente):** Ingresar como `contador`, dirigirse a `/finanzas`, ubicar la cotización generada, cargar comprobante de abono bancario y **Aprobar Pago**.
3. **Aduanas:** Ingresar como `supervisor`, abrir `/gestion-aduanera`, aperturar el expediente aduanero `EXP-2026-0001` con número DAM `118-2026-10-000001`. Comprobar que la compuerta permite la creación exitosa gracias al pago previo.
4. **Inspección SUNAT:** Asignar canal `VERDE` y autorizar levante aduanero.
5. **Transporte:** Ingresar como `transporte`, programar despacho terrestre, asignar camión tracto y chofer MTC, iniciar viaje en ruta y registrar entrega final.
6. **Liquidación:** Ingresar como `contador`, generar liquidación financiera final y descargar reporte en PDF.

---

## 5. Resumen de Entregables del Frontend

Al concluir los 12 commits de esta guía, la réplica del cliente frontend contará con:
* **16 Vistas SPA completas:** Cubriendo la totalidad del ciclo comercial, aduanero, logístico y financiero.
* **14 Modales y Componentes de Alta Densidad:** Formularios con validación en tiempo real y transiciones suaves.
* **8 Módulos Segregados por Rol:** Con menús adaptables y restricción estricta de navegación.
* **Sistema de Diseño *Warm Editorial 2.0*:** Vanilla CSS con variables corporativas y soporte claro/oscuro.
* **Consumo Integral de los 126 Endpoints:** Mediante servicios HTTP desacoplados basados en Axios.
