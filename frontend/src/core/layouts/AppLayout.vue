<template>
  <div class="app-layout">
    <!-- Barra Vertical Izquierda (Sidebar) -->
    <AppSidebar />

    <!-- Área Principal de Contenido -->
    <div class="main-container">
      <!-- Topbar Minimalista con Estilo Editorial Cálido -->
      <header class="app-topbar">
        <div class="topbar-left">
          <div class="breadcrumbs">
            <span class="breadcrumb-root">SGA</span>
            <span class="breadcrumb-sep">/</span>
            <span class="breadcrumb-current">{{ currentSectionName }}</span>
          </div>
          <h1 class="topbar-page-title">{{ currentTitle }}</h1>
        </div>

        <div class="topbar-right">
          <!-- Sede Asignada Pill (trasladada del pie del sidebar) -->
          <div
            v-if="authStore.user?.codigoSede"
            class="sede-topbar-badge"
            :title="`Sede Asignada: ${authStore.user.codigoSede} — ${authStore.user.nombreSede || 'Principal'}`"
          >
            <span class="sede-badge-label">SEDE</span>
            <span class="sede-badge-text">{{ authStore.user.codigoSede }} — {{ authStore.user.nombreSede || 'Sede Central Callao' }}</span>
          </div>

          <!-- Indicador de Backend SGA -->
          <div class="system-status-indicator">
            <span class="status-pulse"></span>
            <span class="status-text">Backend SGA Conectado</span>
          </div>
          
          <!-- Perfil de Usuario con Menú Desplegable -->
          <div class="user-profile-wrapper" ref="userDropdownRef">
            <div
              class="user-profile-header"
              @click="toggleUserDropdown"
              :class="{ 'active': isUserDropdownOpen }"
              title="Opciones de cuenta"
            >
              <div class="user-avatar-circle">
                {{ avatarInitials }}
              </div>
              <div class="user-meta">
                <span class="user-full-name">{{ authStore.userFullName || authStore.user?.username }}</span>
                <span class="user-role-tag">{{ authStore.userRole || 'ADMINISTRADOR' }}</span>
              </div>
              <i :class="['pi pi-chevron-down chevron-icon', { 'rotated': isUserDropdownOpen }]"></i>
            </div>

            <!-- Menú Desplegable Flotante -->
            <transition name="dropdown-fade">
              <div v-if="isUserDropdownOpen" class="user-dropdown-panel" @click.stop>
                <div class="dropdown-header">
                  <div class="dropdown-avatar">{{ avatarInitials }}</div>
                  <div class="dropdown-user-info">
                    <div class="dropdown-user-name">{{ authStore.userFullName || authStore.user?.username }}</div>
                    <div class="dropdown-user-role">{{ authStore.userRole || 'ADMINISTRADOR' }}</div>
                  </div>
                </div>

                <div class="dropdown-divider"></div>

                <div class="dropdown-details">
                  <div v-if="authStore.user?.codigoSede" class="dropdown-detail-row">
                    <span class="detail-label">SEDE:</span>
                    <span class="detail-value">{{ authStore.user.codigoSede }} — {{ authStore.user.nombreSede || 'Principal' }}</span>
                  </div>
                  <div v-if="authStore.user?.username" class="dropdown-detail-row">
                    <span class="detail-label">USUARIO:</span>
                    <span class="detail-value">{{ authStore.user.username }}</span>
                  </div>
                </div>

                <div class="dropdown-divider"></div>

                <!-- Control de Alternancia de Tema (Modo Claro / Modo Oscuro) -->
                <div
                  class="dropdown-theme-row"
                  @click="toggleTheme"
                  role="button"
                  tabindex="0"
                  :title="isDark ? 'Cambiar a Modo Claro' : 'Cambiar a Modo Oscuro'"
                >
                  <div class="theme-info-left">
                    <div class="theme-icon-box" :class="{ 'is-dark': isDark }">
                      <i :class="['pi', isDark ? 'pi-moon' : 'pi-sun']"></i>
                    </div>
                    <div class="theme-text-col">
                      <span class="theme-title">Tema Visual</span>
                      <span class="theme-subtitle">{{ isDark ? 'Modo Oscuro' : 'Modo Claro' }}</span>
                    </div>
                  </div>
                  <div class="theme-switch-control" :class="{ 'is-dark': isDark }">
                    <div class="switch-handle">
                      <i :class="['pi', isDark ? 'pi-moon' : 'pi-sun']"></i>
                    </div>
                  </div>
                </div>

                <div class="dropdown-divider"></div>

                <button type="button" class="dropdown-logout-action" @click="handleLogout">
                  <i class="pi pi-sign-out"></i>
                  <span>Cerrar Sesión</span>
                </button>
              </div>
            </transition>
          </div>
        </div>
      </header>

      <!-- Vista Activa -->
      <main class="page-content">
        <router-view />
      </main>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import AppSidebar from '@/shared/components/AppSidebar.vue'
import { useAuthStore } from '@/features/auth/stores/authStore'
import { useNavigationStore } from '@/core/stores/navigationStore'
import { useTheme } from '@/shared/composables/useTheme'

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()
const navigationStore = useNavigationStore()
const { isDark, toggleTheme } = useTheme()

const isUserDropdownOpen = ref(false)
const userDropdownRef = ref(null)

const toggleUserDropdown = () => {
  isUserDropdownOpen.value = !isUserDropdownOpen.value
}

const handleClickOutside = (e) => {
  if (userDropdownRef.value && !userDropdownRef.value.contains(e.target)) {
    isUserDropdownOpen.value = false
  }
}

onMounted(() => {
  document.addEventListener('click', handleClickOutside)
})

onUnmounted(() => {
  document.removeEventListener('click', handleClickOutside)
})

const handleLogout = () => {
  isUserDropdownOpen.value = false
  authStore.logout()
  router.push('/login')
}

const currentSectionName = computed(() => {
  const map = {
    admin: 'Administración & Control TI',
    gerencia: 'Gerencia General & Sedes',
    ventas: 'Ventas & Comercial',
    supervision: 'Supervisión Aduanera',
    aduanas: 'Agente Aduanero & Operaciones',
    logistica: 'Agente Aduanero & Almacén',
    liquidador: 'Liquidación Aduanera',
    legal: 'Legal & Auditoría',
    finanzas: 'Finanzas',
    transporte: 'Transporte'
  }
  return map[navigationStore.currentModule] || 'Operaciones'
})

const currentTitle = computed(() => {
  if (navigationStore.currentModule === 'admin') {
    if (navigationStore.adminSubOption === 'about') return 'Acerca del Sistema SGA'
    return navigationStore.adminSubOption === 'create-user'
      ? 'Ficha de Registro de Operador'
      : 'Gestor y Directorio de Usuarios TI'
  }
  if (navigationStore.currentModule === 'gerencia') {
    const gMap = {
      overview: 'Panel Ejecutivo y Métricas Generales',
      sedes: 'Gestión Integral de Sedes y Agencias Territoriales',
      catalogos: 'Catálogos Auxiliares (Puertos y Tipos de Carga)'
    }
    return gMap[navigationStore.gerenciaSubOption] || 'Gerencia General'
  }
  if (navigationStore.currentModule === 'ventas') {
    const vMap = {
      cotizaciones: 'Cotizaciones Comerciales',
      clientes: 'Directorio de Clientes & Prospectos',
      ordenes: 'Órdenes de Servicio'
    }
    return vMap[navigationStore.ventasSubOption] || 'Módulo Comercial de Ventas'
  }
  if (navigationStore.currentModule === 'supervision') {
    const sMap = {
      cotizaciones: 'Bandeja de Cotizaciones para Revisión Técnica',
      expedientes: 'Gestión Integral de Expedientes Aduaneros'
    }
    return sMap[navigationStore.supervisionSubOption] || 'Supervisión Aduanera'
  }
  if (navigationStore.currentModule === 'aduanas') {
    return 'Control de Almacén Aduanero y Despachos'
  }
  return route.meta?.title?.replace(' - SGA', '') || 'Operaciones'
})

const avatarInitials = computed(() => {
  if (!authStore.user) return 'CM'
  const n = authStore.user.nombres?.charAt(0) || ''
  const a = authStore.user.apellidos?.charAt(0) || ''
  return (n + a).toUpperCase() || authStore.user.username?.substring(0, 2).toUpperCase() || 'AD'
})
</script>

<style scoped>
.app-layout {
  display: flex;
  min-height: 100vh;
  background: var(--color-bg-base);
  color: var(--color-text-main);
}

.main-container {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-width: 0;
  overflow-x: hidden;
}

.app-topbar {
  min-height: 72px;
  background: var(--color-bg-base);
  border-bottom: 1px solid var(--color-border);
  padding: 0.75rem 2.25rem;
  display: flex;
  align-items: center;
  justify-content: space-between;
  position: sticky;
  top: 0;
  z-index: 50;
  gap: 1.5rem;
}

.topbar-left {
  display: flex;
  flex-direction: column;
  gap: 0.15rem;
}

.breadcrumbs {
  display: flex;
  align-items: center;
  gap: 0.35rem;
  font-size: 0.725rem;
  color: var(--color-text-subtle);
  font-weight: 500;
}

.breadcrumb-sep {
  color: #baa895;
  font-size: 0.7rem;
}

.breadcrumb-current {
  color: var(--color-text-muted);
}

.topbar-page-title {
  font-size: 1.35rem;
  font-weight: 700;
  color: var(--color-text-main);
  letter-spacing: -0.01em;
  margin: 0;
  line-height: 1.2;
}

.topbar-right {
  display: flex;
  align-items: center;
  gap: 1.25rem;
}

/* Sede Asignada Pill en el Topbar */
.sede-topbar-badge {
  display: inline-flex;
  align-items: center;
  gap: 0.45rem;
  background: var(--color-bg-subtle);
  border: 1px solid var(--color-border);
  padding: 0.35rem 0.75rem;
  border-radius: var(--radius-pill);
  font-size: 0.725rem;
  cursor: default;
}

.sede-badge-label {
  font-size: 0.6rem;
  font-weight: 800;
  letter-spacing: 0.08em;
  color: var(--color-accent-kicker);
  background: var(--color-bg-muted);
  padding: 0.1rem 0.35rem;
  border-radius: 4px;
}

.sede-badge-text {
  font-weight: 600;
  color: var(--color-text-main);
  font-size: 0.725rem;
  white-space: nowrap;
}

.system-status-indicator {
  display: inline-flex;
  align-items: center;
  gap: 0.45rem;
  background: var(--color-bg-subtle);
  border: 1px solid var(--color-border);
  padding: 0.35rem 0.75rem;
  border-radius: var(--radius-pill);
  font-size: 0.725rem;
}

.status-pulse {
  width: 7px;
  height: 7px;
  border-radius: 50%;
  background: #228b49;
  box-shadow: 0 0 5px rgba(34, 139, 73, 0.4);
}

.status-text {
  font-weight: 600;
  color: var(--color-text-main);
  font-size: 0.725rem;
}

/* Botón Rápido de Tema en Topbar */
.topbar-theme-btn {
  width: 36px;
  height: 36px;
  border-radius: var(--radius-md);
  background: var(--color-bg-surface);
  border: 1px solid var(--color-border);
  color: var(--color-text-muted);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 0.95rem;
  cursor: pointer;
  transition: all var(--transition-fast);
  box-shadow: var(--shadow-subtle);
}

.topbar-theme-btn:hover {
  background: var(--color-bg-subtle);
  color: var(--color-text-main);
  border-color: var(--color-border-strong);
}

/* User Profile Header & Wrapper */
.user-profile-wrapper {
  position: relative;
}

.user-profile-header {
  display: flex;
  align-items: center;
  gap: 0.65rem;
  cursor: pointer;
  padding: 0.35rem 0.6rem;
  border-radius: var(--radius-md);
  transition: all var(--transition-fast);
  user-select: none;
}

.user-profile-header:hover,
.user-profile-header.active {
  background: var(--color-bg-subtle);
}

.user-avatar-circle {
  width: 38px;
  height: 38px;
  border-radius: 50%;
  background: var(--color-primary);
  color: #efe7dd;
  font-weight: 700;
  font-size: 0.85rem;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  box-shadow: 0 2px 6px rgba(53, 47, 42, 0.2);
}

.user-meta {
  display: flex;
  flex-direction: column;
  line-height: 1.2;
}

.user-full-name {
  font-weight: 700;
  font-size: 0.85rem;
  color: var(--color-text-main);
}

.user-role-tag {
  font-size: 0.65rem;
  font-weight: 700;
  letter-spacing: 0.06em;
  color: var(--color-text-subtle);
  text-transform: uppercase;
}

.chevron-icon {
  font-size: 0.75rem;
  color: var(--color-text-subtle);
  transition: transform var(--transition-fast);
}

.chevron-icon.rotated {
  transform: rotate(180deg);
}

/* Menú Desplegable Flotante del Perfil */
.user-dropdown-panel {
  position: absolute;
  top: calc(100% + 0.5rem);
  right: 0;
  width: 275px;
  background: var(--color-bg-surface);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-md);
  box-shadow: var(--shadow-elevated);
  padding: 0.85rem;
  z-index: 100;
}

.dropdown-header {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  padding-bottom: 0.25rem;
}

.dropdown-avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  background: var(--color-primary);
  color: #efe7dd;
  font-weight: 700;
  font-size: 0.825rem;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.dropdown-user-info {
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.dropdown-user-name {
  font-weight: 700;
  font-size: 0.825rem;
  color: var(--color-text-main);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.dropdown-user-role {
  font-size: 0.65rem;
  font-weight: 700;
  color: var(--color-text-subtle);
  text-transform: uppercase;
  letter-spacing: 0.05em;
}

.dropdown-divider {
  height: 1px;
  background: var(--color-border);
  margin: 0.6rem 0;
}

.dropdown-details {
  display: flex;
  flex-direction: column;
  gap: 0.35rem;
  font-size: 0.725rem;
}

.dropdown-detail-row {
  display: flex;
  align-items: baseline;
  gap: 0.4rem;
}

.detail-label {
  font-size: 0.6rem;
  font-weight: 800;
  color: var(--color-accent-kicker);
  letter-spacing: 0.05em;
  flex-shrink: 0;
}

.detail-value {
  color: var(--color-text-muted);
  font-weight: 600;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

/* Fila de Alternancia de Tema en el Dropdown */
.dropdown-theme-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0.5rem 0.55rem;
  border-radius: var(--radius-sm);
  cursor: pointer;
  user-select: none;
  transition: background var(--transition-fast);
}

.dropdown-theme-row:hover {
  background: var(--color-bg-subtle);
}

.theme-info-left {
  display: flex;
  align-items: center;
  gap: 0.65rem;
}

.theme-icon-box {
  width: 28px;
  height: 28px;
  border-radius: 6px;
  background: var(--color-bg-subtle);
  color: var(--color-accent-kicker);
  border: 1px solid var(--color-border-subtle);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 0.8rem;
  transition: all var(--transition-fast);
}

.theme-icon-box.is-dark {
  background: var(--color-bg-muted);
  color: var(--color-accent-kicker);
  border-color: var(--color-border);
}

.theme-text-col {
  display: flex;
  flex-direction: column;
  line-height: 1.2;
}

.theme-title {
  font-size: 0.775rem;
  font-weight: 700;
  color: var(--color-text-main);
}

.theme-subtitle {
  font-size: 0.65rem;
  font-weight: 600;
  color: var(--color-text-subtle);
}

/* Control Switch Cápsula Minimalista */
.theme-switch-control {
  width: 44px;
  height: 24px;
  background: #dacbb9;
  border-radius: var(--radius-pill);
  padding: 2px;
  position: relative;
  transition: background var(--transition-normal);
  flex-shrink: 0;
  box-shadow: inset 0 1px 2px rgba(0, 0, 0, 0.12);
}

.theme-switch-control.is-dark {
  background: var(--color-border-strong);
}

.switch-handle {
  width: 20px;
  height: 20px;
  background: #ffffff;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 0.65rem;
  color: #7b6d5e;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.2);
  transform: translateX(0);
  transition: transform var(--transition-normal), background var(--transition-normal), color var(--transition-normal);
}

.theme-switch-control.is-dark .switch-handle {
  transform: translateX(20px);
  background: var(--color-primary);
  color: var(--color-primary-text);
}

.dropdown-logout-action {
  width: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 0.5rem;
  padding: 0.5rem;
  background: var(--color-danger-bg);
  border: 1px solid var(--color-danger-border);
  color: var(--color-danger-text);
  border-radius: var(--radius-sm);
  font-size: 0.775rem;
  font-weight: 700;
  cursor: pointer;
  transition: all var(--transition-fast);
}

.dropdown-logout-action:hover {
  filter: brightness(0.95);
}

.dropdown-fade-enter-active,
.dropdown-fade-leave-active {
  transition: all 0.18s ease;
}

.dropdown-fade-enter-from,
.dropdown-fade-leave-to {
  opacity: 0;
  transform: translateY(-8px);
}

/* Botón Directo de Cerrar Sesión en Topbar */
.topbar-logout-btn {
  display: inline-flex;
  align-items: center;
  gap: 0.45rem;
  background: var(--color-bg-surface);
  border: 1px solid var(--color-danger-border);
  color: var(--color-danger-text);
  padding: 0.38rem 0.85rem;
  border-radius: var(--radius-pill);
  font-size: 0.75rem;
  font-weight: 700;
  cursor: pointer;
  transition: all var(--transition-fast);
  white-space: nowrap;
}

.topbar-logout-btn:hover {
  background: var(--color-danger-bg);
  box-shadow: 0 1px 4px rgba(185, 28, 28, 0.12);
}

.topbar-logout-btn i {
  font-size: 0.8rem;
}

.page-content {
  flex: 1;
  padding: 2rem 2.25rem 3rem;
  background: var(--color-bg-base);
}

@media (max-width: 992px) {
  .sede-topbar-badge .sede-badge-text {
    max-width: 140px;
    overflow: hidden;
    text-overflow: ellipsis;
  }
}

@media (max-width: 768px) {
  .app-topbar {
    padding: 0.75rem 1.25rem;
  }
  .page-content {
    padding: 1.25rem;
  }
  .system-status-indicator,
  .sede-topbar-badge {
    display: none;
  }
  .topbar-logout-btn .logout-text {
    display: none;
  }
  .topbar-logout-btn {
    padding: 0.4rem 0.55rem;
  }
}
</style>
