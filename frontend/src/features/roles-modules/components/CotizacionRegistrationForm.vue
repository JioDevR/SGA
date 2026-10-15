<template>
  <div class="cotizacion-registration-form-view">
    <!-- 1. Header Bar de Navegación y Acciones -->
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
          {{ isEditMode ? `Editar Cotización: ${form.codigoCotizacion}` : 'Emitir Nueva Cotización Comercial' }}
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
          @click="handleSubmit"
          :disabled="submitting || !isFormReady"
        >
          <i v-if="submitting" class="pi pi-spin pi-spinner"></i>
          <i v-else class="pi pi-check"></i>
          <span>{{ isEditMode ? 'Guardar Cambios' : 'Emitir Cotización' }}</span>
        </button>
      </div>
    </div>

    <!-- Banner de Error si la validación falla -->
    <div v-if="errorMessage" class="error-banner">
      <i class="pi pi-exclamation-triangle"></i>
      <div class="error-content">
        <strong>Error:</strong>
        <span>{{ errorMessage }}</span>
      </div>
      <button type="button" class="error-close-btn" @click="errorMessage = ''">
        <i class="pi pi-times"></i>
      </button>
    </div>

    <!-- Banner de Observación de Supervisión Aduanera si está en REVISION u OBSERVADA -->
    <div v-if="props.cotizacionToEdit?.estado === 'REVISION' || props.cotizacionToEdit?.estado === 'OBSERVADA' || props.cotizacionToEdit?.estado === 'EN_REVISION'" class="revision-alert-banner">
      <i class="pi pi-exclamation-circle revision-icon"></i>
      <div class="revision-body">
        <span class="revision-kicker">OBSERVADA POR SUPERVISIÓN ADUANERA</span>
        <strong class="revision-title">Esta cotización fue devuelta para corrección técnica:</strong>
        <p class="revision-note">{{ props.cotizacionToEdit?.motivoObservacion || 'Se requieren correcciones antes de la aprobación definitiva.' }}</p>
        <span class="revision-hint">Realice los ajustes requeridos y presione "Guardar Cambios". La cotización retornará a la bandeja de Supervisión como Enviada.</span>
      </div>
    </div>

    <!-- Barra de Pasos / Stepper Interactivo -->
    <div class="stepper-nav-bar">
      <button
        type="button"
        :class="['stepper-step-btn', { active: currentStep === 1, completed: currentStep > 1 || !!form.idCliente }]"
        @click="currentStep = 1"
      >
        <span class="step-badge">
          <i v-if="form.idCliente" class="pi pi-check"></i>
          <span v-else>1</span>
        </span>
        <div class="step-info">
          <span class="step-label">PASO 01</span>
          <span class="step-title">Cliente Solicitante</span>
        </div>
      </button>

      <div class="stepper-divider"></div>

      <button
        type="button"
        :class="['stepper-step-btn', { active: currentStep === 2, completed: currentStep > 2 || !!form.regimenAduanero }]"
        @click="currentStep = 2"
      >
        <span class="step-badge">
          <i v-if="form.regimenAduanero && currentStep > 2" class="pi pi-check"></i>
          <span v-else>2</span>
        </span>
        <div class="step-info">
          <span class="step-label">PASO 02</span>
          <span class="step-title">Régimen & Plazos</span>
        </div>
      </button>

      <div class="stepper-divider"></div>

      <button
        type="button"
        :class="['stepper-step-btn', { active: currentStep === 3, completed: form.totalCotizado > 0 }]"
        @click="currentStep = 3"
      >
        <span class="step-badge">
          <i v-if="form.totalCotizado > 0" class="pi pi-check"></i>
          <span v-else>3</span>
        </span>
        <div class="step-info">
          <span class="step-label">PASO 03</span>
          <span class="step-title">Costos & Liquidación</span>
        </div>
      </button>
    </div>

    <!-- 2. Grid Principal -->
    <div class="form-content-grid">
      <!-- Columna Principal: Pasos del Formulario -->
      <div class="form-blocks-column">

        <!-- ========================================================================= -->
        <!-- PASO 1: CLIENTE SOLICITANTE -->
        <!-- ========================================================================= -->
        <section v-show="currentStep === 1" class="form-block-card">
          <div class="block-header">
            <div class="block-idx">01</div>
            <div class="block-header-info">
              <h3 class="block-title">Identificación y Asignación del Cliente o Prospecto</h3>
              <p class="block-sub">Busque en el directorio de clientes registrados o cree un nuevo cliente inmediatamente para esta propuesta.</p>
            </div>
          </div>

          <div class="block-body">
            <!-- Barra de Búsqueda y Creación Rápida de Cliente -->
            <div class="client-search-actions-bar">
              <div class="search-input-box">
                <i class="pi pi-search search-icon"></i>
                <input
                  v-model.trim="clientSearchQuery"
                  type="text"
                  placeholder="Buscar cliente por RUC, DNI o razón social..."
                  class="client-search-input"
                />
                <button
                  v-if="clientSearchQuery"
                  type="button"
                  class="clear-query-btn"
                  @click="clientSearchQuery = ''"
                >
                  <i class="pi pi-times"></i>
                </button>
              </div>

              <button
                type="button"
                :class="['btn-toggle-quick-client', { active: showQuickClientForm }]"
                @click="showQuickClientForm = !showQuickClientForm"
              >
                <i :class="showQuickClientForm ? 'pi pi-times' : 'pi pi-user-plus'"></i>
                <span>{{ showQuickClientForm ? 'Cerrar Registro' : '+ Crear Nuevo Cliente' }}</span>
              </button>
            </div>

            <!-- Subformulario de Creación Rápida de Cliente (Inline) -->
            <div v-if="showQuickClientForm" class="quick-client-panel">
              <div class="quick-panel-header">
                <div class="panel-header-title">
                  <i class="pi pi-user-plus"></i>
                  <span>Alta Rápida de Cliente Comercial</span>
                </div>
                <span class="panel-header-badge">REGISTRO INMEDIATO</span>
              </div>

              <div class="quick-panel-body">
                <div class="quick-form-grid">
                  <div class="field-group">
                    <label class="field-label">Tipo Documento *</label>
                    <select v-model="quickClient.tipoDocumento" class="form-select">
                      <option value="RUC">RUC</option>
                      <option value="DNI">DNI</option>
                      <option value="CE">Carnet Extranjería</option>
                      <option value="PASAPORTE">Pasaporte</option>
                    </select>
                  </div>

                  <div class="field-group">
                    <label class="field-label">Número de Documento *</label>
                    <input
                      v-model.trim="quickClient.numeroDocumento"
                      type="text"
                      placeholder="Ej. 20601234567"
                      class="form-input"
                      maxlength="15"
                    />
                  </div>

                  <div class="field-group full-width">
                    <label class="field-label">Razón Social o Nombre Completo *</label>
                    <input
                      v-model.trim="quickClient.razonSocial"
                      type="text"
                      placeholder="Ej. COMERCIALIZADORA DEL PACIFICO S.A.C."
                      class="form-input"
                      maxlength="150"
                    />
                  </div>

                  <div class="field-group">
                    <label class="field-label">Correo de Contacto *</label>
                    <input
                      v-model.trim="quickClient.correoContacto"
                      type="email"
                      placeholder="Ej. importaciones@empresa.com"
                      class="form-input"
                      maxlength="100"
                    />
                  </div>

                  <div class="field-group">
                    <label class="field-label">Teléfono Móvil</label>
                    <input
                      v-model.trim="quickClient.telefonoContacto"
                      type="text"
                      placeholder="Ej. +51 999 888 777"
                      class="form-input"
                      maxlength="20"
                    />
                  </div>
                </div>

                <div v-if="quickClientError" class="quick-error-msg">
                  <i class="pi pi-exclamation-circle"></i>
                  <span>{{ quickClientError }}</span>
                </div>

                <div class="quick-panel-actions">
                  <button
                    type="button"
                    class="btn-outline-warm"
                    @click="showQuickClientForm = false"
                    :disabled="savingQuickClient"
                  >
                    Cancelar
                  </button>
                  <button
                    type="button"
                    class="btn-espresso"
                    @click="handleSaveQuickClient"
                    :disabled="savingQuickClient"
                  >
                    <i v-if="savingQuickClient" class="pi pi-spin pi-spinner"></i>
                    <i v-else class="pi pi-check"></i>
                    <span>Registrar y Seleccionar Cliente</span>
                  </button>
                </div>
              </div>
            </div>

            <!-- Si ya hay un cliente seleccionado: Tarjeta de Cliente Activo -->
            <div v-if="selectedClientObject" class="selected-client-card">
              <div class="client-card-left">
                <div class="client-avatar-circle">
                  <i class="pi pi-building"></i>
                </div>
                <div class="client-card-texts">
                  <div class="client-kicker-row">
                    <span class="doc-badge">{{ selectedClientObject.tipoDocumento }}: {{ selectedClientObject.numeroDocumento }}</span>
                    <span :class="['classification-pill', selectedClientObject.esProspecto ? 'pill-prospect' : 'pill-client']">
                      {{ selectedClientObject.esProspecto ? 'Prospecto Comercial' : 'Cliente Efectivo' }}
                    </span>
                  </div>
                  <h4 class="client-name">{{ selectedClientObject.razonSocial }}</h4>
                  <div class="client-meta-row">
                    <span v-if="selectedClientObject.contactoRepresentante" class="meta-item">
                      <i class="pi pi-user"></i> {{ selectedClientObject.contactoRepresentante }}
                    </span>
                    <span class="meta-item">
                      <i class="pi pi-envelope"></i> {{ selectedClientObject.correoContacto }}
                    </span>
                    <span v-if="selectedClientObject.telefonoContacto" class="meta-item">
                      <i class="pi pi-phone"></i> {{ selectedClientObject.telefonoContacto }}
                    </span>
                  </div>
                </div>
              </div>

              <button
                type="button"
                class="btn-change-client"
                @click="clearSelectedClient"
                title="Cambiar cliente seleccionado"
              >
                <i class="pi pi-pencil"></i>
                <span>Cambiar</span>
              </button>
            </div>

            <!-- Lista de Resultados de Búsqueda de Clientes si no hay ninguno seleccionado -->
            <div v-else class="clients-selection-list">
              <label class="field-label">Seleccione un cliente de la lista ({{ filteredAvailableClients.length }} disponibles):</label>
              
              <div v-if="filteredAvailableClients.length === 0" class="empty-clients-box">
                <i class="pi pi-users empty-icon"></i>
                <p>No se encontraron clientes coincidentes con "{{ clientSearchQuery }}".</p>
                <button
                  type="button"
                  class="btn-link-action"
                  @click="showQuickClientForm = true"
                >
                  ¿Desea registrar a este cliente ahora?
                </button>
              </div>

              <div v-else class="clients-grid">
                <div
                  v-for="c in filteredAvailableClients.slice(0, 8)"
                  :key="c.idCliente"
                  class="client-select-card"
                  @click="selectClient(c)"
                >
                  <div class="client-top-row">
                    <span class="doc-badge">{{ c.tipoDocumento }}: {{ c.numeroDocumento }}</span>
                    <span :class="['classification-pill-mini', c.esProspecto ? 'pill-prospect' : 'pill-client']">
                      {{ c.esProspecto ? 'Prospecto' : 'Cliente' }}
                    </span>
                  </div>
                  <span class="client-card-name">{{ c.razonSocial }}</span>
                  <span class="client-card-email">{{ c.correoContacto }}</span>
                </div>
              </div>
            </div>

            <!-- Botón de Siguiente Paso -->
            <div class="step-footer-actions">
              <div></div>
              <button
                type="button"
                class="btn-espresso"
                :disabled="!form.idCliente"
                @click="currentStep = 2"
              >
                <span>Continuar a Régimen y Plazos</span>
                <i class="pi pi-arrow-right"></i>
              </button>
            </div>
          </div>
        </section>

        <!-- ========================================================================= -->
        <!-- PASO 2: RÉGIMEN ADUANERO Y VALIDEZ -->
        <!-- ========================================================================= -->
        <!-- ========================================================================= -->
        <!-- PASO 2: RÉGIMEN, LOGÍSTICA & PLAZOS -->
        <!-- ========================================================================= -->
        <section v-show="currentStep === 2" class="form-block-card">
          <div class="block-header">
            <div class="block-idx">02</div>
            <div class="block-header-info">
              <h3 class="block-title">Régimen Aduanero, Logística Portuaria & Mercancía</h3>
              <p class="block-sub">Determine el régimen oficial (Importación / Exportación), puertos de tráfico, tipo de carga, partida arancelaria y servicio de transporte.</p>
            </div>
          </div>

          <div class="block-body">
            <!-- 1. Régimen Aduanero: Solo 2 Oficiales (Importación 10 y Exportación 40) -->
            <div class="field-group">
              <label class="field-label">Régimen Aduanero Oficial *</label>
              <div class="regimenes-grid two-cols">
                <div
                  v-for="reg in regimenOptions"
                  :key="reg.value"
                  :class="['regimen-card', { selected: form.regimenAduanero === reg.value }]"
                  @click="form.regimenAduanero = reg.value"
                >
                  <div class="regimen-header">
                    <span class="regimen-code-badge">{{ reg.codigo }}</span>
                    <div class="regimen-radio-indicator">
                      <i v-if="form.regimenAduanero === reg.value" class="pi pi-check-circle"></i>
                      <span v-else class="radio-circle"></span>
                    </div>
                  </div>
                  <h4 class="regimen-title">{{ reg.label }}</h4>
                  <p class="regimen-desc">{{ reg.desc }}</p>
                </div>
              </div>
            </div>

            <!-- 2. Ruta y Puertos (Origen & Destino desde API) -->
            <div class="field-row-grid">
              <div class="field-group">
                <label for="cot-puerto-envio" class="field-label">Puerto de Origen / Envío</label>
                <Select
                  id="cot-puerto-envio"
                  v-model="form.idPuertoEnvio"
                  :options="puertos"
                  optionLabel="nombre"
                  optionValue="idPuerto"
                  filter
                  filterPlaceholder="Buscar puerto o país..."
                  placeholder="Seleccione puerto de embarque..."
                  class="sga-custom-select"
                  :disabled="submitting || loadingCatalogos"
                >
                  <template #value="slotProps">
                    <div v-if="selectedPuertoEnvio" class="select-val-row">
                      <span class="badge-code-select">{{ selectedPuertoEnvio.codigoPuerto }}</span>
                      <span class="select-label-text">{{ selectedPuertoEnvio.nombre }} ({{ selectedPuertoEnvio.pais }})</span>
                      <span class="badge-type-pill">{{ selectedPuertoEnvio.tipo }}</span>
                    </div>
                    <span v-else class="select-placeholder-text">{{ slotProps.placeholder }}</span>
                  </template>
                  <template #option="slotProps">
                    <div class="select-opt-row">
                      <div class="opt-left">
                        <span class="badge-code-select">{{ slotProps.option.codigoPuerto }}</span>
                        <span class="opt-text">{{ slotProps.option.nombre }} ({{ slotProps.option.pais }})</span>
                      </div>
                      <span class="badge-type-pill">{{ slotProps.option.tipo }}</span>
                    </div>
                  </template>
                </Select>
                <span class="field-hint-mini">Terminal o puerto internacional de salida</span>
              </div>

              <div class="field-group">
                <label for="cot-puerto-destino" class="field-label">Puerto de Destino / Arribo</label>
                <Select
                  id="cot-puerto-destino"
                  v-model="form.idPuertoDestino"
                  :options="puertos"
                  optionLabel="nombre"
                  optionValue="idPuerto"
                  filter
                  filterPlaceholder="Buscar puerto o aduana..."
                  placeholder="Seleccione puerto de arribo..."
                  class="sga-custom-select"
                  :disabled="submitting || loadingCatalogos"
                >
                  <template #value="slotProps">
                    <div v-if="selectedPuertoDestino" class="select-val-row">
                      <span class="badge-code-select">{{ selectedPuertoDestino.codigoPuerto }}</span>
                      <span class="select-label-text">{{ selectedPuertoDestino.nombre }} ({{ selectedPuertoDestino.pais }})</span>
                      <span class="badge-type-pill">{{ selectedPuertoDestino.tipo }}</span>
                    </div>
                    <span v-else class="select-placeholder-text">{{ slotProps.placeholder }}</span>
                  </template>
                  <template #option="slotProps">
                    <div class="select-opt-row">
                      <div class="opt-left">
                        <span class="badge-code-select">{{ slotProps.option.codigoPuerto }}</span>
                        <span class="opt-text">{{ slotProps.option.nombre }} ({{ slotProps.option.pais }})</span>
                      </div>
                      <span class="badge-type-pill">{{ slotProps.option.tipo }}</span>
                    </div>
                  </template>
                </Select>
                <span class="field-hint-mini">Aduana o terminal de arribo nacional</span>
              </div>
            </div>

            <!-- 3. Tipo de Carga y Selección de Mercancía del Catálogo -->
            <div class="field-row-grid">
              <div class="field-group">
                <label for="cot-tipo-carga" class="field-label">Tipo de Carga / Unidad Logística</label>
                <Select
                  id="cot-tipo-carga"
                  v-model="form.idTipoCarga"
                  :options="tiposCarga"
                  optionLabel="nombre"
                  optionValue="idTipoCarga"
                  placeholder="Seleccione tipo de carga..."
                  class="sga-custom-select"
                  :disabled="submitting || loadingCatalogos"
                >
                  <template #value="slotProps">
                    <div v-if="selectedTipoCarga" class="select-val-row">
                      <span class="badge-code-select">{{ selectedTipoCarga.codigo }}</span>
                      <span class="select-label-text">{{ selectedTipoCarga.nombre }}</span>
                    </div>
                    <span v-else class="select-placeholder-text">{{ slotProps.placeholder }}</span>
                  </template>
                  <template #option="slotProps">
                    <div class="select-opt-row">
                      <span class="badge-code-select">{{ slotProps.option.codigo }}</span>
                      <span class="opt-text">{{ slotProps.option.nombre }}</span>
                    </div>
                  </template>
                </Select>
                <span class="field-hint-mini">Modalidad de estiba y transporte de la mercancía</span>
              </div>

              <div class="field-group">
                <label for="cot-mercancia" class="field-label">Catálogo de Mercancía (Partida Arancelaria)</label>
                <Select
                  id="cot-mercancia"
                  v-model="form.idMercancia"
                  :options="mercancias"
                  optionLabel="nombre"
                  optionValue="idMercancia"
                  filter
                  filterPlaceholder="Buscar por código, nombre o sector..."
                  placeholder="Seleccione mercancía clasificada..."
                  class="sga-custom-select"
                  :disabled="submitting || loadingCatalogos"
                  @change="handleMercanciaChange"
                >
                  <template #value="slotProps">
                    <div v-if="selectedMercancia" class="select-val-row">
                      <span class="badge-code-select">{{ selectedMercancia.codigo }}</span>
                      <span class="select-label-text">{{ selectedMercancia.nombre }}</span>
                      <Tag
                        v-if="selectedMercancia.requierePermiso"
                        :value="'Req: ' + selectedMercancia.entidadReguladora"
                        severity="warn"
                        rounded
                      />
                      <Tag
                        v-else
                        value="Libre Trámite"
                        severity="success"
                        rounded
                      />
                    </div>
                    <span v-else class="select-placeholder-text">{{ slotProps.placeholder }}</span>
                  </template>
                  <template #option="slotProps">
                    <div class="select-opt-row merc-opt-row">
                      <div class="opt-left">
                        <span class="badge-code-select">{{ slotProps.option.codigo }}</span>
                        <div class="opt-text-col">
                          <span class="opt-title-text">{{ slotProps.option.nombre }}</span>
                          <span class="opt-category-sub">{{ slotProps.option.categoria }} • Arancel Base: {{ slotProps.option.arancelBasePct }}%</span>
                        </div>
                      </div>
                      <Tag
                        v-if="slotProps.option.requierePermiso"
                        :value="'Req: ' + slotProps.option.entidadReguladora"
                        severity="warn"
                        rounded
                      />
                      <Tag
                        v-else
                        value="Libre"
                        severity="success"
                        rounded
                      />
                    </div>
                  </template>
                </Select>
                <span class="field-hint-mini">Partida del Arancel de Aduanas SUNAT</span>
              </div>
            </div>

            <!-- 4. ALERTA / VALIDACIÓN DE MERCANCÍA: LIBRE vs RESTRINGIDA (PERMISOS) -->
            <transition name="fade">
              <div v-if="selectedMercancia" class="mercancia-status-panel">
                <!-- Alerta cuando requiere permiso -->
                <div v-if="selectedMercancia.requierePermiso" class="regulatory-warning-banner">
                  <div class="warning-icon-box">
                    <i class="pi pi-exclamation-triangle"></i>
                  </div>
                  <div class="warning-content">
                    <div class="warning-kicker">MERCANCÍA RESTRINGIDA / REGULADA POR EL ESTADO</div>
                    <h4 class="warning-title">Requiere Permiso y Visto Bueno previo de: {{ selectedMercancia.entidadReguladora }}</h4>
                    <p class="warning-desc">
                      La partida <strong>{{ selectedMercancia.codigo }}</strong> ({{ selectedMercancia.nombre }}) está sujeta a control sectorial. Se debe gestionar y adjuntar la autorización oficial emitida por <strong>{{ selectedMercancia.entidadReguladora }}</strong> antes de la numeración aduanera.
                    </p>
                    <div class="warning-meta-pills">
                      <span class="meta-pill">Sector: {{ selectedMercancia.categoria }}</span>
                      <span class="meta-pill">Entidad: {{ selectedMercancia.entidadReguladora }}</span>
                      <span class="meta-pill">Arancel Ad-Valorem Base: {{ selectedMercancia.arancelBasePct }}%</span>
                    </div>
                  </div>
                </div>

                <!-- Aviso cuando es producto libre -->
                <div v-else class="free-goods-banner">
                  <i class="pi pi-check-circle"></i>
                  <div class="free-content">
                    <strong>Producto de Libre Trámite:</strong>
                    <span>La partida {{ selectedMercancia.codigo }} no está sujeta a regulaciones sectoriales ni permisos especiales previos. Tasa Ad-Valorem base: {{ selectedMercancia.arancelBasePct }}%.</span>
                  </div>
                </div>
              </div>
            </transition>

            <!-- 5. Contratación de Servicio de Transporte Local SGA -->
            <div class="field-group transport-option-box">
              <div class="label-with-badge">
                <label class="field-label">Servicio de Transporte Terrestre Local SGA *</label>
                <span class="kicker-mini-badge">FLOTA SGA</span>
              </div>
              <p class="transport-helper">¿El cliente contratará el servicio de transporte terrestre interno de SGA o utilizará transporte externo?</p>

              <div class="transport-toggle-grid">
                <div
                  :class="['transport-card-opt', { selected: form.servicioTransporte === true }]"
                  @click="form.servicioTransporte = true"
                >
                  <div class="opt-icon"><i class="pi pi-truck"></i></div>
                  <div class="opt-info">
                    <h4 class="opt-title">Sí, Transporte Interno SGA</h4>
                    <p class="opt-desc">Traslado desde terminal portuario/almacén hasta almacén del cliente con unidades monitoreadas por GPS de SGA.</p>
                  </div>
                  <i v-if="form.servicioTransporte === true" class="pi pi-check-circle check-active"></i>
                </div>

                <div
                  :class="['transport-card-opt', { selected: form.servicioTransporte === false }]"
                  @click="form.servicioTransporte = false"
                >
                  <div class="opt-icon"><i class="pi pi-building"></i></div>
                  <div class="opt-info">
                    <h4 class="opt-title">No, Transporte Externo por Cliente</h4>
                    <p class="opt-desc">El cliente gestionará el retiro y flete terrestre con sus propios transportistas o unidades de terceros.</p>
                  </div>
                  <i v-if="form.servicioTransporte === false" class="pi pi-check-circle check-active"></i>
                </div>
              </div>
            </div>

            <!-- 6. Validez de la Propuesta (Días) -->
            <div class="field-group highlight-group">
              <div class="label-with-badge">
                <label for="cot-validez" class="field-label">Validez de la Oferta Comercial (Días Calendario) *</label>
                <span class="kicker-mini-badge">PLAZO VIGENCIA</span>
              </div>
              <div class="input-icon-box">
                <i class="pi pi-calendar input-icon"></i>
                <input
                  id="cot-validez"
                  v-model.number="form.validezDias"
                  type="number"
                  min="1"
                  max="90"
                  class="form-input number-input"
                  required
                  :disabled="submitting"
                />
                <span class="input-suffix">DÍAS</span>
              </div>
              <p class="field-helper-text">
                Transcurrido este plazo, los fletes marítimos/aéreos y tarifas de honorarios quedarán sujetos a re-cotización.
              </p>

              <!-- Atajos de días -->
              <div class="quick-shortcuts-row">
                <span class="shortcuts-label">Plazos estándar:</span>
                <button type="button" class="shortcut-chip" @click="form.validezDias = 15">15 Días</button>
                <button type="button" class="shortcut-chip" @click="form.validezDias = 30">30 Días</button>
                <button type="button" class="shortcut-chip" @click="form.validezDias = 45">45 Días</button>
                <button type="button" class="shortcut-chip" @click="form.validezDias = 60">60 Días</button>
              </div>
            </div>

            <!-- 7. Observaciones y Términos Comerciales (Trasladado al Paso 2) -->
            <div class="field-group">
              <label for="cot-obs" class="field-label">Observaciones, Términos Comerciales y Requerimientos Especiales</label>
              <textarea
                id="cot-obs"
                v-model="form.observaciones"
                rows="3"
                placeholder="Indique detalles de tipo de bultos, requerimientos de cuadrilla en puerto/aeropuerto, almacenaje temporal contratado, etc."
                class="form-textarea"
                :disabled="submitting"
              ></textarea>
            </div>

            <!-- Botones de Navegación del Paso 2 -->
            <div class="step-footer-actions">
              <button
                type="button"
                class="btn-outline-warm"
                @click="currentStep = 1"
              >
                <i class="pi pi-arrow-left"></i>
                <span>Regresar a Cliente</span>
              </button>
              <button
                type="button"
                class="btn-espresso"
                @click="currentStep = 3"
              >
                <span>Continuar a Costos y Liquidación</span>
                <i class="pi pi-arrow-right"></i>
              </button>
            </div>
          </div>
        </section>

        <!-- ========================================================================= -->
        <!-- PASO 3: COSTOS Y LIQUIDACIÓN ESTIMADA -->
        <!-- ========================================================================= -->
        <section v-show="currentStep === 3" class="form-block-card">
          <div class="block-header">
            <div class="block-idx">03</div>
            <div class="block-header-info">
              <h3 class="block-title">Estructura Económica y Liquidación Estimada (USD)</h3>
              <p class="block-sub">Desglose los componentes de flete internacional, agenciamiento aduanero y el cálculo detallado de tributos arancelarios.</p>
            </div>
          </div>

          <div class="block-body">
            <!-- 1. Flete y Honorarios -->
            <div class="costs-two-cols">
              <!-- Flete Internacional -->
              <div class="field-group">
                <label for="cot-flete" class="field-label">Flete Internacional Estimado (USD)</label>
                <div class="input-icon-box">
                  <i class="pi pi-compass input-icon"></i>
                  <input
                    id="cot-flete"
                    v-model.number="form.montoFleteEstimado"
                    type="number"
                    step="0.01"
                    min="0"
                    placeholder="0.00"
                    class="form-input number-input"
                    @input="recalculateTotal"
                    :disabled="submitting"
                  />
                  <span class="input-suffix">USD</span>
                </div>
                <span class="field-hint-mini">Transporte marítimo / aéreo internacional</span>
              </div>

              <!-- Honorarios de Despacho -->
              <div class="field-group">
                <label for="cot-honorarios" class="field-label">Honorarios Agencia Aduanas (USD)</label>
                <div class="input-icon-box">
                  <i class="pi pi-briefcase input-icon"></i>
                  <input
                    id="cot-honorarios"
                    v-model.number="form.montoHonorariosEstimado"
                    type="number"
                    step="0.01"
                    min="0"
                    placeholder="0.00"
                    class="form-input number-input"
                    @input="recalculateTotal"
                    :disabled="submitting"
                  />
                  <span class="input-suffix">USD</span>
                </div>
                <span class="field-hint-mini">Servicios de despacho aduanero y comisión</span>
              </div>
            </div>

            <!-- 2. DESGLOSE DETALLADO DE TRIBUTOS ARANCELARIOS (SUMA AUTOMÁTICA) -->
            <div class="tributos-breakdown-card">
              <div class="tributos-card-header">
                <div class="tributos-header-left">
                  <i class="pi pi-percentage header-icon"></i>
                  <div>
                    <h4 class="tributos-card-title">Desglose de Tributos y Gravámenes SUNAT (USD)</h4>
                    <p class="tributos-card-desc">Ingrese el arancel Ad-Valorem e IGV/IPM. El sistema sumará automáticamente el tributo total estimado para la base de datos.</p>
                  </div>
                </div>
                <div class="tributos-total-badge">
                  <span class="badge-label">TRIBUTO ESTIMADO TOTAL</span>
                  <span class="badge-amount">${{ formatNumber(form.montoTributosEstimado) }} USD</span>
                </div>
              </div>

              <div class="tributos-inputs-grid">
                <!-- Ad-Valorem -->
                <div class="tributo-item">
                  <div class="tributo-item-top">
                    <label for="cot-advalorem" class="tributo-label">Arancel Ad-Valorem (USD)</label>
                    <span v-if="selectedMercancia" class="tributo-rate-pill">Tasa: {{ selectedMercancia.arancelBasePct }}%</span>
                  </div>
                  <div class="input-icon-box">
                    <i class="pi pi-dollar input-icon"></i>
                    <input
                      id="cot-advalorem"
                      v-model.number="form.adValoremEstimado"
                      type="number"
                      step="0.01"
                      min="0"
                      placeholder="0.00"
                      class="form-input number-input"
                      @input="recalculateTributos"
                      :disabled="submitting"
                    />
                  </div>
                  <span class="tributo-helper">Gravamen arancelario sobre valor CIF</span>
                </div>

                <!-- IGV / IPM (18%) -->
                <div class="tributo-item">
                  <div class="tributo-item-top">
                    <label for="cot-igv" class="tributo-label">IGV (16%) + IPM (2%) (USD)</label>
                    <span class="tributo-rate-pill">Tasa: 18%</span>
                  </div>
                  <div class="input-icon-box">
                    <i class="pi pi-dollar input-icon"></i>
                    <input
                      id="cot-igv"
                      v-model.number="form.igvEstimado"
                      type="number"
                      step="0.01"
                      min="0"
                      placeholder="0.00"
                      class="form-input number-input"
                      @input="recalculateTributos"
                      :disabled="submitting"
                    />
                  </div>
                  <span class="tributo-helper">Impuesto General a las Ventas y Promoción Municipal</span>
                </div>

                <!-- Otros Gravámenes / Percepción -->
                <div class="tributo-item">
                  <div class="tributo-item-top">
                    <label for="cot-otros-trib" class="tributo-label">Otros Gravámenes / Percepción (USD)</label>
                    <span class="tributo-rate-pill-optional">Opcional</span>
                  </div>
                  <div class="input-icon-box">
                    <i class="pi pi-dollar input-icon"></i>
                    <input
                      id="cot-otros-trib"
                      v-model.number="form.otrosTributosEstimado"
                      type="number"
                      step="0.01"
                      min="0"
                      placeholder="0.00"
                      class="form-input number-input"
                      @input="recalculateTributos"
                      :disabled="submitting"
                    />
                  </div>
                  <span class="tributo-helper">ISC, salvaguardias o percepción SUNAT</span>
                </div>
              </div>
            </div>

            <!-- Total Cotizado Liquidado -->
            <div class="total-summary-banner">
              <div class="total-banner-info">
                <span class="total-badge">IMPORTE TOTAL LIQUIDADO</span>
                <span class="total-sub">Suma consolidada de flete, honorarios y tributos estimados (Flete + Honorarios + Tributos)</span>
              </div>
              <div class="total-val-box">
                <span class="currency-symbol">$</span>
                <span class="total-number">{{ formatNumber(form.totalCotizado) }}</span>
                <span class="currency-code">USD</span>
              </div>
            </div>

            <!-- Botones de Navegación del Paso 3 -->
            <div class="step-footer-actions">
              <button
                type="button"
                class="btn-outline-warm"
                @click="currentStep = 2"
              >
                <i class="pi pi-arrow-left"></i>
                <span>Regresar a Régimen y Mercancía</span>
              </button>

              <button
                type="button"
                class="btn-espresso"
                @click="handleSubmit"
                :disabled="submitting || !isFormReady"
              >
                <i v-if="submitting" class="pi pi-spin pi-spinner"></i>
                <i v-else class="pi pi-check"></i>
                <span>{{ isEditMode ? 'Guardar Cambios' : 'Emitir Cotización' }}</span>
              </button>
            </div>
          </div>
        </section>
      </div>

      <!-- Columna Lateral Sticky: Previsualización en Vivo de la Proforma -->
      <aside class="form-sidebar-column">
        <!-- Tarjeta de Proforma Comercial en Vivo -->
        <div class="preview-proforma-card">
          <div class="card-ribbon">
            <i class="pi pi-file-edit"></i>
            <span>PROFORMA COMERCIAL OFICIAL</span>
          </div>

          <div class="proforma-code-row">
            <span class="doc-code-lg">{{ form.codigoCotizacion || 'COT-2026-BORRADOR' }}</span>
          </div>

          <div class="proforma-client-snippet">
            <span class="proforma-sub-label">CLIENTE DESTINATARIO:</span>
            <h4 class="proforma-client-name">
              {{ selectedClientObject ? selectedClientObject.razonSocial : 'Seleccione un cliente en Paso 1' }}
            </h4>
            <span v-if="selectedClientObject" class="proforma-client-doc">
              {{ selectedClientObject.tipoDocumento }}: {{ selectedClientObject.numeroDocumento }}
            </span>
          </div>

          <div class="proforma-details-list">
            <div class="proforma-row">
              <span class="p-label">Régimen Aduanero:</span>
              <span class="p-val">{{ getRegimenLabel(form.regimenAduanero) }}</span>
            </div>

            <div v-if="selectedPuertoEnvio || selectedPuertoDestino" class="proforma-row">
              <span class="p-label">Ruta Aduanera:</span>
              <span class="p-val">{{ selectedPuertoEnvio?.codigoPuerto || 'Origen' }} ➔ {{ selectedPuertoDestino?.codigoPuerto || 'Destino' }}</span>
            </div>

            <div v-if="selectedTipoCarga" class="proforma-row">
              <span class="p-label">Tipo de Carga:</span>
              <span class="p-val">{{ selectedTipoCarga.codigo }}</span>
            </div>

            <div v-if="selectedMercancia" class="proforma-row">
              <span class="p-label">Mercancía:</span>
              <div class="p-val-col">
                <span class="merc-code-text">{{ selectedMercancia.codigo }}</span>
                <span v-if="selectedMercancia.requierePermiso" class="badge-restringida-mini">
                  Req: {{ selectedMercancia.entidadReguladora }}
                </span>
                <span v-else class="badge-libre-mini">Libre</span>
              </div>
            </div>

            <div class="proforma-row">
              <span class="p-label">Transporte SGA:</span>
              <span :class="['p-val', form.servicioTransporte ? 'text-accent' : '']">
                {{ form.servicioTransporte ? '✓ Interno SGA' : 'Externo' }}
              </span>
            </div>

            <div class="proforma-row">
              <span class="p-label">Vigencia:</span>
              <span class="p-val">{{ form.validezDias }} Días Calendario</span>
            </div>

            <div class="proforma-divider"></div>

            <div class="proforma-row">
              <span class="p-label">Flete Estimado:</span>
              <span class="p-val">${{ formatNumber(form.montoFleteEstimado) }}</span>
            </div>

            <div class="proforma-row">
              <span class="p-label">Honorarios:</span>
              <span class="p-val">${{ formatNumber(form.montoHonorariosEstimado) }}</span>
            </div>

            <div class="proforma-row">
              <span class="p-label">Tributos Totales:</span>
              <span class="p-val">${{ formatNumber(form.montoTributosEstimado) }}</span>
            </div>

            <div class="proforma-divider"></div>

            <div class="proforma-row total-row">
              <span class="p-label-total">TOTAL ESTIMADO:</span>
              <span class="p-val-total">${{ formatNumber(form.totalCotizado) }} USD</span>
            </div>
          </div>

          <div class="card-footer-stamp">
            <i class="pi pi-check-circle"></i>
            <span>SISTEMA DE GESTIÓN ADUANERA • MÓDULO COMERCIAL</span>
          </div>
        </div>

        <!-- Tarjeta de Control de Calidad y Validación -->
        <div class="checklist-card">
          <h4 class="checklist-title">
            <i class="pi pi-list-check"></i>
            <span>Requisitos para Emisión</span>
          </h4>

          <ul class="checklist-items">
            <li :class="['checklist-row', { valid: !!form.idCliente }]">
              <i :class="form.idCliente ? 'pi pi-check-circle' : 'pi pi-circle'"></i>
              <span>Cliente o prospecto asignado</span>
            </li>
            <li :class="['checklist-row', { valid: !!form.regimenAduanero }]">
              <i :class="form.regimenAduanero ? 'pi pi-check-circle' : 'pi pi-circle'"></i>
              <span>Régimen aduanero determinado</span>
            </li>
            <li :class="['checklist-row', { valid: form.validezDias > 0 }]">
              <i :class="form.validezDias > 0 ? 'pi pi-check-circle' : 'pi pi-circle'"></i>
              <span>Plazo de validez especificado</span>
            </li>
            <li :class="['checklist-row', { valid: form.totalCotizado > 0 }]">
              <i :class="form.totalCotizado > 0 ? 'pi pi-check-circle' : 'pi pi-circle'"></i>
              <span>Monto total cotizado (> $0.00 USD)</span>
            </li>
          </ul>

          <div class="sidebar-action-buttons">
            <button
              type="button"
              class="btn-espresso full-width"
              @click="handleSubmit"
              :disabled="submitting || !isFormReady"
            >
              <i v-if="submitting" class="pi pi-spin pi-spinner"></i>
              <i v-else class="pi pi-save"></i>
              <span>{{ isEditMode ? 'Guardar Cambios' : 'Emitir Cotización' }}</span>
            </button>

            <button
              type="button"
              class="btn-outline-warm full-width"
              @click="$emit('cancel')"
              :disabled="submitting"
            >
              <span>Descartar y Regresar</span>
            </button>
          </div>
        </div>
      </aside>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch, onMounted } from 'vue'
import Select from 'primevue/select'
import Tag from 'primevue/tag'
import { useAuthStore } from '@/features/auth/stores/authStore'
import { ventasService } from '../services/ventasService'
import { catalogoService } from '@/features/admin/catalogos/services/catalogoService'

const props = defineProps({
  cotizacionToEdit: {
    type: Object,
    default: null
  },
  clientes: {
    type: Array,
    default: () => []
  }
})

const emit = defineEmits(['cancel', 'saved', 'client-created'])
const authStore = useAuthStore()

const currentStep = ref(1)
const submitting = ref(false)
const errorMessage = ref('')
const loadingCatalogos = ref(false)

// Catálogos auxiliares traídos desde API
const puertos = ref([])
const tiposCarga = ref([])
const mercancias = ref([])

// Función utilitaria para desenvolver listas del envoltorio ApiResponse
const unwrapList = (res) => {
  if (!res) return []
  if (Array.isArray(res)) return res
  if (Array.isArray(res.data)) return res.data
  return []
}

// Lista local reactiva de clientes para soportar alta inmediata
const localClientsList = ref([...props.clientes])

watch(
  () => props.clientes,
  (newClients) => {
    localClientsList.value = [...(newClients || [])]
  },
  { deep: true }
)

// Búsqueda de clientes
const clientSearchQuery = ref('')

// Formulario Rápido de Nuevo Cliente Inline
const showQuickClientForm = ref(false)
const savingQuickClient = ref(false)
const quickClientError = ref('')
const quickClient = ref({
  tipoDocumento: 'RUC',
  numeroDocumento: '',
  razonSocial: '',
  correoContacto: '',
  telefonoContacto: '',
  esProspecto: true
})

// Opciones de Régimen Aduanero Oficial (Únicamente los 2 solicitados)
const regimenOptions = [
  {
    value: 'IMPORTACION_DEFINITIVA',
    codigo: 'REG-10',
    label: 'Importación para el Consumo',
    desc: 'Ingreso legal de mercancías para consumo definitivo en el país previo pago de tributos arancelarios.'
  },
  {
    value: 'EXPORTACION_DEFINITIVA',
    codigo: 'REG-40',
    label: 'Exportación Definitiva',
    desc: 'Salida de mercancías nacionales o nacionalizadas del territorio aduanero para uso o consumo en el exterior.'
  }
]

// Estado del Formulario
const defaultForm = () => ({
  codigoCotizacion: '',
  idCliente: '',
  idUsuarioComercial: authStore.user?.idUsuario || 1,
  validezDias: 15,
  regimenAduanero: 'IMPORTACION_DEFINITIVA',
  idPuertoEnvio: null,
  idPuertoDestino: null,
  idTipoCarga: null,
  idMercancia: null,
  servicioTransporte: false,
  montoFleteEstimado: 0,
  montoHonorariosEstimado: 0,
  adValoremEstimado: 0,
  igvEstimado: 0,
  otrosTributosEstimado: 0,
  montoTributosEstimado: 0,
  totalCotizado: 0,
  observaciones: ''
})

const form = ref(defaultForm())

const isEditMode = computed(() => !!props.cotizacionToEdit && !!props.cotizacionToEdit.idCotizacion)

// Computed de Entidades Seleccionadas
const selectedClientObject = computed(() => {
  if (!form.value.idCliente) return null
  return localClientsList.value.find((c) => c.idCliente === form.value.idCliente) || null
})

const selectedMercancia = computed(() => {
  if (!form.value.idMercancia) return null
  return mercancias.value.find((m) => m.idMercancia === form.value.idMercancia) || null
})

const selectedPuertoEnvio = computed(() => {
  if (!form.value.idPuertoEnvio) return null
  return puertos.value.find((p) => p.idPuerto === form.value.idPuertoEnvio) || null
})

const selectedPuertoDestino = computed(() => {
  if (!form.value.idPuertoDestino) return null
  return puertos.value.find((p) => p.idPuerto === form.value.idPuertoDestino) || null
})

const selectedTipoCarga = computed(() => {
  if (!form.value.idTipoCarga) return null
  return tiposCarga.value.find((tc) => tc.idTipoCarga === form.value.idTipoCarga) || null
})

// Cargar catálogos desde backend con desenvolvimiento seguro
const loadCatalogos = async () => {
  loadingCatalogos.value = true
  try {
    const [puertosRes, tiposCargaRes, mercanciasRes] = await Promise.allSettled([
      catalogoService.getPuertos(true),
      catalogoService.getTiposCarga(true),
      catalogoService.getMercancias(true)
    ])
    if (puertosRes.status === 'fulfilled') {
      puertos.value = unwrapList(puertosRes.value)
    }
    if (tiposCargaRes.status === 'fulfilled') {
      tiposCarga.value = unwrapList(tiposCargaRes.value)
    }
    if (mercanciasRes.status === 'fulfilled') {
      mercancias.value = unwrapList(mercanciasRes.value)
    }
  } catch (err) {
    console.error('Error cargando catálogos para cotización:', err)
  } finally {
    loadingCatalogos.value = false
  }
}

onMounted(() => {
  loadCatalogos()
})

watch(
  () => props.cotizacionToEdit,
  (val) => {
    errorMessage.value = ''
    if (val && val.idCotizacion) {
      form.value = {
        codigoCotizacion: val.codigoCotizacion || '',
        idCliente: val.idCliente || '',
        idUsuarioComercial: val.idUsuarioComercial || authStore.user?.idUsuario || 1,
        validezDias: val.validezDias || 15,
        regimenAduanero: val.regimenAduanero || 'IMPORTACION_DEFINITIVA',
        idPuertoEnvio: val.idPuertoEnvio || null,
        idPuertoDestino: val.idPuertoDestino || null,
        idTipoCarga: val.idTipoCarga || null,
        idMercancia: val.idMercancia || null,
        servicioTransporte: val.servicioTransporte === true,
        montoFleteEstimado: Number(val.montoFleteEstimado || 0),
        montoHonorariosEstimado: Number(val.montoHonorariosEstimado || 0),
        adValoremEstimado: 0,
        igvEstimado: 0,
        otrosTributosEstimado: 0,
        montoTributosEstimado: Number(val.montoTributosEstimado || 0),
        totalCotizado: Number(val.totalCotizado || 0),
        observaciones: val.observaciones || ''
      }
      // Si ya hay montoTributosEstimado pero los desgloses vienen en 0, se mantiene montoTributosEstimado
    } else {
      form.value = defaultForm()
    }
  },
  { immediate: true }
)

// Filtrado de Clientes para el Selector
const filteredAvailableClients = computed(() => {
  const query = clientSearchQuery.value.toLowerCase().trim()
  if (!query) return localClientsList.value

  return localClientsList.value.filter((c) => {
    return (
      c.razonSocial?.toLowerCase().includes(query) ||
      c.numeroDocumento?.includes(query) ||
      c.correoContacto?.toLowerCase().includes(query)
    )
  })
})

const selectClient = (client) => {
  form.value.idCliente = client.idCliente
  clientSearchQuery.value = ''
}

const clearSelectedClient = () => {
  form.value.idCliente = ''
}

// Guardar nuevo cliente desde formulario rápido inline
const handleSaveQuickClient = async () => {
  quickClientError.value = ''
  if (!quickClient.value.numeroDocumento || !quickClient.value.razonSocial || !quickClient.value.correoContacto) {
    quickClientError.value = 'Complete los campos obligatorios: Documento, Razón Social y Correo.'
    return
  }

  savingQuickClient.value = true
  try {
    const createdClient = await ventasService.createCliente(quickClient.value)
    localClientsList.value.unshift(createdClient)
    form.value.idCliente = createdClient.idCliente
    showQuickClientForm.value = false
    quickClient.value = {
      tipoDocumento: 'RUC',
      numeroDocumento: '',
      razonSocial: '',
      correoContacto: '',
      telefonoContacto: '',
      esProspecto: true
    }
    emit('client-created', createdClient)
  } catch (err) {
    quickClientError.value = err.response?.data?.message || 'Error al registrar nuevo cliente.'
  } finally {
    savingQuickClient.value = false
  }
}

// Acción al cambiar mercancía
const handleMercanciaChange = () => {
  // Opcional: si la mercancía tiene arancel base y el usuario no ha ingresado tributos
}

// Recálculo dinámico de tributos (Ad-Valorem + IGV/IPM + Otros)
const recalculateTributos = () => {
  const adv = Number(form.value.adValoremEstimado) || 0
  const igv = Number(form.value.igvEstimado) || 0
  const otros = Number(form.value.otrosTributosEstimado) || 0
  form.value.montoTributosEstimado = Number((adv + igv + otros).toFixed(2))
  recalculateTotal()
}

// Recálculo dinámico de costos totales (Flete + Honorarios + Tributos)
const recalculateTotal = () => {
  const f = Number(form.value.montoFleteEstimado) || 0
  const h = Number(form.value.montoHonorariosEstimado) || 0
  const t = Number(form.value.montoTributosEstimado) || 0
  form.value.totalCotizado = Number((f + h + t).toFixed(2))
}

const formatNumber = (num) => {
  if (num === null || num === undefined || isNaN(num)) return '0.00'
  return Number(num).toLocaleString('en-US', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
}

const getRegimenLabel = (val) => {
  const match = regimenOptions.find((r) => r.value === val)
  return match ? `${match.label} (${match.codigo})` : val
}

const isFormReady = computed(() => {
  return (
    !!form.value.idCliente &&
    !!form.value.regimenAduanero &&
    form.value.validezDias > 0 &&
    form.value.totalCotizado > 0
  )
})

const handleSubmit = () => {
  errorMessage.value = ''
  if (!form.value.idCliente) {
    errorMessage.value = 'Debe seleccionar o registrar un cliente en el Paso 01.'
    currentStep.value = 1
    return
  }

  if (form.value.totalCotizado <= 0) {
    errorMessage.value = 'El total cotizado debe ser mayor a $0.00 USD. Configure los montos en el Paso 03.'
    currentStep.value = 3
    return
  }

  submitting.value = true
  emit('saved', {
    isEdit: isEditMode.value,
    cotizacionId: props.cotizacionToEdit?.idCotizacion,
    payload: {
      ...form.value,
      idUsuarioComercial: authStore.user?.idUsuario || form.value.idUsuarioComercial || 1
    }
  })
}
</script>

<style scoped>
.cotizacion-registration-form-view {
  display: flex;
  flex-direction: column;
  gap: var(--space-6, 1.5rem);
  padding-bottom: var(--space-10, 2.5rem);
  max-width: 1400px;
  margin: 0 auto;
}

/* 1. Header Bar */
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
  background: var(--color-bg-subtle);
  border-color: var(--color-border-subtle);
}

.btn-espresso {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: var(--space-2, 0.5rem);
  padding: 0.65rem 1.4rem;
  font-size: var(--font-size-sm, 0.875rem);
  font-weight: 700;
  border-radius: var(--radius-md, 0.5rem);
  border: none;
  background: var(--color-primary);
  color: var(--color-primary-contrast);
  cursor: pointer;
  transition: all var(--transition-fast);
  box-shadow: var(--shadow-sm);
}

.btn-espresso:hover:not(:disabled) {
  opacity: 0.92;
  transform: translateY(-1px);
}

.btn-espresso:disabled,
.btn-outline-warm:disabled {
  opacity: 0.5;
  cursor: not-allowed;
  transform: none;
}

/* Banner de Error */
.error-banner {
  display: flex;
  align-items: center;
  gap: var(--space-3, 0.75rem);
  padding: var(--space-4, 1rem) var(--space-5, 1.25rem);
  background: var(--color-danger-subtle);
  border: 1px solid var(--color-danger);
  border-radius: var(--radius-lg, 0.75rem);
  color: var(--color-danger);
  font-size: var(--font-size-sm, 0.875rem);
}

.error-content {
  flex: 1;
}

.error-close-btn {
  background: none;
  border: none;
  color: var(--color-danger);
  cursor: pointer;
  padding: 0.25rem;
}

/* Banner de Observación de Supervisión */
.revision-alert-banner {
  display: flex;
  align-items: flex-start;
  gap: var(--space-4, 1rem);
  padding: var(--space-4, 1rem) var(--space-5, 1.25rem);
  background: var(--color-warning-bg);
  border: 1px solid var(--color-warning-border);
  border-radius: var(--radius-lg, 0.75rem);
  color: var(--color-warning-text);
}

.revision-icon {
  font-size: 1.5rem;
  margin-top: 0.15rem;
  flex-shrink: 0;
}

.revision-body {
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
}

.revision-kicker {
  font-size: 0.6875rem;
  font-weight: 700;
  letter-spacing: 0.1em;
  text-transform: uppercase;
  opacity: 0.9;
}

.revision-title {
  font-size: 0.9375rem;
  font-weight: 700;
}

.revision-note {
  margin: 0.25rem 0;
  font-size: 0.875rem;
  line-height: 1.45;
  font-weight: 500;
}

.revision-hint {
  font-size: 0.75rem;
  opacity: 0.85;
}

/* Stepper Navigation Bar */
.stepper-nav-bar {
  display: flex;
  align-items: center;
  background: var(--color-bg-surface);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-xl, 1rem);
  padding: var(--space-3, 0.75rem) var(--space-6, 1.5rem);
  box-shadow: var(--shadow-sm);
  gap: var(--space-4, 1rem);
  overflow-x: auto;
}

.stepper-step-btn {
  display: flex;
  align-items: center;
  gap: var(--space-3, 0.75rem);
  background: none;
  border: none;
  cursor: pointer;
  padding: var(--space-2, 0.5rem) var(--space-3, 0.75rem);
  border-radius: var(--radius-lg, 0.75rem);
  transition: all var(--transition-fast);
  flex-shrink: 0;
}

.stepper-step-btn:hover {
  background: var(--color-bg-subtle);
}

.step-badge {
  width: 2.2rem;
  height: 2.2rem;
  border-radius: 50%;
  border: 2px solid var(--color-border);
  background: var(--color-bg-subtle);
  color: var(--color-text-muted);
  font-weight: 800;
  font-size: var(--font-size-xs, 0.75rem);
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all var(--transition-fast);
}

.stepper-step-btn.active .step-badge {
  background: var(--color-primary);
  color: var(--color-primary-contrast);
  border-color: var(--color-primary);
}

.stepper-step-btn.completed .step-badge {
  background: var(--color-success-subtle);
  color: var(--color-success);
  border-color: var(--color-success);
}

.step-info {
  display: flex;
  flex-direction: column;
  text-align: left;
}

.step-label {
  font-size: var(--font-size-2xs, 0.65rem);
  font-weight: 800;
  letter-spacing: 0.08em;
  color: var(--color-text-muted);
}

.step-title {
  font-size: var(--font-size-xs, 0.75rem);
  font-weight: 700;
  color: var(--color-text-main);
}

.stepper-step-btn.active .step-title {
  color: var(--color-primary);
}

.stepper-divider {
  flex: 1;
  height: 2px;
  background: var(--color-border-subtle);
  min-width: 2rem;
}

/* 2. Grid de Contenido */
.form-content-grid {
  display: grid;
  grid-template-columns: 1fr 390px;
  gap: var(--space-6, 1.5rem);
  align-items: start;
}

@media (max-width: 1120px) {
  .form-content-grid {
    grid-template-columns: 1fr;
  }
}

/* Bloques de Formulario */
.form-blocks-column {
  display: flex;
  flex-direction: column;
  gap: var(--space-6, 1.5rem);
}

.form-block-card {
  background: var(--color-bg-surface);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-xl, 1rem);
  padding: var(--space-6, 1.5rem) var(--space-8, 2rem);
  box-shadow: var(--shadow-sm);
}

.block-header {
  display: flex;
  align-items: flex-start;
  gap: var(--space-4, 1rem);
  margin-bottom: var(--space-6, 1.5rem);
  padding-bottom: var(--space-4, 1rem);
  border-bottom: 1px solid var(--color-border-subtle);
}

.block-idx {
  font-family: var(--font-family-serif);
  font-size: var(--font-size-lg, 1.25rem);
  font-weight: 800;
  color: var(--color-primary);
  background: var(--color-primary-subtle);
  width: 2.25rem;
  height: 2.25rem;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: var(--radius-md, 0.5rem);
  flex-shrink: 0;
}

.block-header-info {
  display: flex;
  flex-direction: column;
  gap: 0.2rem;
}

.block-title {
  font-size: var(--font-size-md, 1.05rem);
  font-weight: 800;
  color: var(--color-text-main);
  margin: 0;
}

.block-sub {
  font-size: var(--font-size-xs, 0.75rem);
  color: var(--color-text-muted);
  margin: 0;
}

.block-body {
  display: flex;
  flex-direction: column;
  gap: var(--space-5, 1.25rem);
}

/* Acciones de Búsqueda y Creación de Cliente */
.client-search-actions-bar {
  display: flex;
  gap: var(--space-3, 0.75rem);
  align-items: center;
  flex-wrap: wrap;
}

.search-input-box {
  position: relative;
  flex: 1;
  display: flex;
  align-items: center;
  min-width: 260px;
}

.search-icon {
  position: absolute;
  left: 0.9rem;
  color: var(--color-text-muted);
  font-size: 0.95rem;
  pointer-events: none;
}

.client-search-input {
  width: 100%;
  padding: 0.7rem 2.2rem 0.7rem 2.5rem;
  font-size: var(--font-size-sm, 0.875rem);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-md, 0.5rem);
  background: var(--color-bg-subtle);
  color: var(--color-text-main);
  transition: all var(--transition-fast);
}

.client-search-input:focus {
  outline: none;
  border-color: var(--color-primary);
  background: var(--color-bg-surface);
  box-shadow: 0 0 0 2px var(--color-primary-subtle);
}

.clear-query-btn {
  position: absolute;
  right: 0.75rem;
  background: none;
  border: none;
  color: var(--color-text-muted);
  cursor: pointer;
  padding: 0.25rem;
}

.btn-toggle-quick-client {
  display: inline-flex;
  align-items: center;
  gap: var(--space-2, 0.5rem);
  padding: 0.7rem 1.15rem;
  font-size: var(--font-size-sm, 0.875rem);
  font-weight: 700;
  border-radius: var(--radius-md, 0.5rem);
  border: 1px dashed var(--color-primary);
  background: var(--color-primary-subtle);
  color: var(--color-primary);
  cursor: pointer;
  transition: all var(--transition-fast);
}

.btn-toggle-quick-client:hover {
  background: var(--color-primary);
  color: var(--color-primary-contrast);
}

/* Panel de Alta Rápida Inline */
.quick-client-panel {
  border: 1px solid var(--color-border);
  border-radius: var(--radius-lg, 0.75rem);
  background: var(--color-bg-subtle);
  padding: var(--space-5, 1.25rem);
  display: flex;
  flex-direction: column;
  gap: var(--space-4, 1rem);
}

.quick-panel-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  border-bottom: 1px solid var(--color-border-subtle);
  padding-bottom: var(--space-3, 0.75rem);
}

.panel-header-title {
  display: flex;
  align-items: center;
  gap: var(--space-2, 0.5rem);
  font-size: var(--font-size-sm, 0.875rem);
  font-weight: 800;
  color: var(--color-text-main);
}

.panel-header-badge {
  font-size: var(--font-size-2xs, 0.65rem);
  font-weight: 800;
  letter-spacing: 0.08em;
  background: var(--color-primary-subtle);
  color: var(--color-primary);
  padding: 0.15rem 0.45rem;
  border-radius: var(--radius-sm, 0.25rem);
}

.quick-form-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: var(--space-3, 0.75rem);
}

.quick-error-msg {
  display: flex;
  align-items: center;
  gap: var(--space-2, 0.5rem);
  color: var(--color-danger);
  font-size: var(--font-size-xs, 0.75rem);
}

.quick-panel-actions {
  display: flex;
  justify-content: flex-end;
  gap: var(--space-3, 0.75rem);
  margin-top: var(--space-2, 0.5rem);
}

/* Tarjeta de Cliente Seleccionado */
.selected-client-card {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: var(--space-4, 1rem);
  padding: var(--space-4, 1rem) var(--space-5, 1.25rem);
  background: var(--color-primary-subtle);
  border: 1.5px solid var(--color-primary);
  border-radius: var(--radius-lg, 0.75rem);
}

.client-card-left {
  display: flex;
  align-items: center;
  gap: var(--space-4, 1rem);
}

.client-avatar-circle {
  width: 3rem;
  height: 3rem;
  border-radius: var(--radius-md, 0.5rem);
  background: var(--color-primary);
  color: var(--color-primary-contrast);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1.25rem;
  flex-shrink: 0;
}

.client-card-texts {
  display: flex;
  flex-direction: column;
  gap: 0.2rem;
}

.client-kicker-row {
  display: flex;
  align-items: center;
  gap: var(--space-2, 0.5rem);
}

.doc-badge {
  font-family: monospace;
  font-size: var(--font-size-2xs, 0.65rem);
  font-weight: 800;
  padding: 0.15rem 0.45rem;
  background: var(--color-bg-surface);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-sm, 0.25rem);
  color: var(--color-text-main);
}

.classification-pill {
  font-size: var(--font-size-2xs, 0.65rem);
  font-weight: 800;
  padding: 0.15rem 0.5rem;
  border-radius: var(--radius-full, 9999px);
}

.pill-client {
  background: var(--color-success-subtle);
  color: var(--color-success);
}

.pill-prospect {
  background: var(--color-warning-subtle);
  color: var(--color-warning);
}

.client-name {
  font-size: var(--font-size-md, 1rem);
  font-weight: 800;
  color: var(--color-text-main);
  margin: 0;
}

.client-meta-row {
  display: flex;
  align-items: center;
  gap: var(--space-3, 0.75rem);
  flex-wrap: wrap;
}

.meta-item {
  display: inline-flex;
  align-items: center;
  gap: 0.3rem;
  font-size: var(--font-size-xs, 0.75rem);
  color: var(--color-text-muted);
}

.btn-change-client {
  display: inline-flex;
  align-items: center;
  gap: var(--space-2, 0.5rem);
  padding: 0.5rem 0.85rem;
  border: 1px solid var(--color-border);
  border-radius: var(--radius-md, 0.5rem);
  background: var(--color-bg-surface);
  color: var(--color-text-main);
  font-size: var(--font-size-xs, 0.75rem);
  font-weight: 700;
  cursor: pointer;
  transition: all var(--transition-fast);
}

.btn-change-client:hover {
  border-color: var(--color-primary);
  color: var(--color-primary);
}

/* Lista de Selección de Clientes */
.clients-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(260px, 1fr));
  gap: var(--space-3, 0.75rem);
}

.client-select-card {
  padding: var(--space-3, 0.75rem) var(--space-4, 1rem);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-lg, 0.75rem);
  background: var(--color-bg-subtle);
  cursor: pointer;
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
  transition: all var(--transition-fast);
}

.client-select-card:hover {
  border-color: var(--color-primary);
  background: var(--color-bg-surface);
  transform: translateY(-1px);
}

.client-top-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.classification-pill-mini {
  font-size: var(--font-size-2xs, 0.65rem);
  font-weight: 800;
  padding: 0.1rem 0.4rem;
  border-radius: var(--radius-full, 9999px);
}

.client-card-name {
  font-size: var(--font-size-xs, 0.75rem);
  font-weight: 800;
  color: var(--color-text-main);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.client-card-email {
  font-size: var(--font-size-2xs, 0.65rem);
  color: var(--color-text-muted);
}

.empty-clients-box {
  padding: var(--space-6, 1.5rem);
  text-align: center;
  background: var(--color-bg-subtle);
  border-radius: var(--radius-lg, 0.75rem);
  border: 1px dashed var(--color-border);
}

.btn-link-action {
  background: none;
  border: none;
  color: var(--color-primary);
  font-weight: 700;
  text-decoration: underline;
  cursor: pointer;
}

/* Regímenes Grid */
.regimenes-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: var(--space-3, 0.75rem);
}

.regimen-card {
  padding: var(--space-4, 1rem);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-lg, 0.75rem);
  background: var(--color-bg-subtle);
  cursor: pointer;
  display: flex;
  flex-direction: column;
  gap: 0.35rem;
  transition: all var(--transition-fast);
}

.regimen-card:hover {
  border-color: var(--color-border-subtle);
  background: var(--color-bg-surface);
}

.regimen-card.selected {
  border-color: var(--color-primary);
  background: var(--color-primary-subtle);
  box-shadow: 0 0 0 1px var(--color-primary);
}

.regimen-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.regimen-code-badge {
  font-family: monospace;
  font-size: var(--font-size-2xs, 0.65rem);
  font-weight: 800;
  background: var(--color-bg-surface);
  border: 1px solid var(--color-border);
  padding: 0.15rem 0.5rem;
  border-radius: var(--radius-sm, 0.25rem);
  color: var(--color-primary);
}

.regimen-radio-indicator {
  color: var(--color-primary);
  font-size: 1rem;
}

.radio-circle {
  display: inline-block;
  width: 0.9rem;
  height: 0.9rem;
  border-radius: 50%;
  border: 1.5px solid var(--color-border);
}

.regimen-title {
  font-size: var(--font-size-sm, 0.875rem);
  font-weight: 800;
  color: var(--color-text-main);
  margin: 0;
}

.regimen-desc {
  font-size: var(--font-size-xs, 0.75rem);
  color: var(--color-text-muted);
  line-height: 1.35;
  margin: 0;
}

/* Desglose de Costos */
.costs-three-cols {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: var(--space-4, 1rem);
}

@media (max-width: 768px) {
  .costs-three-cols {
    grid-template-columns: 1fr;
  }
}

.field-hint-mini {
  font-size: var(--font-size-2xs, 0.65rem);
  color: var(--color-text-muted);
}

.total-summary-banner {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: var(--space-5, 1.25rem) var(--space-6, 1.5rem);
  background: var(--color-primary-subtle);
  border: 1px solid var(--color-primary);
  border-radius: var(--radius-xl, 1rem);
  gap: var(--space-4, 1rem);
  flex-wrap: wrap;
}

.total-banner-info {
  display: flex;
  flex-direction: column;
  gap: 0.2rem;
}

.total-badge {
  font-size: var(--font-size-2xs, 0.65rem);
  font-weight: 800;
  letter-spacing: 0.12em;
  color: var(--color-primary);
}

.total-sub {
  font-size: var(--font-size-xs, 0.75rem);
  color: var(--color-text-muted);
}

.total-val-box {
  display: flex;
  align-items: baseline;
  gap: 0.35rem;
  font-family: var(--font-family-serif);
  color: var(--color-primary);
}

.currency-symbol {
  font-size: var(--font-size-xl, 1.5rem);
  font-weight: 800;
}

.total-number {
  font-size: var(--font-size-3xl, 2.25rem);
  font-weight: 900;
  line-height: 1;
}

.currency-code {
  font-size: var(--font-size-xs, 0.75rem);
  font-weight: 800;
  letter-spacing: 0.05em;
}

/* Campos Generales */
.field-group {
  display: flex;
  flex-direction: column;
  gap: var(--space-2, 0.5rem);
}

.full-width {
  grid-column: 1 / -1;
}

.label-with-badge {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.field-label {
  font-size: var(--font-size-xs, 0.75rem);
  font-weight: 700;
  letter-spacing: 0.05em;
  text-transform: uppercase;
  color: var(--color-text-main);
}

.kicker-mini-badge {
  font-size: var(--font-size-2xs, 0.65rem);
  font-weight: 800;
  letter-spacing: 0.08em;
  text-transform: uppercase;
  background: var(--color-bg-subtle);
  color: var(--color-text-muted);
  padding: 0.15rem 0.45rem;
  border-radius: var(--radius-sm, 0.25rem);
}

.input-icon-box {
  position: relative;
  display: flex;
  align-items: center;
}

.input-icon {
  position: absolute;
  left: 0.9rem;
  color: var(--color-text-muted);
  font-size: 0.95rem;
  pointer-events: none;
}

.input-suffix {
  position: absolute;
  right: 0.9rem;
  font-size: var(--font-size-xs, 0.75rem);
  font-weight: 800;
  color: var(--color-text-muted);
  pointer-events: none;
}

.form-input,
.form-select,
.form-textarea {
  width: 100%;
  padding: 0.7rem 0.9rem 0.7rem 2.5rem;
  font-size: var(--font-size-sm, 0.875rem);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-md, 0.5rem);
  background: var(--color-bg-subtle);
  color: var(--color-text-main);
  transition: all var(--transition-fast);
}

.form-select {
  padding-left: 0.9rem;
}

.form-textarea {
  padding: 0.75rem;
  resize: vertical;
}

.number-input {
  padding-right: 3rem;
  font-weight: 700;
  font-family: monospace;
}

.form-input:focus,
.form-select:focus,
.form-textarea:focus {
  outline: none;
  border-color: var(--color-primary);
  background: var(--color-bg-surface);
  box-shadow: 0 0 0 2px var(--color-primary-subtle);
}

.field-helper-text {
  font-size: var(--font-size-xs, 0.75rem);
  color: var(--color-text-muted);
  margin: 0;
  line-height: 1.4;
}

.quick-shortcuts-row {
  display: flex;
  align-items: center;
  gap: var(--space-2, 0.5rem);
  flex-wrap: wrap;
  margin-top: 0.25rem;
}

.shortcuts-label {
  font-size: var(--font-size-2xs, 0.65rem);
  font-weight: 700;
  color: var(--color-text-muted);
  text-transform: uppercase;
  letter-spacing: 0.05em;
}

.shortcut-chip {
  background: var(--color-bg-subtle);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-full, 9999px);
  padding: 0.2rem 0.55rem;
  font-size: var(--font-size-2xs, 0.65rem);
  font-weight: 700;
  color: var(--color-text-main);
  cursor: pointer;
  transition: all var(--transition-fast);
}

.shortcut-chip:hover {
  background: var(--color-primary-subtle);
  border-color: var(--color-primary);
  color: var(--color-primary);
}

.step-footer-actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-top: 1px solid var(--color-border-subtle);
  padding-top: var(--space-5, 1.25rem);
  margin-top: var(--space-2, 0.5rem);
}

/* 3. Columna Lateral Sticky */
.form-sidebar-column {
  position: sticky;
  top: 1.5rem;
  display: flex;
  flex-direction: column;
  gap: var(--space-5, 1.25rem);
}

/* Tarjeta Proforma Comercial */
.preview-proforma-card {
  background: var(--color-bg-surface);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-xl, 1rem);
  padding: var(--space-6, 1.5rem);
  box-shadow: var(--shadow-sm);
  display: flex;
  flex-direction: column;
  position: relative;
  overflow: hidden;
}

.card-ribbon {
  display: inline-flex;
  align-items: center;
  gap: 0.35rem;
  font-size: var(--font-size-2xs, 0.65rem);
  font-weight: 800;
  letter-spacing: 0.1em;
  color: var(--color-primary);
  background: var(--color-primary-subtle);
  padding: 0.25rem 0.65rem;
  border-radius: var(--radius-full, 9999px);
  margin-bottom: var(--space-3, 0.75rem);
  align-self: flex-start;
}

.proforma-code-row {
  margin-bottom: var(--space-3, 0.75rem);
}

.doc-code-lg {
  font-family: monospace;
  font-size: var(--font-size-md, 1rem);
  font-weight: 800;
  letter-spacing: 0.05em;
  color: var(--color-text-main);
  background: var(--color-bg-subtle);
  border: 1px solid var(--color-border);
  padding: 0.3rem 0.75rem;
  border-radius: var(--radius-md, 0.5rem);
  display: inline-block;
}

.proforma-client-snippet {
  display: flex;
  flex-direction: column;
  gap: 0.15rem;
  margin-bottom: var(--space-4, 1rem);
  padding-bottom: var(--space-3, 0.75rem);
  border-bottom: 1px solid var(--color-border-subtle);
}

.proforma-sub-label {
  font-size: var(--font-size-2xs, 0.65rem);
  font-weight: 700;
  letter-spacing: 0.06em;
  color: var(--color-text-muted);
}

.proforma-client-name {
  font-family: var(--font-family-serif);
  font-size: var(--font-size-sm, 0.875rem);
  font-weight: 800;
  color: var(--color-text-main);
  margin: 0;
  line-height: 1.3;
}

.proforma-client-doc {
  font-family: monospace;
  font-size: var(--font-size-2xs, 0.65rem);
  color: var(--color-primary);
  font-weight: 700;
}

.proforma-details-list {
  display: flex;
  flex-direction: column;
  gap: var(--space-2, 0.5rem);
  margin-bottom: var(--space-5, 1.25rem);
}

.proforma-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: var(--font-size-xs, 0.75rem);
  gap: var(--space-2, 0.5rem);
}

.p-label {
  color: var(--color-text-muted);
  font-weight: 600;
}

.p-val {
  color: var(--color-text-main);
  font-weight: 700;
  text-align: right;
}

.proforma-divider {
  height: 1px;
  background: var(--color-border-subtle);
  margin: 0.25rem 0;
}

.total-row {
  padding-top: var(--space-2, 0.5rem);
}

.p-label-total {
  font-size: var(--font-size-xs, 0.75rem);
  font-weight: 800;
  letter-spacing: 0.05em;
  color: var(--color-text-main);
}

.p-val-total {
  font-family: var(--font-family-serif);
  font-size: var(--font-size-md, 1.15rem);
  font-weight: 900;
  color: var(--color-primary);
}

.card-footer-stamp {
  display: flex;
  align-items: center;
  gap: 0.35rem;
  font-size: var(--font-size-2xs, 0.65rem);
  color: var(--color-text-muted);
  letter-spacing: 0.05em;
  font-weight: 700;
}

/* Tarjeta Checklist */
.checklist-card {
  background: var(--color-bg-surface);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-xl, 1rem);
  padding: var(--space-5, 1.25rem);
  box-shadow: var(--shadow-sm);
  display: flex;
  flex-direction: column;
  gap: var(--space-4, 1rem);
}

.checklist-title {
  display: flex;
  align-items: center;
  gap: var(--space-2, 0.5rem);
  font-size: var(--font-size-xs, 0.75rem);
  font-weight: 800;
  letter-spacing: 0.05em;
  text-transform: uppercase;
  color: var(--color-text-main);
  margin: 0;
}

.checklist-items {
  list-style: none;
  padding: 0;
  margin: 0;
  display: flex;
  flex-direction: column;
  gap: var(--space-2, 0.5rem);
}

.checklist-row {
  display: flex;
  align-items: center;
  gap: var(--space-2, 0.5rem);
  font-size: var(--font-size-xs, 0.75rem);
  color: var(--color-text-muted);
  transition: color var(--transition-fast);
}

.checklist-row.valid {
  color: var(--color-success);
  font-weight: 600;
}

.sidebar-action-buttons {
  display: flex;
  flex-direction: column;
  gap: var(--space-2, 0.5rem);
  margin-top: var(--space-2, 0.5rem);
}

.full-width {
  width: 100%;
}

/* Layout y Campos Agrupados */
.two-cols {
  grid-template-columns: repeat(2, minmax(0, 1fr)) !important;
}

.field-row-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: var(--space-4, 1rem);
}

.costs-two-cols {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: var(--space-4, 1rem);
}

.field-hint-mini {
  font-size: var(--font-size-2xs, 0.65rem);
  color: var(--color-text-subtle);
  margin-top: 0.35rem;
  display: block;
}

/* =========================================================================
   ESTILOS PERSONALIZADOS PARA PRIMEVUE SELECT (Conformidad reglaArq.md)
   ========================================================================= */
.sga-custom-select {
  width: 100%;
  background: var(--color-bg-surface) !important;
  border: 1px solid var(--color-border) !important;
  border-radius: var(--radius-lg, 0.75rem) !important;
  color: var(--color-text-main) !important;
  transition: all var(--transition-fast);
}

.sga-custom-select:hover {
  border-color: var(--color-border-strong) !important;
}

.sga-custom-select:focus-within {
  border-color: var(--color-primary) !important;
  box-shadow: 0 0 0 3px var(--color-border-subtle) !important;
}

.select-val-row {
  display: flex;
  align-items: center;
  gap: var(--space-2, 0.5rem);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.select-placeholder-text {
  color: var(--color-text-subtle);
  font-size: var(--font-size-sm, 0.875rem);
}

.select-label-text {
  font-size: var(--font-size-sm, 0.875rem);
  font-weight: 600;
  color: var(--color-text-main);
}

.badge-code-select {
  font-family: var(--font-family-mono, monospace);
  font-size: var(--font-size-2xs, 0.65rem);
  font-weight: 700;
  padding: 0.15rem 0.45rem;
  background: var(--color-bg-subtle);
  border: 1px solid var(--color-border);
  color: var(--color-text-main);
  border-radius: var(--radius-sm, 0.25rem);
}

.badge-type-pill {
  font-size: var(--font-size-2xs, 0.65rem);
  font-weight: 600;
  padding: 0.1rem 0.35rem;
  background: var(--color-bg-muted);
  color: var(--color-text-muted);
  border-radius: var(--radius-sm, 0.25rem);
}

.select-opt-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  width: 100%;
  padding: 0.25rem 0;
  gap: var(--space-3, 0.75rem);
}

.select-opt-row .opt-left {
  display: flex;
  align-items: center;
  gap: var(--space-2, 0.5rem);
  overflow: hidden;
}

.select-opt-row .opt-text {
  font-size: var(--font-size-sm, 0.875rem);
  color: inherit;
}

.merc-opt-row .opt-text-col {
  display: flex;
  flex-direction: column;
  gap: 0.1rem;
}

.opt-title-text {
  font-size: var(--font-size-xs, 0.75rem);
  font-weight: 600;
  color: inherit;
}

.opt-category-sub {
  font-size: var(--font-size-2xs, 0.65rem);
  color: var(--color-text-muted);
}

/* =========================================================================
   ALERTAS DE MERCANCÍA: RESTRINGIDA vs LIBRE (Regla 1 & Regla 4 reglaArq.md)
   ========================================================================= */
.mercancia-status-panel {
  margin: var(--space-2, 0.5rem) 0 var(--space-4, 1rem);
}

.regulatory-warning-banner {
  display: flex;
  gap: var(--space-4, 1rem);
  background: var(--color-warning-bg);
  border: 1px solid var(--color-warning-border);
  border-left: 5px solid var(--color-warning-text);
  border-radius: var(--radius-lg, 0.75rem);
  padding: var(--space-4, 1rem);
  color: var(--color-warning-text);
}

.warning-icon-box {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 2.5rem;
  height: 2.5rem;
  border-radius: var(--radius-md, 0.5rem);
  background: var(--color-bg-surface);
  color: var(--color-warning-text);
  font-size: 1.25rem;
  flex-shrink: 0;
  border: 1px solid var(--color-warning-border);
}

.warning-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 0.35rem;
}

.warning-kicker {
  font-size: var(--font-size-2xs, 0.65rem);
  font-weight: 800;
  letter-spacing: 0.08em;
  color: var(--color-warning-text);
  text-transform: uppercase;
}

.warning-title {
  margin: 0;
  font-size: var(--font-size-sm, 0.875rem);
  font-weight: 700;
  color: var(--color-text-main);
}

.warning-desc {
  margin: 0;
  font-size: var(--font-size-xs, 0.75rem);
  line-height: 1.45;
  color: var(--color-text-muted);
}

.warning-meta-pills {
  display: flex;
  flex-wrap: wrap;
  gap: 0.5rem;
  margin-top: 0.25rem;
}

.meta-pill {
  font-size: var(--font-size-2xs, 0.65rem);
  font-weight: 600;
  background: var(--color-bg-surface);
  color: var(--color-warning-text);
  border: 1px solid var(--color-warning-border);
  padding: 0.2rem 0.5rem;
  border-radius: 9999px;
}

.free-goods-banner {
  display: flex;
  align-items: center;
  gap: var(--space-3, 0.75rem);
  background: var(--color-success-bg);
  border: 1px solid var(--color-success-border);
  border-left: 4px solid var(--color-success-text);
  border-radius: var(--radius-lg, 0.75rem);
  padding: 0.75rem 1rem;
  color: var(--color-success-text);
  font-size: var(--font-size-xs, 0.75rem);
}

.free-content {
  display: flex;
  gap: 0.35rem;
  flex-wrap: wrap;
}

/* =========================================================================
   SELECTOR DE TRANSPORTE TERRESTRE SGA (Regla 3 reglaArq.md)
   ========================================================================= */
.transport-option-box {
  margin-top: var(--space-2, 0.5rem);
}

.transport-helper {
  font-size: var(--font-size-xs, 0.75rem);
  color: var(--color-text-muted);
  margin-bottom: var(--space-3, 0.75rem);
}

.transport-toggle-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: var(--space-4, 1rem);
}

.transport-card-opt {
  display: flex;
  align-items: flex-start;
  gap: var(--space-3, 0.75rem);
  padding: var(--space-4, 1rem);
  border: 1.5px solid var(--color-border);
  border-radius: var(--radius-lg, 0.75rem);
  background: var(--color-bg-surface);
  cursor: pointer;
  transition: all var(--transition-fast);
  position: relative;
}

.transport-card-opt:hover {
  border-color: var(--color-border-strong);
  background: var(--color-bg-subtle);
  transform: translateY(-1px);
}

.transport-card-opt.selected {
  border-color: var(--color-primary);
  background: var(--color-bg-subtle);
  box-shadow: var(--shadow-sm);
}

.opt-icon {
  font-size: 1.25rem;
  color: var(--color-primary);
  display: flex;
  align-items: center;
  justify-content: center;
  width: 2.25rem;
  height: 2.25rem;
  border-radius: var(--radius-md, 0.5rem);
  background: var(--color-bg-muted);
  flex-shrink: 0;
}

.opt-info {
  flex: 1;
}

.opt-title {
  margin: 0 0 0.25rem 0;
  font-size: var(--font-size-xs, 0.75rem);
  font-weight: 700;
  color: var(--color-text-main);
}

.opt-desc {
  margin: 0;
  font-size: var(--font-size-2xs, 0.65rem);
  color: var(--color-text-muted);
  line-height: 1.35;
}

.check-active {
  position: absolute;
  top: 0.75rem;
  right: 0.75rem;
  color: var(--color-primary);
  font-size: 1rem;
}

/* =========================================================================
   TARJETA DESGLOSE DE TRIBUTOS SUNAT (Regla 1 & Regla 3 reglaArq.md)
   ========================================================================= */
.tributos-breakdown-card {
  background: var(--color-bg-surface);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-xl, 1rem);
  padding: var(--space-5, 1.25rem);
  display: flex;
  flex-direction: column;
  gap: var(--space-4, 1rem);
  box-shadow: var(--shadow-sm);
}

.tributos-card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  flex-wrap: wrap;
  gap: var(--space-3, 0.75rem);
  border-bottom: 1px solid var(--color-border-subtle);
  padding-bottom: var(--space-3, 0.75rem);
}

.tributos-header-left {
  display: flex;
  align-items: center;
  gap: var(--space-3, 0.75rem);
}

.header-icon {
  font-size: 1.35rem;
  color: var(--color-primary);
}

.tributos-card-title {
  margin: 0;
  font-size: var(--font-size-sm, 0.875rem);
  font-weight: 700;
  color: var(--color-text-main);
}

.tributos-card-desc {
  margin: 0.15rem 0 0 0;
  font-size: var(--font-size-2xs, 0.65rem);
  color: var(--color-text-muted);
}

.tributos-total-badge {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  background: var(--color-bg-subtle);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-md, 0.5rem);
  padding: 0.4rem 0.75rem;
}

.badge-label {
  font-size: var(--font-size-2xs, 0.65rem);
  font-weight: 700;
  color: var(--color-text-muted);
}

.badge-amount {
  font-family: var(--font-family-serif);
  font-size: var(--font-size-md, 1.1rem);
  font-weight: 800;
  color: var(--color-primary);
}

.tributos-inputs-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: var(--space-4, 1rem);
}

.tributo-item {
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
}

.tributo-item-top {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.tributo-label {
  font-size: var(--font-size-xs, 0.75rem);
  font-weight: 600;
  color: var(--color-text-main);
}

.tributo-rate-pill {
  font-size: var(--font-size-2xs, 0.65rem);
  font-weight: 700;
  background: var(--color-bg-subtle);
  border: 1px solid var(--color-border);
  color: var(--color-primary);
  padding: 0.1rem 0.4rem;
  border-radius: var(--radius-sm, 0.25rem);
}

.tributo-rate-pill-optional {
  font-size: var(--font-size-2xs, 0.65rem);
  color: var(--color-text-subtle);
}

.tributo-helper {
  font-size: var(--font-size-2xs, 0.65rem);
  color: var(--color-text-muted);
  line-height: 1.25;
}

/* Sidebar Live Badges */
.p-val-col {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 0.15rem;
}

.merc-code-text {
  font-weight: 700;
  font-size: var(--font-size-xs, 0.75rem);
  color: var(--color-text-main);
}

.badge-restringida-mini {
  font-size: var(--font-size-2xs, 0.65rem);
  background: var(--color-warning-bg);
  color: var(--color-warning-text);
  border: 1px solid var(--color-warning-border);
  padding: 0.1rem 0.35rem;
  border-radius: var(--radius-sm, 0.25rem);
  font-weight: 700;
}

.badge-libre-mini {
  font-size: var(--font-size-2xs, 0.65rem);
  background: var(--color-success-bg);
  color: var(--color-success-text);
  border: 1px solid var(--color-success-border);
  padding: 0.1rem 0.35rem;
  border-radius: var(--radius-sm, 0.25rem);
  font-weight: 700;
}

.text-accent {
  color: var(--color-primary) !important;
  font-weight: 700;
}

@media (max-width: 768px) {
  .two-cols,
  .field-row-grid,
  .costs-two-cols,
  .transport-toggle-grid,
  .tributos-inputs-grid {
    grid-template-columns: 1fr !important;
  }
}
</style>
