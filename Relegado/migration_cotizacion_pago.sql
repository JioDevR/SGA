-- =============================================================================
-- MIGRACIÓN: AGREGAR GESTIÓN DE PAGOS Y ANTICIPOS A COTIZACIÓN
-- Permite validación previa por Finanzas antes de la apertura de Expediente
-- =============================================================================

USE SGAdb;

-- 1. Agregar columnas de control de pagos en tabla cotizacion
ALTER TABLE cotizacion
  ADD COLUMN estado_pago VARCHAR(25) NOT NULL DEFAULT 'SIN_PAGO' AFTER estado,
  ADD COLUMN total_abonado DECIMAL(12, 2) NOT NULL DEFAULT 0.00 AFTER estado_pago,
  ADD COLUMN saldo_pendiente DECIMAL(12, 2) NOT NULL DEFAULT 0.00 AFTER total_abonado;

-- Inicializar saldo pendiente con el total_cotizado
UPDATE cotizacion SET saldo_pendiente = total_cotizado WHERE total_cotizado > 0;

-- 2. Crear tabla de pagos / anticipos de cotizaciones
CREATE TABLE IF NOT EXISTS cotizacion_pago (
    id_pago INT NOT NULL AUTO_INCREMENT,
    id_cotizacion INT NOT NULL,
    codigo_pago VARCHAR(30) NOT NULL,
    tipo_abono VARCHAR(30) NOT NULL DEFAULT 'ADELANTO',
    monto_abonado DECIMAL(12, 2) NOT NULL,
    moneda VARCHAR(5) NOT NULL DEFAULT 'USD',
    tipo_cambio DECIMAL(8, 4) NOT NULL DEFAULT 3.7500,
    monto_equivalente_pen DECIMAL(12, 2) NOT NULL,
    metodo_pago VARCHAR(50) NOT NULL,
    banco_origen VARCHAR(80) NULL,
    banco_destino VARCHAR(80) NOT NULL,
    numero_operacion VARCHAR(60) NOT NULL,
    fecha_operacion DATE NOT NULL,
    id_documento_comprobante INT NULL,
    estado_pago VARCHAR(25) NOT NULL DEFAULT 'PENDIENTE_REVISION',
    id_usuario_finanzas INT NULL,
    fecha_validacion DATETIME NULL,
    observacion_finanzas TEXT NULL,
    id_usuario_registro INT NOT NULL,
    fecha_registro DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id_pago),
    CONSTRAINT uq_cotizacion_pago_codigo UNIQUE (codigo_pago),
    CONSTRAINT fk_pago_cotizacion FOREIGN KEY (id_cotizacion) REFERENCES cotizacion (id_cotizacion) ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT fk_pago_usuario_finanzas FOREIGN KEY (id_usuario_finanzas) REFERENCES usuario (id_usuario) ON UPDATE CASCADE ON DELETE SET NULL,
    CONSTRAINT fk_pago_usuario_registro FOREIGN KEY (id_usuario_registro) REFERENCES usuario (id_usuario) ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT fk_pago_doc_comprobante FOREIGN KEY (id_documento_comprobante) REFERENCES documento_adjunto (id_documento) ON UPDATE CASCADE ON DELETE SET NULL
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

CREATE INDEX idx_pago_cotizacion_estado ON cotizacion_pago (id_cotizacion, estado_pago);
CREATE INDEX idx_pago_estado_pago ON cotizacion_pago (estado_pago);
