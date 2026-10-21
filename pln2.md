# pln2 — Implementaciones Finales

> Alcance: **solo plan**. No se modifica código hasta aprobar este documento.
> Rama de trabajo: **`implementacion-final`** (se crea desde `feature/roles-definitivos`, que es la rama activa actual).
> Este documento es **incremental**: cada bloque de cambios que indique el usuario se agrega como una sección nueva (§A, §B…).

---

## Cambio A — «Emitir Cotización Comercial»: nuevo paso *Documentación* + rediseño de *Costos & Liquidación*

### A.0 Estado actual (resumen del análisis)

| Pieza | Archivo | Qué hace hoy |
| :-- | :-- | :-- |
| Formulario (3 pasos) | `frontend/src/features/roles-modules/components/CotizacionRegistrationForm.vue` | Secciones `v-show="currentStep === 1/2/3"`; `currentStep` es un `ref`; paso 3 = costos + botón *Emitir/Guardar* |
| Stepper | `.../components/CotizacionStepperNav.vue` | 3 botones fijos (Cliente · Régimen & Plazos · Costos & Liquidación) con `completed` calculado desde `form` |
| Vista de preview | `.../components/CotizacionProformaPreview.vue` | Panel lateral sticky que lee `form` |
| Botón **Docs** | `.../views/RecepcionComercialView.vue` (L212-220, L536-545, L583-595) | Abre `shared/components/DocumentosManagerModal.vue` con `entidad-tipo="COTIZACION"` + `id-entidad` |
| Servicio docs | `features/documentos/services/documentoService.js` | `upload(files, {categoria, descripcion, entidadTipo, idEntidad, idUsuario})` → `POST /documentos/upload` |
| Backend docs | `DocumentoController` / `DocumentoServiceImpl` | Si `idEntidad` viene, valida que la cotización **exista** (`findById`); si no existe lanza `ResourceNotFound` |
| Guardado | `RecepcionComercialView.handleCotizacionSaved` | `createCotizacion(payload)` y **descarta la respuesta** (no usa el id creado) |
| Costos | `Cotizacion` (entity) / `CotizacionRequestDTO` / `CotizacionServiceImpl` | Solo persiste `montoFleteEstimado`, `montoHonorariosEstimado`, `montoTributosEstimado`, `totalCotizado` |

**Hallazgos relevantes**

1. **Un documento no puede subirse antes de que exista la cotización** (el backend valida el id). En «Emitir» (modo crear) aún no hay `idCotizacion` → hay que **encolar archivos en el formulario y subirlos justo después de crear la cotización**.
2. Hoy `adValoremEstimado`, `igvEstimado` y `otrosTributosEstimado` **no se guardan** (solo su suma); al editar una cotización vuelven a 0. El nuevo cuadro lo resuelve guardando los insumos (FOB, tasas) y recalculando.
3. `ddl-auto=update` está activo (`application.properties`), por lo que las columnas nuevas se crean solas; no hay migraciones Flyway/Liquibase.

---

### A.1 Nuevo paso «Documentación» (Paso 3 de 4)

**Nuevo orden del stepper**

| Paso | Título | Cambio |
| :-: | :-- | :-- |
| 01 | Cliente Solicitante | sin cambio |
| 02 | Régimen & Plazos | sin cambio |
| **03** | **Documentación** | **nuevo** |
| 04 | Costos & Liquidación | era el 03 |

**Frontend**

1. **Componente reutilizable `DocumentosUploadPanel.vue`** (en `shared/components/`), extrayendo de `DocumentosManagerModal.vue` la parte de dropzone, categoría, descripción, cola de archivos, lista de adjuntos, previsualizar/descargar/eliminar y los estilos. Dos modos:
   - **Diferido (`deferred`)** – crear cotización: no llama al API; mantiene `queuedFiles` (con categoría/descripción por archivo) y los expone con `v-model`.
   - **Directo (`live`)** – editar cotización (ya hay `idCotizacion`): sube/lista/elimina en el momento, igual que hoy.
2. `DocumentosManagerModal.vue` pasa a envolver `DocumentosUploadPanel` en modo `live` → el botón **Docs** de la bandeja sigue funcionando igual (regresión cero) y la lógica queda en un solo lugar.
3. `CotizacionRegistrationForm.vue`:
   - Insertar `<section v-show="currentStep === 3">` con cabecera `block-idx 03`, título «Documentación Sustentatoria», y el panel.
   - Renumerar costos a `currentStep === 4` (`block-idx 04`).
   - Actualizar navegación: 2 → *Continuar a Documentación*; 3 → *Regresar a Régimen* / *Continuar a Costos y Liquidación*; 4 → *Regresar a Documentación* + *Emitir/Guardar*.
   - En `handleSubmit`: el atajo de error de total pasa de `currentStep = 3` a `4`.
   - Nuevo estado `queuedDocs` y emitirlo en `saved` junto al payload: `emit('saved', { isEdit, cotizacionId, payload, documentos: queuedDocs })`.
   - Banner contextual: si la mercancía seleccionada `requierePermiso`, mostrar aviso «Adjunte el permiso de {entidadReguladora}» y preseleccionar categoría `PERMISO_ESPECIAL`.
4. `CotizacionStepperNav.vue`: agregar el 4.º botón y divider; `completed` del paso 3 = `docsCount > 0` (nueva prop `docsCount`); el paso 4 usa `form.totalCotizado > 0`.
5. `RecepcionComercialView.handleCotizacionSaved`: tomar el `idCotizacion` devuelto por `createCotizacion` y, si hay `documentos` en cola, llamar `documentoService.upload(files, {entidadTipo:'COTIZACION', idEntidad, idUsuario})`. Si la subida falla, **la cotización ya queda creada**: avisar con toast de advertencia («Cotización creada, pero algunos documentos no se cargaron; use el botón Docs») en lugar de revertir.
6. `CotizacionProformaPreview.vue`: opcionalmente mostrar «N documentos adjuntos».

**Backend**: sin cambios (el endpoint `/documentos/upload` ya soporta el flujo). Verificar solo el límite multipart (`spring.servlet.multipart.max-file-size`).

---

### A.2 Rediseño del cuadro «Costos & Liquidación» (Paso 4)

Según el cuadro «Resumen de campos (Inputs vs. Automáticos)»:

| Campo | Acción | Comportamiento | Fórmula / detalle |
| :-- | :-- | :-- | :-- |
| Valor de Mercancía (FOB) | **Agregar** (obligatorio) | Manual | Monto de la factura comercial. Sin él no hay base de cálculo |
| Flete Internacional | Mantener | Manual | Costo del flete marítimo/aéreo |
| Seguro Estimado | **Agregar** | Automático **editable** | Por defecto `fob × 0.0075`; si el usuario lo edita se respeta su valor (botón «Restablecer automático») |
| Valor CIF | **Agregar** (badge/campo) | Auto, solo lectura | `cif = fob + flete + seguro` |
| Gastos Operativos y Agencia | **Renombrar** el actual «Honorarios Agencia Aduanas» | Manual / tarifa plana | Honorarios + visto bueno + almacén + flete local (ref. USD 800–1 500) |
| Tasa Ad-Valórem | Cambiar a **selector** | Dropdown | `0%`, `6%`, `11%` (según subpartida) |
| Monto Ad-Valórem | Mantener | Auto, solo lectura | `advalorem = cif × tasa_advalorem` |
| IGV (16%) + IPM (2%) | Mantener | Auto, solo lectura | `igv_ipm = (cif + advalorem) × 0.18` |
| Percepción SUNAT | Mantener (opcional) | Selector + auto | Selector `3.5%` (frecuente), `5%`, `10%` (primera importación) + «Sin percepción»; `percepcion = (cif + advalorem + igv_ipm) × tasa_percepcion` |
| Tributo Estimado Total | Mantener | Auto, solo lectura | `advalorem + igv_ipm + percepcion` |
| Importe Total Liquidado | Mantener | Auto, solo lectura | `flete + gastos_operativos_agencia + tributos_totales` |

> El **Importe Total** sigue sin incluir FOB ni seguro (son valor de mercancía/base imponible, no costo del servicio SGA), tal como lo define el cuadro.

---

#### A.2.1 Diferenciación de Cobros y Liquidación: Importación vs. Exportación (REG-40)

En la legislación aduanera peruana (Ley General de Aduanas - D.L. 1053 y TUO del IGV) y la práctica operativa de agencias de aduanas, el tratamiento de costos y liquidación difiere sustancialmente entre ambos regímenes:

| Componente | Importación Definitiva (REG-10) | Exportación Definitiva (REG-40) | Razón técnica / Tratamiento en el sistema |
| :-- | :-- | :-- | :-- |
| **Valor de Mercancía (FOB)** | **Obligatorio** | **Obligatorio** | En importación es la base para calcular Seguro y CIF. En exportación es el **valor oficial de la DAM de exportación** (factura comercial del exportador nacional). Se almacena en la columna nueva `valor_fob`. |
| **Flete Internacional** | Manual (marítimo/aéreo) | Manual / Opcional ($0.00 por defecto) | En exportación depende del Incoterm acordado: si es FOB/EXW el flete lo asume el comprador foráneo ($0); si el cliente exporta CFR/CIF/CIP/CPT, SGA lo cotiza. |
| **Seguro Estimado** | Automático `fob * 0.0075` (editable) | Opcional / $0.00 por defecto | Solo aplica si el exportador pacta término CIF/CIP o contrata póliza de exportación con SGA. No se autocalcula de forma obligatoria. |
| **Base Imponible Visible** | **Valor CIF Calculado** (`fob + flete + seguro`) | **Valor FOB Declarado** | La SUNAT liquida tributos de importación sobre CIF; en exportación la valoración y estadística oficial de aduana se basa en **FOB**. |
| **Gastos Operativos y Agencia** | Manual / Tarifa Plana | Manual / Tarifa Plana | **Aplica igual en ambos regímenes.** Es el honorario y gasto operativo del despacho aduanero y logístico cobrado por SGA. |
| **Tributos SUNAT (Ad-Valórem, IGV, Percepción)** | Calculados según selectores (0%, 6%, 11% / 18% / 3.5%, 5%, 10%) | **$0.00 USD (Inafecto)** | Según el Art. 60 de la Ley General de Aduanas y la Ley del IGV, la exportación de bienes está **inafecta de derechos arancelarios e IGV**. Tampoco existe percepción de SUNAT. |
| **Importe Total Liquidado** | `flete + gastos_operativos + tributos` | `flete (si aplica) + gastos_operativos` | Refleja únicamente los conceptos a facturar por SGA al exportador. |

##### Comportamiento Dinámico en la Interfaz (Paso 4) según el Régimen elegido en el Paso 2:
1. **Si el usuario eligió Importación Definitiva (`IMPORTACION_DEFINITIVA`):**
   - Se muestra el flujo completo del cuadro: FOB obligatorio, Seguro automático editable, Badge CIF, Gastos Operativos, y el Bloque de Tributos SUNAT con sus selectores de tasas (Ad-valórem e IGV/Percepción) y cálculos automáticos.
2. **Si el usuario eligió Exportación Definitiva (`EXPORTACION_DEFINITIVA`):**
   - **Valor FOB:** Se solicita obligatoriamente con etiqueta *"Valor Comercial de Mercancía a Exportar (FOB USD)"*.
   - **Flete y Seguro:** Se muestran como opcionales con un switch o texto aclaratorio *"Aplica si el Incoterm convenido es CFR o CIF"*.
   - **Bloque de Tributos SUNAT:** En lugar de mostrar los selectores de impuestos de importación, se muestra un banner limpio e informativo:
     > ℹ️ **Régimen de Exportación Definitiva (REG-40): Inafecto de Tributos Arancelarios**  
     > Conforme al Art. 60 de la Ley General de Aduanas y el TUO del IGV, las exportaciones definitivas no están afectas a aranceles Ad-Valórem, IGV/IPM ni percepción SUNAT.  
     > **Tributos SUNAT Estimados: $0.00 USD**
   - En el backend y en el formulario, las tasas `tasaAdValorem`, `tasaPercepcion` y `montoTributosEstimado` se registran en `0.00`.
   - **Total Liquidado:** Suma inmediata de `flete + gastos_operativos`.

---

#### A.2.2 Impacto y Modificación en Base de Datos (Columna `valor_fob` y adiciones)

Para que el nuevo modelo soporte tanto importación como exportación y no pierda información de la liquidación, se realizarán las siguientes modificaciones en la base de datos:

1. **Tabla `cotizacion` (Columnas nuevas):**
   ```sql
   ALTER TABLE cotizacion 
     ADD COLUMN valor_fob DECIMAL(12,2) NOT NULL DEFAULT 0.00,
     ADD COLUMN monto_seguro_estimado DECIMAL(12,2) NOT NULL DEFAULT 0.00,
     ADD COLUMN tasa_advalorem DECIMAL(5,2) NOT NULL DEFAULT 0.00,
     ADD COLUMN tasa_percepcion DECIMAL(5,2) NOT NULL DEFAULT 0.00;
   ```
   - `valor_fob`: Monto de factura comercial en USD. Obligatorio para ambos regímenes.
   - `monto_seguro_estimado`: Monto del seguro (calculado o manual en importación, opcional en exportación).
   - `tasa_advalorem`: Porcentaje de arancel (0.00, 6.00, 11.00; en exportación siempre 0.00).
   - `tasa_percepcion`: Porcentaje de percepción (0.00, 3.50, 5.00, 10.00; en exportación siempre 0.00).

2. **Compatibilidad y Migración:**
   - Como Spring Boot tiene configurado `spring.jpa.hibernate.ddl-auto=update`, Hibernate creará las columnas automáticamente en el arranque al agregarlas en `pe.sga.entity.Cotizacion`.
   - Si existen scripts SQL (`database/schema.sql` y `database/data.sql`), se actualizarán con las 4 columnas nuevas y valores por defecto para mantener consistencia.
   - Los registros existentes de cotizaciones tendrán valor `0.00` por defecto sin generar fallos ni inconsistencias de nulidad (`NULL`).

#### A.2.3 Cambios en Frontend (`CotizacionRegistrationForm.vue` y componentes)

1. **Estado del formulario (`form`):**
   - Incorporar `valorFob: 0`, `montoSeguroEstimado: 0`, `seguroManual: false` (flag solo de UI), `tasaAdValorem: 0`, `tasaPercepcion: 0`, `valorCif: 0`.
   - Renombrar en pantalla `montoHonorariosEstimado` a **"Gastos Operativos y Agencia"**.
2. **Motor de cálculo reactivo unificado (`recalcularLiquidacion`):**
   - Si `regimenAduanero === 'IMPORTACION_DEFINITIVA'`:
     - `seguro = seguroManual ? form.montoSeguroEstimado : Number((form.valorFob * 0.0075).toFixed(2))`
     - `cif = form.valorFob + form.montoFleteEstimado + seguro`
     - `advalorem = Number((cif * (form.tasaAdValorem / 100)).toFixed(2))`
     - `igv_ipm = Number(((cif + advalorem) * 0.18).toFixed(2))`
     - `percepcion = form.tasaPercepcion > 0 ? Number(((cif + advalorem + igv_ipm) * (form.tasaPercepcion / 100)).toFixed(2)) : 0`
     - `tributosTotales = advalorem + igv_ipm + percepcion`
     - `totalCotizado = form.montoFleteEstimado + form.montoHonorariosEstimado + tributosTotales`
   - Si `regimenAduanero === 'EXPORTACION_DEFINITIVA'`:
     - `tasaAdValorem = 0`, `tasaPercepcion = 0`, `tributosTotales = 0`, `advalorem = 0`, `igv_ipm = 0`, `percepcion = 0`
     - `totalCotizado = form.montoFleteEstimado + form.montoHonorariosEstimado`
3. **UI del Paso 4 Dinámica:**
   - Si Importación: Bloque Mercancía (FOB, Flete, Seguro editable, Badge CIF) + Bloque Gastos Operativos + Bloque Tributos SUNAT (selectores de tasas 0/6/11% y percepciones) + Banner Total.
   - Si Exportación: Bloque Mercancía con Valor FOB (obligatorio) + Flete opcional + Bloque Gastos Operativos + Banner Informativo de Exportación Inafecta (Tributos $0) + Banner Total.
4. **Validaciones (`isFormReady` / `handleSubmit`):**
   - Exigir `form.valorFob > 0` tanto en importación como en exportación.
   - Exigir `form.totalCotizado > 0` (Flete + Gastos Operativos en exportación; Flete + Gastos + Tributos en importación).
5. **Previsualización lateral (`CotizacionProformaPreview.vue`):**
   - Adaptar para mostrar Valor FOB, CIF (solo importación) y desglose de tasas según el régimen.

---

#### A.2.4 Cambios en Backend

| Archivo | Cambio |
| :-- | :-- |
| `entity/Cotizacion.java` | Anotar columnas `@Column(name = "valor_fob", precision = 12, scale = 2)`, `monto_seguro_estimado`, `tasa_advalorem`, `tasa_percepcion` (`BigDecimal` con default `BigDecimal.ZERO`). |
| `dto/request/CotizacionRequestDTO.java` | Agregar los 4 campos con `@NotNull` en `valorFob`. |
| `dto/response/CotizacionResponseDTO.java` | Exponer los 4 campos + getters/setters/builder. |
| `service/impl/CotizacionServiceImpl.java` | Mapear los campos en crear y actualizar. Implementar la validación/recálculo en servidor de acuerdo con el régimen (importación vs exportación) para certificar que el `totalCotizado` sea matemáticamente íntegro. |
| `database/schema.sql` y `data.sql` | Agregar las 4 columnas a las tablas y a las semillas existentes. |

**Otras pantallas a verificar:** `FinanzasView`, `LiquidacionAduaneraView`, `GestionAduaneraView` para validar que lean adecuadamente `totalCotizado` y los nuevos desgloses.

---

### A.3 Orden de ejecución propuesto

1. Crear rama `implementacion-final`.
2. Backend de costos y base de datos (entity, DTOs, `schema.sql`, `data.sql`, service con recálculo importación/exportación) + compilar.
3. Extraer `DocumentosUploadPanel` y adaptar `DocumentosManagerModal` (verificar que el botón **Docs** siga igual).
4. Stepper de 4 pasos + sección Documentación + subida diferida tras crear.
5. Nuevo cuadro de costos (conmutación dinámica Importación vs. Exportación) + preview.
6. Pruebas manuales: crear con/sin documentos, editar, cotización observada/corregida, mercancía restringida, importación vs. exportación.
7. Commit(s) en la rama `implementacion-final`.

### A.4 Criterios de aceptación

- El stepper muestra 4 pasos en el orden definido (Cliente -> Régimen -> Documentación -> Costos) y todos los botones *Continuar/Regresar* navegan correctamente.
- Al emitir una cotización con archivos en cola, quedan adjuntos a la cotización creada y visibles desde el botón **Docs**.
- Subir/borrar documentos desde el botón **Docs** de la bandeja funciona igual que antes.
- **En Importación:** Cambiar FOB, flete, seguro o cualquier tasa recalcula CIF, tributos y total al instante según las fórmulas oficiales.
- **En Exportación:** Exige Valor FOB (DAM), muestra banner de inafectación tributaria ($0.00 USD), flete opcional y total = flete + gastos operativos.
- Al editar una cotización se recuperan FOB, tasas y desglose intactos sin volver a cero.
- La columna `valor_fob` y demás columnas nuevas persisten en base de datos sin errores de migración.

### A.5 Decisiones y precisiones acordadas

1. **Documentos: ¿obligatorios u opcionales?** Opcionales de manera estándar, con banner de alerta cuando la mercancía requiera permisos especiales (DIGESA, SENASA, SUCAMEC, etc.).
2. **Exportación (REG-40):** Aclarado en §A.2.1: El Valor FOB sí se registra (base declarada DAM), los tributos son $0.00 (inafectos) y el total liquida solo flete y gastos operativos.
3. **Columna `valor_fob` en Base de Datos:** Confirmado en §A.2.2: se agrega a `cotizacion` con `DECIMAL(12,2) DEFAULT 0.00`.
4. **Renombrado de «Honorarios»:** Se renombra la etiqueta visual a "Gastos Operativos y Agencia".
5. **Subida diferida de documentos:** Se asegura la creación de la cotización y se notificará en toast si algún archivo individual fallara al subir.

---

<!-- Siguientes cambios del usuario: agregar como §B, §C… -->

