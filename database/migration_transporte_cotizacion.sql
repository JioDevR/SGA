-- =============================================================================
-- MIGRACIÓN: AGREGAR RUTA Y COSTO DE TRANSPORTE INTERNO A COTIZACIÓN
-- =============================================================================

USE SGAdb;

ALTER TABLE cotizacion 
  ADD COLUMN origen_departamento VARCHAR(60) NULL AFTER servicio_transporte,
  ADD COLUMN origen_provincia VARCHAR(60) NULL AFTER origen_departamento,
  ADD COLUMN destino_departamento VARCHAR(60) NULL AFTER origen_provincia,
  ADD COLUMN destino_provincia VARCHAR(60) NULL AFTER destino_departamento,
  ADD COLUMN costo_transporte_interno DECIMAL(12, 2) NOT NULL DEFAULT 0.00 AFTER destino_provincia;
