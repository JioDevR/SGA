# Desglose Arquitectural y Tecnológico del Frontend (SGA)

Este documento presenta un análisis exhaustivo y técnico de las tecnologías, librerías, dependencias, patrones de diseño y arquitectura utilizados en el frontend del **Sistema de Gestión Aduanera (SGA)** (`frontend`).

---

## 1. Núcleo y Runtime

| Componente | Versión / Herramienta | Rol y Justificación |
| :--- | :--- | :--- |
| **Vue.js** | **3.5.42** (Composition API) | Framework reactivo moderno progresivo. Utiliza `<script setup>`, reactividad profunda basada en Proxies de ES6 y un ciclo de vida optimizado. |
| **Vite** | **8.3.0** (`@vitejs/plugin-vue` v6.0.8) | Herramienta de compilación y empaquetador ultrarrápido basado en Rollup y ES Modules nativos con Hot Module Replacement (HMR) casi instantáneo. |
| **JavaScript / ESM** | ECMAScript Modules (`"type": "module"`) | Estándar moderno de importación/exportación de módulos JavaScript sin transpilaciones innecesarias en desarrollo. |

---

## 2. Dependencias y Librerías Principales

A continuación se detalla cada una de las dependencias declaradas en el [`package.json`](file:///home/jiodev/Documents/Proyects/SGA/frontend/package.json) y su función específica en la solución:

### A. PrimeVue & Sistema de Componentes UI
* **Dependencias:**
  * `primevue` (v4.5.5)
  * `@primevue/themes` (v4.5.4)
  * `primeicons` (v8.0.2)
* **¿Qué hace en el sistema?**
  * Provee una suite completa y accesible de componentes UI para aplicaciones empresariales de misión crítica: tablas de datos interactivas (`DataTable`, `Column`), diálogos modales (`Dialog`), tarjetas (`Card`), menús, botones (`Button`), campos de texto (`InputText`, `InputNumber`), selectores (`Dropdown` / `Select`), etiquetas de estado (`Tag`), spinners y barras de progreso.
  * **PrimeIcons:** Colección de más de 200 íconos vectoriales SVG optimizados para acciones de interfaz (candados, usuarios, edificios, barcos, camiones, edición, reportes y estados).
  * **Servicios Globales de Interacción:**
    * `ToastService`: Notificaciones emergentes ligeras y reactivas (`toast.add({ severity: 'success', summary: '...', detail: '...' })`).
    * `ConfirmationService`: Diálogos modales de confirmación con callbacks seguros (`confirm.require(...)`) para eliminar o actualizar registros críticos.

---

### B. Sistema de Temas y Dark Mode (`SgaPreset` sobre Aura)
* **Mecanismo:** Definición mediante `definePreset(Aura, { ... })` en [`main.js`](file:///home/jiodev/Documents/Proyects/SGA/frontend/src/main.js).
* **¿Qué hace en el sistema?**
  * Implementa la identidad visual exclusiva del SGA inspirada en tonos tierra y café de alta gama:
    * **Primary Palette (Espresso & Café):** Desde tonos crema (`50: #f6f3ee`) hasta el color principal espresso profundo (`500: #352f2a`) y negro tostado (`950: #0a0807`).
    * **Surface Light:** Fondos cálidos marfil/arena (`#ffffff`, `#fbf9f6`, `#ede6db`).
    * **Surface Dark:** Modo oscuro refinado carbón profundo con matices marrones (`#191815`, `#1e1e1b`, `#282723`), evitando el negro puro estéril.
  * **Selector de Modo Oscuro:** `darkModeSelector: '.dark-theme'`.
  * **Composable de Estado:** [`useTheme.js`](file:///home/jiodev/Documents/Proyects/SGA/frontend/src/shared/composables/useTheme.js) permite alternar el modo oscuro, sincronizarlo con la preferencia del sistema operativo (`prefers-color-scheme`) y persistirlo en `localStorage` bajo la clave `sga-theme`.

---

### C. Enrutamiento y Navegación con Permisos (Vue Router)
* **Dependencias:**
  * `vue-router` (v4.6.4)
* **¿Qué hace en el sistema?**
  * Controla la navegación del lado del cliente utilizando historial HTML5 limpio (`createWebHistory()`) en [`router/index.js`](file:///home/jiodev/Documents/Proyects/SGA/frontend/src/core/router/index.js).
  * **Guardias Globales (`beforeEach`):**
    * **Protección de rutas autenticadas:** Redirige automáticamente a `/login` si no existe un token válido y guarda la URL previa (`redirect query param`).
    * **Control de acceso basado en roles (RBAC):** Valida las metas de ruta (`meta.roles`) contra el rol del usuario autenticado en la sesión (`ADMIN`, `GERENTE`, `VENTAS`, etc.), impidiendo accesos no autorizados.
    * **Sincronización con el Menú Lateral:** Detecta el segmento de la URL activa y actualiza automáticamente el estado en `navigationStore`.
    * **Actualización dinámica de títulos:** Modifica `document.title` en cada cambio de vista.

---

### D. Gestión de Estado Global (Pinia)
* **Dependencias:**
  * `pinia` (v4.0.3)
* **¿Qué hace en el sistema?**
  * Almacén de estado reactivo y modular para Vue 3:
    * **`authStore` ([`authStore.js`](file:///home/jiodev/Documents/Proyects/SGA/frontend/src/features/auth/stores/authStore.js)):** Gestiona el estado de autenticación, el token JWT activo, los datos del usuario logueado (`username`, `nombres`, `rol`, `sede`), el guardado en `localStorage` y la ruta inicial recomendada según el rol del usuario.
    * **`navigationStore` ([`navigationStore.js`](file:///home/jiodev/Documents/Proyects/SGA/frontend/src/core/stores/navigationStore.js)):** Mantiene el módulo actualmente activo en el menú lateral, submódulos de administración (`users`, `about`), submódulos de gerencia (`sedes`, `catalogos`, `overview`) y el colapso responsivo del sidebar.

---

### E. Comunicación HTTP y Consumo de API REST (Axios)
* **Dependencias:**
  * `axios` (v1.20.0)
* **¿Qué hace en el sistema?**
  * Cliente HTTP configurado de forma centralizada en [`apiClient.js`](file:///home/jiodev/Documents/Proyects/SGA/frontend/src/core/api/apiClient.js) con timeout de 15 segundos y base URL configurable (`http://localhost:8085/api/v1`).
  * **Interceptores de Petición:** Inserta automáticamente la cabecera `Authorization: Bearer <token>` obtenida de `localStorage` en cada solicitud saliente hacia el backend.
  * **Interceptores de Respuesta:** Captura respuestas de error HTTP `401 Unauthorized` (token caducado o inválido), limpia la sesión almacenada en el navegador y redirige inmediatamente al usuario a `/login`.
  * **Soporte de Descarga Binaria:** Permite solicitar reportes en formato Blob (`responseType: 'blob'`) para abrir o descargar los archivos PDF generados por JasperReports.

---

## 3. Arquitectura y Módulos de la Aplicación

El frontend está estructurado bajo una arquitectura modular por características (*Feature-Driven Architecture*):

1. **`core/` (Núcleo de Infraestructura):**
   * **`api/`:** Cliente HTTP singleton configurado (`apiClient.js`).
   * **`layouts/`:** Layout maestro [`AppLayout.vue`](file:///home/jiodev/Documents/Proyects/SGA/frontend/src/core/layouts/AppLayout.vue) con barra superior (TopBar), barra lateral reactiva (Sidebar), botón de alternancia de tema y área de contenido dinámico (`<router-view />`).
   * **`router/`:** Enrutador maestro y guardias de seguridad.
   * **`stores/`:** Estado global de navegación y control de layout.

2. **`features/auth/` (Autenticación y Sesión):**
   * Formulario de inicio de sesión empresarial con credenciales de acceso, validaciones de campo y retroalimentación interactiva de errores.

3. **`features/admin/` (Seguridad TI y Administración):**
   * **`users/`:** Mantenimiento CRUD completo de cuentas de usuario, asignación de roles, activación/desactivación de cuentas y botón de exportación oficial a PDF vía JasperReports.
   * **`about/`:** Ficha técnica del sistema, especificaciones de arquitectura y estado del servidor.

4. **`features/gerencia/` (Gerencia y Catálogos Operativos):**
   * **`sedes/`:** Gestión de agencias y sedes aduaneras (Callao, Paita, Aeropuerto Jorge Chávez, etc.) con exportación a PDF.
   * **`catalogos/`:** Mantenimiento de puertos marítimos/aéreos, tipos de mercancía y tipos de contenedores/carga.
   * **`overview/`:** Resumen ejecutivo e indicadores clave de gestión aduanera.

5. **`features/roles-modules/` (Módulos Especializados por Perfil):**
   * Módulos dedicados para Operaciones Aduaneras, Ventas/Cotizaciones, Transporte y Despacho, Supervisión, Almacén y Liquidación Financiera.

6. **`shared/` (Elementos Compartidos):**
   * **`composables/`:** Funciones de lógica reutilizable como `useTheme` (Dark Mode).
   * **`components/`:** Botones de acción, badges, cabeceras y tarjetas reutilizables.
   * **`utils/`:** Formateadores de fecha, moneda y estados.

---

## 4. Estructura de Directorios del Código Fuente

```text
frontend/
├── package.json                          # Dependencias, scripts y metadatos npm
├── vite.config.js                        # Configuración Vite (puerto 5180, alias @, proxy /api)
├── index.html                            # Punto de entrada HTML5 con fuentes tipográficas
└── src/
    ├── main.js                           # Inicialización de Vue 3, Pinia, PrimeVue y Tema
    ├── App.vue                           # Componente raíz con contenedor de Toasts y Diálogos
    ├── style.css                         # Variables CSS de diseño, resets y tema personalizado
    ├── assets/                           # Logotipos, imágenes y recursos estáticos
    ├── core/
    │   ├── api/                          # Cliente Axios e interceptores (apiClient.js)
    │   ├── layouts/                      # Layout general de la aplicación (AppLayout.vue)
    │   ├── router/                       # Enrutador principal y guardias RBAC
    │   └── stores/                       # Store de navegación global (navigationStore.js)
    ├── features/
    │   ├── auth/                         # Vistas, stores, rutas y servicios de autenticación
    │   ├── admin/                        # Módulos de administración de usuarios y catálogo TI
    │   ├── gerencia/                     # Vistas ejecutivas, sedes y catálogos maestros
    │   └── roles-modules/                # Vistas y flujos para perfiles operativos
    └── shared/
        ├── components/                   # Componentes visuales transversales
        ├── composables/                  # Composables reutilizables (useTheme, etc.)
        └── utils/                        # Utilidades auxiliares
```

---

## 5. Configuración de Red, Puerto y Proxy de Desarrollo

* **Puerto Local:** `http://localhost:5180` (configurado en [`vite.config.js`](file:///home/jiodev/Documents/Proyects/SGA/frontend/vite.config.js)).
* **Proxy de Desarrollo:** Las solicitudes que inician con `/api` son redirigidas de forma transparente a `http://localhost:8085` para evitar problemas de CORS durante el desarrollo local:
  ```javascript
  server: {
    port: 5180,
    proxy: {
      '/api': {
        target: 'http://localhost:8085',
        changeOrigin: true
      }
    }
  }
  ```
* **Variables de Entorno:**
  * `VITE_API_URL`: Permite sobreescribir la URL base de la API REST (por defecto `http://localhost:8085/api/v1`).
