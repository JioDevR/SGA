export const gerenciaRoutes = [
  {
    path: '/gerencia',
    redirect: '/gerencia/sedes'
  },
  {
    path: '/gerencia/sedes',
    name: 'GerenciaSedes',
    component: () => import('@/features/admin/sedes/views/SedesManagementView.vue'),
    meta: {
      requiresAuth: true,
      roles: ['GERENTE', 'ADMIN_TI', 'ADMINISTRADOR'],
      title: 'Sedes Territoriales - Gerencia SGA'
    }
  },
  {
    path: '/gerencia/catalogos',
    name: 'GerenciaCatalogos',
    component: () => import('@/features/admin/catalogos/views/CatalogosManagementView.vue'),
    meta: {
      requiresAuth: true,
      roles: ['GERENTE', 'ADMIN_TI', 'ADMINISTRADOR'],
      title: 'Catálogos Auxiliares - Gerencia SGA'
    }
  },
  {
    path: '/gerencia/resumen',
    name: 'GerenciaResumen',
    component: () => import('@/features/admin/overview/views/AdminOverviewView.vue'),
    meta: {
      requiresAuth: true,
      roles: ['GERENTE', 'ADMIN_TI', 'ADMINISTRADOR'],
      title: 'Panel Ejecutivo - Gerencia SGA'
    }
  }
]

export default gerenciaRoutes
