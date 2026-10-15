import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '@/features/auth/stores/authStore'
import { useNavigationStore } from '@/core/stores/navigationStore'
import AppLayout from '@/core/layouts/AppLayout.vue'

import authRoutes from '@/features/auth/routes/auth.routes'
import adminRoutes from '@/features/admin/users/routes/admin.routes'
import gerenciaRoutes from '@/features/gerencia/routes/gerencia.routes'
import rolesRoutes from '@/features/roles-modules/routes/roles.routes'

const routes = [
  ...authRoutes,
  {
    path: '/',
    component: AppLayout,
    meta: { requiresAuth: true },
    children: [
      {
        path: '',
        name: 'Root',
        redirect: () => {
          const authStore = useAuthStore()
          if (!authStore.isAuthenticated) {
            return '/login'
          }
          return authStore.homePath
        }
      },
      ...adminRoutes,
      ...gerenciaRoutes,
      ...rolesRoutes
    ]
  },
  {
    path: '/:pathMatch(.*)*',
    redirect: '/'
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// Guardia de Navegación Global
router.beforeEach(async (to, from, next) => {
  const authStore = useAuthStore()

  // Actualizar título de documento
  if (to.meta?.title) {
    document.title = to.meta.title
  } else {
    document.title = 'SGA - Sistema de Gestión Aduanera'
  }

  const isAuth = authStore.isAuthenticated

  // Si ingresa por defecto a la raíz '/'
  if (to.path === '/') {
    if (!isAuth) {
      return next('/login')
    }
    return next(authStore.homePath)
  }

  // Si ya está autenticado e intenta acceder al login
  if (to.path === '/login' && isAuth) {
    return next(authStore.homePath)
  }

  // Si cualquier ruta requiere autenticación y no hay sesión activa
  const requiresAuth = to.matched.some((record) => record.meta?.requiresAuth)
  if (requiresAuth && !isAuth) {
    return next({ path: '/login', query: { redirect: to.fullPath } })
  }

  // Validación de permisos por rol
  if (to.meta?.roles && to.meta.roles.length > 0) {
    const userRole = authStore.userRole
    if (!to.meta.roles.includes(userRole)) {
      return next(authStore.homePath)
    }
  }

  // Sincronizar activeModule con la ruta actual
  const navStore = useNavigationStore()
  if (to.path.startsWith('/admin')) {
    navStore.setActiveModule('admin')
    if (to.path.includes('acerca-de')) {
      navStore.setAdminSubOption('about')
    } else {
      navStore.setAdminSubOption('users')
    }
  } else if (to.path.startsWith('/gerencia')) {
    navStore.setActiveModule('gerencia')
    if (to.path.includes('sedes')) {
      navStore.setGerenciaSubOption('sedes')
    } else if (to.path.includes('catalogos')) {
      navStore.setGerenciaSubOption('catalogos')
    } else if (to.path.includes('resumen')) {
      navStore.setGerenciaSubOption('overview')
    }
  } else if (to.path.startsWith('/ventas')) {
    navStore.setActiveModule('ventas')
  } else if (to.path.startsWith('/supervision')) {
    navStore.setActiveModule('supervision')
  } else if (to.path.startsWith('/aduanas') || to.path.startsWith('/logistica')) {
    navStore.setActiveModule('aduanas')
  } else if (to.path.startsWith('/liquidador')) {
    navStore.setActiveModule('liquidador')
  } else if (to.path.startsWith('/legal')) {
    navStore.setActiveModule('legal')
  } else if (to.path.startsWith('/finanzas')) {
    navStore.setActiveModule('finanzas')
  } else if (to.path.startsWith('/transporte')) {
    navStore.setActiveModule('transporte')
  }

  next()
})

export default router
