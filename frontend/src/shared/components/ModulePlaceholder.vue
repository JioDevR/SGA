<template>
  <div class="module-placeholder">
    <div class="placeholder-card">
      <div class="placeholder-icon-wrapper" :style="{ background: iconBg, color: iconColor }">
        <i :class="icon"></i>
      </div>

      <span class="role-pill">{{ roleName }}</span>
      <h2 class="module-title">{{ title }}</h2>
      <p class="module-desc">{{ description }}</p>

      <div class="info-box">
        <div class="info-row">
          <span class="info-label">Estado del Módulo:</span>
          <span class="info-val status-pill">En Preparación</span>
        </div>
        <div class="info-row">
          <span class="info-label">Acceso Autorizado:</span>
          <span class="info-val">{{ roleName }}</span>
        </div>
        <div class="info-row">
          <span class="info-label">Arquitectura:</span>
          <span class="info-val">Modular Basada en Características</span>
        </div>
      </div>

      <div class="placeholder-footer">
        <p class="footer-msg">
          <i class="pi pi-info-circle"></i>
          Esta vista se encuentra reservada para su rol. Las funciones operativas se activarán en las siguientes fases.
        </p>
      </div>
    </div>
  </div>
</template>

<script setup>
defineProps({
  title: {
    type: String,
    required: true
  },
  roleName: {
    type: String,
    required: true
  },
  description: {
    type: String,
    default: 'Módulo operativo en proceso de desarrollo según el alcance del SGA.'
  },
  icon: {
    type: String,
    default: 'pi pi-compass'
  },
  iconColor: {
    type: String,
    default: 'var(--color-primary)'
  },
  iconBg: {
    type: String,
    default: 'var(--color-bg-subtle)'
  }
})
</script>

<style scoped>
.module-placeholder {
  min-height: 65vh;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 2rem;
}

.placeholder-card {
  background: var(--color-bg-surface);
  border: 1px dashed var(--color-border);
  border-radius: var(--radius-xl);
  max-width: 580px;
  width: 100%;
  padding: 3rem 2.5rem;
  text-align: center;
  box-shadow: var(--shadow-subtle);
}

.placeholder-icon-wrapper {
  width: 68px;
  height: 68px;
  border-radius: var(--radius-lg);
  display: inline-flex;
  align-items: center;
  justify-content: center;
  font-size: 2rem;
  margin-bottom: 1.25rem;
  box-shadow: 0 2px 8px rgba(50, 40, 30, 0.08);
}

.role-pill {
  display: inline-block;
  background: var(--color-bg-subtle);
  border: 1px solid var(--color-border);
  color: var(--color-text-muted);
  padding: 0.25rem 0.75rem;
  border-radius: var(--radius-pill);
  font-size: 0.75rem;
  font-weight: 700;
  letter-spacing: 0.05em;
  margin-bottom: 0.75rem;
  text-transform: uppercase;
}

.module-title {
  font-size: 1.65rem;
  font-weight: 700;
  color: var(--color-text-main);
  margin: 0;
  letter-spacing: -0.02em;
}

.module-desc {
  font-size: 0.9rem;
  color: var(--color-text-muted);
  margin: 0.65rem auto 1.75rem;
  max-width: 440px;
  line-height: 1.5;
}

.info-box {
  background: var(--color-bg-subtle);
  border: 1px solid var(--color-border-subtle);
  border-radius: var(--radius-md);
  padding: 1.1rem 1.4rem;
  display: flex;
  flex-direction: column;
  gap: 0.65rem;
  text-align: left;
  margin-bottom: 1.75rem;
}

.info-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 0.825rem;
}

.info-label {
  color: var(--color-text-subtle);
}

.info-val {
  color: var(--color-text-main);
  font-weight: 600;
}

.status-pill {
  background: var(--color-warning-bg);
  color: var(--color-warning-text);
  border: 1px solid var(--color-warning-border);
  padding: 0.15rem 0.55rem;
  border-radius: 4px;
  font-size: 0.75rem;
}

.placeholder-footer {
  border-top: 1px solid var(--color-border-subtle);
  padding-top: 1.25rem;
}

.footer-msg {
  font-size: 0.8rem;
  color: var(--color-text-subtle);
  margin: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 0.5rem;
}
</style>
