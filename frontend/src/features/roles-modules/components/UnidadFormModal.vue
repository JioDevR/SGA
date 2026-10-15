<template>
  <BaseModal
    :visible="visible"
    :title="isEditing ? 'Editar Unidad de Transporte' : 'Registrar Unidad Vehicular'"
    :subtitle="isEditing ? 'Actualizar datos de la flota operativa' : 'Incorporar tracto o plataforma a la flota autorizada'"
    icon="pi pi-car"
    max-width="580px"
    :error-message="errorMessage"
    :busy="loading"
    @close="handleClose"
  >
    <form @submit.prevent="handleSubmit" class="form-body">
      <div class="form-grid">
        <div class="field-item">
          <label for="uni-placa">Placa Tracto / Camión *</label>
          <input
            id="uni-placa"
            v-model.trim="form.placaTracto"
            type="text"
            placeholder="Ej. V4Z-891"
            class="text-input font-mono uppercase"
            required
            maxlength="15"
          />
        </div>

        <div class="field-item">
          <label for="uni-carreta">Placa Carreta / Plataforma</label>
          <input
            id="uni-carreta"
            v-model.trim="form.placaCarreta"
            type="text"
            placeholder="Ej. TC-332"
            class="text-input font-mono uppercase"
            maxlength="15"
          />
        </div>

        <div class="field-item full-width">
          <label for="uni-marca">Marca y Modelo *</label>
          <input
            id="uni-marca"
            v-model.trim="form.marcaModelo"
            type="text"
            placeholder="Ej. Volvo FH 500 Globetrotter 6x4"
            class="text-input"
            required
            maxlength="100"
          />
        </div>

        <div class="field-item">
          <label for="uni-tipo">Tipo de Vehículo *</label>
          <select id="uni-tipo" v-model="form.tipoVehiculo" class="select-input" required>
            <option value="TRACTO_PLATAFORMA">Tracto con Plataforma</option>
            <option value="TRACTO_FURGON">Tracto Furgón Cerrado</option>
            <option value="CAMION_BARANDA">Camión Baranda</option>
            <option value="CAMION_FURGON">Camión Furgón</option>
            <option value="PORTACONTENEDOR">Portacontenedor / Chasis</option>
          </select>
        </div>

        <div class="field-item">
          <label for="uni-cap">Capacidad de Carga (kg) *</label>
          <input
            id="uni-cap"
            v-model.number="form.capacidadCargaKg"
            type="number"
            step="100"
            min="100"
            max="60000"
            placeholder="Ej. 32000"
            class="text-input"
            required
          />
        </div>

        <div class="field-item full-width">
          <label for="uni-estado-op">Estado Operativo *</label>
          <select id="uni-estado-op" v-model="form.estadoOperativo" class="select-input" required>
            <option value="DISPONIBLE">DISPONIBLE (Listo para ruta)</option>
            <option value="EN_MANTENIMIENTO">EN MANTENIMIENTO TÉCNICO</option>
            <option value="FUERA_DE_SERVICIO">FUERA DE SERVICIO</option>
          </select>
        </div>
      </div>

      <div class="modal-actions">
        <button type="button" class="btn-modal-cancel" @click="handleClose" :disabled="loading">
          Cancelar
        </button>
        <button type="submit" class="btn-modal-save" :disabled="loading">
          <i v-if="loading" class="pi pi-spin pi-spinner"></i>
          <i v-else class="pi pi-check"></i>
          <span>{{ isEditing ? 'Guardar Cambios' : 'Registrar Unidad' }}</span>
        </button>
      </div>
    </form>
  </BaseModal>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import BaseModal from '@/shared/components/BaseModal.vue'
import { transporteService } from '../services/transporteService'

const props = defineProps({
  visible: {
    type: Boolean,
    default: false
  },
  unidad: {
    type: Object,
    default: null
  }
})

const emit = defineEmits(['close', 'saved'])

const loading = ref(false)
const errorMessage = ref('')
const isEditing = computed(() => !!props.unidad)

const defaultForm = () => ({
  placaTracto: '',
  placaCarreta: '',
  marcaModelo: '',
  tipoVehiculo: 'TRACTO_PLATAFORMA',
  capacidadCargaKg: 30000,
  estadoOperativo: 'DISPONIBLE',
  estadoActivo: true
})

const form = ref(defaultForm())

watch(
  () => props.visible,
  (newVal) => {
    if (newVal) {
      errorMessage.value = ''
      if (props.unidad) {
        form.value = {
          placaTracto: props.unidad.placaTracto || '',
          placaCarreta: props.unidad.placaCarreta || '',
          marcaModelo: props.unidad.marcaModelo || '',
          tipoVehiculo: props.unidad.tipoVehiculo || 'TRACTO_PLATAFORMA',
          capacidadCargaKg: props.unidad.capacidadCargaKg || 30000,
          estadoOperativo: props.unidad.estadoOperativo || 'DISPONIBLE',
          estadoActivo: props.unidad.estadoActivo ?? true
        }
      } else {
        form.value = defaultForm()
      }
    }
  }
)

const handleClose = () => {
  emit('close')
}

const handleSubmit = async () => {
  errorMessage.value = ''
  if (!form.value.placaTracto || !form.value.marcaModelo || !form.value.capacidadCargaKg) {
    errorMessage.value = 'Por favor complete todos los campos obligatorios (*)'
    return
  }

  loading.value = true
  try {
    const payload = {
      placaTracto: form.value.placaTracto.toUpperCase(),
      placaCarreta: form.value.placaCarreta ? form.value.placaCarreta.toUpperCase() : null,
      marcaModelo: form.value.marcaModelo,
      tipoVehiculo: form.value.tipoVehiculo,
      capacidadCargaKg: form.value.capacidadCargaKg,
      estadoOperativo: form.value.estadoOperativo,
      estadoActivo: form.value.estadoActivo
    }

    let res
    if (isEditing.value) {
      res = await transporteService.updateUnidad(props.unidad.idUnidad, payload)
    } else {
      res = await transporteService.createUnidad(payload)
    }

    emit('saved', res)
    handleClose()
  } catch (err) {
    console.error('Error guardando unidad de transporte:', err)
    errorMessage.value =
      err.response?.data?.message || 'Error al procesar la unidad vehicular.'
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.uppercase {
  text-transform: uppercase;
}
</style>
