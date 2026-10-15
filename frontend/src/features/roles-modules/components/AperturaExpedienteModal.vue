<template>
  <BaseModal
    :visible="visible"
    title="Apertura de Expediente Aduanero"
    :subtitle="`Operación vinculada a la Cotización ${cotizacion?.codigoCotizacion || ''}`"
    icon="pi pi-folder-open"
    max-width="700px"
    :error-message="errorMessage"
    :busy="loading"
    @close="handleClose"
  >
    <form @submit.prevent="handleSubmit" class="form-body">
      <!-- Resumen de Cotización -->
      <div class="summary-pill-card">
        <div class="summary-col">
          <span class="summary-label">CLIENTE IMPORTADOR/EXPORTADOR</span>
          <strong class="summary-val">{{ cotizacion?.clienteRazonSocial || '—' }}</strong>
          <span class="summary-sub">{{ cotizacion?.clienteNumeroDocumento }}</span>
        </div>
        <div class="summary-col">
          <span class="summary-label">RÉGIMEN ADUANERO</span>
          <strong class="summary-val">{{ cotizacion?.regimenAduanero || 'IMPORTACION_DEFINITIVA' }}</strong>
        </div>
        <div class="summary-col">
          <span class="summary-label">TOTAL COTIZADO</span>
          <strong class="summary-val text-accent">${{ Number(cotizacion?.totalCotizado || 0).toLocaleString('en-US', { minimumFractionDigits: 2 }) }}</strong>
        </div>
      </div>

      <div class="form-grid">
        <!-- Agente Aduanero Responsable -->
        <div class="field-item full-width">
          <label for="exp-agente">Agente Aduanero Responsable *</label>
          <div class="agent-select-wrap">
            <i class="pi pi-user-edit field-icon"></i>
            <select id="exp-agente" v-model="form.idAgenteAduanero" class="select-input" required>
              <option :value="authStore.user?.idUsuario || 1">
                {{ authStore.userFullName || 'Agente Aduanero Actual' }} ({{ authStore.user?.username || 'Usuario Actual' }})
              </option>
              <option v-for="u in agentesList" :key="u.idUsuario" :value="u.idUsuario">
                {{ u.nombres }} {{ u.apellidos }} ({{ u.username }})
              </option>
            </select>
          </div>
          <span class="field-hint">Especialista que gestionará la declaración aduanera y trámites SUNAT</span>
        </div>

        <!-- Contacto en el Exterior (Proveedor / Comprador) -->
        <div class="field-item">
          <label for="exp-contacto-nombre">Contacto en Exterior (Proveedor/Embarcador)</label>
          <input
            id="exp-contacto-nombre"
            v-model.trim="form.contactoExteriorNombre"
            type="text"
            placeholder="Ej. Shanghai Logistics Co. / John Smith"
            class="text-input"
            maxlength="150"
          />
        </div>

        <div class="field-item">
          <label for="exp-contacto-tel">Teléfono Contacto Exterior</label>
          <input
            id="exp-contacto-tel"
            v-model.trim="form.contactoExteriorTelefono"
            type="text"
            placeholder="Ej. +86 21 5555 1234"
            class="text-input"
            maxlength="30"
          />
        </div>

        <!-- Fechas de Tránsito Internacional -->
        <div class="field-item">
          <label for="exp-etd">Fecha Estimada de Zarpe (ETD)</label>
          <input
            id="exp-etd"
            v-model="form.fechaZarpeEtd"
            type="date"
            class="text-input"
          />
        </div>

        <div class="field-item">
          <label for="exp-eta">Fecha Estimada de Arribo (ETA)</label>
          <input
            id="exp-eta"
            v-model="form.fechaLlegadaEta"
            type="date"
            class="text-input"
          />
        </div>

        <!-- Nave / Viaje -->
        <div class="field-item">
          <label for="exp-nave">Nave / Embarcación / N° Viaje</label>
          <input
            id="exp-nave"
            v-model.trim="form.naveViaje"
            type="text"
            placeholder="Ej. MSC CAPELLA V-420E"
            class="text-input"
            maxlength="100"
          />
        </div>

        <!-- Contenedor -->
        <div class="field-item">
          <label for="exp-contenedor">N° Contenedor</label>
          <input
            id="exp-contenedor"
            v-model.trim="form.numeroContenedor"
            type="text"
            placeholder="Ej. MSCU-987654-3"
            class="text-input uppercase"
            maxlength="30"
          />
        </div>

        <!-- Precinto -->
        <div class="field-item">
          <label for="exp-precinto">Precinto de Seguridad Aduanero</label>
          <input
            id="exp-precinto"
            v-model.trim="form.precintoAduanero"
            type="text"
            placeholder="Ej. SUNAT-774921-A"
            class="text-input uppercase"
            maxlength="50"
          />
        </div>

        <!-- Observaciones -->
        <div class="field-item full-width">
          <label for="exp-obs">Instrucciones Operativas</label>
          <textarea
            id="exp-obs"
            v-model.trim="form.observaciones"
            rows="2"
            placeholder="Instrucciones sobre revisión física, balanza VGM o entrega preferente..."
            class="textarea-input"
          ></textarea>
        </div>
      </div>

      <div class="modal-actions">
        <button type="button" class="btn-modal-cancel" @click="handleClose" :disabled="loading">
          Cancelar
        </button>
        <button type="submit" class="btn-modal-save" :disabled="loading">
          <i v-if="loading" class="pi pi-spin pi-spinner"></i>
          <i v-else class="pi pi-folder-open"></i>
          <span>Aperturar Expediente</span>
        </button>
      </div>
    </form>
  </BaseModal>
</template>

<script setup>
import { ref, watch, onMounted } from 'vue'
import BaseModal from '@/shared/components/BaseModal.vue'
import { expedienteService } from '../services/expedienteService'
import { userService } from '@/features/admin/users/services/userService'
import { useAuthStore } from '@/features/auth/stores/authStore'

const props = defineProps({
  visible: {
    type: Boolean,
    default: false
  },
  cotizacion: {
    type: Object,
    default: null
  }
})

const emit = defineEmits(['close', 'saved'])
const authStore = useAuthStore()

const loading = ref(false)
const errorMessage = ref('')
const agentesList = ref([])

const defaultForm = () => ({
  idCotizacion: null,
  idAgenteAduanero: authStore.user?.idUsuario || 1,
  contactoExteriorNombre: '',
  contactoExteriorTelefono: '',
  contactoExteriorCorreo: '',
  fechaZarpeEtd: '',
  fechaLlegadaEta: '',
  numeroContenedor: '',
  precintoAduanero: '',
  naveViaje: '',
  observaciones: ''
})

const form = ref(defaultForm())

const loadAgentes = async () => {
  try {
    const users = await userService.getUsers(true)
    // Filtramos usuarios con rol SUPERVISOR, SUPERVISION, LOGISTICA o ADMINISTRADOR
    agentesList.value = (users || []).filter(
      (u) => u.idUsuario !== authStore.user?.idUsuario
    )
  } catch (e) {
    console.error('Error cargando usuarios para agente aduanero:', e)
  }
}

watch(
  () => props.visible,
  (newVal) => {
    if (newVal) {
      errorMessage.value = ''
      form.value = defaultForm()
      if (props.cotizacion) {
        form.value.idCotizacion = props.cotizacion.idCotizacion
      }
      loadAgentes()
    }
  }
)

const handleClose = () => {
  emit('close')
}

const handleSubmit = async () => {
  errorMessage.value = ''
  if (!form.value.idCotizacion || !form.value.idAgenteAduanero) {
    errorMessage.value = 'Debe especificar la cotización y el agente aduanero responsable.'
    return
  }

  loading.value = true
  try {
    const payload = {
      idCotizacion: form.value.idCotizacion,
      idAgenteAduanero: form.value.idAgenteAduanero,
      contactoExteriorNombre: form.value.contactoExteriorNombre || null,
      contactoExteriorTelefono: form.value.contactoExteriorTelefono || null,
      contactoExteriorCorreo: form.value.contactoExteriorCorreo || null,
      fechaZarpeEtd: form.value.fechaZarpeEtd || null,
      fechaLlegadaEta: form.value.fechaLlegadaEta || null,
      numeroContenedor: form.value.numeroContenedor || null,
      precintoAduanero: form.value.precintoAduanero || null,
      naveViaje: form.value.naveViaje || null,
      observaciones: form.value.observaciones || null
    }

    const creado = await expedienteService.createExpediente(payload)
    emit('saved', creado)
    handleClose()
  } catch (err) {
    console.error('Error al aperturar expediente:', err)
    errorMessage.value =
      err.response?.data?.message || 'Error al aperturar el expediente aduanero.'
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.summary-pill-card {
  display: flex;
  justify-content: space-between;
  gap: 1rem;
  background: var(--color-bg-subtle, #f5f2eb);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-md, 8px);
  padding: 0.875rem 1.25rem;
  margin-bottom: 1.25rem;
  flex-wrap: wrap;
}

.summary-col {
  display: flex;
  flex-direction: column;
}

.summary-label {
  font-size: 0.6875rem;
  font-weight: 700;
  color: var(--color-text-subtle);
  letter-spacing: 0.05em;
}

.summary-val {
  font-size: 0.9375rem;
  color: var(--color-text-main);
}

.summary-sub {
  font-size: 0.75rem;
  color: var(--color-text-muted);
}

.text-accent {
  color: var(--color-accent-copper, #c2410c);
}

.agent-select-wrap {
  position: relative;
  display: flex;
  align-items: center;
}

.agent-select-wrap .field-icon {
  position: absolute;
  left: 0.75rem;
  color: var(--color-text-muted);
  pointer-events: none;
}

.agent-select-wrap .select-input {
  padding-left: 2.25rem;
}

.uppercase {
  text-transform: uppercase;
}

.textarea-input {
  width: 100%;
  padding: 0.625rem 0.875rem;
  border: 1px solid var(--color-border);
  border-radius: var(--radius-md, 8px);
  background: var(--color-bg-surface);
  color: var(--color-text-main);
  font-family: inherit;
  font-size: 0.875rem;
  resize: vertical;
}

.textarea-input:focus {
  outline: none;
  border-color: var(--color-accent-kicker);
}
</style>
