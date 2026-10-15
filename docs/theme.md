# Especificación del Lenguaje de Diseño y Componentes — SGA (Sistema de Gestión Aduanera)

Este documento describe la especificación completa del sistema de diseño visual, la arquitectura de tokens, la integración con PrimeVue 4 y el catálogo de componentes utilizados en el frontend de **SGA**.

---

## 1. Filosofía de Diseño: *Warm Editorial*

El frontend de SGA adopta un lenguaje visual denominado **Warm Editorial**:
* **Sensación:** Cálida, sobria, documental, ejecutiva y orientada a la precisión de las operaciones aduaneras.
* **Metáfora visual:** Papel marfil, arena desértica portuaria, tonos café latte y espresso tostado, complementados en modo oscuro por gris titanio pizarra y acentos en **arena dorada**.
* **Diferenciación:** Se aleja intencionalmente de las interfaces empresariales frías y genéricas (azules sintéticos o grises fríos predeterminados de Tailwind), ofreciendo un estilo refinado con aspecto de publicación editorial técnica.
* **Micro-interacciones:** Elevación sutil en tarjetas (`translateY(-2px)`), desenfoque de fondo en modales (`backdrop-filter: blur(6px)`), e indicadores de estado con pulsos luminosos de conectividad (`.status-pulse`).

---

## 2. Tokens de Diseño (Design Tokens)

Todas las variables globales de diseño están declaradas en `frontend/src/style.css`, organizadas mediante variables CSS reactivas que se conmutan a través de la clase `.dark-theme` en el elemento raíz `<html>`.

### 2.1. Paleta de Colores

| Token CSS | Modo Claro | Modo Oscuro (`.dark-theme`) | Uso Principal |
| :--- | :--- | :--- | :--- |
| `--color-bg-base` | `#f6f3ee` (Arena base) | `#1e1e1b` (Gris carbón) | Fondo general de la aplicación |
| `--color-bg-sidebar` | `#ede6db` (Latte suave) | `#191815` (Gris titanio) | Fondo de la barra lateral |
| `--color-bg-surface` | `#ffffff` (Blanco puro) | `#282723` (Piedra cálida) | Tarjetas, modales, celdas de tablas |
| `--color-bg-subtle` | `#f2ebe1` (Avena suave) | `#2e2d27` (Gris suave) | Headers de tablas, fondos secundarios |
| `--color-bg-muted` | `#efe8de` | `#383630` | Hover en listas y estados inactivos |
| `--color-bg-hero` | `#ebdcc9` | `#33312a` | Fondos de banners destacados |
| `--color-border` | `#e4dacb` | `#444138` | Bordes estándar de tarjetas e inputs |
| `--color-border-subtle` | `#ede5d8` | `#36342c` | Separadores y divisores sutiles |
| `--color-border-strong` | `#d5c8b6` | `#5c564a` | Bordes activos y hover |
| `--color-primary` | `#352f2a` (Espresso) | `#d6b476` (Arena dorada) | Botones primarios, acentos activos |
| `--color-primary-hover`| `#484039` | `#e5c78f` | Hover de acciones principales |
| `--color-primary-text` | `#ffffff` | `#1d1b15` | Texto contrastado sobre color primario |
| `--color-accent-kicker`| `#9a7f60` (Caramelo) | `#e0c287` (Oro cálido) | Subtítulos kicker, etiquetas numéricas |
| `--color-text-main` | `#2b2520` (Carbón) | `#f6f2ea` (Marfil cálido) | Títulos principales y texto de lectura |
| `--color-text-muted` | `#6f6356` | `#c4bcab` | Texto secundario y descriptivo |
| `--color-text-subtle` | `#998c7e` | `#968e7e` | Metadatos, fechas, placeholders |

### 2.2. Estados Funcionales Semánticos

| Estado | Fondo (Claro / Oscuro) | Borde (Claro / Oscuro) | Texto / Icono (Claro / Oscuro) |
| :--- | :--- | :--- | :--- |
| **Success** (Aprobado/Activo) | `#eaf6ed` / `#1c2b1f` | `#bce5c6` / `#2d5738` | `#1e6638` / `#7ee79d` |
| **Warning / Pending** | `#fdf5e6` / `#342914` | `#f8dab3` / `#695222` | `#8c5b16` / `#fcd34d` |
| **Danger** (Rechazado/Error) | `#fdf0f0` / `#321818` | `#f6c8c8` / `#652a2a` | `#9e2a2b` / `#fca5a5` |
| **Info / Progress** | `#eef4fb` / `#172635` | `#cadff4` / `#254b6d` | `#21598f` / `#93c5fd` |

### 2.3. Radios, Sombras y Transiciones

* **Radios de Borde:**
  * `--radius-xs: 0.25rem;` (4px)
  * `--radius-sm: 0.375rem;` (6px)
  * `--radius-md: 0.5rem;` (8px — Estándar para botones e inputs)
  * `--radius-lg: 0.75rem;` (12px — Estándar para tarjetas y paneles)
  * `--radius-xl: 1rem;` (16px — Modales y diálogos)
  * `--radius-pill: 9999px;` (Píldoras y badges redondeados)

* **Sombras de Elevación:**
  * `--shadow-subtle`: `0 1px 3px rgba(45, 35, 25, 0.04), 0 1px 2px rgba(45, 35, 25, 0.02)`
  * `--shadow-card`: `0 4px 16px -2px rgba(50, 40, 30, 0.04), 0 2px 6px -1px rgba(50, 40, 30, 0.02)`
  * `--shadow-elevated`: `0 12px 28px -6px rgba(45, 35, 25, 0.08), 0 4px 10px -2px rgba(45, 35, 25, 0.03)` (Modales, tooltips y menús desplegables)

* **Curvas de Transición:**
  * `--transition-fast`: `0.15s cubic-bezier(0.4, 0, 0.2, 1)`
  * `--transition-normal`: `0.2s cubic-bezier(0.4, 0, 0.2, 1)`

---

## 3. Tipografía y Jerarquía

* **Fuente Principal:** `'Inter', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif`.
* **Kickers (`.badge-kicker`):** Mini-encabezados en mayúsculas sostenidas, tamaño `0.6875rem` (11px), peso `700`, interletraje `letter-spacing: 0.08em` a `0.1em`, color `--color-accent-kicker`. Se utilizan para contextualizar jerárquicamente secciones y vistas (ej: `SUB-OPCIÓN 01`, `PANEL DE CONTROL CENTRAL // TI & AUDITORÍA`).
* **Títulos de Página / Hero (`.hero-title`):** `1.625rem` (26px), peso `700`, espaciado `letter-spacing: -0.015em`, interlineado `1.25`.
* **Métricas y KPIs (`.metric-val`):** `1.85rem` (30px), peso `700`, `letter-spacing: -0.02em`.
* **Cuerpo y Controles de Formulario:** `0.84rem` a `0.875rem` (13.5px - 14px), interlineado `1.5`.

---

## 4. Configuración del Preset de PrimeVue (`SgaPreset`)

En `frontend/src/main.js` se define un preset personalizado que extiende el tema **Aura** (`@primevue/themes/aura`) usando la función `definePreset`:

```javascript
import PrimeVue from 'primevue/config'
import { definePreset } from '@primevue/themes'
import Aura from '@primevue/themes/aura'
import ToastService from 'primevue/toastservice'
import ConfirmationService from 'primevue/confirmationservice'

const SgaPreset = definePreset(Aura, {
  semantic: {
    primary: {
      50: '#f6f3ee',
      100: '#ede6db',
      200: '#e4dacb',
      300: '#cfc0ab',
      400: '#9a7f60',
      500: '#352f2a',
      600: '#2b2520',
      700: '#231e1a',
      800: '#1c1815',
      900: '#120f0d',
      950: '#0a0807'
    },
    colorScheme: {
      light: {
        surface: {
          0: '#ffffff', 50: '#fbf9f6', 100: '#f6f3ee', 200: '#ede6db',
          300: '#e4dacb', 400: '#cfc0ab', 500: '#998c7e', 600: '#6f6356',
          700: '#484039', 800: '#352f2a', 900: '#2b2520', 950: '#1c1815'
        }
      },
      dark: {
        surface: {
          0: '#191815', 50: '#1e1e1b', 100: '#282723', 200: '#2e2d27',
          300: '#383630', 400: '#444138', 500: '#5c564a', 600: '#968e7e',
          700: '#c4bcab', 800: '#ede6db', 900: '#f6f2ea', 950: '#ffffff'
        }
      }
    }
  }
})

app.use(PrimeVue, {
  theme: {
    preset: SgaPreset,
    options: {
      darkModeSelector: '.dark-theme'
    }
  }
})
app.use(ToastService)
app.use(ConfirmationService)
```

### Gestión de Tema Reactivo (`useTheme.js`)
El estado claro/oscuro se administra en `frontend/src/shared/composables/useTheme.js`:
* Persiste la clave `sga-theme` (`light` | `dark`) en `localStorage`.
* Detecta automáticamente las preferencias del sistema operativo (`prefers-color-scheme: dark`) si no hay valor guardado.
* Añade o retira la clase `.dark-theme` del elemento `document.documentElement`.

---

## 5. Catálogo de Componentes del Sistema

### 5.1. Componentes de Estructura y Layout

1. **`AppLayout.vue` (`src/core/layouts/AppLayout.vue`):**
   * **Topbar:** Breadcrumbs con raíz `SGA / {Módulo}`, título de página activo.
   * **Badge de Sede:** Indicador de la sede territorial asignada al operador (`Callao`, `Paita`, etc.).
   * **Heartbeat de Conexión:** Indicador interactivo con pulso (`.status-pulse`) que valida la conexión en tiempo real con el backend Spring Boot.
   * **Menú de Perfil & Conmutador de Tema:** Menú desplegable flotante con avatar, nombre, rol, sede y el switch reactivo para alternar entre Modo Claro y Modo Oscuro.

2. **`AppSidebar.vue` (`src/shared/components/AppSidebar.vue`):**
   * **Barra Lateral Colapsable:** Botón para comprimir/expandir la barra lateral (`isCollapsed`).
   * **Selector de Módulo Activo:** Menú dropdown para usuarios autorizados (Administrador TI, Gerencia) que permite cambiar de módulo de trabajo en tiempo real.
   * **Navegación Indexada:** Lista de sub-opciones ordenadas con numeración editorial (`01`, `02`, `03`...) y estado activo destacado.

### 5.2. Componentes Compartidos (Shared UI Components)

Ubicados en `src/shared/components/`:

1. **`HeroBannerCard.vue` (`src/shared/components/HeroBannerCard.vue`):**
   * Banner principal de cada sección con degradado oscuro (`linear-gradient(135deg, #1c1917, #292524, #453406)`), borde con toque dorado, kicker badge, título, descripción y marca de agua `SGA` al 3% de opacidad.
   * **Props:** `title` (String), `kicker` (String), `description` (String), `watermark` (String).

2. **`MetricCard.vue` (`src/shared/components/MetricCard.vue`):**
   * Tarjeta de métrica/KPI con índice numérico (`01`, `02`), título, valor numérico destacado y subtítulo descriptivo con variantes semánticas.
   * **Props:** `index` (String), `label` (String), `value` (String|Number), `sublabel` (String), `highlight` (Boolean), `severity` (`accent` | `success` | `warning` | `pending` | `danger`).

3. **`BaseModal.vue` (`src/shared/components/BaseModal.vue`):**
   * Diálogo modal envolvente sobre `Dialog` de PrimeVue con desenfoque de fondo (`backdrop-filter: blur(6px)`).
   * Encabezado integrado con icono temático, título, subtítulo, botón de cierre y sección para alertas de error.
   * Provee clases compartidas para formularios: `.form-body`, `.form-grid`, `.field-item`, `.text-input`, `.modal-actions`.
   * **Props:** `visible` (Boolean), `title` (String), `subtitle` (String), `icon` (String), `maxWidth` (String), `errorMessage` (String), `busy` (Boolean), `closeOnBackdrop` (Boolean).

4. **`BasePagination.vue` (`src/shared/components/BasePagination.vue`):**
   * Barra de paginación para tablas con desglose de registros (`Mostrando 1 a 10 de 50 registros`), etiqueta de página actual y botones de paginación anterior/siguiente integrados al diseño cálido.
   * **Props:** `currentPage` (Number), `totalPages` (Number), `totalItems` (Number), `startIndex` (Number), `endIndex` (Number), `itemLabel` (String), `disabled` (Boolean).

5. **`UserRoleBadge.vue` (`src/features/admin/users/components/UserRoleBadge.vue`):**
   * Tag redondeado que mapea automáticamente iconos de PrimeIcons y severidades de PrimeVue según el rol de aduanas (`ADMINISTRADOR`, `VENTAS`, `LOGISTICA`, `AGENTE_ADUANERO`, `LIQUIDADOR`, `LEGAL`, `FINANZAS`, `TRANSPORTE`).

6. **`ModulePlaceholder.vue` (`src/shared/components/ModulePlaceholder.vue`):**
   * Pantalla de estado vacío / en construcción con icono representativo, descripción técnica y accesos rápidos.

---

## 6. Clases Utilitarias Globales

| Clase CSS | Descripción / Efecto |
| :--- | :--- |
| `.btn-espresso` | Botón primario en color espresso con texto blanco (o arena dorada en dark mode), sombra sutil y elevación `translateY(-1px)` en hover. |
| `.btn-outline-warm` | Botón secundario con borde `--color-border`, fondo de superficie y texto principal. |
| `.badge-kicker` | Mini-etiqueta en mayúsculas para encabezados y categorización. |
| `.status-badge` | Píldora base para estados del flujo aduanero. |
| `.status-approved` | Píldora verde cálida para cotizaciones u órdenes autorizadas. |
| `.status-pending` | Píldora ámbar cálida para estados en espera o revisión. |
| `.status-rejected` | Píldora carmesí suave para desaprobaciones o bloqueos. |
| `.status-progress` | Píldora azul suave para trámites en curso o levante aduanero. |
| `.tabs-nav-bar` | Contenedor de pestañas horizontales con borde inferior cálido. |
| `.tab-btn` / `.tab-btn.active` | Pestaña editorial con indicador inferior sólido y esquinas superiores redondeadas. |

---

## 7. Guía de Implementación para Nuevas Vistas

Ejemplo de estructura recomendada para construir una nueva pantalla respetando el estándar visual de SGA:

```vue
<template>
  <div class="modulo-view">
    <!-- 1. Hero Banner Superior -->
    <HeroBannerCard
      kicker="OPERACIONES // CALLAO"
      title="Gestión de Trámites y Despachos"
      description="Supervisión de expedientes con canal asignado por SUNAT."
      watermark="SGA"
    />

    <!-- 2. Rejilla de Indicadores (KPIs) -->
    <div class="metrics-grid">
      <MetricCard
        index="01"
        label="En Proceso"
        :value="18"
        sublabel="Expedientes en aforo"
        severity="accent"
      />
      <MetricCard
        index="02"
        label="Levante Autorizado"
        :value="42"
        sublabel="Listos para retiro"
        severity="success"
      />
    </div>

    <!-- 3. Modal Estándar SGA -->
    <BaseModal
      :visible="isModalOpen"
      title="Nuevo Trámite Aduanero"
      subtitle="Complete los datos según la DUA / DAM correspondiente"
      icon="pi pi-file-edit"
      maxWidth="680px"
      @close="isModalOpen = false"
    >
      <form class="form-body" @submit.prevent="guardar">
        <div class="form-grid">
          <div class="field-item">
            <label>Número de Declaración</label>
            <input type="text" class="text-input" placeholder="Ej: 118-2026-10-..." required />
          </div>
          <div class="field-item">
            <label>Régimen</label>
            <select class="select-input">
              <option value="10">Importación para el Consumo (10)</option>
              <option value="40">Exportación Definitiva (40)</option>
            </select>
          </div>
        </div>

        <div class="modal-actions">
          <button type="button" class="btn-modal-cancel" @click="isModalOpen = false">Cancelar</button>
          <button type="submit" class="btn-modal-save">
            <i class="pi pi-check"></i>
            Guardar Expediente
          </button>
        </div>
      </form>
    </BaseModal>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import HeroBannerCard from '@/shared/components/HeroBannerCard.vue'
import MetricCard from '@/shared/components/MetricCard.vue'
import BaseModal from '@/shared/components/BaseModal.vue'

const isModalOpen = ref(false)
const guardar = () => {
  // Lógica de guardado
}
</script>
```
