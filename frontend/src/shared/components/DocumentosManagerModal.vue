<template>
  <Dialog
    :visible="visible"
    modal
    :header="modalTitle"
    :style="{ width: '880px', maxWidth: '95vw' }"
    class="custom-sga-dialog"
    @update:visible="val => !val && handleClose()"
  >
    <div class="docs-manager-body">
      <!-- 1. Banner Superior Editorial -->
      <div class="docs-header-card">
        <div class="header-left">
          <span class="badge-kicker">GESTIÓN DOCUMENTAL // ALMACÉN DIGITAL</span>
          <div class="entity-title-row">
            <span class="entity-code font-mono">{{ codigoEntidad || 'General' }}</span>
            <span class="entity-name">{{ tituloEntidad || 'Documentos del Sistema' }}</span>
          </div>
          <span class="header-sub">
            Suba y consulte comprobantes, certificados, facturas o papeles de sustento operativo.
          </span>
        </div>
        <div class="header-right">
          <div class="doc-counter-box">
            <span class="counter-num">{{ documentos.length }}</span>
            <span class="counter-label">Archivos Adjuntos</span>
          </div>
        </div>
      </div>

      <!-- 2. Zona de Carga de Nuevos Documentos (Dropzone) -->
      <div class="upload-section-card">
        <div class="upload-controls-row">
          <div class="control-item">
            <label class="control-label" for="doc-cat">Categoría de Documento *</label>
            <select id="doc-cat" v-model="formCategoria" class="select-input">
              <option value="GENERAL">Documento General</option>
              <option value="FACTURA">Factura Comercial (Invoice)</option>
              <option value="PACKING_LIST">Lista de Empaque (Packing List)</option>
              <option value="TRANSPORTE_BL">Conocimiento de Embarque (B/L o Guía Aérea)</option>
              <option value="CERTIFICADO">Certificado de Origen / Calidad</option>
              <option value="PERMISO_ESPECIAL">Permiso Especial (DIGESA/SENASA/SUCAMEC)</option>
              <option value="IDENTIFICACION">Identificación / RUC / Ficha Técnica</option>
            </select>
          </div>

          <div class="control-item flex-grow">
            <label class="control-label" for="doc-desc">Descripción / Instrucciones (Opcional)</label>
            <input
              id="doc-desc"
              v-model.trim="formDescripcion"
              type="text"
              class="text-input"
              placeholder="Ej. Factura final visada con vistos buenos..."
              maxlength="200"
            />
          </div>
        </div>

        <!-- Dropzone Interactivo -->
        <div
          class="dropzone-area"
          :class="{ 'is-dragging': isDragging }"
          @dragover.prevent="isDragging = true"
          @dragleave.prevent="isDragging = false"
          @drop.prevent="handleFileDrop"
          @click="triggerFileInput"
        >
          <input
            ref="fileInputRef"
            type="file"
            multiple
            class="hidden-file-input"
            @change="handleFileSelect"
          />
          <div class="dropzone-content">
            <div class="dropzone-icon">
              <i class="pi pi-cloud-upload"></i>
            </div>
            <p class="dropzone-text">
              <strong>Haga clic para seleccionar archivos</strong> o arrástrelos aquí
            </p>
            <span class="dropzone-sub">
              Soporta PDF, imágenes (PNG, JPG), hojas de cálculo (XLSX), documentos Word y archivos comprimidos (hasta 25 MB c/u)
            </span>
          </div>
        </div>

        <!-- Lista de archivos en cola para subir -->
        <div v-if="queuedFiles.length > 0" class="queued-files-panel">
          <div class="queue-header">
            <span>Archivos listos para subir ({{ queuedFiles.length }}):</span>
            <button type="button" class="btn-clear-queue" @click="queuedFiles = []">
              Limpiar selección
            </button>
          </div>
          <div class="queued-list">
            <div v-for="(file, idx) in queuedFiles" :key="idx" class="queued-item">
              <div class="queued-item-left">
                <i :class="getFileIconClass(file.name)"></i>
                <span class="queued-name font-mono">{{ file.name }}</span>
                <span class="queued-size">({{ formatBytes(file.size) }})</span>
              </div>
              <button type="button" class="btn-remove-queued" @click="removeQueuedFile(idx)">
                <i class="pi pi-times"></i>
              </button>
            </div>
          </div>

          <div class="upload-action-row">
            <button
              type="button"
              class="btn-upload-submit"
              :disabled="uploading"
              @click="submitUpload"
            >
              <i v-if="uploading" class="pi pi-spin pi-spinner"></i>
              <i v-else class="pi pi-upload"></i>
              <span>{{ uploading ? 'Subiendo archivos...' : `Confirmar y Subir ${queuedFiles.length} archivo(s)` }}</span>
            </button>
          </div>
        </div>
      </div>

      <!-- 3. Lista de Documentos Guardados -->
      <div class="docs-list-section">
        <div class="list-section-header">
          <h3 class="list-title">Archivos Registrados en Repositorio</h3>
          <button type="button" class="btn-refresh-docs" :disabled="loading" @click="loadDocumentos">
            <i :class="['pi pi-sync', { 'pi-spin': loading }]"></i>
            <span>Actualizar</span>
          </button>
        </div>

        <!-- Estado de Carga -->
        <div v-if="loading" class="docs-loading-state">
          <i class="pi pi-spin pi-spinner"></i>
          <span>Consultando repositorio de documentos...</span>
        </div>

        <!-- Estado Vacío -->
        <div v-else-if="documentos.length === 0" class="docs-empty-state">
          <i class="pi pi-folder-open empty-icon"></i>
          <p class="empty-title">No hay documentos registrados para esta operación</p>
          <p class="empty-desc">Use la zona superior para adjuntar facturas, guías, certificados o papeles aduaneros.</p>
        </div>

        <!-- Tabla de Documentos -->
        <div v-else class="table-container">
          <DataTable
            :value="documentos"
            responsiveLayout="scroll"
            class="custom-datatable"
            dataKey="idDocumento"
          >
            <Column header="Documento / Nombre">
              <template #body="{ data: d }">
                <div class="doc-file-cell">
                  <div class="file-icon-box">
                    <i :class="getFileIconClass(d.nombreOriginal)"></i>
                  </div>
                  <div class="file-text-col">
                    <span class="file-name font-mono" :title="d.nombreOriginal">{{ d.nombreOriginal }}</span>
                    <span class="file-meta">{{ formatBytes(d.tamanoBytes) }} • {{ d.extension?.toUpperCase() }}</span>
                  </div>
                </div>
              </template>
            </Column>

            <Column header="Categoría">
              <template #body="{ data: d }">
                <Tag
                  :value="getCategoriaLabel(d.categoria)"
                  :severity="getCategoriaSeverity(d.categoria)"
                  rounded
                />
              </template>
            </Column>

            <Column header="Descripción / Notas">
              <template #body="{ data: d }">
                <span class="doc-desc-text">{{ d.descripcion || '—' }}</span>
              </template>
            </Column>

            <Column header="Subido por">
              <template #body="{ data: d }">
                <div class="user-cell">
                  <span class="user-name">{{ d.usuarioCreadorNombre || 'Usuario del Sistema' }}</span>
                  <span class="date-sub">{{ formatDateTime(d.fechaCreacion) }}</span>
                </div>
              </template>
            </Column>

            <Column header="Acciones" headerClass="text-right" bodyClass="text-right">
              <template #body="{ data: d }">
                <div class="actions-wrapper">
                  <!-- Botón Previsualizar -->
                  <button
                    v-if="canPreview(d.formatoMime)"
                    type="button"
                    class="action-btn-primary"
                    title="Previsualizar documento"
                    @click="previewFile(d)"
                  >
                    <i class="pi pi-eye"></i>
                    <span>Ver</span>
                  </button>

                  <!-- Botón Descargar -->
                  <button
                    type="button"
                    class="action-btn-outline"
                    title="Descargar archivo a su equipo"
                    @click="downloadFile(d)"
                  >
                    <i class="pi pi-download"></i>
                    <span>Bajar</span>
                  </button>

                  <!-- Botón Eliminar -->
                  <button
                    type="button"
                    class="action-btn-outline reject"
                    title="Eliminar documento del repositorio"
                    @click="confirmDelete(d)"
                  >
                    <i class="pi pi-trash"></i>
                  </button>
                </div>
              </template>
            </Column>
          </DataTable>
        </div>
      </div>
    </div>

    <!-- 4. Diálogo de Previsualización Embebida -->
    <Dialog
      v-model:visible="showPreviewModal"
      modal
      :header="`Vista Previa: ${previewDoc?.nombreOriginal || ''}`"
      :style="{ width: '85vw', maxWidth: '1000px', height: '85vh' }"
      class="custom-sga-dialog preview-dialog"
    >
      <div v-if="previewDoc && previewBlobUrl" class="preview-container">
        <iframe
          v-if="previewDoc.formatoMime?.includes('pdf')"
          :src="previewBlobUrl"
          class="preview-iframe"
          title="Vista Previa PDF"
        ></iframe>
        <img
          v-else-if="previewDoc.formatoMime?.includes('image')"
          :src="previewBlobUrl"
          class="preview-image"
          alt="Vista Previa de Imagen"
        />
        <div v-else class="preview-unsupported">
          <i class="pi pi-file empty-icon"></i>
          <p>Este formato no admite previsualización directa en el navegador.</p>
          <button type="button" class="btn-outline-warm" @click="downloadFile(previewDoc)">
            <i class="pi pi-download"></i>
            <span>Descargar archivo para ver</span>
          </button>
        </div>
      </div>
      <div v-else class="preview-loading">
        <i class="pi pi-spin pi-spinner"></i>
        <span>Cargando previsualización...</span>
      </div>
      <template #footer>
        <div class="dialog-actions-row">
          <button type="button" class="btn-outline-warm" @click="downloadFile(previewDoc)">
            <i class="pi pi-download"></i>
            <span>Descargar</span>
          </button>
          <button type="button" class="btn-outline-warm" @click="closePreview">
            <span>Cerrar</span>
          </button>
        </div>
      </template>
    </Dialog>

    <template #footer>
      <div class="dialog-actions-row">
        <button type="button" class="btn-outline-warm" @click="handleClose">
          <span>Listo / Cerrar</span>
        </button>
      </div>
    </template>
  </Dialog>
</template>

<script setup>
import { ref, watch, computed } from 'vue'
import Dialog from 'primevue/dialog'
import DataTable from 'primevue/datatable'
import Column from 'primevue/column'
import Tag from 'primevue/tag'
import { documentoService } from '@/features/documentos/services/documentoService'
import { useAuthStore } from '@/features/auth/stores/authStore'
import { useNotification } from '@/shared/composables/useNotification'
import { useConfirmation } from '@/shared/composables/useConfirmation'

const props = defineProps({
  visible: {
    type: Boolean,
    default: false
  },
  entidadTipo: {
    type: String,
    default: 'GENERAL' // 'COTIZACION' | 'EXPEDIENTE' | 'CLIENTE' | 'GENERAL'
  },
  idEntidad: {
    type: Number,
    default: null
  },
  entidadId: {
    type: Number,
    default: null
  },
  codigoEntidad: {
    type: String,
    default: ''
  },
  tituloEntidad: {
    type: String,
    default: ''
  }
})

const resolvedEntidadId = computed(() => {
  return props.entidadId !== null && props.entidadId !== undefined ? props.entidadId : props.idEntidad
})

const emit = defineEmits(['close', 'updated'])

const authStore = useAuthStore()
const { showSuccess, showError, showInfo } = useNotification()
const { confirmAction } = useConfirmation()

const loading = ref(false)
const uploading = ref(false)
const documentos = ref([])

// Formulario de Carga
const formCategoria = ref('GENERAL')
const formDescripcion = ref('')
const isDragging = ref(false)
const fileInputRef = ref(null)
const queuedFiles = ref([])

// Previsualización
const showPreviewModal = ref(false)
const previewDoc = ref(null)
const previewBlobUrl = ref(null)

const modalTitle = computed(() => {
  return `Expediente Documentario: ${props.codigoEntidad || 'General'}`
})

const triggerFileInput = () => {
  fileInputRef.value?.click()
}

const handleFileSelect = (e) => {
  const selected = Array.from(e.target.files || [])
  addFilesToQueue(selected)
  e.target.value = ''
}

const handleFileDrop = (e) => {
  isDragging.value = false
  const dropped = Array.from(e.dataTransfer.files || [])
  addFilesToQueue(dropped)
}

const addFilesToQueue = (files) => {
  const validFiles = files.filter(f => f.size <= 25 * 1024 * 1024)
  if (validFiles.length < files.length) {
    showError('Algunos archivos superan el límite de 25 MB y fueron omitidos.')
  }
  queuedFiles.value.push(...validFiles)
}

const removeQueuedFile = (index) => {
  queuedFiles.value.splice(index, 1)
}

const submitUpload = async () => {
  if (queuedFiles.value.length === 0) return

  uploading.value = true
  try {
    await documentoService.upload(queuedFiles.value, {
      categoria: formCategoria.value,
      descripcion: formDescripcion.value,
      entidadTipo: props.entidadTipo,
      idEntidad: resolvedEntidadId.value,
      idUsuario: authStore.user?.idUsuario || 1
    })

    showSuccess(`¡${queuedFiles.value.length} archivo(s) subido(s) con éxito!`)
    queuedFiles.value = []
    formDescripcion.value = ''
    await loadDocumentos()
    emit('updated')
  } catch (err) {
    showError(err.response?.data?.message || 'Error al subir los documentos.')
  } finally {
    uploading.value = false
  }
}

const loadDocumentos = async () => {
  loading.value = true
  try {
    const params = {
      entidadTipo: props.entidadTipo
    }
    if (props.entidadTipo === 'COTIZACION') {
      params.idCotizacion = resolvedEntidadId.value
    } else if (props.entidadTipo === 'EXPEDIENTE') {
      params.idExpediente = resolvedEntidadId.value
    } else if (props.entidadTipo === 'CLIENTE') {
      params.idCliente = resolvedEntidadId.value
    }

    const res = await documentoService.getDocumentos(params)
    documentos.value = res || []
  } catch (err) {
    showError('No se pudieron recuperar los documentos adjuntos.')
  } finally {
    loading.value = false
  }
}

const downloadFile = async (doc) => {
  try {
    await documentoService.downloadDocumento(doc.idDocumento, doc.nombreOriginal)
  } catch (err) {
    showError('Error al descargar el archivo.')
  }
}

const canPreview = (mime) => {
  if (!mime) return false
  return mime.includes('pdf') || mime.includes('image')
}

const previewFile = async (doc) => {
  previewDoc.value = doc
  previewBlobUrl.value = null
  showPreviewModal.value = true

  try {
    const res = await documentoService.getPreviewBlobUrl(doc.idDocumento)
    previewBlobUrl.value = res.url
  } catch (err) {
    showError('No se pudo generar la previsualización del documento.')
    showPreviewModal.value = false
  }
}

const closePreview = () => {
  if (previewBlobUrl.value) {
    window.URL.revokeObjectURL(previewBlobUrl.value)
    previewBlobUrl.value = null
  }
  showPreviewModal.value = false
  previewDoc.value = null
}

const confirmDelete = async (doc) => {
  const confirmed = await confirmAction(
    `¿Desea eliminar el archivo "${doc.nombreOriginal}" del repositorio?`,
    'Eliminar Documento'
  )
  if (!confirmed) return

  try {
    await documentoService.deleteDocumento(doc.idDocumento)
    showInfo(`Archivo "${doc.nombreOriginal}" eliminado.`)
    await loadDocumentos()
    emit('updated')
  } catch (err) {
    showError('No se pudo eliminar el documento.')
  }
}

const handleClose = () => {
  queuedFiles.value = []
  emit('close')
}

watch(
  () => [props.visible, resolvedEntidadId.value],
  ([val]) => {
    if (val) {
      queuedFiles.value = []
      formDescripcion.value = ''
      loadDocumentos()
    }
  }
)

// Helpers de Estilo
const getFileIconClass = (filename) => {
  if (!filename) return 'pi pi-file'
  const ext = filename.split('.').pop()?.toLowerCase()
  switch (ext) {
    case 'pdf':
      return 'pi pi-file-pdf text-danger'
    case 'xls':
    case 'xlsx':
    case 'csv':
      return 'pi pi-file-excel text-success'
    case 'doc':
    case 'docx':
      return 'pi pi-file-word text-info'
    case 'png':
    case 'jpg':
    case 'jpeg':
    case 'webp':
      return 'pi pi-image text-accent'
    case 'zip':
    case 'rar':
      return 'pi pi-box'
    default:
      return 'pi pi-file'
  }
}

const getCategoriaLabel = (cat) => {
  const map = {
    GENERAL: 'General',
    FACTURA: 'Factura Comercial',
    PACKING_LIST: 'Packing List',
    TRANSPORTE_BL: 'B/L / AWB',
    CERTIFICADO: 'Certificado de Origen',
    PERMISO_ESPECIAL: 'Permiso Especial',
    IDENTIFICACION: 'Identificación / RUC'
  }
  return map[cat] || cat
}

const getCategoriaSeverity = (cat) => {
  switch (cat) {
    case 'FACTURA':
      return 'success'
    case 'TRANSPORTE_BL':
      return 'info'
    case 'PERMISO_ESPECIAL':
      return 'warn'
    case 'CERTIFICADO':
      return 'contrast'
    default:
      return 'secondary'
  }
}

const formatBytes = (bytes) => {
  if (!bytes) return '0 B'
  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return parseFloat((bytes / Math.pow(k, i)).toFixed(1)) + ' ' + sizes[i]
}

const formatDateTime = (val) => {
  if (!val) return '—'
  try {
    const d = new Date(val)
    if (isNaN(d.getTime())) return val
    return d.toLocaleDateString('es-PE', {
      day: '2-digit',
      month: '2-digit',
      year: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    })
  } catch {
    return val
  }
}
</script>

<style scoped>
.docs-manager-body {
  display: flex;
  flex-direction: column;
  gap: 1.25rem;
  padding: 0.25rem 0;
}

.docs-header-card {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 1.25rem;
  padding: 1.25rem 1.5rem;
  background: var(--color-bg-subtle, #f5f2eb);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-lg, 12px);
  flex-wrap: wrap;
}

.badge-kicker {
  font-size: 0.6875rem;
  font-weight: 800;
  letter-spacing: 0.08em;
  color: var(--color-accent-kicker, #9a7f60);
  display: block;
  margin-bottom: 0.25rem;
}

.entity-title-row {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  flex-wrap: wrap;
}

.entity-code {
  font-size: 1.25rem;
  font-weight: 800;
  color: var(--color-text-main);
}

.entity-name {
  font-size: 0.9375rem;
  color: var(--color-text-muted);
}

.header-sub {
  font-size: 0.75rem;
  color: var(--color-text-muted);
  display: block;
  margin-top: 0.25rem;
}

.doc-counter-box {
  display: flex;
  flex-direction: column;
  align-items: center;
  background: var(--color-bg-surface, #ffffff);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-md, 8px);
  padding: 0.5rem 1rem;
}

.counter-num {
  font-family: var(--font-mono, monospace);
  font-size: 1.375rem;
  font-weight: 800;
  color: var(--color-accent-kicker, #c2410c);
}

.counter-label {
  font-size: 0.6875rem;
  font-weight: 700;
  color: var(--color-text-subtle);
  letter-spacing: 0.05em;
}

.upload-section-card {
  background: var(--color-bg-surface, #ffffff);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-md, 8px);
  padding: 1rem 1.25rem;
}

.upload-controls-row {
  display: flex;
  gap: 1rem;
  margin-bottom: 1rem;
  flex-wrap: wrap;
}

.control-item {
  display: flex;
  flex-direction: column;
  gap: 0.35rem;
}

.control-item.flex-grow {
  flex: 1;
  min-width: 250px;
}

.control-label {
  font-size: 0.75rem;
  font-weight: 700;
  color: var(--color-text-main);
}

.dropzone-area {
  border: 2px dashed var(--color-border-strong, #c4bcab);
  background: var(--color-bg-subtle, #f9f7f2);
  border-radius: var(--radius-md, 8px);
  padding: 1.5rem 1rem;
  text-align: center;
  cursor: pointer;
  transition: all 0.2s ease;
}

.dropzone-area:hover,
.dropzone-area.is-dragging {
  border-color: var(--color-primary, #352f2a);
  background: var(--color-bg-muted, #ede6db);
}

.hidden-file-input {
  display: none;
}

.dropzone-icon {
  font-size: 2.25rem;
  color: var(--color-accent-kicker, #9a7f60);
  margin-bottom: 0.5rem;
}

.dropzone-text {
  font-size: 0.875rem;
  color: var(--color-text-main);
  margin: 0;
}

.dropzone-sub {
  font-size: 0.75rem;
  color: var(--color-text-muted);
  display: block;
  margin-top: 0.35rem;
}

.queued-files-panel {
  margin-top: 1rem;
  background: var(--color-bg-subtle, #f5f2eb);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-md, 8px);
  padding: 0.75rem 1rem;
}

.queue-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 0.75rem;
  font-weight: 700;
  color: var(--color-text-main);
  margin-bottom: 0.5rem;
}

.btn-clear-queue {
  background: none;
  border: none;
  color: var(--color-danger-text, #9e2a2b);
  font-size: 0.75rem;
  cursor: pointer;
}

.queued-list {
  display: flex;
  flex-direction: column;
  gap: 0.35rem;
  max-height: 140px;
  overflow-y: auto;
}

.queued-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: var(--color-bg-surface);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-sm, 6px);
  padding: 0.35rem 0.6rem;
  font-size: 0.8125rem;
}

.queued-item-left {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  overflow: hidden;
}

.queued-name {
  font-weight: 600;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  max-width: 380px;
}

.queued-size {
  color: var(--color-text-subtle);
  font-size: 0.75rem;
}

.btn-remove-queued {
  background: none;
  border: none;
  color: var(--color-text-muted);
  cursor: pointer;
  padding: 0.2rem;
}

.upload-action-row {
  margin-top: 0.75rem;
  display: flex;
  justify-content: flex-end;
}

.btn-upload-submit {
  display: inline-flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.5rem 1.25rem;
  background: var(--color-primary, #352f2a);
  color: #fff;
  border: none;
  border-radius: var(--radius-md, 8px);
  font-weight: 700;
  font-size: 0.875rem;
  cursor: pointer;
  transition: all 0.2s ease;
}

.btn-upload-submit:hover:not(:disabled) {
  background: var(--color-primary-hover, #484039);
  transform: translateY(-1px);
}

.docs-list-section {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
}

.list-section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.list-title {
  font-size: 0.9375rem;
  font-weight: 700;
  color: var(--color-text-main);
  margin: 0;
}

.btn-refresh-docs {
  display: inline-flex;
  align-items: center;
  gap: 0.35rem;
  background: none;
  border: 1px solid var(--color-border);
  border-radius: var(--radius-sm, 6px);
  padding: 0.35rem 0.6rem;
  font-size: 0.75rem;
  color: var(--color-text-main);
  cursor: pointer;
}

.docs-loading-state,
.docs-empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 2.5rem 1rem;
  background: var(--color-bg-surface);
  border: 1px dashed var(--color-border);
  border-radius: var(--radius-md, 8px);
  text-align: center;
  gap: 0.5rem;
  color: var(--color-text-muted);
}

.empty-icon {
  font-size: 2.25rem;
  color: var(--color-text-subtle);
}

.empty-title {
  font-weight: 700;
  margin: 0;
  color: var(--color-text-main);
}

.empty-desc {
  font-size: 0.8125rem;
  margin: 0;
}

.doc-file-cell {
  display: flex;
  align-items: center;
  gap: 0.75rem;
}

.file-icon-box {
  width: 2.25rem;
  height: 2.25rem;
  border-radius: var(--radius-sm, 6px);
  background: var(--color-bg-subtle);
  border: 1px solid var(--color-border);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1.125rem;
  flex-shrink: 0;
}

.file-text-col {
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.file-name {
  font-weight: 700;
  font-size: 0.8125rem;
  color: var(--color-text-main);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  max-width: 220px;
}

.file-meta {
  font-size: 0.6875rem;
  color: var(--color-text-subtle);
}

.doc-desc-text {
  font-size: 0.8125rem;
  color: var(--color-text-muted);
}

.user-cell {
  display: flex;
  flex-direction: column;
}

.user-name {
  font-size: 0.75rem;
  font-weight: 600;
  color: var(--color-text-main);
}

.date-sub {
  font-size: 0.6875rem;
  color: var(--color-text-subtle);
}

.preview-container {
  width: 100%;
  height: 65vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #2b2b2b;
  border-radius: var(--radius-md, 8px);
  overflow: hidden;
}

.preview-iframe {
  width: 100%;
  height: 100%;
  border: none;
}

.preview-image {
  max-width: 100%;
  max-height: 100%;
  object-fit: contain;
}

.preview-unsupported,
.preview-loading {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 1rem;
  color: #fff;
}

.dialog-actions-row {
  display: flex;
  justify-content: flex-end;
  gap: 0.75rem;
}

.text-danger {
  color: var(--color-danger-text, #9e2a2b);
}

.text-success {
  color: var(--color-success-text, #1e6638);
}

.text-info {
  color: var(--color-info-text, #21598f);
}

.text-accent {
  color: var(--color-accent-copper, #c2410c);
}
</style>
