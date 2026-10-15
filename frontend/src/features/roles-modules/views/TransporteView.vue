<template>
  <div class="transporte-view">
    <!-- 1. Hero Banner Card Editorial Cálido -->
    <div class="hero-banner-card">
      <div class="hero-content">
        <span class="badge-kicker">TRANSPORTE & DISTRIBUCIÓN // FLOTA Y RUTAS LOCALES</span>
        <h2 class="hero-title">Programación de Despachos, Flota Vehicular y Conductores MTC</h2>
        <p class="hero-desc">
          Planificación de traslados aduaneros con Guías de Remisión Electrónica (GRE-T), asignación de tractos y carretas autorizadas, monitoreo de ruta y confirmación de entrega en almacén de cliente.
        </p>
      </div>
      <div class="hero-watermark">
        <span>SGA</span>
      </div>
    </div>

    <!-- 2. Pestañas de Navegación del Módulo -->
    <div class="module-tabs-nav">
      <button
        type="button"
        class="tab-nav-btn"
        :class="{ active: activeTab === 'despachos' }"
        @click="activeTab = 'despachos'"
      >
        <i class="pi pi-compass"></i>
        <span>Despachos & Monitoreo</span>
        <span class="tab-counter">{{ despachosList.length }}</span>
      </button>

      <button
        type="button"
        class="tab-nav-btn"
        :class="{ active: activeTab === 'unidades' }"
        @click="activeTab = 'unidades'"
      >
        <i class="pi pi-truck"></i>
        <span>Flota de Unidades</span>
        <span class="tab-counter">{{ unidadesList.length }}</span>
      </button>

      <button
        type="button"
        class="tab-nav-btn"
        :class="{ active: activeTab === 'conductores' }"
        @click="activeTab = 'conductores'"
      >
        <i class="pi pi-users"></i>
        <span>Conductores MTC</span>
        <span class="tab-counter">{{ conductoresList.length }}</span>
      </button>
    </div>

    <!-- ===================================================================== -->
    <!-- TAB 1: DESPACHOS Y MONITOREO DE RUTA                                  -->
    <!-- ===================================================================== -->
    <div v-if="activeTab === 'despachos'" class="tab-container">
      <!-- Métricas Rápidas Despachos -->
      <div class="metrics-grid">
        <div class="metric-card highlight-card">
          <div class="metric-top">
            <span class="metric-idx">01</span>
            <span class="metric-label">En Tránsito / Ruta</span>
          </div>
          <div class="metric-val text-pending">{{ despachosEnTransitoCount }}</div>
        </div>

        <div class="metric-card">
          <div class="metric-top">
            <span class="metric-idx">02</span>
            <span class="metric-label">Programados</span>
          </div>
          <div class="metric-val text-warning">{{ despachosProgramadosCount }}</div>
        </div>

        <div class="metric-card">
          <div class="metric-top">
            <span class="metric-idx">03</span>
            <span class="metric-label">Entregados Conforme</span>
          </div>
          <div class="metric-val text-success">{{ despachosEntregadosCount }}</div>
        </div>

        <div class="metric-card">
          <div class="metric-top">
            <span class="metric-idx">04</span>
            <span class="metric-label">Total Operaciones</span>
          </div>
          <div class="metric-val text-accent">{{ despachosList.length }}</div>
        </div>
      </div>

      <!-- Barra de Filtros y Acciones -->
      <div class="tab-content-section">
        <div class="section-header-row">
          <div>
            <span class="badge-kicker">ORDEN DE TRASLADO LOCAL</span>
            <h3 class="section-title">Despachos Terrestres y Tracking Operativo</h3>
          </div>

          <div class="section-controls">
            <!-- Filtro Estado -->
            <select v-model="filterDespachoEstado" class="filter-select">
              <option value="">Todos los Estados</option>
              <option value="PROGRAMADO">Programados</option>
              <option value="EN_TRANSITO">En Tránsito</option>
              <option value="ENTREGADO">Entregados</option>
              <option value="CANCELADO">Cancelados</option>
            </select>

            <!-- Búsqueda -->
            <div class="search-input-wrap">
              <i class="pi pi-search search-icon"></i>
              <input
                v-model="searchDespachoQuery"
                type="text"
                class="search-input"
                placeholder="Buscar por tracking, exp o guía..."
              />
            </div>

            <button type="button" class="btn-outline-warm" @click="loadDespachos" :disabled="loadingDespachos">
              <i :class="['pi pi-sync', { 'pi-spin': loadingDespachos }]"></i>
              <span>Actualizar</span>
            </button>

            <button type="button" class="btn-primary-action" @click="showCrearDespachoModal = true">
              <i class="pi pi-plus"></i>
              <span>+ Programar Despacho</span>
            </button>
          </div>
        </div>

        <!-- Tabla Despachos -->
        <div class="table-container">
          <DataTable
            :value="filteredDespachos"
            :loading="loadingDespachos"
            responsiveLayout="scroll"
            class="custom-datatable"
            dataKey="idDespacho"
          >
            <template #empty>
              <div class="empty-state">
                <i class="pi pi-truck empty-icon"></i>
                <p class="empty-title">No hay despachos registrados</p>
                <p class="empty-desc">Programe una orden de traslado terrestre para iniciar el flujo de reparto.</p>
              </div>
            </template>

            <Column header="N° Tracking">
              <template #body="{ data: d }">
                <div class="ticket-cell">
                  <i class="pi pi-compass"></i>
                  <span class="font-mono font-bold">{{ d.numeroTracking || ('DSP-' + d.idDespacho) }}</span>
                </div>
              </template>
            </Column>

            <Column header="Expediente / Cliente">
              <template #body="{ data: d }">
                <div class="client-cell">
                  <span class="font-bold text-xs font-mono">{{ d.codigoExpediente || 'EXP-SGA' }}</span>
                  <span class="client-sub">{{ d.clienteRazonSocial || 'Cliente Importador' }}</span>
                </div>
              </template>
            </Column>

            <Column header="Guía GRE-T">
              <template #body="{ data: d }">
                <span class="font-mono text-xs font-semibold">{{ d.guiaRemisionTransporte || 'PENDIENTE' }}</span>
              </template>
            </Column>

            <Column header="Unidad & Conductor">
              <template #body="{ data: d }">
                <div v-if="d.placaTracto" class="client-cell">
                  <span class="font-bold text-xs">Tracto: {{ d.placaTracto }}</span>
                  <span class="client-sub">{{ d.nombresConductor ? d.nombresConductor + ' ' + (d.apellidosConductor || '') : 'Sin conductor' }}</span>
                </div>
                <div v-else>
                  <Tag severity="danger" value="Sin Recursos" rounded class="tag-xs" />
                </div>
              </template>
            </Column>

            <Column header="Destino / Entrega">
              <template #body="{ data: d }">
                <div class="client-cell">
                  <span class="client-name">{{ d.distritoLlegada || 'Lima' }}</span>
                  <span class="client-sub truncate-text" :title="d.direccionLlegada">{{ d.direccionLlegada }}</span>
                </div>
              </template>
            </Column>

            <Column header="Fecha Prog.">
              <template #body="{ data: d }">
                <span class="text-xs font-mono">{{ formatDateTime(d.fechaProgramada) }}</span>
              </template>
            </Column>

            <Column header="Estado">
              <template #body="{ data: d }">
                <Tag
                  :severity="getDespachoSeverity(d.estadoDespacho)"
                  :value="formatDespachoEstado(d.estadoDespacho)"
                  rounded
                  class="tag-xs"
                />
              </template>
            </Column>

            <Column header="Acciones" headerStyle="width: 140px; text-align: right;" bodyStyle="text-align: right;">
              <template #body="{ data: d }">
                <div class="action-buttons-cell">
                  <!-- Botón Asignar Recursos si no tiene unidad o conductor -->
                  <button
                    v-if="d.estadoDespacho === 'PROGRAMADO' && (!d.idConductor || !d.idUnidad)"
                    type="button"
                    class="btn-row-action btn-row-warning"
                    title="Asignar Conductor y Tracto"
                    @click="openAsignarRecursos(d)"
                  >
                    <i class="pi pi-user-plus"></i>
                  </button>

                  <!-- Botón Iniciar Ruta si está PROGRAMADO y tiene recursos -->
                  <button
                    v-if="d.estadoDespacho === 'PROGRAMADO'"
                    type="button"
                    class="btn-row-action btn-row-primary"
                    title="Iniciar Ruta (Salida a destino)"
                    @click="handleIniciarRuta(d)"
                  >
                    <i class="pi pi-play"></i>
                  </button>

                  <!-- Botón Confirmar Entrega si está EN_TRANSITO -->
                  <button
                    v-if="d.estadoDespacho === 'EN_TRANSITO'"
                    type="button"
                    class="btn-row-action btn-row-success"
                    title="Confirmar Entrega Conforme"
                    @click="openConfirmarEntrega(d)"
                  >
                    <i class="pi pi-check-circle"></i>
                  </button>

                  <!-- Badge Entregado -->
                  <span v-if="d.estadoDespacho === 'ENTREGADO'" class="text-xs text-success font-semibold">
                    <i class="pi pi-verified"></i> Entregado
                  </span>
                </div>
              </template>
            </Column>
          </DataTable>
        </div>
      </div>
    </div>

    <!-- ===================================================================== -->
    <!-- TAB 2: FLOTA DE UNIDADES (TRACTOS / CARRETAS)                         -->
    <!-- ===================================================================== -->
    <div v-if="activeTab === 'unidades'" class="tab-container">
      <!-- Métricas Unidades -->
      <div class="metrics-grid">
        <div class="metric-card highlight-card">
          <div class="metric-top">
            <span class="metric-idx">01</span>
            <span class="metric-label">Disponibles / Operativas</span>
          </div>
          <div class="metric-val text-success">{{ unidadesDisponiblesCount }}</div>
        </div>

        <div class="metric-card">
          <div class="metric-top">
            <span class="metric-idx">02</span>
            <span class="metric-label">En Mantenimiento</span>
          </div>
          <div class="metric-val text-warning">{{ unidadesMantenimientoCount }}</div>
        </div>

        <div class="metric-card">
          <div class="metric-top">
            <span class="metric-idx">03</span>
            <span class="metric-label">Capacidad Flota (Tn)</span>
          </div>
          <div class="metric-val text-accent">{{ capacidadTotalTn }} Tn</div>
        </div>

        <div class="metric-card">
          <div class="metric-top">
            <span class="metric-idx">04</span>
            <span class="metric-label">Total Unidades</span>
          </div>
          <div class="metric-val text-primary">{{ unidadesList.length }}</div>
        </div>
      </div>

      <!-- Tabla Unidades -->
      <div class="tab-content-section">
        <div class="section-header-row">
          <div>
            <span class="badge-kicker">PARQUE AUTOMOTOR AUTORIZADO</span>
            <h3 class="section-title">Flota de Tractos, Furgones y Plataformas</h3>
          </div>

          <div class="section-controls">
            <select v-model="filterUnidadOperativo" class="filter-select">
              <option value="">Cualquier Estado Operativo</option>
              <option value="DISPONIBLE">Disponibles</option>
              <option value="EN_MANTENIMIENTO">En Mantenimiento</option>
              <option value="FUERA_DE_SERVICIO">Fuera de Servicio</option>
            </select>

            <div class="search-input-wrap">
              <i class="pi pi-search search-icon"></i>
              <input
                v-model="searchUnidadQuery"
                type="text"
                class="search-input"
                placeholder="Buscar por placa, modelo o tipo..."
              />
            </div>

            <button type="button" class="btn-outline-warm" @click="loadUnidades" :disabled="loadingUnidades">
              <i :class="['pi pi-sync', { 'pi-spin': loadingUnidades }]"></i>
              <span>Actualizar</span>
            </button>

            <button type="button" class="btn-primary-action" @click="openCreateUnidad">
              <i class="pi pi-plus"></i>
              <span>+ Registrar Unidad</span>
            </button>
          </div>
        </div>

        <div class="table-container">
          <DataTable
            :value="filteredUnidades"
            :loading="loadingUnidades"
            responsiveLayout="scroll"
            class="custom-datatable"
            dataKey="idUnidad"
          >
            <template #empty>
              <div class="empty-state">
                <i class="pi pi-car empty-icon"></i>
                <p class="empty-title">No hay unidades vehiculares registradas</p>
                <p class="empty-desc">Registre los tractos y plataformas autorizadas para iniciar despachos.</p>
              </div>
            </template>

            <Column header="Placa Tracto">
              <template #body="{ data: u }">
                <div class="ticket-cell">
                  <i class="pi pi-car"></i>
                  <span class="font-mono font-bold">{{ u.placaTracto }}</span>
                </div>
              </template>
            </Column>

            <Column header="Placa Carreta">
              <template #body="{ data: u }">
                <span class="font-mono text-xs">{{ u.placaCarreta || '— S/C —' }}</span>
              </template>
            </Column>

            <Column header="Marca / Modelo">
              <template #body="{ data: u }">
                <div class="client-cell">
                  <span class="client-name">{{ u.marcaModelo }}</span>
                  <span class="client-sub">{{ formatTipoVehiculo(u.tipoVehiculo) }}</span>
                </div>
              </template>
            </Column>

            <Column header="Capacidad">
              <template #body="{ data: u }">
                <span class="font-mono text-xs font-bold">{{ Number(u.capacidadCargaKg || 0).toLocaleString('es-PE') }} kg</span>
              </template>
            </Column>

            <Column header="Condición">
              <template #body="{ data: u }">
                <Tag
                  :severity="getUnidadOperativoSeverity(u.estadoOperativo)"
                  :value="u.estadoOperativo || 'DISPONIBLE'"
                  rounded
                  class="tag-xs"
                />
              </template>
            </Column>

            <Column header="Estado">
              <template #body="{ data: u }">
                <Tag
                  :severity="u.estadoActivo ? 'success' : 'danger'"
                  :value="u.estadoActivo ? 'ACTIVO' : 'INACTIVO'"
                  rounded
                  class="tag-xs"
                />
              </template>
            </Column>

            <Column header="Acciones" headerStyle="width: 110px; text-align: right;" bodyStyle="text-align: right;">
              <template #body="{ data: u }">
                <div class="action-buttons-cell">
                  <button
                    type="button"
                    class="btn-row-action"
                    title="Editar Unidad"
                    @click="openEditUnidad(u)"
                  >
                    <i class="pi pi-pencil"></i>
                  </button>

                  <button
                    v-if="u.estadoActivo"
                    type="button"
                    class="btn-row-action btn-row-danger"
                    title="Desactivar Unidad"
                    @click="handleToggleUnidad(u)"
                  >
                    <i class="pi pi-trash"></i>
                  </button>
                  <button
                    v-else
                    type="button"
                    class="btn-row-action btn-row-success"
                    title="Reactivar Unidad"
                    @click="handleToggleUnidad(u)"
                  >
                    <i class="pi pi-refresh"></i>
                  </button>
                </div>
              </template>
            </Column>
          </DataTable>
        </div>
      </div>
    </div>

    <!-- ===================================================================== -->
    <!-- TAB 3: CONDUCTORES AUTORIZADOS MTC                                    -->
    <!-- ===================================================================== -->
    <div v-if="activeTab === 'conductores'" class="tab-container">
      <!-- Métricas Conductores -->
      <div class="metrics-grid">
        <div class="metric-card highlight-card">
          <div class="metric-top">
            <span class="metric-idx">01</span>
            <span class="metric-label">Conductores Activos</span>
          </div>
          <div class="metric-val text-success">{{ conductoresActivosCount }}</div>
        </div>

        <div class="metric-card">
          <div class="metric-top">
            <span class="metric-idx">02</span>
            <span class="metric-label">Licencia A-IIIc</span>
          </div>
          <div class="metric-val text-primary">{{ conductoresA3cCount }}</div>
        </div>

        <div class="metric-card">
          <div class="metric-top">
            <span class="metric-idx">03</span>
            <span class="metric-label">Inactivos / Bajas</span>
          </div>
          <div class="metric-val text-muted">{{ conductoresInactivosCount }}</div>
        </div>

        <div class="metric-card">
          <div class="metric-top">
            <span class="metric-idx">04</span>
            <span class="metric-label">Total Conductores</span>
          </div>
          <div class="metric-val text-accent">{{ conductoresList.length }}</div>
        </div>
      </div>

      <!-- Tabla Conductores -->
      <div class="tab-content-section">
        <div class="section-header-row">
          <div>
            <span class="badge-kicker">PERSONAL DE OPERACIONES TERRESTRES</span>
            <h3 class="section-title">Choferes Profesionales y Licencias MTC</h3>
          </div>

          <div class="section-controls">
            <div class="search-input-wrap">
              <i class="pi pi-search search-icon"></i>
              <input
                v-model="searchConductorQuery"
                type="text"
                class="search-input"
                placeholder="Buscar por nombre, brevete o teléfono..."
              />
            </div>

            <button type="button" class="btn-outline-warm" @click="loadConductores" :disabled="loadingConductores">
              <i :class="['pi pi-sync', { 'pi-spin': loadingConductores }]"></i>
              <span>Actualizar</span>
            </button>

            <button type="button" class="btn-primary-action" @click="openCreateConductor">
              <i class="pi pi-plus"></i>
              <span>+ Registrar Conductor</span>
            </button>
          </div>
        </div>

        <div class="table-container">
          <DataTable
            :value="filteredConductores"
            :loading="loadingConductores"
            responsiveLayout="scroll"
            class="custom-datatable"
            dataKey="idConductor"
          >
            <template #empty>
              <div class="empty-state">
                <i class="pi pi-users empty-icon"></i>
                <p class="empty-title">No hay conductores registrados</p>
                <p class="empty-desc">Añada los choferes habilitados por el MTC para operar en el sistema SGA.</p>
              </div>
            </template>

            <Column header="Nombres y Apellidos">
              <template #body="{ data: c }">
                <div class="client-cell">
                  <span class="client-name font-bold">{{ c.nombres }} {{ c.apellidos }}</span>
                  <span class="client-sub">ID Chofer: #{{ c.idConductor }}</span>
                </div>
              </template>
            </Column>

            <Column header="N° Licencia / Brevete">
              <template #body="{ data: c }">
                <span class="font-mono text-xs font-bold">{{ c.numeroLicencia }}</span>
              </template>
            </Column>

            <Column header="Categoría MTC">
              <template #body="{ data: c }">
                <Tag severity="info" :value="c.tipoLicencia || 'A-IIIc'" rounded class="tag-xs" />
              </template>
            </Column>

            <Column header="Teléfono Móvil">
              <template #body="{ data: c }">
                <div class="text-xs">
                  <i class="pi pi-phone text-muted mr-1"></i>
                  <span>{{ c.telefono || 'Sin teléfono' }}</span>
                </div>
              </template>
            </Column>

            <Column header="Estado">
              <template #body="{ data: c }">
                <Tag
                  :severity="c.estadoActivo ? 'success' : 'danger'"
                  :value="c.estadoActivo ? 'ACTIVO' : 'INACTIVO'"
                  rounded
                  class="tag-xs"
                />
              </template>
            </Column>

            <Column header="Acciones" headerStyle="width: 110px; text-align: right;" bodyStyle="text-align: right;">
              <template #body="{ data: c }">
                <div class="action-buttons-cell">
                  <button
                    type="button"
                    class="btn-row-action"
                    title="Editar Conductor"
                    @click="openEditConductor(c)"
                  >
                    <i class="pi pi-pencil"></i>
                  </button>

                  <button
                    v-if="c.estadoActivo"
                    type="button"
                    class="btn-row-action btn-row-danger"
                    title="Desactivar Conductor"
                    @click="handleToggleConductor(c)"
                  >
                    <i class="pi pi-trash"></i>
                  </button>
                  <button
                    v-else
                    type="button"
                    class="btn-row-action btn-row-success"
                    title="Reactivar Conductor"
                    @click="handleToggleConductor(c)"
                  >
                    <i class="pi pi-refresh"></i>
                  </button>
                </div>
              </template>
            </Column>
          </DataTable>
        </div>
      </div>
    </div>

    <!-- ===================================================================== -->
    <!-- MODALES DE OPERACIÓN                                                  -->
    <!-- ===================================================================== -->
    <!-- Modal 1: Programar Despacho -->
    <CrearDespachoModal
      :visible="showCrearDespachoModal"
      @close="showCrearDespachoModal = false"
      @saved="onDespachoSaved"
    />

    <!-- Modal 2: Asignar Recursos -->
    <AsignarRecursosModal
      :visible="showAsignarRecursosModal"
      :despacho="selectedDespacho"
      @close="showAsignarRecursosModal = false"
      @saved="onDespachoSaved"
    />

    <!-- Modal 3: Confirmar Entrega -->
    <ConfirmarEntregaModal
      :visible="showConfirmarEntregaModal"
      :despacho="selectedDespacho"
      @close="showConfirmarEntregaModal = false"
      @saved="onDespachoSaved"
    />

    <!-- Modal 4: Registrar / Editar Unidad -->
    <UnidadFormModal
      :visible="showUnidadModal"
      :unidad="selectedUnidad"
      @close="showUnidadModal = false"
      @saved="onUnidadSaved"
    />

    <!-- Modal 5: Registrar / Editar Conductor -->
    <ConductorFormModal
      :visible="showConductorModal"
      :conductor="selectedConductor"
      @close="showConductorModal = false"
      @saved="onConductorSaved"
    />
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import DataTable from 'primevue/datatable'
import Column from 'primevue/column'
import Tag from 'primevue/tag'

import { transporteService } from '../services/transporteService'
import CrearDespachoModal from '../components/CrearDespachoModal.vue'
import AsignarRecursosModal from '../components/AsignarRecursosModal.vue'
import ConfirmarEntregaModal from '../components/ConfirmarEntregaModal.vue'
import UnidadFormModal from '../components/UnidadFormModal.vue'
import ConductorFormModal from '../components/ConductorFormModal.vue'

// ==========================================
// ESTADO GENERAL
// ==========================================
const activeTab = ref('despachos')

// ==========================================
// 1. DESPACHOS
// ==========================================
const despachosList = ref([])
const loadingDespachos = ref(false)
const filterDespachoEstado = ref('')
const searchDespachoQuery = ref('')

const showCrearDespachoModal = ref(false)
const showAsignarRecursosModal = ref(false)
const showConfirmarEntregaModal = ref(false)
const selectedDespacho = ref(null)

const despachosEnTransitoCount = computed(
  () => despachosList.value.filter((d) => d.estadoDespacho === 'EN_TRANSITO').length
)
const despachosProgramadosCount = computed(
  () => despachosList.value.filter((d) => d.estadoDespacho === 'PROGRAMADO').length
)
const despachosEntregadosCount = computed(
  () => despachosList.value.filter((d) => d.estadoDespacho === 'ENTREGADO').length
)

const filteredDespachos = computed(() => {
  return despachosList.value.filter((d) => {
    const matchesEstado = filterDespachoEstado.value
      ? d.estadoDespacho === filterDespachoEstado.value
      : true

    const q = searchDespachoQuery.value.trim().toLowerCase()
    const matchesQuery = !q
      ? true
      : (d.numeroTracking && d.numeroTracking.toLowerCase().includes(q)) ||
        (d.codigoExpediente && d.codigoExpediente.toLowerCase().includes(q)) ||
        (d.guiaRemisionTransporte && d.guiaRemisionTransporte.toLowerCase().includes(q)) ||
        (d.direccionLlegada && d.direccionLlegada.toLowerCase().includes(q)) ||
        (d.placaTracto && d.placaTracto.toLowerCase().includes(q))

    return matchesEstado && matchesQuery
  })
})

const loadDespachos = async () => {
  loadingDespachos.value = true
  try {
    const data = await transporteService.getDespachos()
    despachosList.value = data || []
  } catch (err) {
    console.error('Error al cargar despachos de transporte:', err)
  } finally {
    loadingDespachos.value = false
  }
}

const openAsignarRecursos = (despacho) => {
  selectedDespacho.value = despacho
  showAsignarRecursosModal.value = true
}

const openConfirmarEntrega = (despacho) => {
  selectedDespacho.value = despacho
  showConfirmarEntregaModal.value = true
}

const handleIniciarRuta = async (despacho) => {
  if (!confirm(`¿Iniciar la ruta para el despacho con tracking ${despacho.numeroTracking || despacho.idDespacho}?`)) {
    return
  }
  try {
    await transporteService.iniciarRuta(despacho.idDespacho, {
      observaciones: 'Unidad parte a ruta con precinto verificado'
    })
    await loadDespachos()
  } catch (err) {
    alert(err.response?.data?.message || 'Error al iniciar ruta de transporte')
  }
}

const onDespachoSaved = () => {
  loadDespachos()
}

// ==========================================
// 2. UNIDADES DE TRANSPORTE
// ==========================================
const unidadesList = ref([])
const loadingUnidades = ref(false)
const filterUnidadOperativo = ref('')
const searchUnidadQuery = ref('')

const showUnidadModal = ref(false)
const selectedUnidad = ref(null)

const unidadesDisponiblesCount = computed(
  () => unidadesList.value.filter((u) => u.estadoOperativo === 'DISPONIBLE' && u.estadoActivo).length
)
const unidadesMantenimientoCount = computed(
  () => unidadesList.value.filter((u) => u.estadoOperativo === 'EN_MANTENIMIENTO').length
)
const capacidadTotalTn = computed(() => {
  const totalKg = unidadesList.value.reduce((acc, u) => acc + Number(u.capacidadCargaKg || 0), 0)
  return (totalKg / 1000).toFixed(1)
})

const filteredUnidades = computed(() => {
  return unidadesList.value.filter((u) => {
    const matchesOp = filterUnidadOperativo.value
      ? u.estadoOperativo === filterUnidadOperativo.value
      : true

    const q = searchUnidadQuery.value.trim().toLowerCase()
    const matchesQuery = !q
      ? true
      : (u.placaTracto && u.placaTracto.toLowerCase().includes(q)) ||
        (u.placaCarreta && u.placaCarreta.toLowerCase().includes(q)) ||
        (u.marcaModelo && u.marcaModelo.toLowerCase().includes(q)) ||
        (u.tipoVehiculo && u.tipoVehiculo.toLowerCase().includes(q))

    return matchesOp && matchesQuery
  })
})

const loadUnidades = async () => {
  loadingUnidades.value = true
  try {
    const data = await transporteService.getUnidades()
    unidadesList.value = data || []
  } catch (err) {
    console.error('Error al cargar unidades:', err)
  } finally {
    loadingUnidades.value = false
  }
}

const openCreateUnidad = () => {
  selectedUnidad.value = null
  showUnidadModal.value = true
}

const openEditUnidad = (unidad) => {
  selectedUnidad.value = unidad
  showUnidadModal.value = true
}

const handleToggleUnidad = async (unidad) => {
  try {
    if (unidad.estadoActivo) {
      if (!confirm(`¿Dar de baja temporalmente a la unidad ${unidad.placaTracto}?`)) return
      await transporteService.deleteUnidad(unidad.idUnidad)
    } else {
      await transporteService.reactivarUnidad(unidad.idUnidad)
    }
    await loadUnidades()
  } catch (err) {
    alert(err.response?.data?.message || 'Error al cambiar estado de la unidad')
  }
}

const onUnidadSaved = () => {
  loadUnidades()
}

// ==========================================
// 3. CONDUCTORES MTC
// ==========================================
const conductoresList = ref([])
const loadingConductores = ref(false)
const searchConductorQuery = ref('')

const showConductorModal = ref(false)
const selectedConductor = ref(null)

const conductoresActivosCount = computed(
  () => conductoresList.value.filter((c) => c.estadoActivo).length
)
const conductoresA3cCount = computed(
  () => conductoresList.value.filter((c) => c.tipoLicencia === 'A-IIIc').length
)
const conductoresInactivosCount = computed(
  () => conductoresList.value.filter((c) => !c.estadoActivo).length
)

const filteredConductores = computed(() => {
  return conductoresList.value.filter((c) => {
    const q = searchConductorQuery.value.trim().toLowerCase()
    if (!q) return true
    return (
      (c.nombres && c.nombres.toLowerCase().includes(q)) ||
      (c.apellidos && c.apellidos.toLowerCase().includes(q)) ||
      (c.numeroLicencia && c.numeroLicencia.toLowerCase().includes(q)) ||
      (c.telefono && c.telefono.toLowerCase().includes(q))
    )
  })
})

const loadConductores = async () => {
  loadingConductores.value = true
  try {
    const data = await transporteService.getConductores()
    conductoresList.value = data || []
  } catch (err) {
    console.error('Error al cargar conductores:', err)
  } finally {
    loadingConductores.value = false
  }
}

const openCreateConductor = () => {
  selectedConductor.value = null
  showConductorModal.value = true
}

const openEditConductor = (conductor) => {
  selectedConductor.value = conductor
  showConductorModal.value = true
}

const handleToggleConductor = async (conductor) => {
  try {
    if (conductor.estadoActivo) {
      if (!confirm(`¿Desactivar al conductor ${conductor.nombres} ${conductor.apellidos}?`)) return
      await transporteService.deleteConductor(conductor.idConductor)
    } else {
      await transporteService.reactivarConductor(conductor.idConductor)
    }
    await loadConductores()
  } catch (err) {
    alert(err.response?.data?.message || 'Error al cambiar estado del conductor')
  }
}

const onConductorSaved = () => {
  loadConductores()
}

// ==========================================
// HELPERS FORMATO
// ==========================================
const formatDateTime = (val) => {
  if (!val) return '—'
  const d = new Date(val)
  return d.toLocaleDateString('es-PE', {
    day: '2-digit',
    month: '2-digit',
    year: 'numeric',
    hour: '2-digit',
    minute: '2-digit'
  })
}

const formatDespachoEstado = (st) => {
  const map = {
    PROGRAMADO: 'Programado',
    EN_TRANSITO: 'En Tránsito',
    ENTREGADO: 'Entregado Conforme',
    CANCELADO: 'Cancelado'
  }
  return map[st] || st || 'Programado'
}

const getDespachoSeverity = (st) => {
  switch (st) {
    case 'PROGRAMADO':
      return 'warning'
    case 'EN_TRANSITO':
      return 'info'
    case 'ENTREGADO':
      return 'success'
    case 'CANCELADO':
      return 'danger'
    default:
      return 'secondary'
  }
}

const formatTipoVehiculo = (t) => {
  const map = {
    TRACTO_PLATAFORMA: 'Tracto con Plataforma',
    TRACTO_FURGON: 'Tracto Furgón Cerrado',
    CAMION_BARANDA: 'Camión Baranda',
    CAMION_FURGON: 'Camión Furgón',
    PORTACONTENEDOR: 'Portacontenedor / Chasis'
  }
  return map[t] || t || 'Tracto'
}

const getUnidadOperativoSeverity = (op) => {
  switch (op) {
    case 'DISPONIBLE':
      return 'success'
    case 'EN_MANTENIMIENTO':
      return 'warning'
    case 'FUERA_DE_SERVICIO':
      return 'danger'
    default:
      return 'secondary'
  }
}

onMounted(() => {
  loadDespachos()
  loadUnidades()
  loadConductores()
})
</script>

<style scoped>
.transporte-view {
  padding: 1.5rem;
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

/* 1. Hero Banner */
.hero-banner-card {
  position: relative;
  background: linear-gradient(135deg, #1c1917 0%, #292524 60%, #431407 100%);
  border-radius: var(--radius-lg, 12px);
  padding: 1.75rem 2rem;
  color: #fafaf9;
  overflow: hidden;
  box-shadow: 0 4px 18px rgba(0, 0, 0, 0.12);
}

.hero-content {
  position: relative;
  z-index: 2;
  max-width: 780px;
}

.badge-kicker {
  display: inline-block;
  font-size: 0.6875rem;
  font-weight: 700;
  letter-spacing: 0.1em;
  text-transform: uppercase;
  color: #ea580c;
  margin-bottom: 0.5rem;
}

.hero-title {
  font-size: 1.5rem;
  font-weight: 700;
  line-height: 1.25;
  margin: 0 0 0.5rem 0;
  color: #fff;
}

.hero-desc {
  font-size: 0.875rem;
  line-height: 1.5;
  color: #d6d3d1;
  margin: 0;
}

.hero-watermark {
  position: absolute;
  right: 1.5rem;
  top: 50%;
  transform: translateY(-50%);
  font-size: 7rem;
  font-weight: 900;
  color: rgba(255, 255, 255, 0.03);
  user-select: none;
  pointer-events: none;
}

/* 2. Pestañas de Navegación */
.module-tabs-nav {
  display: flex;
  gap: 0.75rem;
  border-bottom: 1px solid var(--color-border);
  padding-bottom: 0.5rem;
}

.tab-nav-btn {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.65rem 1.25rem;
  border-radius: var(--radius-md, 8px);
  border: 1px solid transparent;
  background: transparent;
  color: var(--color-text-muted);
  font-size: 0.875rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s ease;
}

.tab-nav-btn:hover {
  background: var(--color-bg-alt);
  color: var(--color-text-main);
}

.tab-nav-btn.active {
  background: var(--color-bg-surface);
  border-color: var(--color-border);
  color: #ea580c;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.04);
}

.tab-counter {
  background: var(--color-bg-alt);
  border: 1px solid var(--color-border);
  padding: 0.1rem 0.45rem;
  border-radius: 9999px;
  font-size: 0.7rem;
  font-weight: 700;
}

/* 3. Métricas Grid */
.metrics-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 1rem;
}

.metric-card {
  background: var(--color-bg-surface);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-md, 8px);
  padding: 1.15rem 1.25rem;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.02);
}

.highlight-card {
  border-left: 3px solid #ea580c;
}

.metric-top {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 0.5rem;
}

.metric-idx {
  font-size: 0.6875rem;
  font-family: monospace;
  color: var(--color-text-muted);
}

.metric-label {
  font-size: 0.75rem;
  font-weight: 600;
  color: var(--color-text-muted);
  text-transform: uppercase;
  letter-spacing: 0.05em;
}

.metric-val {
  font-size: 1.75rem;
  font-weight: 700;
  line-height: 1;
}

.text-pending { color: #f97316; }
.text-warning { color: #eab308; }
.text-success { color: #16a34a; }
.text-accent  { color: #d97706; }
.text-primary { color: #2563eb; }
.text-muted   { color: #78716c; }

/* 4. Sección de Contenido */
.tab-content-section {
  background: var(--color-bg-surface);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-md, 8px);
  padding: 1.25rem;
  display: flex;
  flex-direction: column;
  gap: 1.25rem;
}

.section-header-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 1rem;
}

.section-title {
  font-size: 1.15rem;
  font-weight: 700;
  color: var(--color-text-main);
  margin: 0;
}

.section-controls {
  display: flex;
  align-items: center;
  gap: 0.65rem;
  flex-wrap: wrap;
}

.filter-select {
  padding: 0.5rem 0.85rem;
  border: 1px solid var(--color-border);
  border-radius: var(--radius-md, 8px);
  background: var(--color-bg-alt);
  color: var(--color-text-main);
  font-size: 0.8125rem;
  outline: none;
}

.search-input-wrap {
  position: relative;
  min-width: 240px;
}

.search-icon {
  position: absolute;
  left: 0.75rem;
  top: 50%;
  transform: translateY(-50%);
  color: var(--color-text-muted);
  font-size: 0.8125rem;
}

.search-input {
  width: 100%;
  padding: 0.5rem 0.85rem 0.5rem 2.25rem;
  border: 1px solid var(--color-border);
  border-radius: var(--radius-md, 8px);
  background: var(--color-bg-alt);
  color: var(--color-text-main);
  font-size: 0.8125rem;
}

.btn-outline-warm {
  display: inline-flex;
  align-items: center;
  gap: 0.45rem;
  padding: 0.5rem 0.95rem;
  border: 1px solid var(--color-border);
  border-radius: var(--radius-md, 8px);
  background: transparent;
  color: var(--color-text-main);
  font-size: 0.8125rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s;
}

.btn-outline-warm:hover {
  background: var(--color-bg-alt);
  border-color: #ea580c;
  color: #ea580c;
}

.btn-primary-action {
  display: inline-flex;
  align-items: center;
  gap: 0.45rem;
  padding: 0.5rem 1rem;
  border: none;
  border-radius: var(--radius-md, 8px);
  background: #ea580c;
  color: #fff;
  font-size: 0.8125rem;
  font-weight: 600;
  cursor: pointer;
  box-shadow: 0 2px 8px rgba(234, 88, 12, 0.25);
  transition: all 0.2s;
}

.btn-primary-action:hover {
  background: #c2410c;
  transform: translateY(-1px);
}

/* 5. Tabla y Celdas */
.table-container {
  overflow-x: auto;
}

.custom-datatable {
  font-size: 0.875rem;
}

.ticket-cell {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.ticket-cell i {
  color: #ea580c;
}

.client-cell {
  display: flex;
  flex-direction: column;
  gap: 0.15rem;
}

.client-name {
  font-weight: 600;
  color: var(--color-text-main);
  font-size: 0.85rem;
}

.client-sub {
  font-size: 0.75rem;
  color: var(--color-text-muted);
}

.truncate-text {
  max-width: 220px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.tag-xs {
  font-size: 0.6875rem !important;
  padding: 0.15rem 0.55rem !important;
}

.action-buttons-cell {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 0.4rem;
}

.btn-row-action {
  width: 30px;
  height: 30px;
  border-radius: 6px;
  border: 1px solid var(--color-border);
  background: var(--color-bg-surface);
  color: var(--color-text-muted);
  display: inline-flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.15s;
}

.btn-row-action:hover {
  border-color: #ea580c;
  color: #ea580c;
  background: var(--color-bg-alt);
}

.btn-row-primary {
  background: rgba(37, 99, 235, 0.1);
  color: #2563eb;
  border-color: rgba(37, 99, 235, 0.2);
}

.btn-row-primary:hover {
  background: #2563eb;
  color: #fff;
  border-color: #2563eb;
}

.btn-row-warning {
  background: rgba(234, 88, 12, 0.1);
  color: #ea580c;
  border-color: rgba(234, 88, 12, 0.2);
}

.btn-row-warning:hover {
  background: #ea580c;
  color: #fff;
  border-color: #ea580c;
}

.btn-row-success {
  background: rgba(22, 163, 74, 0.1);
  color: #16a34a;
  border-color: rgba(22, 163, 74, 0.2);
}

.btn-row-success:hover {
  background: #16a34a;
  color: #fff;
  border-color: #16a34a;
}

.btn-row-danger:hover {
  border-color: #ef4444;
  color: #ef4444;
  background: rgba(239, 68, 68, 0.05);
}

.empty-state {
  padding: 3rem 1rem;
  text-align: center;
}

.empty-icon {
  font-size: 2.5rem;
  color: var(--color-text-muted);
  margin-bottom: 0.75rem;
  opacity: 0.5;
}

.empty-title {
  font-weight: 700;
  font-size: 1rem;
  color: var(--color-text-main);
  margin: 0 0 0.25rem 0;
}

.empty-desc {
  font-size: 0.8125rem;
  color: var(--color-text-muted);
  margin: 0;
}
</style>
