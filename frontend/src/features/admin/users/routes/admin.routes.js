export const adminRoutes = [
  {
    path: '/admin',
    redirect: '/admin/usuarios'
  },
  {
    path: '/admin/usuarios',
    name: 'AdminUsers',
    component: () => import('../views/UserManagementView.vue'),
    meta: {
      requiresAuth: true,
      roles: ['ADMIN_TI', 'ADMINISTRADOR', 'GERENTE'],
      title: 'Gestor de Usuarios TI - SGA'
    }
  },
  {
    path: '/admin/acerca-de',
    name: 'AdminAbout',
    component: () => import('@/features/admin/about/views/AboutSystemView.vue'),
    meta: {
      requiresAuth: true,
      roles: ['ADMIN_TI', 'ADMINISTRADOR', 'GERENTE'],
      title: 'Acerca del Sistema - SGA'
    }
  }
]

export default adminRoutes
