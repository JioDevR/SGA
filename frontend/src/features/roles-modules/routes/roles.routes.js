export const rolesRoutes = [
  {
    path: '/ventas',
    name: 'VentasModule',
    component: () => import('../views/VentasView.vue'),
    meta: {
      requiresAuth: true,
      roles: ['ADMIN_TI', 'ADMINISTRADOR', 'GERENTE', 'VENTAS'],
      title: 'Ventas & Comercial - SGA'
    }
  },
  {
    path: '/supervision',
    name: 'SupervisionModule',
    component: () => import('../views/SupervisionView.vue'),
    meta: {
      requiresAuth: true,
      roles: ['ADMIN_TI', 'ADMINISTRADOR', 'GERENTE', 'SUPERVISION', 'SUPERVISOR'],
      title: 'Supervisión Aduanera - SGA'
    }
  },
  {
    path: '/aduanas',
    name: 'AduanasModule',
    component: () => import('../views/LogisticaView.vue'),
    meta: {
      requiresAuth: true,
      roles: ['ADMIN_TI', 'ADMINISTRADOR', 'GERENTE', 'AGENTE_ADUANERO', 'LOGISTICA', 'LIQUIDADOR'],
      title: 'Agente Aduanero & Operaciones - SGA'
    }
  },
  {
    path: '/logistica',
    redirect: '/aduanas'
  },
  {
    path: '/transporte',
    name: 'TransporteModule',
    component: () => import('../views/TransporteView.vue'),
    meta: {
      requiresAuth: true,
      roles: ['ADMIN_TI', 'ADMINISTRADOR', 'GERENTE', 'TRANSPORTE'],
      title: 'Transporte & Flota - SGA'
    }
  },
  {
    path: '/finanzas',
    name: 'FinanzasModule',
    component: () => import('../views/FinanzasView.vue'),
    meta: {
      requiresAuth: true,
      roles: ['ADMIN_TI', 'ADMINISTRADOR', 'GERENTE', 'FINANZAS'],
      title: 'Finanzas & Cobranzas - SGA'
    }
  },
  {
    path: '/liquidador',
    name: 'LiquidadorModule',
    component: () => import('../views/LiquidadorView.vue'),
    meta: {
      requiresAuth: true,
      roles: ['ADMIN_TI', 'ADMINISTRADOR', 'GERENTE', 'LIQUIDADOR'],
      title: 'Liquidación Aduanera - SGA'
    }
  },
  {
    path: '/legal',
    name: 'LegalModule',
    component: () => import('../views/LegalView.vue'),
    meta: {
      requiresAuth: true,
      roles: ['ADMIN_TI', 'ADMINISTRADOR', 'GERENTE', 'LEGAL'],
      title: 'Legal & Auditoría - SGA'
    }
  }
]

export default rolesRoutes
