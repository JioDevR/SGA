<template>
  <div class="ventas-view">

    <!-- Vista Completa: Formulario Maestro de Cotización Comercial (en Pasos) -->
    <CotizacionRegistrationForm
      v-if="showCotizacionForm"
      :cotizacion-to-edit="selectedCotizacion"
      :clientes="clientes"
      @cancel="closeAllForms"
      @saved="handleCotizacionSaved"
      @client-created="handleQuickClientCreated"
    />

    <!-- Vista Completa: Formulario Maestro de Cliente / Prospecto -->
    <ClienteRegistrationForm
      v-else-if="showClienteForm"
      :cliente-to-edit="selectedCliente"
      @cancel="closeAllForms"
      @saved="handleClienteSaved"
    />

    <!-- Vista Principal: Tablero Comercial y Listados -->
    <div v-else class="ventas-master-container">

      <!-- 1. Hero Banner Card con Estilo Editorial Cálido Reutilizable -->
      <HeroBannerCard
        kicker="RECEPCIÓN COMERCIAL // PROSPECTOS & EMISIÓN DE COTIZACIONES"
        title="Control de Cotizaciones, Clientes y Expedientes"
        description="Supervisión de prospectos comerciales, emisión de presupuestos de despacho aduanero y formalización de expedientes operativos."
      />

      <!-- 2. Métricas Comerciales Rápidas Reutilizando MetricCard -->
      <div class="metrics-grid">
        <MetricCard
          index="01"
          label="Cotizaciones Emitidas"
          :value="cotizaciones.length"
          severity="accent"
        />
        <MetricCard
          index="02"
          label="Propuestas Aprobadas"
          :value="aprobadasCount"
          severity="success"
        />
        <MetricCard
          index="03"
          label="Clientes & Prospectos"
          :value="clientes.length"
        />
        <MetricCard
          index="04"
          label="Expedientes Aduaneros"
          :value="ordenesServicio.length"
          severity="pending"
        />
      </div>

      <!-- 3. Selector de Pestañas Principales -->
      <div class="tabs-nav-bar">
        <button
          type="button"
          :class="['tab-btn', { active: activeTab === 'cotizaciones' }]"
          @click="switchTab('cotizaciones')"
        >
          <i class="pi pi-file-edit"></i>
          <span>Cotizaciones ({{ cotizaciones.length }})</span>
        </button>

        <button
          type="button"
          :class="['tab-btn', { active: activeTab === 'clientes' }]"
          @click="switchTab('clientes')"
        >
          <i class="pi pi-building"></i>
          <span>Clientes & Prospectos ({{ clientes.length }})</span>
        </button>

        <button
          type="button"
          :class="['tab-btn', { active: activeTab === 'ordenes' }]"
          @click="switchTab('ordenes')"
        >
          <i class="pi pi-send"></i>
          <span>Expedientes Aduaneros ({{ ordenesServicio.length }})</span>
        </button>
      </div>

    <!-- TAB 1: COTIZACIONES -->
    <div v-if="activeTab === 'cotizaciones'" class="tab-content-section">
      <div class="section-header-row">
        <div>
          <span class="badge-kicker">PROPUESTAS ADUANERAS</span>
          <h3 class="section-title">Registro de Cotizaciones Comerciales</h3>
        </div>

        <div class="section-controls">
          <select v-model="filterEstadoCotizacion" class="filter-select">
            <option value="">Todos los Estados</option>
            <option value="PENDIENTE">Pendientes de Revisión</option>
            <option value="OBSERVADA">Observadas (Para Corrección)</option>
            <option value="APROBADA">Aprobadas</option>
            <option value="RECHAZADA">Rechazadas</option>
            <option value="ANULADA">Anuladas</option>
          </select>

          <button type="button" class="btn-outline-warm" @click="loadData" :disabled="loading">
            <i :class="['pi pi-sync', { 'pi-spin': loading }]"></i>
            <span>Actualizar</span>
          </button>

          <button type="button" class="btn-espresso" @click="openCreateCotizacion">
            <i class="pi pi-plus"></i>
            <span>+ Nueva Cotización</span>
          </button>
        </div>
      </div>

      <!-- Tabla de Cotizaciones con PrimeVue DataTable -->
      <div class="table-container">
        <DataTable
          :value="filteredCotizaciones"
          :loading="loading"
          responsiveLayout="scroll"
          class="custom-datatable"
          dataKey="idCotizacion"
        >
          <template #empty>
            <div class="empty-state">
              <i class="pi pi-file-edit empty-icon"></i>
              <p class="empty-title">No hay cotizaciones registradas</p>
              <p class="empty-desc">Haga clic en "+ Nueva Cotización" para emitir una nueva oferta comercial.</p>
            </div>
          </template>
          <template #loading>
            <div class="loading-state">
              <i class="pi pi-spin pi-spinner"></i>
              <span>Cargando cotizaciones comerciales...</span>
            </div>
          </template>

          <Column header="N° Cotización">
            <template #body="{ data: c }">
              <span class="doc-code">{{ c.codigoCotizacion }}</span>
            </template>
          </Column>

          <Column header="Cliente / Prospecto">
            <template #body="{ data: c }">
              <div class="client-cell">
                <span class="client-name">{{ c.clienteRazonSocial || 'Cliente N/A' }}</span>
                <span class="client-sub">{{ c.clienteNumeroDocumento }}</span>
              </div>
            </template>
          </Column>

          <Column header="Régimen Aduanero">
            <template #body="{ data: c }">
              <span class="regimen-tag">{{ c.regimenAduanero }}</span>
            </template>
          </Column>

          <Column header="Total Cotizado">
            <template #body="{ data: c }">
              <span class="total-amount">${{ Number(c.totalCotizado || 0).toLocaleString('en-US', { minimumFractionDigits: 2 }) }}</span>
            </template>
          </Column>

          <Column header="Fecha Emisión">
            <template #body="{ data: c }">
              <span class="date-text">{{ c.fechaEmision }}</span>
            </template>
          </Column>

          <Column header="Estado">
            <template #body="{ data: c }">
              <div class="status-cell">
                <Tag
                  :value="getCotizacionLabel(c.estado)"
                  :severity="getCotizacionSeverity(c.estado)"
                  rounded
                />
                <button
                  v-if="(c.estado === 'REVISION' || c.estado === 'OBSERVADA') && c.motivoObservacion"
                  type="button"
                  class="obs-warning-pill"
                  @click="openViewObservation(c)"
                  title="Ver observación de Supervisión Aduanera"
                >
                  <i class="pi pi-exclamation-triangle"></i>
                  <span>Ver motivo</span>
                </button>
              </div>
            </template>
          </Column>

          <Column header="Expediente Aduanero">
            <template #body="{ data: c }">
              <span v-if="c.tieneExpediente" class="os-linked-tag">
                <i class="pi pi-folder"></i> {{ c.codigoExpediente }}
              </span>
              <span v-else-if="c.estado === 'APROBADA'" class="text-muted-dash">
                Aprobada (Listo para Expediente)
              </span>
              <span v-else class="text-muted-dash">—</span>
            </template>
          </Column>

          <Column header="Acciones" headerClass="text-right" bodyClass="text-right">
            <template #body="{ data: c }">
              <div class="actions-wrapper">
                <button
                  type="button"
                  class="action-btn-outline"
                  title="Ver / Adjuntar Documentos de la Cotización"
                  @click="openDocumentosCotizacion(c)"
                >
                  <i class="pi pi-paperclip"></i>
                  <span>Docs</span>
                </button>

                <button
                  v-if="c.estado === 'REVISION' || c.estado === 'OBSERVADA'"
                  type="button"
                  class="action-btn-outline observe-fix"
                  title="Corregir observaciones y enviar nuevamente"
                  @click="openEditCotizacion(c)"
                >
                  <i class="pi pi-file-edit"></i>
                  <span>Corregir</span>
                </button>
                <button
                  v-else
                  type="button"
                  class="action-btn-outline"
                  title="Editar cotización"
                  @click="openEditCotizacion(c)"
                >
                  Editar
                </button>
                <button
                  v-if="!c.tieneExpediente && c.estado !== 'APROBADA'"
                  type="button"
                  class="action-btn-outline btn-danger-action"
                  title="Eliminar cotización"
                  @click="handleDeleteCotizacion(c)"
                >
                  Eliminar
                </button>
              </div>
            </template>
          </Column>
        </DataTable>
      </div>
    </div>

    <!-- TAB 2: CLIENTES & PROSPECTOS -->
    <div v-if="activeTab === 'clientes'" class="tab-content-section">
      <div class="section-header-row">
        <div>
          <span class="badge-kicker">CARTERA COMERCIAL</span>
          <h3 class="section-title">Directorio de Clientes & Prospectos</h3>
        </div>

        <div class="section-controls">
          <input
            v-model="searchCliente"
            type="text"
            placeholder="Buscar por RUC o razón social..."
            class="search-mini-input"
          />

          <select v-model="filterTipoCliente" class="filter-select">
            <option value="ALL">Todos (Clientes y Prospectos)</option>
            <option value="PROSPECTO">Solo Prospectos</option>
            <option value="CLIENTE">Clientes Efectivos</option>
          </select>

          <button type="button" class="btn-espresso" @click="openCreateCliente">
            <i class="pi pi-plus"></i>
            <span>+ Nuevo Cliente</span>
          </button>
        </div>
      </div>

      <!-- Tabla de Clientes con PrimeVue DataTable -->
      <div class="table-container">
        <DataTable
          :value="filteredClientes"
          :loading="loading"
          responsiveLayout="scroll"
          class="custom-datatable"
          dataKey="idCliente"
        >
          <template #empty>
            <div class="empty-state">
              <i class="pi pi-building empty-icon"></i>
              <p class="empty-title">No hay clientes encontrados</p>
              <p class="empty-desc">Registre su primer cliente o prospecto para empezar a cotizar.</p>
            </div>
          </template>

          <Column header="Documento">
            <template #body="{ data: cl }">
              <div class="doc-badge">
                <span class="doc-type">{{ cl.tipoDocumento }}</span>
                <span class="doc-number">{{ cl.numeroDocumento }}</span>
              </div>
            </template>
          </Column>

          <Column header="Razón Social / Contacto">
            <template #body="{ data: cl }">
              <div class="client-cell">
                <span class="client-name">{{ cl.razonSocial }}</span>
                <span class="client-sub" v-if="cl.contactoRepresentante">Rep: {{ cl.contactoRepresentante }}</span>
              </div>
            </template>
          </Column>

          <Column header="Correo Institucional">
            <template #body="{ data: cl }">
              <span class="contact-text">{{ cl.correoContacto }}</span>
            </template>
          </Column>

          <Column header="Teléfono">
            <template #body="{ data: cl }">
              <span class="contact-text">{{ cl.telefonoContacto || '—' }}</span>
            </template>
          </Column>

          <Column header="Dirección Fiscal">
            <template #body="{ data: cl }">
              <span class="address-text">{{ cl.direccionFiscal || '—' }}</span>
            </template>
          </Column>

          <Column header="Clasificación">
            <template #body="{ data: cl }">
              <Tag
                :value="cl.esProspecto ? 'Prospecto' : 'Cliente Efectivo'"
                :severity="cl.esProspecto ? 'warn' : 'success'"
                rounded
                :title="cl.esProspecto ? 'Se promoverá automáticamente a Cliente Efectivo al aprobarse una cotización' : 'Cuenta formalizada con cotizaciones aprobadas'"
              />
            </template>
          </Column>

          <Column header="Acciones" headerClass="text-right" bodyClass="text-right">
            <template #body="{ data: cl }">
              <div class="actions-wrapper">
                <button type="button" class="action-btn-outline" @click="openEditCliente(cl)">
                  Editar
                </button>
                <button type="button" class="action-btn-outline btn-danger-action" @click="handleDeleteCliente(cl)">
                  Eliminar
                </button>
              </div>
            </template>
          </Column>
        </DataTable>
      </div>
    </div>

    <!-- TAB 3: EXPEDIENTES ADUANEROS -->
    <div v-if="activeTab === 'ordenes'" class="tab-content-section">
      <div class="section-header-row">
        <div>
          <span class="badge-kicker">SEGUIMIENTO OPERATIVO</span>
          <h3 class="section-title">Expedientes Aduaneros Generados</h3>
        </div>

        <div class="section-controls">
          <select v-model="filterEstadoOs" class="filter-select">
            <option value="">Todos los Estados</option>
            <option value="GENERADA">GENERADA</option>
            <option value="EN_OPERACIONES">EN OPERACIONES</option>
            <option value="CONCLUIDA">CONCLUIDA</option>
          </select>

          <button type="button" class="btn-outline-warm" @click="loadData" :disabled="loading">
            <i :class="['pi pi-sync', { 'pi-spin': loading }]"></i>
            <span>Actualizar</span>
          </button>
        </div>
      </div>

      <!-- Tabla de Expedientes Aduaneros con PrimeVue DataTable -->
      <div class="table-container">
        <DataTable
          :value="filteredOrdenes"
          :loading="loading"
          responsiveLayout="scroll"
          class="custom-datatable"
          dataKey="idOrdenServicio"
        >
          <template #empty>
            <div class="empty-state">
              <i class="pi pi-folder-open empty-icon"></i>
              <p class="empty-title">No hay expedientes aduaneros</p>
              <p class="empty-desc">Los expedientes operativos se gestionan a partir de cotizaciones comerciales aprobadas.</p>
            </div>
          </template>

          <Column header="Código Expediente">
            <template #body="{ data: os }">
              <span class="doc-code-os">{{ os.codigoOs }}</span>
            </template>
          </Column>

          <Column header="Cotización Vinculada">
            <template #body="{ data: os }">
              <span class="doc-code">{{ os.codigoCotizacion }}</span>
            </template>
          </Column>

          <Column header="Cliente">
            <template #body="{ data: os }">
              <span class="client-name">{{ os.clienteRazonSocial || '—' }}</span>
            </template>
          </Column>

          <Column header="Monto Contratado">
            <template #body="{ data: os }">
              <span class="total-amount">${{ Number(os.totalCotizado || 0).toLocaleString('en-US', { minimumFractionDigits: 2 }) }}</span>
            </template>
          </Column>

          <Column header="Fecha Emisión">
            <template #body="{ data: os }">
              <span class="date-text">{{ formatDateTime(os.fechaEmision) }}</span>
            </template>
          </Column>

          <Column header="Estado Operativo">
            <template #body="{ data: os }">
              <Tag
                :value="os.estadoServicio"
                :severity="getOsSeverity(os.estadoServicio)"
                rounded
              />
            </template>
          </Column>

          <Column header="Observaciones">
            <template #body="{ data: os }">
              <span class="obs-snippet">{{ os.observaciones || '—' }}</span>
            </template>
          </Column>

          <Column header="Acciones" headerClass="text-right" bodyClass="text-right">
            <template #body="{ data: os }">
              <div class="actions-wrapper">
                <button
                  v-if="os.estadoServicio === 'GENERADA'"
                  type="button"
                  class="action-btn-outline approve"
                  @click="handleCambiarEstadoOS(os, 'EN_OPERACIONES')"
                >
                  Iniciar Operación
                </button>
                <button
                  v-else-if="os.estadoServicio === 'EN_OPERACIONES'"
                  type="button"
                  class="action-btn-outline finish"
                  @click="handleCambiarEstadoOS(os, 'CONCLUIDA')"
                >
                  Concluir
                </button>
                <button
                  type="button"
                  class="action-btn-outline btn-danger-action"
                  @click="handleDeleteOS(os)"
                >
                  Eliminar
                </button>
              </div>
            </template>
          </Column>
        </DataTable>
      </div>
    </div>

    <!-- Modal: Detalle de Observación de Supervisión Aduanera -->
    <Dialog
      v-model:visible="showObservationDialog"
      modal
      header="Observaciones de Supervisión Aduanera"
      :style="{ width: '500px', maxWidth: '95vw' }"
      class="custom-sga-dialog"
    >
      <div v-if="selectedObservationCotizacion" class="observation-view-content">
        <div class="obs-box-header">
          <span class="badge-kicker">EXPEDIENTE N° {{ selectedObservationCotizacion.codigoCotizacion }}</span>
          <strong class="obs-box-title">{{ selectedObservationCotizacion.clienteRazonSocial || 'Cliente Asignado' }}</strong>
        </div>
        <div class="obs-box-body">
          <i class="pi pi-exclamation-triangle obs-box-icon"></i>
          <div>
            <span class="obs-box-label">Indicaciones de Supervisión Aduanera:</span>
            <p class="obs-box-text">{{ selectedObservationCotizacion.motivoObservacion || 'Se requieren ajustes en la cotización antes de la aprobación.' }}</p>
          </div>
        </div>
        <p class="obs-box-hint">
          Haga clic en "Corregir Cotización" para realizar los ajustes solicitados. Una vez guardada, volverá automáticamente a Supervisión como Enviada.
        </p>
      </div>
      <template #footer>
        <div class="dialog-footer-actions">
          <button type="button" class="btn-outline-warm" @click="showObservationDialog = false">
            <span>Cerrar</span>
          </button>
          <button
            type="button"
            class="btn-espresso"
            @click="openEditFromObservation"
          >
            <i class="pi pi-pencil"></i>
            <span>Corregir Cotización</span>
          </button>
        </div>
      </template>
    </Dialog>

    <!-- Modal: Gestión Integral de Documentos Adjuntos -->
    <DocumentosManagerModal
      :visible="showDocumentosModal"
      :entidad-tipo="'COTIZACION'"
      :id-entidad="selectedDocCotizacionId"
      :entidad-id="selectedDocCotizacionId"
      :codigo-entidad="selectedDocCotizacionCodigo"
      :titulo-entidad="selectedDocCotizacionTitulo"
      @close="showDocumentosModal = false"
    />
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, watch } from 'vue'
import DataTable from 'primevue/datatable'
import Column from 'primevue/column'
import Tag from 'primevue/tag'
import Dialog from 'primevue/dialog'
import { comercialService } from '../services/comercialService'
import { expedienteService } from '../services/expedienteService'
import { documentoService } from '@/features/documentos/services/documentoService'
import { useAuthStore } from '@/features/auth/stores/authStore'
import { useNavigationStore } from '@/core/stores/navigationStore'
import { useNotification } from '@/shared/composables/useNotification'
import { useConfirmation } from '@/shared/composables/useConfirmation'
import ClienteRegistrationForm from '../components/ClienteRegistrationForm.vue'
import CotizacionRegistrationForm from '../components/CotizacionRegistrationForm.vue'
import DocumentosManagerModal from '@/shared/components/DocumentosManagerModal.vue'
import HeroBannerCard from '@/shared/components/HeroBannerCard.vue'
import MetricCard from '@/shared/components/MetricCard.vue'

const authStore = useAuthStore()
const navigationStore = useNavigationStore()
const { showSuccess, showError, showInfo } = useNotification()
const { confirmAction, confirmDelete } = useConfirmation()

const activeTab = ref('cotizaciones')
const loading = ref(false)

const showClienteForm = ref(false)
const selectedCliente = ref(null)

const showCotizacionForm = ref(false)
const selectedCotizacion = ref(null)

const showObservationDialog = ref(false)
const selectedObservationCotizacion = ref(null)

// Modal de Gestión de Documentos
const showDocumentosModal = ref(false)
const selectedDocCotizacionId = ref(null)
const selectedDocCotizacionCodigo = ref('')
const selectedDocCotizacionTitulo = ref('')

const openDocumentosCotizacion = (cotizacion) => {
  if (!cotizacion) return
  selectedDocCotizacionId.value = cotizacion.idCotizacion
  selectedDocCotizacionCodigo.value = cotizacion.codigoCotizacion || `COT-${cotizacion.idCotizacion}`
  selectedDocCotizacionTitulo.value = `${cotizacion.codigoCotizacion || 'Cotización'} — ${cotizacion.clienteRazonSocial || 'Cliente'}`
  showDocumentosModal.value = true
}

const openViewObservation = (cotizacion) => {
  selectedObservationCotizacion.value = cotizacion
  showObservationDialog.value = true
}

const openEditFromObservation = () => {
  if (selectedObservationCotizacion.value) {
    const c = selectedObservationCotizacion.value
    showObservationDialog.value = false
    openEditCotizacion(c)
  }
}

// Cerrar todos los formularios de vista completa y volver a los listados
const closeAllForms = () => {
  showCotizacionForm.value = false
  showClienteForm.value = false
  selectedCotizacion.value = null
  selectedCliente.value = null
}

// Cambio de pestaña desde la barra horizontal superior
const switchTab = (tabName) => {
  closeAllForms()
  activeTab.value = tabName
  navigationStore.setComercialSubOption(tabName)
}

// Sincronizar activeTab y resetear formularios con sub-opción de navegación Comercial y navTick
watch(
  () => [navigationStore.comercialSubOption, navigationStore.navTick],
  ([newSub]) => {
    if (newSub && ['cotizaciones', 'clientes', 'ordenes'].includes(newSub)) {
      activeTab.value = newSub
      closeAllForms()
    }
  },
  { immediate: true }
)

// Escuchar disparadores de acciones desde el Sidebar
watch(
  () => [navigationStore.comercialActionTrigger, navigationStore.navTick],
  ([action]) => {
    if (action === 'nueva-cotizacion') {
      openCreateCotizacion()
      setTimeout(() => {
        if (navigationStore.comercialActionTrigger === 'nueva-cotizacion') {
          navigationStore.comercialActionTrigger = null
        }
      }, 50)
    } else if (action === 'nuevo-cliente') {
      openCreateCliente()
      setTimeout(() => {
        if (navigationStore.comercialActionTrigger === 'nuevo-cliente') {
          navigationStore.comercialActionTrigger = null
        }
      }, 50)
    }
  }
)

const cotizaciones = ref([])
const clientes = ref([])
const ordenesServicio = ref([])

const filterEstadoCotizacion = ref('')
const searchCliente = ref('')
const filterTipoCliente = ref('ALL')
const filterEstadoOs = ref('')

const showToast = (message, type = 'success') => {
  if (type === 'error') {
    showError(message)
  } else if (type === 'info') {
    showInfo(message)
  } else {
    showSuccess(message)
  }
}

const aprobadasCount = computed(() => {
  return cotizaciones.value.filter((c) => c.estado === 'APROBADA').length
})

const filteredCotizaciones = computed(() => {
  if (!filterEstadoCotizacion.value) return cotizaciones.value
  if (filterEstadoCotizacion.value === 'ENVIADA' || filterEstadoCotizacion.value === 'PENDIENTE') {
    return cotizaciones.value.filter((c) => c.estado === 'ENVIADA' || c.estado === 'PENDIENTE')
  }
  if (filterEstadoCotizacion.value === 'REVISION' || filterEstadoCotizacion.value === 'OBSERVADA') {
    return cotizaciones.value.filter((c) => c.estado === 'REVISION' || c.estado === 'OBSERVADA')
  }
  return cotizaciones.value.filter((c) => c.estado === filterEstadoCotizacion.value)
})

const filteredClientes = computed(() => {
  return clientes.value.filter((cl) => {
    const q = searchCliente.value.toLowerCase().trim()
    const matchSearch =
      !q ||
      cl.razonSocial?.toLowerCase().includes(q) ||
      cl.numeroDocumento?.includes(q)

    let matchType = true
    if (filterTipoCliente.value === 'PROSPECTO') {
      matchType = cl.esProspecto === true
    } else if (filterTipoCliente.value === 'CLIENTE') {
      matchType = cl.esProspecto === false
    }

    return matchSearch && matchType
  })
})

const filteredOrdenes = computed(() => {
  if (!filterEstadoOs.value) return ordenesServicio.value
  return ordenesServicio.value.filter((os) => os.estadoServicio === filterEstadoOs.value)
})

const loadData = async () => {
  loading.value = true
  try {
    const [cots, cls, exps] = await Promise.all([
      comercialService.getCotizaciones(),
      comercialService.getClientes(),
      expedienteService.getExpedientes()
    ])
    cotizaciones.value = cots || []
    clientes.value = cls || []
    ordenesServicio.value = (exps || []).map((exp) => ({
      ...exp,
      idOrdenServicio: exp.idExpediente,
      codigoOs: exp.codigoExpediente,
      estadoServicio: exp.estadoExpediente,
      fechaEmision: exp.fechaApertura
    }))
  } catch (err) {
    showToast(err.response?.data?.message || 'Error al conectar con la API de ventas.', 'error')
  } finally {
    loading.value = false
  }
}

// ==========================================
// CLIENTES ACTIONS
// ==========================================
const openCreateCliente = () => {
  selectedCliente.value = null
  showClienteForm.value = true
}

const openEditCliente = (cliente) => {
  selectedCliente.value = { ...cliente }
  showClienteForm.value = true
}

const handleClienteSaved = async ({ isEdit, clienteId, payload }) => {
  try {
    if (isEdit) {
      await comercialService.updateCliente(clienteId, payload)
      showToast('Cliente actualizado correctamente.')
    } else {
      await comercialService.createCliente(payload)
      showToast('Nuevo cliente registrado exitosamente.')
    }
    showClienteForm.value = false
    await loadData()
  } catch (err) {
    showToast(err.response?.data?.message || 'Error al guardar cliente.', 'error')
  }
}

const handleDeleteCliente = async (cliente) => {
  const confirmed = await confirmDelete(
    `¿Desea eliminar el cliente ${cliente.razonSocial}?`,
    'Confirmar Eliminación de Cliente'
  )
  if (!confirmed) return

  try {
    await comercialService.deleteCliente(cliente.idCliente)
    showToast('Cliente desactivado correctamente.', 'info')
    await loadData()
  } catch (err) {
    showToast(err.response?.data?.message || 'No se pudo eliminar el cliente.', 'error')
  }
}

// ==========================================
// COTIZACIONES ACTIONS
// ==========================================
const openCreateCotizacion = () => {
  selectedCotizacion.value = null
  showCotizacionForm.value = true
}

const openEditCotizacion = (cotizacion) => {
  selectedCotizacion.value = { ...cotizacion }
  showCotizacionForm.value = true
}

const handleQuickClientCreated = async (newClient) => {
  await loadData()
}

const handleCotizacionSaved = async ({ isEdit, cotizacionId, payload, documentos }) => {
  try {
    if (isEdit) {
      await comercialService.updateCotizacion(cotizacionId, payload)
      showToast('Cotización actualizada exitosamente.')
    } else {
      const created = await comercialService.createCotizacion(payload)
      const newId = created?.idCotizacion

      if (newId && documentos && documentos.length > 0) {
        let uploadedCount = 0
        for (const doc of documentos) {
          try {
            await documentoService.upload([doc.file], {
              categoria: doc.categoria,
              descripcion: doc.descripcion || '',
              entidadTipo: 'COTIZACION',
              idEntidad: newId,
              idUsuario: authStore.user?.idUsuario || 1
            })
            uploadedCount++
          } catch (docErr) {
            console.error('Error al subir documento sustentatorio:', docErr)
          }
        }
        showToast(`Cotización registrada exitosamente con ${uploadedCount} documento(s) adjunto(s).`)
      } else {
        showToast('Cotización registrada exitosamente.')
      }
    }
    showCotizacionForm.value = false
    await loadData()
  } catch (err) {
    showToast(err.response?.data?.message || 'Error al guardar cotización.', 'error')
  }
}

const handleCambiarEstadoCotizacion = async (cotizacion, nuevoEstado) => {
  let motivo = ''
  if (nuevoEstado === 'RECHAZADA') {
    motivo = window.prompt('Ingrese el motivo de rechazo (opcional):') || ''
  }

  try {
    await comercialService.updateCotizacionEstado(cotizacion.idCotizacion, {
      estado: nuevoEstado,
      motivoObservacion: motivo
    })
    showToast(`Cotización marcada como ${nuevoEstado}.`)
    await loadData()
  } catch (err) {
    showToast(err.response?.data?.message || 'Error al actualizar estado.', 'error')
  }
}

const handleDeleteCotizacion = async (cotizacion) => {
  const confirmed = await confirmDelete(
    `¿Desea eliminar la cotización ${cotizacion.codigoCotizacion}?`,
    'Confirmar Eliminación de Cotización'
  )
  if (!confirmed) return

  try {
    await comercialService.deleteCotizacion(cotizacion.idCotizacion)
    showToast('Cotización eliminada exitosamente.', 'info')
    await loadData()
  } catch (err) {
    showToast(err.response?.data?.message || 'No se puede eliminar la cotización.', 'error')
  }
}

// ==========================================
// ÓRDENES DE SERVICIO ACTIONS
// ==========================================
const handleGenerarOS = async (cotizacion) => {
  const confirmed = await confirmAction({
    message: `¿Generar Orden de Servicio operativa para la cotización ${cotizacion.codigoCotizacion}?`,
    header: 'Generar Orden de Servicio',
    icon: 'pi pi-file-edit',
    acceptLabel: 'Generar OS',
    acceptClass: 'p-button-success'
  })
  if (!confirmed) return

  try {
    await comercialService.createOrdenServicio({
      idCotizacion: cotizacion.idCotizacion,
      idAgenteAduanero: cotizacion.idAgenteRevisor || 1,
      observaciones: `Servicio formalizado desde propuesta ${cotizacion.codigoCotizacion}`
    })
    showToast('¡Orden de Servicio generada con éxito!')
    activeTab.value = 'ordenes'
    await loadData()
  } catch (err) {
    showToast(err.response?.data?.message || 'Error al generar Orden de Servicio.', 'error')
  }
}

const handleCambiarEstadoOS = async (os, nuevoEstado) => {
  try {
    await comercialService.updateOrdenServicioEstado(os.idOrdenServicio, {
      estadoServicio: nuevoEstado,
      observaciones: os.observaciones
    })
    showToast(`Estado de la OS ${os.codigoOs} actualizado a ${nuevoEstado}.`)
    await loadData()
  } catch (err) {
    showToast(err.response?.data?.message || 'Error al actualizar OS.', 'error')
  }
}

const handleDeleteOS = async (os) => {
  const confirmed = await confirmDelete(
    `¿Desea eliminar la Orden de Servicio ${os.codigoOs}?`,
    'Confirmar Eliminación de Orden de Servicio'
  )
  if (!confirmed) return

  try {
    await comercialService.deleteOrdenServicio(os.idOrdenServicio)
    showToast('Orden de servicio eliminada.', 'info')
    await loadData()
  } catch (err) {
    showToast(err.response?.data?.message || 'Error al eliminar OS.', 'error')
  }
}

const getCotizacionSeverity = (estado) => {
  if (estado === 'APROBADA') return 'success'
  if (estado === 'RECHAZADA' || estado === 'ANULADA') return 'danger'
  if (estado === 'REVISION' || estado === 'OBSERVADA') return 'warn'
  if (estado === 'EN_REVISION') return 'info'
  return 'contrast'
}

const getCotizacionLabel = (estado) => {
  const map = {
    ENVIADA: 'Pendiente',
    PENDIENTE: 'Pendiente',
    EN_REVISION: 'En Revisión',
    REVISION: 'Observada',
    OBSERVADA: 'Observada',
    APROBADA: 'Aprobada',
    RECHAZADA: 'Rechazada',
    ANULADA: 'Anulada'
  }
  return map[estado] || estado
}

const getOsSeverity = (estado) => {
  if (estado === 'CONCLUIDA') return 'success'
  if (estado === 'EN_OPERACIONES') return 'info'
  return 'warn'
}

const getCotizacionBadgeClass = (estado) => {
  if (estado === 'APROBADA') return 'status-approved'
  if (estado === 'RECHAZADA') return 'status-rejected'
  return 'status-pending'
}

const getOsBadgeClass = (estado) => {
  if (estado === 'CONCLUIDA') return 'status-approved'
  if (estado === 'EN_OPERACIONES') return 'status-progress'
  return 'status-pending'
}

const formatDateTime = (dtStr) => {
  if (!dtStr) return '—'
  return dtStr.replace('T', ' ').substring(0, 16)
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.ventas-view {
  display: flex;
  flex-direction: column;
  gap: 2rem;
  max-width: 1440px;
  margin: 0 auto;
}

/* 1. Hero Banner Card */
.hero-banner-card {
  position: relative;
  background: linear-gradient(135deg, #1c1917 0%, #1c2621 60%, #063d27 100%);
  border: 1px solid rgba(16, 185, 129, 0.25);
  border-radius: var(--radius-lg, 12px);
  padding: 1.75rem 2rem;
  color: #fafaf9;
  overflow: hidden;
  box-shadow: 0 4px 18px rgba(0, 0, 0, 0.12), inset 0 1px 0 rgba(255, 255, 255, 0.05);
}

.hero-content {
  max-width: 780px;
  position: relative;
  z-index: 2;
}

.badge-kicker {
  display: inline-block;
  font-size: 0.6875rem;
  font-weight: 700;
  letter-spacing: 0.1em;
  color: #10b981;
  margin-bottom: 0.5rem;
  text-transform: uppercase;
}

.hero-title {
  font-size: 1.65rem;
  font-weight: 700;
  color: #ffffff;
  letter-spacing: -0.015em;
  margin: 0.15rem 0 0.45rem;
  line-height: 1.25;
}

.hero-desc {
  font-size: 0.875rem;
  color: #d6d3d1;
  line-height: 1.5;
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

/* 2. Grid de Métricas */
.metrics-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 1.25rem;
}

.metric-card {
  background: var(--color-bg-surface);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-lg);
  padding: 1.35rem 1.45rem;
  display: flex;
  flex-direction: column;
  gap: 0.65rem;
  box-shadow: var(--shadow-subtle);
  transition: transform var(--transition-fast), box-shadow var(--transition-fast);
}

.metric-card:hover {
  transform: translateY(-2px);
  box-shadow: var(--shadow-card);
}

.metric-top {
  display: flex;
  align-items: center;
  gap: 0.65rem;
}

.metric-idx {
  font-size: 0.6875rem;
  font-weight: 700;
  color: var(--color-accent-kicker);
  background: var(--color-bg-subtle);
  border: 1px solid var(--color-border-subtle);
  border-radius: 6px;
  padding: 0.15rem 0.45rem;
  letter-spacing: 0.02em;
}

.metric-label {
  font-size: 0.8rem;
  color: var(--color-text-muted);
  font-weight: 600;
}

.metric-val {
  font-size: 2rem;
  font-weight: 700;
  color: var(--color-text-main);
  line-height: 1;
  letter-spacing: -0.02em;
}

/* 3. Barra de Pestañas */
.tabs-nav-bar {
  display: flex;
  gap: 0.75rem;
  border-bottom: 2px solid var(--color-border-subtle);
  padding-bottom: 0.25rem;
}

.tab-btn {
  display: inline-flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.75rem 1.25rem;
  border: none;
  background: transparent;
  color: var(--color-text-muted);
  font-size: 0.875rem;
  font-weight: 600;
  cursor: pointer;
  border-bottom: 3px solid transparent;
  margin-bottom: -0.25rem;
  transition: all var(--transition-fast);
}

.tab-btn:hover {
  color: var(--color-text-main);
  background: var(--color-bg-subtle);
  border-top-left-radius: 8px;
  border-top-right-radius: 8px;
}

.tab-btn.active {
  color: var(--color-text-main);
  border-bottom-color: var(--color-primary);
  background: var(--color-bg-surface);
  border-top-left-radius: 8px;
  border-top-right-radius: 8px;
  box-shadow: var(--shadow-subtle);
}

/* Contenido de cada Tab */
.tab-content-section {
  display: flex;
  flex-direction: column;
  gap: 1.25rem;
}

.section-header-row {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  flex-wrap: wrap;
  gap: 1rem;
}

.section-title {
  font-size: 1.25rem;
  font-weight: 700;
  color: var(--color-text-main);
  margin: 0;
  letter-spacing: -0.01em;
}

.section-controls {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  flex-wrap: wrap;
}

.filter-select,
.search-mini-input {
  background: var(--color-bg-surface);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-md);
  padding: 0.55rem 0.85rem;
  font-size: 0.825rem;
  color: var(--color-text-main);
  outline: none;
}

.filter-select:focus,
.search-mini-input:focus {
  border-color: var(--color-primary);
}

/* Tablas */
.table-container {
  background: var(--color-bg-surface);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-lg);
  overflow: hidden;
  box-shadow: var(--shadow-subtle);
}

.table-responsive {
  width: 100%;
  overflow-x: auto;
}

.custom-table {
  width: 100%;
  border-collapse: collapse;
  text-align: left;
  font-size: 0.86rem;
}

.custom-table thead th {
  background: var(--color-bg-subtle);
  color: var(--color-text-muted);
  font-weight: 700;
  font-size: 0.725rem;
  text-transform: uppercase;
  letter-spacing: 0.06em;
  padding: 0.95rem 1.25rem;
  border-bottom: 1px solid var(--color-border);
}

.custom-table tbody tr {
  border-bottom: 1px solid var(--color-border-subtle);
  transition: background var(--transition-fast);
  background: var(--color-bg-surface);
}

.custom-table tbody tr:hover {
  background: var(--color-bg-subtle);
}

.custom-table tbody td {
  padding: 0.95rem 1.25rem;
  vertical-align: middle;
}

.doc-code {
  font-weight: 700;
  color: var(--color-text-main);
  font-family: monospace;
  font-size: 0.85rem;
}

.doc-code-os {
  font-weight: 700;
  color: var(--color-success-text);
  background: var(--color-success-bg);
  border: 1px solid var(--color-success-border);
  padding: 0.2rem 0.5rem;
  border-radius: 4px;
  font-family: monospace;
  font-size: 0.85rem;
}

.client-cell {
  display: flex;
  flex-direction: column;
}

.client-name {
  font-weight: 600;
  color: var(--color-text-main);
}

.client-sub {
  font-size: 0.75rem;
  color: var(--color-text-subtle);
}

.regimen-tag {
  font-size: 0.75rem;
  color: var(--color-text-muted);
}

.total-amount {
  font-weight: 700;
  color: var(--color-text-main);
}

.date-text {
  font-size: 0.8rem;
  color: var(--color-text-muted);
}

.status-badge {
  display: inline-flex;
  align-items: center;
  padding: 0.25rem 0.65rem;
  border-radius: var(--radius-pill);
  font-size: 0.725rem;
  font-weight: 700;
  letter-spacing: 0.02em;
}

.status-pending {
  background: var(--color-warning-bg);
  color: var(--color-warning-text);
  border: 1px solid var(--color-warning-border);
}

.status-approved {
  background: var(--color-success-bg);
  color: var(--color-success-text);
  border: 1px solid var(--color-success-border);
}

.status-rejected {
  background: var(--color-danger-bg);
  color: var(--color-danger-text);
  border: 1px solid var(--color-danger-border);
}

.status-progress {
  background: var(--color-info-bg);
  color: var(--color-info-text);
  border: 1px solid var(--color-info-border);
}

.os-linked-tag {
  display: inline-flex;
  align-items: center;
  gap: 0.35rem;
  font-size: 0.785rem;
  font-weight: 600;
  color: var(--color-success-text);
}

.btn-generate-os {
  background: var(--color-bg-subtle);
  border: 1px solid var(--color-border);
  color: var(--color-text-main);
  padding: 0.3rem 0.65rem;
  border-radius: 6px;
  font-size: 0.75rem;
  font-weight: 600;
  cursor: pointer;
  display: inline-flex;
  align-items: center;
  gap: 0.3rem;
  transition: all var(--transition-fast);
}

.btn-generate-os:hover {
  background: var(--color-bg-muted);
  color: var(--color-text-main);
}

.text-muted-dash {
  color: var(--color-text-subtle);
}

.text-right {
  text-align: right;
}

.actions-wrapper {
  display: inline-flex;
  gap: 0.45rem;
}

.action-btn-outline {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  background: var(--color-bg-surface);
  border: 1px solid var(--color-border);
  color: var(--color-text-main);
  padding: 0.3rem 0.75rem;
  border-radius: 6px;
  font-size: 0.785rem;
  font-weight: 500;
  cursor: pointer;
  transition: all var(--transition-fast);
}

.action-btn-outline:hover {
  background: var(--color-bg-subtle);
  border-color: var(--color-border-strong);
  color: var(--color-text-main);
}

.action-btn-outline.approve:hover {
  background: var(--color-success-bg);
  border-color: var(--color-success-border);
  color: var(--color-success-text);
}

.action-btn-outline.reject:hover {
  background: var(--color-danger-bg);
  border-color: var(--color-danger-border);
  color: var(--color-danger-text);
}

.action-btn-outline.finish:hover {
  background: var(--color-success-bg);
  border-color: var(--color-success-border);
  color: var(--color-success-text);
}

.btn-danger-action:hover {
  background: var(--color-danger-bg);
  border-color: var(--color-danger-border);
  color: var(--color-danger-text);
}

.doc-badge {
  display: flex;
  align-items: center;
  gap: 0.35rem;
}

.doc-type {
  font-size: 0.65rem;
  font-weight: 700;
  background: var(--color-bg-subtle);
  color: var(--color-text-muted);
  padding: 0.1rem 0.35rem;
  border-radius: 4px;
}

.doc-number {
  font-weight: 700;
  font-family: monospace;
}

.contact-text,
.address-text {
  font-size: 0.8rem;
  color: var(--color-text-muted);
}

.client-tag {
  display: inline-flex;
  align-items: center;
  font-size: 0.725rem;
  font-weight: 700;
  padding: 0.15rem 0.55rem;
  border-radius: 4px;
}

.prospect-tag {
  background: var(--color-warning-bg);
  color: var(--color-warning-text);
  border: 1px solid var(--color-warning-border);
}

.client-effective-tag {
  background: var(--color-success-bg);
  color: var(--color-success-text);
  border: 1px solid var(--color-success-border);
}

.obs-snippet {
  font-size: 0.785rem;
  color: var(--color-text-muted);
  max-width: 200px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.loading-state,
.empty-state {
  text-align: center;
  padding: 3.5rem 1.5rem;
  color: var(--color-text-muted);
}

.empty-icon {
  font-size: 2.25rem;
  color: var(--color-text-subtle);
  margin-bottom: 0.75rem;
}

.empty-title {
  font-weight: 700;
  color: var(--color-text-main);
  margin: 0;
}

.empty-desc {
  font-size: 0.8rem;
  margin: 0.35rem 0 0;
  color: var(--color-text-muted);
}

@media (max-width: 1024px) {
  .metrics-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 640px) {
  .metrics-grid {
    grid-template-columns: 1fr;
  }
}

/* Estado Cell & Observation Pill */
.status-cell {
  display: flex;
  flex-direction: column;
  gap: 0.3rem;
  align-items: flex-start;
}

.obs-warning-pill {
  display: inline-flex;
  align-items: center;
  gap: 0.25rem;
  padding: 0.15rem 0.45rem;
  background: var(--color-warning-bg);
  border: 1px solid var(--color-warning-border);
  color: var(--color-warning-text);
  border-radius: var(--radius-sm, 6px);
  font-size: 0.6875rem;
  font-weight: 700;
  cursor: pointer;
  transition: all var(--transition-fast);
}

.obs-warning-pill:hover {
  filter: brightness(0.95);
}

.action-btn-outline.observe-fix {
  background: var(--color-warning-bg);
  color: var(--color-warning-text);
  border-color: var(--color-warning-border);
  font-weight: 700;
}

.action-btn-outline.observe-fix:hover {
  filter: brightness(0.95);
}

/* Contenido Modal de Observación */
.observation-view-content {
  display: flex;
  flex-direction: column;
  gap: var(--space-4, 1rem);
  padding: var(--space-2, 0.5rem) 0;
}

.obs-box-header {
  display: flex;
  flex-direction: column;
  gap: 0.2rem;
  padding-bottom: var(--space-2, 0.5rem);
  border-bottom: 1px solid var(--color-border);
}

.obs-box-title {
  font-size: 1rem;
  color: var(--color-text-main);
}

.obs-box-body {
  display: flex;
  align-items: flex-start;
  gap: var(--space-3, 0.75rem);
  padding: var(--space-4, 1rem);
  background: var(--color-warning-bg);
  border: 1px solid var(--color-warning-border);
  border-radius: var(--radius-md, 8px);
  color: var(--color-warning-text);
}

.obs-box-icon {
  font-size: 1.5rem;
  margin-top: 0.15rem;
  flex-shrink: 0;
}

.obs-box-label {
  font-size: 0.75rem;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: 0.05em;
  display: block;
}

.obs-box-text {
  margin: 0.25rem 0 0 0;
  font-size: 0.875rem;
  line-height: 1.45;
  font-weight: 500;
}

.obs-box-hint {
  font-size: 0.8125rem;
  color: var(--color-text-muted);
  line-height: 1.45;
  margin: 0;
}

.dialog-footer-actions {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: var(--space-3, 0.75rem);
}
</style>
