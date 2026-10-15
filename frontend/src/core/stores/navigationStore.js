import { defineStore } from 'pinia'
import { useAuthStore } from '@/features/auth/stores/authStore'

export const useNavigationStore = defineStore('navigation', {
  state: () => ({
    // Módulo activo: 'admin' | 'gerencia' | 'ventas' | 'supervision' | 'aduanas' | 'transporte'
    activeModule: 'admin',
    // Sub-opciones en Administración TI: 'users' | 'about' | 'create-user'
    adminSubOption: 'users',
    // Sub-opciones en Gerencia: 'overview' | 'sedes' | 'catalogos'
    gerenciaSubOption: 'sedes',
    // Sub-opciones en Ventas: 'cotizaciones' | 'clientes' | 'ordenes'
    ventasSubOption: 'cotizaciones',
    // Sub-opciones en Supervisión Aduanera: 'cotizaciones' | 'ordenes'
    supervisionSubOption: 'cotizaciones',
    // Sub-opciones en Agente Aduanero: 'almacen' | 'expedientes' | 'despacho'
    aduanasSubOption: 'almacen',
    // Disparador de acciones modales directas
    ventasActionTrigger: null,
    adminActionTrigger: null,
    // Contador reactivo para forzar actualización de vistas en clics repetidos
    navTick: 0
  }),

  getters: {
    /**
     * Retorna el módulo efectivo. Si el usuario puede conmutar (Admin TI o Gerente),
     * respeta activeModule. De lo contrario, está anclado a su rol.
     */
    currentModule: (state) => {
      const authStore = useAuthStore()
      if (authStore.canSwitchModules) {
        return state.activeModule
      }
      const roleModuleMap = {
        ADMIN_TI: 'admin',
        ADMINISTRADOR: 'admin',
        GERENTE: 'gerencia',
        VENTAS: 'ventas',
        SUPERVISION: 'supervision',
        SUPERVISOR: 'supervision',
        AGENTE_ADUANERO: 'aduanas',
        LOGISTICA: 'aduanas',
        LIQUIDADOR: 'aduanas',
        LEGAL: 'legal',
        FINANZAS: 'finanzas',
        TRANSPORTE: 'transporte'
      }
      return roleModuleMap[authStore.userRole] || 'ventas'
    },

    moduleTitle: (state) => {
      const titles = {
        admin: 'Administración TI & Usuarios',
        gerencia: 'Gerencia General & Sedes',
        ventas: 'Módulo de Ventas & Comercial',
        supervision: 'Módulo de Supervisión Aduanera',
        aduanas: 'Agente Aduanero & Trámites',
        logistica: 'Módulo de Logística & Almacén',
        liquidador: 'Módulo de Liquidación Aduanera',
        legal: 'Módulo Legal & Auditoría',
        finanzas: 'Módulo de Finanzas',
        transporte: 'Módulo de Transporte'
      }
      return titles[state.currentModule] || 'Operaciones'
    }
  },

  actions: {
    setActiveModule(moduleName) {
      this.activeModule = moduleName
      this.navTick++
    },

    setAdminSubOption(option) {
      this.adminSubOption = option
      this.navTick++
    },

    setGerenciaSubOption(option) {
      this.gerenciaSubOption = option
      this.navTick++
    },

    setVentasSubOption(option) {
      this.ventasSubOption = option
      this.navTick++
    },

    setSupervisionSubOption(option) {
      this.supervisionSubOption = option
      this.navTick++
    },

    setAduanasSubOption(option) {
      this.aduanasSubOption = option
      this.navTick++
    },

    triggerVentasAction(action) {
      this.ventasActionTrigger = action
      this.navTick++
    },

    triggerAdminAction(action) {
      this.adminActionTrigger = action
      this.navTick++
    }
  }
})
