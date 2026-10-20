<template>
  <div class="form-header-bar">
    <div class="header-titles">
      <button
        type="button"
        class="btn-back-link"
        @click="$emit('cancel')"
        title="Regresar al panel de cotizaciones"
      >
        <i class="pi pi-arrow-left"></i>
        <span>Volver al Registro de Cotizaciones</span>
      </button>

      <div class="kicker-row">
        <span class="badge-kicker">PROPUESTAS ADUANERAS // EMISIÓN COMERCIAL</span>
        <span :class="['mode-pill', isEditMode ? 'mode-edit' : 'mode-create']">
          <i :class="isEditMode ? 'pi pi-pencil' : 'pi pi-plus'"></i>
          <span>{{ isEditMode ? 'MODO EDICIÓN' : 'MODO REGISTRO' }}</span>
        </span>
      </div>

      <h2 class="form-main-title">
        {{ isEditMode ? `Editar Cotización: ${codigoCotizacion}` : 'Emitir Nueva Cotización Comercial' }}
      </h2>
      <p class="form-main-desc">
        Estructure la propuesta comercial paso a paso: asigne el cliente o prospecto, especifique el régimen de aduanas y liquide los costos estimados de flete, honorarios y aranceles.
      </p>
    </div>

    <div class="header-actions">
      <button
        type="button"
        class="btn-outline-warm"
        @click="$emit('cancel')"
        :disabled="submitting"
      >
        <span>Cancelar</span>
      </button>
      <button
        type="button"
        class="btn-espresso"
        @click="$emit('submit')"
        :disabled="submitting || !isFormReady"
      >
        <i v-if="submitting" class="pi pi-spin pi-spinner"></i>
        <i v-else class="pi pi-check"></i>
        <span>{{ isEditMode ? 'Guardar Cambios' : 'Emitir Cotización' }}</span>
      </button>
    </div>
  </div>
</template>

<script setup>
defineProps({
  isEditMode: {
    type: Boolean,
    default: false
  },
  codigoCotizacion: {
    type: String,
    default: ''
  },
  submitting: {
    type: Boolean,
    default: false
  },
  isFormReady: {
    type: Boolean,
    default: false
  }
})

defineEmits(['cancel', 'submit'])
</script>

<style scoped>
.form-header-bar {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: var(--space-6, 1.5rem);
  padding: var(--space-6, 1.5rem) var(--space-8, 2rem);
  background: var(--color-bg-surface);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-xl, 1rem);
  box-shadow: var(--shadow-sm);
  flex-wrap: wrap;
}

.header-titles {
  display: flex;
  flex-direction: column;
  gap: var(--space-2, 0.5rem);
  max-width: 800px;
}

.btn-back-link {
  display: inline-flex;
  align-items: center;
  gap: var(--space-2, 0.5rem);
  background: none;
  border: none;
  color: var(--color-text-muted);
  font-size: var(--font-size-xs, 0.75rem);
  font-weight: 700;
  letter-spacing: 0.05em;
  text-transform: uppercase;
  cursor: pointer;
  padding: 0;
  margin-bottom: var(--space-1, 0.25rem);
  transition: color var(--transition-fast);
}

.btn-back-link:hover {
  color: var(--color-primary);
}

.kicker-row {
  display: flex;
  align-items: center;
  gap: var(--space-3, 0.75rem);
  flex-wrap: wrap;
}

.badge-kicker {
  font-size: var(--font-size-2xs, 0.65rem);
  font-weight: 800;
  letter-spacing: 0.12em;
  text-transform: uppercase;
  color: var(--color-primary);
  background: var(--color-primary-subtle);
  padding: 0.2rem 0.55rem;
  border-radius: var(--radius-sm, 0.25rem);
}

.mode-pill {
  display: inline-flex;
  align-items: center;
  gap: 0.35rem;
  font-size: var(--font-size-2xs, 0.65rem);
  font-weight: 800;
  letter-spacing: 0.08em;
  text-transform: uppercase;
  padding: 0.2rem 0.6rem;
  border-radius: var(--radius-full, 9999px);
}

.mode-create {
  background: var(--color-success-subtle);
  color: var(--color-success);
}

.mode-edit {
  background: var(--color-warning-subtle);
  color: var(--color-warning);
}

.form-main-title {
  font-family: var(--font-family-serif);
  font-size: var(--font-size-2xl, 1.75rem);
  font-weight: 800;
  color: var(--color-text-main);
  line-height: 1.2;
  margin: 0;
}

.form-main-desc {
  font-size: var(--font-size-sm, 0.875rem);
  color: var(--color-text-muted);
  line-height: 1.5;
  margin: 0;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: var(--space-3, 0.75rem);
}

.btn-outline-warm {
  display: inline-flex;
  align-items: center;
  gap: var(--space-2, 0.5rem);
  padding: 0.65rem 1.25rem;
  font-size: var(--font-size-sm, 0.875rem);
  font-weight: 700;
  border-radius: var(--radius-md, 0.5rem);
  border: 1px solid var(--color-border);
  background: var(--color-bg-surface);
  color: var(--color-text-main);
  cursor: pointer;
  transition: all var(--transition-fast);
}

.btn-outline-warm:hover:not(:disabled) {
  border-color: var(--color-border-strong);
  background: var(--color-bg-subtle);
}

.btn-espresso {
  display: inline-flex;
  align-items: center;
  gap: var(--space-2, 0.5rem);
  padding: 0.65rem 1.35rem;
  font-size: var(--font-size-sm, 0.875rem);
  font-weight: 700;
  border-radius: var(--radius-md, 0.5rem);
  border: 1px solid var(--color-primary);
  background: var(--color-primary);
  color: var(--color-primary-text);
  cursor: pointer;
  transition: all var(--transition-fast);
}

.btn-espresso:hover:not(:disabled) {
  background: var(--color-primary-hover);
  border-color: var(--color-primary-hover);
}

.btn-espresso:disabled,
.btn-outline-warm:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}
</style>
