<template>
  <BaseModal
    :visible="visible"
    :title="isEditing ? 'Editar Conductor Autorizado' : 'Registrar Conductor MTC'"
    :subtitle="isEditing ? 'Modificar datos del conductor' : 'Añadir chofer profesional habilitado para traslado aduanero'"
    icon="pi pi-user-plus"
    max-width="540px"
    :error-message="errorMessage"
    :busy="loading"
    @close="handleClose"
  >
    <form @submit.prevent="handleSubmit" class="form-body">
      <div class="form-grid">
        <div class="field-item">
          <label for="cond-nom">Nombres *</label>
          <input
            id="cond-nom"
            v-model.trim="form.nombres"
            type="text"
            placeholder="Ej. Marcos"
            class="text-input"
            required
            maxlength="100"
          />
        </div>

        <div class="field-item">
          <label for="cond-ape">Apellidos *</label>
          <input
            id="cond-ape"
            v-model.trim="form.apellidos"
            type="text"
            placeholder="Ej. Quispe Huamán"
            class="text-input"
            required
            maxlength="100"
          />
        </div>

        <div class="field-item">
          <label for="cond-tipo-lic">Categoría de Licencia MTC *</label>
          <select id="cond-tipo-lic" v-model="form.tipoLicencia" class="select-input" required>
            <option value="A-IIIc">A-IIIc (Tractocamión y Cargas Especiales)</option>
            <option value="A-IIIb">A-IIIb (Camiones pesados y Remolques)</option>
            <option value="A-IIIa">A-IIIa (Omnibuses interprovinciales)</option>
            <option value="A-IIb">A-IIb (Camiones hasta 12 toneladas)</option>
          </select>
        </div>

        <div class="field-item">
          <label for="cond-lic">N° de Licencia / Brevete *</label>
          <input
            id="cond-lic"
            v-model.trim="form.numeroLicencia"
            type="text"
            placeholder="Ej. Q45890123"
            class="text-input font-mono uppercase"
            required
            maxlength="20"
          />
        </div>

        <div class="field-item full-width">
          <label for="cond-tel">Teléfono / Móvil Operativo</label>
          <input
            id="cond-tel"
            v-model.trim="form.telefono"
            type="tel"
            placeholder="Ej. +51 977 112 233"
            class="text-input"
            maxlength="20"
          />
        </div>
      </div>

      <div class="modal-actions">
        <button type="button" class="btn-modal-cancel" @click="handleClose" :disabled="loading">
          Cancelar
        </button>
        <button type="submit" class="btn-modal-save" :disabled="loading">
          <i v-if="loading" class="pi pi-spin pi-spinner"></i>
          <i v-else class="pi pi-check"></i>
          <span>{{ isEditing ? 'Guardar Cambios' : 'Registrar Conductor' }}</span>
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
  conductor: {
    type: Object,
    default: null
  }
})

const emit = defineEmits(['close', 'saved'])

const loading = ref(false)
const errorMessage = ref('')
const isEditing = computed(() => !!props.conductor)

const defaultForm = () => ({
  nombres: '',
  apellidos: '',
  tipoLicencia: 'A-IIIc',
  numeroLicencia: '',
  telefono: '',
  estadoActivo: true
})

const form = ref(defaultForm())

watch(
  () => props.visible,
  (newVal) => {
    if (newVal) {
      errorMessage.value = ''
      if (props.conductor) {
        form.value = {
          nombres: props.conductor.nombres || '',
          apellidos: props.conductor.apellidos || '',
          tipoLicencia: props.conductor.tipoLicencia || 'A-IIIc',
          numeroLicencia: props.conductor.numeroLicencia || '',
          telefono: props.conductor.telefono || '',
          estadoActivo: props.conductor.estadoActivo ?? true
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
  if (!form.value.nombres || !form.value.apellidos || !form.value.numeroLicencia) {
    errorMessage.value = 'Por favor complete todos los campos obligatorios (*)'
    return
  }

  loading.value = true
  try {
    const payload = {
      nombres: form.value.nombres,
      apellidos: form.value.apellidos,
      tipoLicencia: form.value.tipoLicencia,
      numeroLicencia: form.value.numeroLicencia.toUpperCase(),
      telefono: form.value.telefono || null,
      estadoActivo: form.value.estadoActivo
    }

    let res
    if (isEditing.value) {
      res = await transporteService.updateConductor(props.conductor.idConductor, payload)
    } else {
      res = await transporteService.createConductor(payload)
    }

    emit('saved', res)
    handleClose()
  } catch (err) {
    console.error('Error guardando conductor:', err)
    errorMessage.value =
      err.response?.data?.message || 'Error al guardar el conductor.'
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
