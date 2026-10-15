<template>
  <Tag :severity="roleSeverity" class="role-tag" rounded>
    <template #icon>
      <i :class="roleIcon"></i>
    </template>
    <span>{{ roleName || 'SIN ROL' }}</span>
  </Tag>
</template>

<script setup>
import { computed } from 'vue'
import Tag from 'primevue/tag'

const props = defineProps({
  roleName: {
    type: String,
    default: ''
  }
})

const roleConfig = {
  ADMINISTRADOR: { icon: 'pi pi-shield', severity: 'contrast' },
  ADMIN_TI: { icon: 'pi pi-shield', severity: 'contrast' },
  GERENTE: { icon: 'pi pi-briefcase', severity: 'contrast' },
  VENTAS: { icon: 'pi pi-briefcase', severity: 'success' },
  SUPERVISION: { icon: 'pi pi-verified', severity: 'info' },
  SUPERVISOR: { icon: 'pi pi-verified', severity: 'info' },
  LOGISTICA: { icon: 'pi pi-box', severity: 'info' },
  AGENTE_ADUANERO: { icon: 'pi pi-compass', severity: 'info' },
  LIQUIDADOR: { icon: 'pi pi-chart-pie', severity: 'warn' },
  LEGAL: { icon: 'pi pi-bookmark', severity: 'danger' },
  FINANZAS: { icon: 'pi pi-dollar', severity: 'success' },
  TRANSPORTE: { icon: 'pi pi-truck', severity: 'warn' }
}

const roleSeverity = computed(() => {
  return roleConfig[props.roleName]?.severity || 'secondary'
})

const roleIcon = computed(() => {
  return roleConfig[props.roleName]?.icon || 'pi pi-user'
})
</script>

<style scoped>
.role-tag {
  display: inline-flex;
  align-items: center;
  gap: 0.35rem;
  padding: 0.25rem 0.65rem;
  font-size: 0.725rem;
  font-weight: 700;
  letter-spacing: 0.04em;
  text-transform: uppercase;
}

.role-tag i {
  font-size: 0.7rem;
}
</style>
