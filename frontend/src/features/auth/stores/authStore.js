import { defineStore } from 'pinia'
import { authService } from '../services/authService'
import { ROLES, ROLE_HOME_PATH } from '@/core/auth/roles'

export const useAuthStore = defineStore('auth', {
  state: () => ({
    token: localStorage.getItem('sga_token') || null,
    user: JSON.parse(localStorage.getItem('sga_user') || 'null'),
    loading: false,
    error: null
  }),

  getters: {
    isAuthenticated: (state) => !!state.token,
    userRole: (state) => state.user?.rol || '',
    isAdministradorTI: (state) => state.user?.rol === ROLES.ADMINISTRADOR_TI,
    isGerenteGeneral: (state) => state.user?.rol === ROLES.GERENTE_GENERAL,
    // Compatibilidad
    isAdminTI: (state) => state.user?.rol === ROLES.ADMINISTRADOR_TI,
    isGerente: (state) => state.user?.rol === ROLES.GERENTE_GENERAL,
    isAdmin: (state) => [ROLES.ADMINISTRADOR_TI, ROLES.GERENTE_GENERAL].includes(state.user?.rol),
    canSwitchModules: (state) => [ROLES.ADMINISTRADOR_TI, ROLES.GERENTE_GENERAL].includes(state.user?.rol),
    userFullName: (state) => {
      if (!state.user) return ''
      return `${state.user.nombres || ''} ${state.user.apellidos || ''}`.trim()
    },
    homePath: (state) => {
      const rol = state.user?.rol
      return ROLE_HOME_PATH[rol] || '/administracion-ti/usuarios'
    }
  },

  actions: {
    /**
     * Iniciar sesión con usuario y contraseña
     */
    async login(credentials) {
      this.loading = true
      this.error = null
      try {
        const authData = await authService.login(credentials)
        this.token = authData.token
        this.user = {
          idUsuario: authData.idUsuario,
          username: authData.username,
          nombres: authData.nombres,
          apellidos: authData.apellidos,
          correoInstitucional: authData.correoInstitucional,
          rol: authData.rol,
          idSede: authData.idSede,
          codigoSede: authData.codigoSede,
          nombreSede: authData.nombreSede
        }

        localStorage.setItem('sga_token', this.token)
        localStorage.setItem('sga_user', JSON.stringify(this.user))

        return this.user
      } catch (err) {
        const message =
          err.response?.data?.message ||
          err.response?.data?.error ||
          'Error al iniciar sesión. Verifique sus credenciales.'
        this.error = message
        throw new Error(message)
      } finally {
        this.loading = false
      }
    },

    /**
     * Sincronizar datos del usuario actual desde el backend
     */
    async syncCurrentUser() {
      if (!this.token) return null
      try {
        const userData = await authService.getMe()
        this.user = {
          ...this.user,
          ...userData,
          rol: userData.nombreRol || this.user?.rol
        }
        localStorage.setItem('sga_user', JSON.stringify(this.user))
        return this.user
      } catch (err) {
        console.warn('No se pudo sincronizar el usuario activo:', err)
        return null
      }
    },

    /**
     * Cerrar sesión y limpiar estado
     */
    logout() {
      this.token = null
      this.user = null
      this.error = null
      localStorage.removeItem('sga_token')
      localStorage.removeItem('sga_user')
    }
  }
})
