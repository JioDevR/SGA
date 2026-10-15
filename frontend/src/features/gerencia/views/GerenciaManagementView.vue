<template>
  <div class="gerencia-management-view">
    <!-- 01. Vista General (Dashboard Central de Métricas Ejecutivas) -->
    <AdminOverviewView
      v-if="navigationStore.gerenciaSubOption === 'overview'"
    />

    <!-- 02. Gestión Integral de Sedes y Agencias Territoriales -->
    <SedesManagementView
      v-else-if="navigationStore.gerenciaSubOption === 'sedes'"
    />

    <!-- 03. Vista de Catálogos Auxiliares (Puertos y Tipos de Carga) -->
    <CatalogosManagementView
      v-else-if="navigationStore.gerenciaSubOption === 'catalogos'"
    />
  </div>
</template>

<script setup>
import { defineAsyncComponent } from 'vue'
import { useNavigationStore } from '@/core/stores/navigationStore'

const AdminOverviewView = defineAsyncComponent(() => import('@/features/admin/overview/views/AdminOverviewView.vue'))
const SedesManagementView = defineAsyncComponent(() => import('@/features/admin/sedes/views/SedesManagementView.vue'))
const CatalogosManagementView = defineAsyncComponent(() => import('@/features/admin/catalogos/views/CatalogosManagementView.vue'))

const navigationStore = useNavigationStore()
</script>

<style scoped>
.gerencia-management-view {
  width: 100%;
  padding-bottom: 2rem;
}
</style>
