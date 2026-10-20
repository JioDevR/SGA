<template>
  <div class="supervision-view">
    <!-- 1. Hero Banner Editorial Cálido Reutilizable -->
    <HeroBannerCard
      kicker="SUPERVISIÓN ADUANERA // AUDITORÍA & CONTROL OPERATIVO"
      title="Bandeja de Supervisión Aduanera y Gestión de Expedientes"
      description="Auditoría técnica de propuestas comerciales, control de partidas arancelarias, validación de regímenes aduaneros, apertura de Expedientes Aduaneros y control de canales SUNAT."
    />

    <!-- 2. Métricas Dinámicas según Pestaña Activa Reutilizando MetricCard -->
    <div v-if="activeTab === 'cotizaciones'" class="metrics-grid">
      <MetricCard index="01" label="Por Revisar (Enviadas)" :value="enviadasCount" severity="pending" highlight />
      <MetricCard index="02" label="En Observación (Revisión)" :value="revisionCount" severity="warning" />
      <MetricCard index="03" label="Cotizaciones Aprobadas" :value="aprobadasCount" severity="success" />
      <MetricCard index="04" label="Pendientes de Expediente" :value="cotizacionesAprobadasSinExpediente.length" severity="accent" />
    </div>

    <div v-else class="metrics-grid">
      <MetricCard index="01" label="Expedientes Registrados" :value="expedientes.length" severity="pending" highlight />
      <MetricCard index="02" label="Canal SUNAT Pendiente" :value="expedientesPendienteCanalCount" severity="warning" />
      <MetricCard index="03" label="Con Canal Asignado" :value="expedientesConCanalCount" severity="accent" />
      <MetricCard index="04" label="Levante Autorizado" :value="expedientesConLevanteCount" severity="success" />
    </div>

    <!-- 3. Barra de Navegación de Pestañas Principales -->
    <div class="tabs-nav-bar">
      <button
        type="button"
        :class="['tab-btn', { active: activeTab === 'cotizaciones' }]"
        @click="switchTab('cotizaciones')"
      >
        <i class="pi pi-inbox"></i>
        <span>Bandeja de Cotizaciones ({{ cotizaciones.length }})</span>
      </button>

      <button
        type="button"
        :class="['tab-btn', { active: activeTab === 'expedientes' }]"
        @click="switchTab('expedientes')"
      >
        <i class="pi pi-folder-open"></i>
        <span>Gestión de Expedientes Aduaneros ({{ expedientes.length }})</span>
      </button>
    </div>

    <!-- ============================================================== -->
    <!-- PESTAÑA 1: BANDEJA DE COTIZACIONES ADUANERAS                   -->
    <!-- ============================================================== -->
    <div v-if="activeTab === 'cotizaciones'" class="tab-content-section">
      <div class="section-header-row">
        <div>
          <span class="badge-kicker">CONTROL ADUANERO</span>
          <h3 class="section-title">Expedientes de Cotización para Revisión Técnica</h3>
        </div>

        <div class="section-controls">
          <!-- Filtro por Estado -->
          <select v-model="filterEstado" class="filter-select">
            <option value="">Todos los Estados</option>
            <option value="PENDIENTE">Por Revisar (Pendientes)</option>
            <option value="OBSERVADA">En Observación (Observadas)</option>
            <option value="APROBADA">Aprobadas</option>
            <option value="RECHAZADA">Rechazadas</option>
          </select>

          <!-- Búsqueda Rápida -->
          <div class="search-input-wrap">
            <i class="pi pi-search search-icon"></i>
            <input
              v-model="searchQuery"
              type="text"
              class="search-input"
              placeholder="Buscar por código, cliente o RUC..."
            />
          </div>

          <!-- Botón de Recarga -->
          <button type="button" class="btn-outline-warm" @click="loadData" :disabled="loading">
            <i :class="['pi pi-sync', { 'pi-spin': loading }]"></i>
            <span>Actualizar</span>
          </button>
        </div>
      </div>

      <!-- Tabla de Cotizaciones -->
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
              <i class="pi pi-check-circle empty-icon"></i>
              <p class="empty-title">No hay cotizaciones con este criterio</p>
              <p class="empty-desc">Las cotizaciones emitidas por los ejecutivos de ventas aparecerán aquí para su validación.</p>
            </div>
          </template>

          <template #loading>
            <div class="loading-state">
              <i class="pi pi-spin pi-spinner"></i>
              <span>Cargando expedientes aduaneros...</span>
            </div>
          </template>

          <Column header="N° Cotización">
            <template #body="{ data: c }">
              <div class="code-column">
                <span class="doc-code">{{ c.codigoCotizacion }}</span>
                <span class="date-sub">{{ c.fechaEmision }}</span>
              </div>
            </template>
          </Column>

          <Column header="Cliente / RUC">
            <template #body="{ data: c }">
              <div class="client-cell">
                <span class="client-name">{{ c.clienteRazonSocial || 'Cliente no asignado' }}</span>
                <span class="client-sub">RUC/Doc: {{ c.clienteNumeroDocumento }}</span>
              </div>
            </template>
          </Column>

          <Column header="Comercial">
            <template #body="{ data: c }">
              <span class="comercial-name">{{ c.usuarioComercialNombre || 'Asesor Comercial' }}</span>
            </template>
          </Column>

          <Column header="Régimen Aduanero">
            <template #body="{ data: c }">
              <span class="regimen-tag">{{ formatRegimen(c.regimenAduanero) }}</span>
            </template>
          </Column>

          <Column header="Total Cotizado">
            <template #body="{ data: c }">
              <span class="total-amount">${{ Number(c.totalCotizado || 0).toLocaleString('en-US', { minimumFractionDigits: 2 }) }}</span>
            </template>
          </Column>

          <Column header="Estado">
            <template #body="{ data: c }">
              <Tag
                :value="getEstadoLabel(c.estado)"
                :severity="getEstadoSeverity(c.estado)"
                rounded
              />
            </template>
          </Column>

          <Column header="Expediente Aduanero">
            <template #body="{ data: c }">
              <div v-if="c.tieneExpediente" class="exp-linked-tag">
                <i class="pi pi-folder"></i>
                <span>{{ c.codigoExpediente }}</span>
              </div>
              <span v-else-if="c.estado === 'APROBADA'" class="text-muted-dash">Listo para Expediente</span>
              <span v-else class="text-muted-dash">—</span>
            </template>
          </Column>

          <Column header="Acciones de Supervisión" headerClass="text-right" bodyClass="text-right">
            <template #body="{ data: c }">
              <div class="actions-wrapper">
                <!-- Botón de Inspección Completa -->
                <button
                  type="button"
                  class="action-btn-primary"
                  title="Auditar y Ver Detalle Completo"
                  @click="openInspectModal(c)"
                >
                  <i class="pi pi-eye"></i>
                  <span>Auditar</span>
                </button>

                <!-- Botón de Documentos Adjuntos -->
                <button
                  type="button"
                  class="action-btn-outline"
                  title="Gestionar Documentos y Anexos"
                  @click="openDocumentosCotizacion(c)"
                >
                  <i class="pi pi-paperclip"></i>
                  <span>Docs</span>
                </button>

                <!-- Botón Rápido de Aprobación -->
                <button
                  v-if="c.estado === 'PENDIENTE' || c.estado === 'EN_REVISION' || c.estado === 'ENVIADA' || c.estado === 'REVISION' || c.estado === 'OBSERVADA'"
                  type="button"
                  class="action-btn-outline approve"
                  title="Aprobar técnicamente para apertura de Expediente Aduanero"
                  @click="confirmApprove(c)"
                >
                  <i class="pi pi-check"></i>
                  <span>Aprobar</span>
                </button>

                <!-- Botón Rápido de Observación / Revisión -->
                <button
                  v-if="c.estado === 'PENDIENTE' || c.estado === 'EN_REVISION' || c.estado === 'ENVIADA'"
                  type="button"
                  class="action-btn-outline observe"
                  title="Devolver para Revisión a Ventas"
                  @click="openObservationModal(c)"
                >
                  <i class="pi pi-file-edit"></i>
                  <span>Observar</span>
                </button>

                <!-- Botón Rápido de Rechazo -->
                <button
                  v-if="c.estado === 'PENDIENTE' || c.estado === 'EN_REVISION' || c.estado === 'ENVIADA' || c.estado === 'REVISION' || c.estado === 'OBSERVADA'"
                  type="button"
                  class="action-btn-outline reject"
                  title="Rechazar Cotización"
                  @click="openRejectModal(c)"
                >
                  <i class="pi pi-times"></i>
                </button>

                <!-- Botón de Apertura de Expediente si está Aprobada y no tiene Expediente -->
                <button
                  v-if="c.estado === 'APROBADA' && !c.tieneExpediente"
                  type="button"
                  class="action-btn-outline"
                  style="border-color: var(--color-accent-copper, #c2410c); color: var(--color-accent-copper, #c2410c); font-weight: 700;"
                  title="Aperturar Expediente Aduanero para esta cotización"
                  @click="openAperturaModal(c)"
                >
                  <i class="pi pi-folder-open"></i>
                  <span>+ Expediente</span>
                </button>
              </div>
            </template>
          </Column>
        </DataTable>
      </div>
    </div>

    <!-- ============================================================== -->
    <!-- PESTAÑA 2: GESTIÓN INTEGRAL DE EXPEDIENTES ADUANEROS            -->
    <!-- ============================================================== -->
    <div v-else-if="activeTab === 'expedientes'" class="tab-content-section">
      <!-- Alerta de Cotizaciones Aprobadas Pendientes de Apertura -->
      <div v-if="cotizacionesAprobadasSinExpediente.length > 0" class="exp-pending-alert-banner">
        <div class="alert-icon-box">
          <i class="pi pi-bell"></i>
        </div>
        <div class="alert-text-content">
          <strong>{{ cotizacionesAprobadasSinExpediente.length }} Cotización(es) Aprobada(s) Pendiente(s) de Expediente</strong>
          <p>Existen propuestas comerciales con aprobación técnica formal listas para generar su expediente aduanero operativo.</p>
        </div>
        <button
          type="button"
          class="btn-alert-action"
          @click="openAperturaModal(null)"
        >
          <i class="pi pi-plus-circle"></i>
          <span>Aperturar Expediente Ahora</span>
        </button>
      </div>

      <div class="section-header-row">
        <div>
          <span class="badge-kicker">OPERACIONES ADUANERAS // CONTROL DAM & LEVANTE</span>
          <h3 class="section-title">Directorio y Gestión Integral de Expedientes Aduaneros</h3>
        </div>

        <div class="section-controls">
          <!-- Filtro por Canal SUNAT -->
          <select v-model="filterCanalExpediente" class="filter-select">
            <option value="">Todos los Canales SUNAT</option>
            <option value="PENDIENTE">Canal Pendiente</option>
            <option value="VERDE">Canal VERDE (Levante Inmediato)</option>
            <option value="NARANJA">Canal NARANJA (Documentario)</option>
            <option value="ROJO">Canal ROJO (Reconocimiento Físico)</option>
          </select>

          <!-- Filtro por Estado de Expediente -->
          <select v-model="filterEstadoExpediente" class="filter-select">
            <option value="">Todos los Estados</option>
            <option value="ASIGNADO">Asignado</option>
            <option value="EN_TRAMITE">En Trámite</option>
            <option value="CON_CANAL">Con Canal Asignado</option>
            <option value="LEVANTE_AUTORIZADO">Levante Autorizado</option>
            <option value="CERRADO">Cerrado / Concluido</option>
            <option value="ANULADO">Anulado</option>
          </select>

          <!-- Búsqueda Rápida de Expedientes -->
          <div class="search-input-wrap">
            <i class="pi pi-search search-icon"></i>
            <input
              v-model="searchExpedienteQuery"
              type="text"
              class="search-input"
              placeholder="Buscar por código, DAM, cliente o contenedor..."
            />
          </div>

          <!-- Botón de Recarga -->
          <button type="button" class="btn-outline-warm" @click="loadData" :disabled="loading">
            <i :class="['pi pi-sync', { 'pi-spin': loading }]"></i>
            <span>Actualizar</span>
          </button>

          <!-- Botón Principal: + Aperturar Expediente -->
          <button
            type="button"
            class="btn-primary-action"
            @click="openAperturaModal(null)"
            title="Aperturar un nuevo expediente aduanero a partir de una cotización aprobada"
          >
            <i class="pi pi-plus"></i>
            <span>+ Aperturar Expediente</span>
          </button>
        </div>
      </div>

      <!-- Tabla de Expedientes Aduaneros -->
      <div class="table-container">
        <DataTable
          :value="filteredExpedientes"
          :loading="loading"
          responsiveLayout="scroll"
          class="custom-datatable"
          dataKey="idExpediente"
        >
          <template #empty>
            <div class="empty-state">
              <i class="pi pi-folder-open empty-icon"></i>
              <p class="empty-title">No hay expedientes aduaneros registrados</p>
              <p class="empty-desc">Los Expedientes se generan a partir de cotizaciones comerciales aprobadas.</p>
            </div>
          </template>

          <template #loading>
            <div class="loading-state">
              <i class="pi pi-spin pi-spinner"></i>
              <span>Cargando expedientes aduaneros...</span>
            </div>
          </template>

          <Column header="Código Expediente">
            <template #body="{ data: exp }">
              <div class="exp-code-badge" @click="openDetalleExpediente(exp)" title="Clic para ver ficha completa">
                <i class="pi pi-folder"></i>
                <span class="font-bold font-mono">{{ exp.codigoExpediente }}</span>
              </div>
            </template>
          </Column>

          <Column header="Cotización / Régimen">
            <template #body="{ data: exp }">
              <div class="client-cell">
                <span class="doc-code">{{ exp.codigoCotizacion }}</span>
                <span class="client-sub">{{ formatRegimen(exp.regimenAduanero) }}</span>
              </div>
            </template>
          </Column>

          <Column header="Cliente">
            <template #body="{ data: exp }">
              <div class="client-cell">
                <span class="client-name">{{ exp.clienteRazonSocial || '—' }}</span>
                <span class="client-sub">Doc: {{ exp.clienteNumeroDocumento }}</span>
              </div>
            </template>
          </Column>

          <Column header="DAM & Canal SUNAT">
            <template #body="{ data: exp }">
              <div class="client-cell">
                <span class="font-mono text-xs font-bold">{{ exp.numeroDam || 'Sin DAM aún' }}</span>
                <Tag
                  :value="exp.canalSunat || 'PENDIENTE'"
                  :severity="getCanalSeverity(exp.canalSunat)"
                  rounded
                />
              </div>
            </template>
          </Column>

          <Column header="Contenedor / Nave">
            <template #body="{ data: exp }">
              <div class="client-cell">
                <span class="font-mono text-xs font-bold">{{ exp.numeroContenedor || '—' }}</span>
                <span class="client-sub">{{ exp.naveViaje || 'Sin nave asignada' }}</span>
              </div>
            </template>
          </Column>

          <Column header="Estado Trámite">
            <template #body="{ data: exp }">
              <Tag
                :value="exp.estadoExpediente || 'ASIGNADO'"
                :severity="getExpedienteSeverity(exp.estadoExpediente)"
                rounded
              />
            </template>
          </Column>

          <Column header="Levante">
            <template #body="{ data: exp }">
              <span v-if="exp.fechaLevanteAutorizacion" class="text-success font-bold text-xs">
                <i class="pi pi-check-circle"></i> Autorizado
              </span>
              <span v-else class="text-muted-dash">Pendiente</span>
            </template>
          </Column>

          <Column header="Acciones de Gestión" headerClass="text-right" bodyClass="text-right">
            <template #body="{ data: exp }">
              <div class="actions-wrapper">
                <button
                  type="button"
                  class="action-btn-primary"
                  title="Ver Ficha y Auditoría Completa del Expediente"
                  @click="openDetalleExpediente(exp)"
                >
                  <i class="pi pi-eye"></i>
                  <span>Detalle</span>
                </button>

                <button
                  type="button"
                  class="action-btn-outline"
                  title="Gestionar Documentos del Expediente (BL, Factura, DAM, etc.)"
                  @click="openDocumentosExpediente(exp)"
                >
                  <i class="pi pi-paperclip"></i>
                  <span>Docs</span>
                </button>

                <button
                  type="button"
                  class="action-btn-outline"
                  title="Registrar Canal SUNAT y Declaración DAM"
                  @click="openCanalModal(exp)"
                >
                  <i class="pi pi-compass"></i>
                  <span>DAM/Canal</span>
                </button>

                <button
                  v-if="!exp.fechaLevanteAutorizacion"
                  type="button"
                  class="action-btn-outline approve"
                  title="Autorizar Levante Aduanero"
                  @click="openLevanteModal(exp)"
                >
                  <i class="pi pi-shield"></i>
                  <span>Levante</span>
                </button>
              </div>
            </template>
          </Column>
        </DataTable>
      </div>
    </div>

    <!-- ============================================================== -->
    <!-- MODAL 1: AUDITORÍA & INSPECCIÓN COMPLETA DE COTIZACIÓN         -->
    <!-- ============================================================== -->
    <Dialog
      v-model:visible="showInspectModal"
      modal
      :header="`Auditoría Técnica Aduanera: ${inspectedCotizacion?.codigoCotizacion || ''}`"
      :style="{ width: '820px', maxWidth: '95vw' }"
      class="custom-sga-dialog"
    >
      <div v-if="inspectedCotizacion" class="inspection-body">
        <!-- Banner de Estado Superior -->
        <div :class="['inspection-status-banner', `banner-${inspectedCotizacion.estado?.toLowerCase()}`]">
          <div class="status-banner-info">
            <span class="status-banner-kicker">ESTADO ACTUAL DEL EXPEDIENTE</span>
            <div class="status-banner-badge-row">
              <Tag
                :value="getEstadoLabel(inspectedCotizacion.estado)"
                :severity="getEstadoSeverity(inspectedCotizacion.estado)"
                rounded
              />
              <span v-if="inspectedCotizacion.tieneExpediente" class="exp-badge-header">
                <i class="pi pi-check-circle"></i> Expediente Aduanero: {{ inspectedCotizacion.codigoExpediente }}
              </span>
            </div>
          </div>
          <div class="status-banner-code">
            <span>{{ inspectedCotizacion.codigoCotizacion }}</span>
          </div>
        </div>

        <!-- Banner de Observación si aplica -->
        <div v-if="inspectedCotizacion.motivoObservacion" class="observation-history-box">
          <i class="pi pi-exclamation-triangle"></i>
          <div>
            <strong>Nota / Observación Registrada:</strong>
            <p>{{ inspectedCotizacion.motivoObservacion }}</p>
          </div>
        </div>

        <!-- Ficha de Datos en Grilla -->
        <div class="inspection-sections">
          <!-- 1. Datos del Solicitante / Cliente -->
          <div class="inspect-card">
            <div class="inspect-card-header">
              <i class="pi pi-building"></i>
              <span>1. Cliente & Solicitante</span>
            </div>
            <div class="inspect-card-content">
              <div class="inspect-field">
                <span class="inspect-label">Razón Social:</span>
                <span class="inspect-value strong">{{ inspectedCotizacion.clienteRazonSocial || '—' }}</span>
              </div>
              <div class="inspect-field">
                <span class="inspect-label">RUC / Documento:</span>
                <span class="inspect-value">{{ inspectedCotizacion.clienteNumeroDocumento || '—' }}</span>
              </div>
              <div class="inspect-field">
                <span class="inspect-label">Asesor Comercial Emisor:</span>
                <span class="inspect-value">{{ inspectedCotizacion.usuarioComercialNombre || 'Asignado' }}</span>
              </div>
            </div>
          </div>

          <!-- 2. Parámetros Técnicos Aduaneros -->
          <div class="inspect-card">
            <div class="inspect-card-header">
              <i class="pi pi-globe"></i>
              <span>2. Parámetros Técnicos Aduaneros</span>
            </div>
            <div class="inspect-card-content">
              <div class="inspect-field">
                <span class="inspect-label">Régimen Aduanero:</span>
                <span class="inspect-value regimen-badge-value">{{ formatRegimen(inspectedCotizacion.regimenAduanero) }}</span>
              </div>
              <div class="inspect-field">
                <span class="inspect-label">Fecha de Emisión:</span>
                <span class="inspect-value">{{ inspectedCotizacion.fechaEmision }}</span>
              </div>
              <div class="inspect-field">
                <span class="inspect-label">Validez de Oferta:</span>
                <span class="inspect-value">{{ inspectedCotizacion.validezDias }} días calendario</span>
              </div>
            </div>
          </div>
        </div>

        <!-- 3. Desglose Financiero Liquidado -->
        <div class="inspect-financial-card">
          <div class="financial-card-header">
            <i class="pi pi-dollar"></i>
            <span>3. Liquidación Económica Estimada (USD)</span>
          </div>
          <div class="financial-grid">
            <div class="financial-col">
              <span class="fin-label">Flete Estimado</span>
              <span class="fin-amount">${{ Number(inspectedCotizacion.montoFleteEstimado || 0).toLocaleString('en-US', { minimumFractionDigits: 2 }) }}</span>
            </div>
            <div class="financial-col">
              <span class="fin-label">Honorarios Agencia</span>
              <span class="fin-amount">${{ Number(inspectedCotizacion.montoHonorariosEstimado || 0).toLocaleString('en-US', { minimumFractionDigits: 2 }) }}</span>
            </div>
            <div class="financial-col">
              <span class="fin-label">Tributos & Aranceles SUNAT</span>
              <span class="fin-amount">${{ Number(inspectedCotizacion.montoTributosEstimado || 0).toLocaleString('en-US', { minimumFractionDigits: 2 }) }}</span>
            </div>
            <div class="financial-col total-col">
              <span class="fin-label">TOTAL COTIZADO</span>
              <span class="fin-amount total-amount-big">${{ Number(inspectedCotizacion.totalCotizado || 0).toLocaleString('en-US', { minimumFractionDigits: 2 }) }}</span>
            </div>
          </div>
        </div>

        <!-- 4. Observaciones Generales de la Propuesta -->
        <div class="inspect-obs-section">
          <span class="inspect-label">Observaciones y Condiciones Especiales:</span>
          <p class="obs-full-text">{{ inspectedCotizacion.observaciones || 'Sin observaciones adicionales registradas.' }}</p>
        </div>
      </div>

      <template #footer>
        <div class="dialog-actions-row">
          <button
            type="button"
            class="btn-outline-warm"
            style="border-color: var(--color-accent-copper, #c2410c); color: var(--color-accent-copper, #c2410c);"
            @click="openDocumentosCotizacion(inspectedCotizacion)"
          >
            <i class="pi pi-paperclip"></i>
            <span>Documentos</span>
          </button>

          <button type="button" class="btn-outline-warm" @click="showInspectModal = false">
            <span>Cerrar</span>
          </button>

          <!-- Acciones operativas si no está aprobada -->
          <template v-if="inspectedCotizacion?.estado !== 'APROBADA'">
            <button
              type="button"
              class="btn-danger-action"
              @click="openRejectModal(inspectedCotizacion)"
            >
              <i class="pi pi-times"></i>
              <span>Rechazar</span>
            </button>

            <button
              type="button"
              class="btn-warm-action"
              @click="openObservationModal(inspectedCotizacion)"
            >
              <i class="pi pi-file-edit"></i>
              <span>Solicitar Revisión</span>
            </button>

            <button
              type="button"
              class="btn-espresso-success"
              @click="confirmApprove(inspectedCotizacion)"
            >
              <i class="pi pi-check"></i>
              <span>Aprobar y Generar OS</span>
            </button>
          </template>
        </div>
      </template>
    </Dialog>

    <!-- ============================================================== -->
    <!-- MODAL 2: SOLICITAR REVISIÓN / OBSERVACIÓN                      -->
    <!-- ============================================================== -->
    <Dialog
      v-model:visible="showObservationDialog"
      modal
      header="Solicitar Corrección a Ventas (Revisión)"
      :style="{ width: '540px', maxWidth: '95vw' }"
      class="custom-sga-dialog"
    >
      <div class="modal-form-content">
        <p class="modal-desc">
          Especifique las observaciones técnicas o financieras para que el asesor comercial corrija la cotización
          <strong>{{ actionCotizacion?.codigoCotizacion }}</strong>. Una vez corregida, volverá automáticamente a su bandeja para aprobación.
        </p>

        <div class="form-group">
          <label class="form-label">Detalle de Observación / Motivo de Revisión:</label>
          <Textarea
            v-model="observationReason"
            rows="4"
            class="custom-textarea"
            placeholder="Ejemplo: Rectificar partida arancelaria para régimen definitivo o adjuntar póliza de flete estimada..."
          />
        </div>
      </div>

      <template #footer>
        <div class="dialog-actions-row">
          <button type="button" class="btn-outline-warm" @click="showObservationDialog = false">
            <span>Cancelar</span>
          </button>
          <button
            type="button"
            class="btn-warm-action"
            :disabled="!observationReason.trim() || processingAction"
            @click="executeRevision"
          >
            <i v-if="processingAction" class="pi pi-spin pi-spinner"></i>
            <i v-else class="pi pi-send"></i>
            <span>Enviar a Revisión</span>
          </button>
        </div>
      </template>
    </Dialog>

    <!-- ============================================================== -->
    <!-- MODAL 3: RECHAZAR COTIZACIÓN                                   -->
    <!-- ============================================================== -->
    <Dialog
      v-model:visible="showRejectDialog"
      modal
      header="Rechazar Propuesta Comercial"
      :style="{ width: '540px', maxWidth: '95vw' }"
      class="custom-sga-dialog"
    >
      <div class="modal-form-content">
        <p class="modal-desc">
          Indique la razón formal por la cual la cotización <strong>{{ actionCotizacion?.codigoCotizacion }}</strong> no es viable técnica ni legalmente.
        </p>

        <div class="form-group">
          <label class="form-label">Motivo de Rechazo Aduanero:</label>
          <Textarea
            v-model="rejectReason"
            rows="4"
            class="custom-textarea"
            placeholder="Indique la causal de rechazo (incompatibilidad de régimen, restricciones de SUNAT, etc.)..."
          />
        </div>
      </div>

      <template #footer>
        <div class="dialog-actions-row">
          <button type="button" class="btn-outline-warm" @click="showRejectDialog = false">
            <span>Cancelar</span>
          </button>
          <button
            type="button"
            class="btn-danger-action"
            :disabled="!rejectReason.trim() || processingAction"
            @click="executeReject"
          >
            <i v-if="processingAction" class="pi pi-spin pi-spinner"></i>
            <i v-else class="pi pi-times"></i>
            <span>Confirmar Rechazo</span>
          </button>
        </div>
      </template>
    </Dialog>

    <!-- Modal: Apertura de Expediente Aduanero -->
    <AperturaExpedienteModal
      :visible="showAperturaModal"
      :cotizacion="selectedCotizacionParaExpediente"
      :cotizaciones-disponibles="cotizacionesAprobadasSinExpediente"
      @close="showAperturaModal = false"
      @saved="onExpedienteAperturado"
    />

    <!-- Modal: Detalle Integral del Expediente Aduanero -->
    <DetalleExpedienteModal
      :visible="showDetalleModal"
      :expediente="selectedExpedienteParaDetalle"
      @close="showDetalleModal = false"
      @open-docs="openDocumentosExpediente"
    />

    <!-- Modal: Asignación de Canal SUNAT / Levante Aduanero -->
    <CanalLevanteModal
      :visible="showCanalLevanteModal"
      :expediente="selectedExpedienteParaTramite"
      :action-type="canalLevanteActionType"
      @close="showCanalLevanteModal = false"
      @saved="onCanalLevanteSaved"
    />

    <!-- Modal: Gestión Integral de Documentos Adjuntos -->
    <DocumentosManagerModal
      :visible="showDocumentosModal"
      :entidad-tipo="selectedDocEntidadTipo"
      :id-entidad="selectedDocEntidadId"
      :entidad-id="selectedDocEntidadId"
      :codigo-entidad="selectedDocCodigoEntidad"
      :titulo-entidad="selectedDocTituloEntidad"
      @close="showDocumentosModal = false"
    />
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import DataTable from 'primevue/datatable'
import Column from 'primevue/column'
import Tag from 'primevue/tag'
import Dialog from 'primevue/dialog'
import Textarea from 'primevue/textarea'
import { supervisionService } from '../services/supervisionService'
import { expedienteService } from '../services/expedienteService'
import { useAuthStore } from '@/features/auth/stores/authStore'
import { useNavigationStore } from '@/core/stores/navigationStore'
import { useNotification } from '@/shared/composables/useNotification'
import { useConfirmation } from '@/shared/composables/useConfirmation'
import AperturaExpedienteModal from '../components/AperturaExpedienteModal.vue'
import DetalleExpedienteModal from '../components/DetalleExpedienteModal.vue'
import CanalLevanteModal from '../components/CanalLevanteModal.vue'
import DocumentosManagerModal from '@/shared/components/DocumentosManagerModal.vue'
import HeroBannerCard from '@/shared/components/HeroBannerCard.vue'
import MetricCard from '@/shared/components/MetricCard.vue'

const authStore = useAuthStore()
const navigationStore = useNavigationStore()

const { showSuccess, showError, showInfo } = useNotification()
const { confirmAction } = useConfirmation()

const loading = ref(false)
const activeTab = ref('cotizaciones') // 'cotizaciones' | 'expedientes'
const cotizaciones = ref([])
const expedientes = ref([])

// Filtros y Búsqueda
const filterEstado = ref('')
const searchQuery = ref('')
const filterCanalExpediente = ref('')
const filterEstadoExpediente = ref('')
const searchExpedienteQuery = ref('')

// Modales Operativos
const showAperturaModal = ref(false)
const selectedCotizacionParaExpediente = ref(null)

const showDetalleModal = ref(false)
const selectedExpedienteParaDetalle = ref(null)

const showCanalLevanteModal = ref(false)
const selectedExpedienteParaTramite = ref(null)
const canalLevanteActionType = ref('canal')

// Modal de Gestión de Documentos Adjuntos
const showDocumentosModal = ref(false)
const selectedDocEntidadTipo = ref('COTIZACION')
const selectedDocEntidadId = ref(null)
const selectedDocCodigoEntidad = ref('')
const selectedDocTituloEntidad = ref('')

const openDocumentosCotizacion = (cotizacion) => {
  if (!cotizacion) return
  selectedDocEntidadTipo.value = 'COTIZACION'
  selectedDocEntidadId.value = cotizacion.idCotizacion
  selectedDocCodigoEntidad.value = cotizacion.codigoCotizacion || `COT-${cotizacion.idCotizacion}`
  selectedDocTituloEntidad.value = `${cotizacion.codigoCotizacion || 'Cotización'} — ${cotizacion.clienteRazonSocial || 'Cliente'}`
  showDocumentosModal.value = true
}

const openDocumentosExpediente = (expediente) => {
  if (!expediente) return
  selectedDocEntidadTipo.value = 'EXPEDIENTE'
  selectedDocEntidadId.value = expediente.idExpediente
  selectedDocCodigoEntidad.value = expediente.codigoExpediente || `EXP-${expediente.idExpediente}`
  selectedDocTituloEntidad.value = `${expediente.codigoExpediente || 'Expediente'} — ${expediente.clienteRazonSocial || 'Cliente'}`
  showDocumentosModal.value = true
}

// Modales
const showInspectModal = ref(false)
const inspectedCotizacion = ref(null)

const showObservationDialog = ref(false)
const observationReason = ref('')

const showRejectDialog = ref(false)
const rejectReason = ref('')

const actionCotizacion = ref(null)
const processingAction = ref(false)

// Sincronización reactiva con el menú lateral (Sidebar)
const switchTab = (tabName) => {
  activeTab.value = tabName
  navigationStore.setSupervisionSubOption(tabName)
}

watch(
  () => [navigationStore.supervisionSubOption, navigationStore.navTick],
  ([newSub]) => {
    if (newSub && ['cotizaciones', 'expedientes'].includes(newSub)) {
      activeTab.value = newSub
    }
  },
  { immediate: true }
)

// Métricas Computadas
const enviadasCount = computed(() => {
  return cotizaciones.value.filter((c) => c.estado === 'ENVIADA' || c.estado === 'PENDIENTE').length
})

const revisionCount = computed(() => {
  return cotizaciones.value.filter((c) => c.estado === 'REVISION' || c.estado === 'OBSERVADA').length
})

const aprobadasCount = computed(() => {
  return cotizaciones.value.filter((c) => c.estado === 'APROBADA').length
})

const cotizacionesAprobadasSinExpediente = computed(() => {
  return cotizaciones.value.filter((c) => c.estado === 'APROBADA' && !c.tieneExpediente)
})

// Métricas de Expedientes
const expedientesPendienteCanalCount = computed(() => {
  return expedientes.value.filter((e) => !e.canalSunat || e.canalSunat === 'PENDIENTE').length
})

const expedientesConCanalCount = computed(() => {
  return expedientes.value.filter((e) => e.canalSunat && e.canalSunat !== 'PENDIENTE').length
})

const expedientesConLevanteCount = computed(() => {
  return expedientes.value.filter((e) => !!e.fechaLevanteAutorizacion).length
})

// Filtrado de Cotizaciones
const filteredCotizaciones = computed(() => {
  return cotizaciones.value.filter((c) => {
    // Filtro por Estado
    if (filterEstado.value) {
      if (filterEstado.value === 'ENVIADA' || filterEstado.value === 'PENDIENTE') {
        if (c.estado !== 'ENVIADA' && c.estado !== 'PENDIENTE') return false
      } else if (filterEstado.value === 'REVISION' || filterEstado.value === 'OBSERVADA') {
        if (c.estado !== 'REVISION' && c.estado !== 'OBSERVADA') return false
      } else if (c.estado !== filterEstado.value) {
        return false
      }
    }

    // Filtro por Búsqueda
    if (searchQuery.value) {
      const q = searchQuery.value.toLowerCase().trim()
      const match =
        c.codigoCotizacion?.toLowerCase().includes(q) ||
        c.clienteRazonSocial?.toLowerCase().includes(q) ||
        c.clienteNumeroDocumento?.toLowerCase().includes(q) ||
        c.usuarioComercialNombre?.toLowerCase().includes(q) ||
        c.codigoExpediente?.toLowerCase().includes(q)
      if (!match) return false
    }

    return true
  })
})

// Filtrado de Expedientes Aduaneros
const filteredExpedientes = computed(() => {
  return expedientes.value.filter((e) => {
    // Filtro por Canal SUNAT
    if (filterCanalExpediente.value) {
      if (filterCanalExpediente.value === 'PENDIENTE') {
        if (e.canalSunat && e.canalSunat !== 'PENDIENTE') return false
      } else if (e.canalSunat !== filterCanalExpediente.value) {
        return false
      }
    }

    // Filtro por Estado del Expediente
    if (filterEstadoExpediente.value) {
      if (e.estadoExpediente !== filterEstadoExpediente.value) return false
    }

    // Filtro por Búsqueda
    if (searchExpedienteQuery.value) {
      const q = searchExpedienteQuery.value.toLowerCase().trim()
      const match =
        e.codigoExpediente?.toLowerCase().includes(q) ||
        e.codigoCotizacion?.toLowerCase().includes(q) ||
        e.clienteRazonSocial?.toLowerCase().includes(q) ||
        e.clienteNumeroDocumento?.toLowerCase().includes(q) ||
        e.numeroDam?.toLowerCase().includes(q) ||
        e.numeroContenedor?.toLowerCase().includes(q) ||
        e.agenteAduaneroNombre?.toLowerCase().includes(q)
      if (!match) return false
    }

    return true
  })
})

const openAperturaModal = (cotizacion = null) => {
  selectedCotizacionParaExpediente.value = cotizacion
  showAperturaModal.value = true
}

const onExpedienteAperturado = (expedienteCreado) => {
  showAperturaModal.value = false
  showSuccess(`¡Expediente ${expedienteCreado.codigoExpediente} aperturado exitosamente!`)
  loadData()
}

const openDetalleExpediente = (exp) => {
  selectedExpedienteParaDetalle.value = exp
  showDetalleModal.value = true
}

const openCanalModal = (exp) => {
  selectedExpedienteParaTramite.value = exp
  canalLevanteActionType.value = 'canal'
  showCanalLevanteModal.value = true
}

const openLevanteModal = (exp) => {
  selectedExpedienteParaTramite.value = exp
  canalLevanteActionType.value = 'levante'
  showCanalLevanteModal.value = true
}

const onCanalLevanteSaved = (actualizado) => {
  showCanalLevanteModal.value = false
  showSuccess(`Expediente ${actualizado.codigoExpediente} actualizado conforme.`)
  loadData()
}

// Cargar Datos
const loadData = async () => {
  loading.value = true
  try {
    const [cots, exps] = await Promise.all([
      supervisionService.getCotizaciones(),
      expedienteService.getExpedientes()
    ])
    cotizaciones.value = cots || []
    expedientes.value = exps || []

    // Si había una cotización inspeccionada, refrescar su dato
    if (inspectedCotizacion.value) {
      const updated = cotizaciones.value.find((c) => c.idCotizacion === inspectedCotizacion.value.idCotizacion)
      if (updated) inspectedCotizacion.value = updated
    }

    // Si había un expediente en detalle, refrescar su dato
    if (selectedExpedienteParaDetalle.value) {
      const updatedExp = expedientes.value.find((e) => e.idExpediente === selectedExpedienteParaDetalle.value.idExpediente)
      if (updatedExp) selectedExpedienteParaDetalle.value = updatedExp
    }
  } catch (err) {
    showError(err.response?.data?.message || 'Error al sincronizar datos de Supervisión.')
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadData()
})

// ==========================================
// ACCIONES DE AUDITORÍA Y SUPERVISIÓN
// ==========================================
const openInspectModal = (cotizacion) => {
  inspectedCotizacion.value = cotizacion
  showInspectModal.value = true
}

const confirmApprove = async (cotizacion) => {
  const confirmed = await confirmAction(
    `¿Confirma la aprobación de la Cotización ${cotizacion.codigoCotizacion}? Al aprobar, la propuesta queda formalizada técnicamente para apertura de Expediente Aduanero.`,
    'Aprobación Técnica Aduanera'
  )
  if (!confirmed) return

  try {
    loading.value = true
    await supervisionService.revisarCotizacion(cotizacion.idCotizacion, {
      idAgenteRevisor: authStore.user?.idUsuario || 1,
      estado: 'APROBADA',
      motivoObservacion: null,
      observaciones: 'Aprobada formalmente por Supervisión Aduanera.'
    })
    showSuccess(`¡Cotización ${cotizacion.codigoCotizacion} Aprobada! Lista para apertura de Expediente Aduanero.`)
    showInspectModal.value = false
    await loadData()
  } catch (err) {
    showError(err.response?.data?.message || 'No se pudo aprobar la cotización.')
  } finally {
    loading.value = false
  }
}

const openObservationModal = (cotizacion) => {
  actionCotizacion.value = cotizacion
  observationReason.value = cotizacion.motivoObservacion || ''
  showObservationDialog.value = true
}

const executeRevision = async () => {
  if (!actionCotizacion.value || !observationReason.value.trim()) return

  processingAction.value = true
  try {
    await supervisionService.revisarCotizacion(actionCotizacion.value.idCotizacion, {
      idAgenteRevisor: authStore.user?.idUsuario || 1,
      estado: 'OBSERVADA',
      motivoObservacion: observationReason.value.trim(),
      observaciones: 'Observaciones emitidas por Supervisión Aduanera.'
    })
    showInfo(`Cotización ${actionCotizacion.value.codigoCotizacion} marcada como Observada para corrección por Ventas.`)
    showObservationDialog.value = false
    showInspectModal.value = false
    await loadData()
  } catch (err) {
    showError(err.response?.data?.message || 'Error al cambiar estado a Revisión.')
  } finally {
    processingAction.value = false
  }
}

const openRejectModal = (cotizacion) => {
  actionCotizacion.value = cotizacion
  rejectReason.value = ''
  showRejectDialog.value = true
}

const executeReject = async () => {
  if (!actionCotizacion.value || !rejectReason.value.trim()) return

  processingAction.value = true
  try {
    await supervisionService.revisarCotizacion(actionCotizacion.value.idCotizacion, {
      idAgenteRevisor: authStore.user?.idUsuario || 1,
      estado: 'RECHAZADA',
      motivoObservacion: rejectReason.value.trim(),
      observaciones: 'Rechazo técnico emitido por Supervisión Aduanera.'
    })
    showInfo(`Cotización ${actionCotizacion.value.codigoCotizacion} marcada como Rechazada.`)
    showRejectDialog.value = false
    showInspectModal.value = false
    await loadData()
  } catch (err) {
    showError(err.response?.data?.message || 'Error al rechazar cotización.')
  } finally {
    processingAction.value = false
  }
}

// Helpers de Estilo y Etiquetas
const getEstadoLabel = (estado) => {
  const map = {
    ENVIADA: 'Por Revisar (Pendiente)',
    PENDIENTE: 'Por Revisar (Pendiente)',
    EN_REVISION: 'En Evaluación Técnica',
    REVISION: 'Observada (Para Corrección)',
    OBSERVADA: 'Observada (Para Corrección)',
    APROBADA: 'Aprobada',
    RECHAZADA: 'Rechazada',
    ANULADA: 'Anulada'
  }
  return map[estado] || estado
}

const getEstadoSeverity = (estado) => {
  if (estado === 'APROBADA') return 'success'
  if (estado === 'RECHAZADA' || estado === 'ANULADA') return 'danger'
  if (estado === 'REVISION' || estado === 'OBSERVADA') return 'warn'
  if (estado === 'EN_REVISION') return 'info'
  return 'contrast'
}

const getCanalSeverity = (canal) => {
  if (canal === 'VERDE') return 'success'
  if (canal === 'NARANJA') return 'warn'
  if (canal === 'ROJO') return 'danger'
  return 'contrast'
}

const getExpedienteSeverity = (estado) => {
  if (estado === 'LEVANTE_AUTORIZADO' || estado === 'CONCLUIDO') return 'success'
  if (estado === 'ANULADO') return 'danger'
  if (estado === 'CON_CANAL') return 'warn'
  return 'info'
}

const formatRegimen = (regimen) => {
  if (!regimen) return '—'
  return regimen.replace(/_/g, ' ')
}

const formatDateTime = (dtStr) => {
  if (!dtStr) return '—'
  return dtStr.replace('T', ' ').substring(0, 16)
}
</script>

<style scoped>
.supervision-view {
  display: flex;
  flex-direction: column;
  gap: var(--space-6, 1.5rem);
  padding-bottom: var(--space-10, 2.5rem);
}

/* 1. Hero Banner Card Editorial Cálido */
.hero-banner-card {
  position: relative;
  background: linear-gradient(135deg, #1c1917 0%, #1e2638 60%, #0f274a 100%);
  border: 1px solid rgba(59, 130, 246, 0.25);
  border-radius: var(--radius-lg, 12px);
  padding: 1.75rem 2rem;
  color: #fafaf9;
  overflow: hidden;
  box-shadow: 0 4px 18px rgba(0, 0, 0, 0.12), inset 0 1px 0 rgba(255, 255, 255, 0.05);
}

.hero-content {
  position: relative;
  z-index: 2;
  max-width: 820px;
}

.badge-kicker {
  display: inline-block;
  font-size: 0.6875rem;
  font-weight: 700;
  letter-spacing: 0.1em;
  text-transform: uppercase;
  color: #3b82f6;
  margin-bottom: 0.5rem;
}

.hero-title {
  font-size: 1.625rem;
  font-weight: 700;
  color: #ffffff;
  line-height: 1.25;
  margin: 0 0 0.5rem 0;
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

/* 2. Métricas de Supervisión */
.metrics-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: var(--space-4, 1rem);
}

@media (max-width: 900px) {
  .metrics-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

.metric-card {
  background: var(--color-bg-surface);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-md, 8px);
  padding: var(--space-4, 1rem) var(--space-5, 1.25rem);
  box-shadow: var(--shadow-sm);
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  gap: var(--space-2, 0.5rem);
}

.metric-top {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.metric-idx {
  font-family: var(--font-mono, monospace);
  font-size: 0.75rem;
  font-weight: 700;
  color: var(--color-text-subtle);
}

.metric-label {
  font-size: 0.8125rem;
  font-weight: 600;
  color: var(--color-text-muted);
}

.metric-val {
  font-family: var(--font-display, serif);
  font-size: 2rem;
  font-weight: 700;
  color: var(--color-text-main);
  line-height: 1;
}

.text-pending {
  color: var(--color-info-text, #2563eb);
}

.text-warning {
  color: var(--color-warning-text, #d97706);
}

.text-success {
  color: var(--color-success-text, #16a34a);
}

.text-accent {
  color: var(--color-accent-kicker);
}

/* 3. Selector de Pestañas */
.tabs-nav-bar {
  display: flex;
  gap: var(--space-2, 0.5rem);
  border-bottom: 1px solid var(--color-border);
  padding-bottom: var(--space-1, 0.25rem);
}

.tab-btn {
  display: flex;
  align-items: center;
  gap: var(--space-2, 0.5rem);
  padding: var(--space-3, 0.75rem) var(--space-5, 1.25rem);
  background: transparent;
  border: none;
  border-bottom: 2px solid transparent;
  color: var(--color-text-muted);
  font-size: 0.9375rem;
  font-weight: 600;
  cursor: pointer;
  transition: all var(--transition-fast);
}

.tab-btn:hover {
  color: var(--color-text-main);
  background: var(--color-bg-subtle);
  border-radius: var(--radius-sm, 6px) var(--radius-sm, 6px) 0 0;
}

.tab-btn.active {
  color: var(--color-text-main);
  border-bottom-color: var(--color-primary);
  font-weight: 700;
}

/* 4. Cabecera y Controles de Sección */
.tab-content-section {
  display: flex;
  flex-direction: column;
  gap: var(--space-4, 1rem);
}

.section-header-row {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  flex-wrap: wrap;
  gap: var(--space-4, 1rem);
}

.section-title {
  font-family: var(--font-display, serif);
  font-size: 1.25rem;
  font-weight: 700;
  color: var(--color-text-main);
  margin: 0;
}

.section-controls {
  display: flex;
  align-items: center;
  gap: var(--space-3, 0.75rem);
  flex-wrap: wrap;
}

.filter-select {
  padding: var(--space-2, 0.5rem) var(--space-3, 0.75rem);
  background: var(--color-bg-surface);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-md, 8px);
  color: var(--color-text-main);
  font-size: 0.875rem;
  outline: none;
  cursor: pointer;
}

.filter-select:focus {
  border-color: var(--color-border-strong);
}

.search-input-wrap {
  position: relative;
  display: flex;
  align-items: center;
}

.search-icon {
  position: absolute;
  left: 0.75rem;
  color: var(--color-text-subtle);
  pointer-events: none;
}

.search-input {
  padding: var(--space-2, 0.5rem) var(--space-3, 0.75rem) var(--space-2, 0.5rem) 2.25rem;
  background: var(--color-bg-surface);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-md, 8px);
  color: var(--color-text-main);
  font-size: 0.875rem;
  outline: none;
  min-width: 260px;
}

.search-input:focus {
  border-color: var(--color-border-strong);
}

/* Botones Genéricos de Estilo Editorial */
.btn-outline-warm {
  display: flex;
  align-items: center;
  gap: var(--space-2, 0.5rem);
  padding: var(--space-2, 0.5rem) var(--space-4, 1rem);
  background: var(--color-bg-surface);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-md, 8px);
  color: var(--color-text-main);
  font-size: 0.875rem;
  font-weight: 600;
  cursor: pointer;
  transition: all var(--transition-fast);
}

.btn-outline-warm:hover:not(:disabled) {
  background: var(--color-bg-subtle);
  border-color: var(--color-border-strong);
}

/* 5. Tabla de Datos Custom SGA */
.table-container {
  background: var(--color-bg-surface);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-lg, 12px);
  overflow: hidden;
  box-shadow: var(--shadow-sm);
}

.code-column {
  display: flex;
  flex-direction: column;
}

.doc-code {
  font-family: var(--font-mono, monospace);
  font-weight: 700;
  font-size: 0.875rem;
  color: var(--color-text-main);
}

.date-sub {
  font-size: 0.75rem;
  color: var(--color-text-subtle);
}

.client-cell {
  display: flex;
  flex-direction: column;
  gap: 0.125rem;
}

.client-name {
  font-weight: 600;
  color: var(--color-text-main);
  font-size: 0.875rem;
}

.client-sub {
  font-size: 0.75rem;
  color: var(--color-text-subtle);
}

.comercial-name {
  font-size: 0.875rem;
  color: var(--color-text-muted);
}

.regimen-tag {
  display: inline-block;
  padding: 0.2rem 0.5rem;
  background: var(--color-bg-subtle);
  border: 1px solid var(--color-border-subtle);
  border-radius: var(--radius-sm, 6px);
  font-size: 0.75rem;
  font-weight: 600;
  color: var(--color-text-muted);
}

.total-amount {
  font-family: var(--font-mono, monospace);
  font-weight: 700;
  color: var(--color-text-main);
  font-size: 0.9375rem;
}

.exp-linked-tag,
.os-linked-tag {
  display: inline-flex;
  align-items: center;
  gap: 0.35rem;
  padding: 0.2rem 0.55rem;
  background: var(--color-success-bg);
  border: 1px solid var(--color-success-border);
  color: var(--color-success-text);
  border-radius: var(--radius-sm, 6px);
  font-size: 0.75rem;
  font-weight: 700;
  font-family: var(--font-mono, monospace);
}

.os-code-badge,
.exp-code-badge {
  display: inline-flex;
  align-items: center;
  gap: 0.35rem;
  padding: 0.25rem 0.6rem;
  background: var(--color-bg-subtle);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-sm, 6px);
  font-weight: 700;
  font-family: var(--font-mono, monospace);
  color: var(--color-text-main);
  cursor: pointer;
  transition: all var(--transition-fast, 0.2s ease);
}

.exp-code-badge:hover {
  border-color: var(--color-primary);
  background: var(--color-bg-muted);
}

.btn-primary-action {
  display: inline-flex;
  align-items: center;
  gap: 0.5rem;
  padding: var(--space-2, 0.5rem) var(--space-4, 1rem);
  background: var(--color-accent-kicker, #c2410c);
  border: 1px solid var(--color-accent-kicker, #c2410c);
  border-radius: var(--radius-md, 8px);
  color: #fff;
  font-weight: 700;
  font-size: 0.875rem;
  cursor: pointer;
  transition: all var(--transition-fast, 0.2s ease);
  white-space: nowrap;
}

.btn-primary-action:hover {
  background: var(--color-primary, #352f2a);
  border-color: var(--color-primary, #352f2a);
  transform: translateY(-1px);
}

.exp-pending-alert-banner {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 1.25rem;
  background: var(--color-warning-bg, #fdf5e6);
  border: 1px solid var(--color-warning-border, #f8dab3);
  border-radius: var(--radius-lg, 12px);
  padding: 1rem 1.5rem;
  margin-bottom: 1.5rem;
  flex-wrap: wrap;
}

.alert-icon-box {
  width: 2.5rem;
  height: 2.5rem;
  border-radius: var(--radius-md, 8px);
  background: var(--color-warning-border, #f8dab3);
  color: var(--color-warning-text, #8c5b16);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1.25rem;
  flex-shrink: 0;
}

.alert-text-content {
  flex: 1;
  min-width: 240px;
}

.alert-text-content strong {
  font-size: 0.9375rem;
  color: var(--color-warning-text, #8c5b16);
  display: block;
}

.alert-text-content p {
  margin: 0.25rem 0 0 0;
  font-size: 0.8125rem;
  color: var(--color-text-muted);
}

.btn-alert-action {
  display: inline-flex;
  align-items: center;
  gap: 0.5rem;
  background: var(--color-warning-text, #8c5b16);
  color: #ffffff;
  border: none;
  border-radius: var(--radius-md, 8px);
  padding: 0.625rem 1.25rem;
  font-size: 0.8125rem;
  font-weight: 700;
  cursor: pointer;
  transition: all 0.2s ease;
  white-space: nowrap;
}

.btn-alert-action:hover {
  background: #71460e;
  transform: translateY(-1px);
}

.text-muted-dash {
  color: var(--color-text-subtle);
  font-size: 0.8125rem;
}

.obs-snippet {
  font-size: 0.8125rem;
  color: var(--color-text-muted);
  max-width: 250px;
  display: block;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

/* Acciones en la Tabla */
.actions-wrapper {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 0.375rem;
}

.action-btn-primary {
  display: inline-flex;
  align-items: center;
  gap: 0.35rem;
  padding: 0.35rem 0.65rem;
  background: var(--color-primary);
  color: var(--color-primary-text);
  border: 1px solid var(--color-primary);
  border-radius: var(--radius-md, 8px);
  font-size: 0.75rem;
  font-weight: 600;
  cursor: pointer;
  transition: all var(--transition-fast);
}

.action-btn-primary:hover {
  opacity: 0.9;
}

.action-btn-outline {
  display: inline-flex;
  align-items: center;
  gap: 0.25rem;
  padding: 0.35rem 0.55rem;
  background: var(--color-bg-surface);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-md, 8px);
  font-size: 0.75rem;
  font-weight: 600;
  cursor: pointer;
  transition: all var(--transition-fast);
}

.action-btn-outline.approve {
  color: var(--color-success-text);
  border-color: var(--color-success-border);
  background: var(--color-success-bg);
}

.action-btn-outline.approve:hover {
  filter: brightness(0.95);
}

.action-btn-outline.observe {
  color: var(--color-warning-text);
  border-color: var(--color-warning-border);
  background: var(--color-warning-bg);
}

.action-btn-outline.observe:hover {
  filter: brightness(0.95);
}

.action-btn-outline.reject {
  color: var(--color-danger-text);
  border-color: var(--color-danger-border);
  background: var(--color-danger-bg);
}

.action-btn-outline.reject:hover {
  filter: brightness(0.95);
}

/* Empty y Loading States */
.empty-state {
  padding: var(--space-10, 2.5rem);
  text-align: center;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.empty-icon {
  font-size: 2.5rem;
  color: var(--color-text-subtle);
  margin-bottom: var(--space-3, 0.75rem);
}

.empty-title {
  font-size: 1rem;
  font-weight: 700;
  color: var(--color-text-main);
  margin: 0 0 0.25rem 0;
}

.empty-desc {
  font-size: 0.8125rem;
  color: var(--color-text-muted);
  margin: 0;
}

.loading-state {
  padding: var(--space-8, 2rem);
  display: flex;
  align-items: center;
  justify-content: center;
  gap: var(--space-3, 0.75rem);
  color: var(--color-text-muted);
  font-size: 0.875rem;
}

/* ============================================================== */
/* ESTILOS DE LOS MODALES DE SUPERVISIÓN                          */
/* ============================================================== */
.inspection-body {
  display: flex;
  flex-direction: column;
  gap: var(--space-4, 1rem);
  padding: var(--space-2, 0.5rem) 0;
}

.inspection-status-banner {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: var(--space-4, 1rem) var(--space-5, 1.25rem);
  background: var(--color-bg-subtle);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-md, 8px);
}

.status-banner-info {
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
}

.status-banner-kicker {
  font-size: 0.6875rem;
  font-weight: 700;
  letter-spacing: 0.1em;
  color: var(--color-text-subtle);
}

.status-banner-badge-row {
  display: flex;
  align-items: center;
  gap: var(--space-3, 0.75rem);
}

.exp-badge-header,
.os-badge-header {
  display: inline-flex;
  align-items: center;
  gap: 0.35rem;
  font-size: 0.75rem;
  font-weight: 700;
  color: var(--color-success-text);
}

.status-banner-code {
  font-family: var(--font-mono, monospace);
  font-size: 1.125rem;
  font-weight: 700;
  color: var(--color-text-main);
}

.observation-history-box {
  display: flex;
  gap: var(--space-3, 0.75rem);
  padding: var(--space-3, 0.75rem) var(--space-4, 1rem);
  background: var(--color-warning-bg);
  border: 1px solid var(--color-warning-border);
  color: var(--color-warning-text);
  border-radius: var(--radius-md, 8px);
  font-size: 0.8125rem;
}

.observation-history-box p {
  margin: 0.25rem 0 0 0;
  font-weight: 500;
}

.inspection-sections {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: var(--space-4, 1rem);
}

@media (max-width: 700px) {
  .inspection-sections {
    grid-template-columns: 1fr;
  }
}

.inspect-card {
  background: var(--color-bg-surface);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-md, 8px);
  overflow: hidden;
}

.inspect-card-header {
  display: flex;
  align-items: center;
  gap: var(--space-2, 0.5rem);
  padding: var(--space-2, 0.5rem) var(--space-4, 1rem);
  background: var(--color-bg-subtle);
  border-bottom: 1px solid var(--color-border-subtle);
  font-size: 0.8125rem;
  font-weight: 700;
  color: var(--color-text-main);
}

.inspect-card-content {
  padding: var(--space-3, 0.75rem) var(--space-4, 1rem);
  display: flex;
  flex-direction: column;
  gap: var(--space-2, 0.5rem);
}

.inspect-field {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 0.8125rem;
}

.inspect-label {
  color: var(--color-text-muted);
  font-weight: 500;
}

.inspect-value {
  color: var(--color-text-main);
}

.inspect-value.strong {
  font-weight: 700;
}

.regimen-badge-value {
  font-weight: 600;
  color: var(--color-accent-kicker);
}

/* 3. Desglose Financiero */
.inspect-financial-card {
  background: var(--color-bg-surface);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-md, 8px);
  overflow: hidden;
}

.financial-card-header {
  display: flex;
  align-items: center;
  gap: var(--space-2, 0.5rem);
  padding: var(--space-2, 0.5rem) var(--space-4, 1rem);
  background: var(--color-bg-subtle);
  border-bottom: 1px solid var(--color-border-subtle);
  font-size: 0.8125rem;
  font-weight: 700;
  color: var(--color-text-main);
}

.financial-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  padding: var(--space-4, 1rem);
  gap: var(--space-3, 0.75rem);
}

@media (max-width: 700px) {
  .financial-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

.financial-col {
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
}

.financial-col.total-col {
  border-left: 1px solid var(--color-border);
  padding-left: var(--space-3, 0.75rem);
}

.fin-label {
  font-size: 0.75rem;
  font-weight: 600;
  color: var(--color-text-subtle);
  text-transform: uppercase;
}

.fin-amount {
  font-family: var(--font-mono, monospace);
  font-size: 0.9375rem;
  font-weight: 700;
  color: var(--color-text-main);
}

.total-amount-big {
  font-size: 1.25rem;
  color: var(--color-accent-kicker);
}

.inspect-obs-section {
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
  background: var(--color-bg-subtle);
  padding: var(--space-3, 0.75rem) var(--space-4, 1rem);
  border-radius: var(--radius-md, 8px);
  border: 1px solid var(--color-border-subtle);
}

.obs-full-text {
  margin: 0;
  font-size: 0.8125rem;
  color: var(--color-text-main);
  line-height: 1.5;
}

/* Modales de Confirmación y Acciones */
.modal-form-content {
  display: flex;
  flex-direction: column;
  gap: var(--space-3, 0.75rem);
}

.modal-desc {
  font-size: 0.875rem;
  color: var(--color-text-muted);
  line-height: 1.5;
  margin: 0;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 0.375rem;
}

.form-label {
  font-size: 0.8125rem;
  font-weight: 600;
  color: var(--color-text-main);
}

.custom-textarea {
  width: 100%;
  background: var(--color-bg-surface);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-md, 8px);
  color: var(--color-text-main);
  padding: var(--space-2, 0.5rem) var(--space-3, 0.75rem);
  font-size: 0.875rem;
}

.custom-textarea:focus {
  border-color: var(--color-border-strong);
}

.dialog-actions-row {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: var(--space-3, 0.75rem);
  flex-wrap: wrap;
}

.btn-espresso-success {
  display: flex;
  align-items: center;
  gap: 0.35rem;
  padding: var(--space-2, 0.5rem) var(--space-4, 1rem);
  background: var(--color-primary);
  color: var(--color-primary-text);
  border: 1px solid var(--color-primary);
  border-radius: var(--radius-md, 8px);
  font-size: 0.875rem;
  font-weight: 700;
  cursor: pointer;
  transition: all var(--transition-fast);
}

.btn-espresso-success:hover {
  opacity: 0.9;
}

.btn-warm-action {
  display: flex;
  align-items: center;
  gap: 0.35rem;
  padding: var(--space-2, 0.5rem) var(--space-4, 1rem);
  background: var(--color-warning-bg);
  color: var(--color-warning-text);
  border: 1px solid var(--color-warning-border);
  border-radius: var(--radius-md, 8px);
  font-size: 0.875rem;
  font-weight: 600;
  cursor: pointer;
}

.btn-warm-action:hover:not(:disabled) {
  filter: brightness(0.95);
}

.btn-danger-action {
  display: flex;
  align-items: center;
  gap: 0.35rem;
  padding: var(--space-2, 0.5rem) var(--space-4, 1rem);
  background: var(--color-danger-bg);
  color: var(--color-danger-text);
  border: 1px solid var(--color-danger-border);
  border-radius: var(--radius-md, 8px);
  font-size: 0.875rem;
  font-weight: 600;
  cursor: pointer;
}

.btn-danger-action:hover:not(:disabled) {
  filter: brightness(0.95);
}
</style>
