-- =============================================================================
-- SISTEMA SGA - DATOS INICIALES Y SEMILLAS COMPLETAS (TODOS LOS MÓDULOS)
-- =============================================================================
-- Base de Datos: SGAdb
-- Contraseña unificada para todos los usuarios de prueba: Password123!
-- Hash BCrypt: $2a$10$vbDgD4A8kg.sizd9I.P/AO44N1VeIvOSzvk8BbxZoqb5j9iJZ48Ee
-- =============================================================================

USE SGAdb;

SET FOREIGN_KEY_CHECKS = 0;

-- Limpieza previa en orden inverso de dependencias para asegurar idempotencia
DELETE FROM liquidacion_financiera;
DELETE FROM despacho_transporte;
DELETE FROM control_almacen;
DELETE FROM expediente;
DELETE FROM cotizacion;
DELETE FROM cliente;
DELETE FROM usuario;
DELETE FROM conductor;
DELETE FROM unidad_transporte;
DELETE FROM catalogo_mercancia;
DELETE FROM catalogo_tipo_carga;
DELETE FROM catalogo_puerto;
DELETE FROM sede;
DELETE FROM rol;

-- =============================================================================
-- 1. ROLES DEL SISTEMA (SEGURIDAD Y CONTROL TI)
-- =============================================================================
INSERT INTO rol (id_rol, nombre_rol, descripcion, estado_activo) VALUES
(1, 'ADMINISTRADOR', 'Acceso total a la administración, gestión global de usuarios y auditoría', TRUE),
(2, 'ADMIN_TI', 'Administrador de infraestructura, accesos y seguridad informática TI', TRUE),
(3, 'GERENTE', 'Gerente general con acceso de lectura gerencial y KPIs a todos los módulos', TRUE),
(4, 'VENTAS', 'Gestión comercial de clientes, cotizaciones y prospección de negocios', TRUE),
(5, 'SUPERVISION', 'Supervisión ejecutiva de operaciones aduaneras, logísticas y transporte', TRUE),
(6, 'AGENTE_ADUANERO', 'Revisión técnica de cotizaciones, apertura y gestión de DAM / expedientes', TRUE),
(7, 'LOGISTICA', 'Gestión de depósitos temporales, pesaje VGM y custodia de carga', TRUE),
(8, 'TRANSPORTE', 'Monitoreo de flotas, conductores, unidades y guías de remisión', TRUE),
(9, 'LIQUIDADOR', 'Liquidación financiera, cálculos de tributos SUNAT y cobranza', TRUE),
(10, 'LEGAL', 'Revisión normativa, documentación legal y dictámenes aduaneros', TRUE),
(11, 'FINANZAS', 'Control de facturación, cobranzas y pagos bancarios', TRUE);

-- =============================================================================
-- 2. SEDES OPERATIVAS INICIALES
-- =============================================================================
INSERT INTO sede (id_sede, codigo_sede, nombre, tipo, direccion, distrito, telefono, estado) VALUES
(1, 'SED-CALLAO-01', 'Sede Central Callao', 'SEDE_CENTRAL', 'Av. Elmer Faucett 1950, Of. 602', 'Callao', '+51 (1) 452-9800', 'OPERATIVO'),
(2, 'AGE-MOSTRADOR-02', 'Agencia Callao / Ventanilla', 'AGENCIA', 'Av. Guardia Chalaca 1240', 'Callao', '+51 (1) 452-9801', 'OPERATIVO'),
(3, 'TER-APM-03', 'Terminal APM Terminals Muelle Norte', 'PUERTO', 'Av. Manco Cápac s/n - Muelle Norte', 'Callao', '+51 (1) 200-8800', 'OPERATIVO'),
(4, 'TER-DPW-04', 'Terminal DP World Callao Muelle Sur', 'PUERTO', 'Av. Manco Cápac 113', 'Callao', '+51 (1) 200-9900', 'OPERATIVO'),
(5, 'DEP-RANSA-05', 'Depósito Temporal Ransa San Agustín', 'DEPOSITO_TEMPORAL', 'Av. Elmer Faucett 5003', 'Callao', '+51 (1) 313-6000', 'OPERATIVO');

-- =============================================================================
-- 3. CATÁLOGO DE PUERTOS Y TERMINALES INTERNACIONALES
-- =============================================================================
INSERT INTO catalogo_puerto (id_puerto, codigo_puerto, nombre, pais, tipo, estado) VALUES
(1, 'PECLL', 'Puerto del Callao', 'Perú', 'MARITIMO', 'ACTIVO'),
(2, 'PEPAI', 'Puerto de Paita', 'Perú', 'MARITIMO', 'ACTIVO'),
(3, 'PEMAT', 'Puerto de Matarani', 'Perú', 'MARITIMO', 'ACTIVO'),
(4, 'PEAQP', 'Aeropuerto Internacional Jorge Chávez', 'Perú', 'AEREO', 'ACTIVO'),
(5, 'CNSHA', 'Puerto de Shanghái', 'China', 'MARITIMO', 'ACTIVO'),
(6, 'CNNBO', 'Puerto de Ningbo-Zhoushan', 'China', 'MARITIMO', 'ACTIVO'),
(7, 'USMIA', 'Puerto de Miami', 'Estados Unidos', 'MARITIMO', 'ACTIVO'),
(8, 'DEHAM', 'Puerto de Hamburgo', 'Alemania', 'MARITIMO', 'ACTIVO');

-- =============================================================================
-- 4. CATÁLOGO DE TIPOS DE CARGA ADUANERA
-- =============================================================================
INSERT INTO catalogo_tipo_carga (id_tipo_carga, codigo, nombre, estado) VALUES
(1, 'CARGA_GENERAL', 'Carga General Suelta / Paletizada', 'ACTIVO'),
(2, 'CARGA_REFRIGERADA', 'Carga Perecible Refrigerada', 'ACTIVO'),
(3, 'CARGA_PELIGROSA_IMO', 'Carga Peligrosa Regulada IMO', 'ACTIVO'),
(4, 'CARGA_GRANEL', 'Carga a Granel Sólido / Líquido', 'ACTIVO'),
(5, 'CARGA_PROYECTO', 'Carga Proyecto y Sobredimensionada', 'ACTIVO');

-- =============================================================================
-- 5. CATÁLOGO DE MERCANCÍAS Y PARTIDAS ARANCELARIAS SUNAT
-- =============================================================================
INSERT INTO catalogo_mercancia (id_mercancia, codigo, nombre, categoria, arancel_base_pct, requiere_permiso, entidad_reguladora, estado) VALUES
(1, '8517.13.00.00', 'Smartphone / Teléfono inteligente', 'Tecnología', 0.00, TRUE, 'MTC', 'ACTIVO'),
(2, '8471.30.00.00', 'Computadora portátil / Laptop', 'Tecnología', 0.00, FALSE, NULL, 'ACTIVO'),
(3, '8471.30.00.10', 'Tablet con conectividad móvil', 'Tecnología', 0.00, TRUE, 'MTC', 'ACTIVO'),
(4, '8517.62.20.00', 'Reloj inteligente / Smartwatch', 'Tecnología', 0.00, TRUE, 'MTC', 'ACTIVO'),
(5, '8528.72.00.00', 'Televisor Smart TV', 'Tecnología', 0.00, FALSE, NULL, 'ACTIVO'),
(6, '9504.50.00.00', 'Consola de videojuegos', 'Entretenimiento', 0.00, FALSE, NULL, 'ACTIVO'),
(7, '8703.22.90.00', 'Automóvil nuevo a gasolina (1000 a 1500 cc)', 'Vehículos', 6.00, FALSE, NULL, 'ACTIVO'),
(8, '8703.23.90.10', 'Automóvil nuevo a gasolina (1500 a 3000 cc)', 'Vehículos', 6.00, FALSE, NULL, 'ACTIVO'),
(9, '8703.80.00.00', 'Automóvil nuevo 100% eléctrico', 'Vehículos', 0.00, FALSE, NULL, 'ACTIVO'),
(10, '8703.40.00.00', 'Automóvil nuevo híbrido gasolina-eléctrico', 'Vehículos', 6.00, FALSE, NULL, 'ACTIVO'),
(11, '8703.23.90.90', 'Automóvil usado a gasolina (hasta 2 años)', 'Vehículos', 6.00, TRUE, 'MTC / CITV', 'ACTIVO'),
(12, '8711.20.00.00', 'Motocicleta nueva a gasolina (50 a 250 cc)', 'Vehículos', 6.00, FALSE, NULL, 'ACTIVO'),
(13, '8704.31.10.10', 'Camioneta Pick-up nueva (bien de capital)', 'Vehículos', 6.00, FALSE, NULL, 'ACTIVO'),
(14, '8712.00.00.00', 'Bicicleta convencional sin motor', 'Transporte', 11.00, FALSE, NULL, 'ACTIVO'),
(15, '9503.00.99.00', 'Juguetes diversos de plástico y muñecos', 'Juguetería', 6.00, TRUE, 'DIGESA', 'ACTIVO'),
(16, '9504.90.90.00', 'Juegos de mesa y cartas', 'Juguetería', 6.00, FALSE, NULL, 'ACTIVO'),
(17, '6404.11.00.00', 'Calzado deportivo / Zapatillas', 'Calzado', 11.00, FALSE, NULL, 'ACTIVO'),
(18, '6403.59.00.00', 'Zapatos con suela y capellada de cuero', 'Calzado', 11.00, FALSE, NULL, 'ACTIVO'),
(19, '6109.10.00.30', 'Camisetas y polos de algodón', 'Confecciones', 11.00, FALSE, NULL, 'ACTIVO'),
(20, '6203.42.00.00', 'Pantalones y jeans de algodón', 'Confecciones', 11.00, FALSE, NULL, 'ACTIVO'),
(21, '3004.90.29.00', 'Medicamentos para uso humano dosificados', 'Farmacia', 0.00, TRUE, 'DIGEMID', 'ACTIVO'),
(22, '3304.99.00.00', 'Productos cosméticos y cuidado de la piel', 'Cosmética', 6.00, TRUE, 'DIGEMID', 'ACTIVO'),
(23, '2106.90.79.00', 'Suplementos alimenticios y vitaminas', 'Nutrición', 6.00, TRUE, 'DIGESA', 'ACTIVO'),
(24, '2208.30.00.00', 'Whisky y licores destilados', 'Licores', 6.00, TRUE, 'DIGESA', 'ACTIVO'),
(25, '2203.00.00.00', 'Cerveza de malta', 'Bebidas', 6.00, TRUE, 'DIGESA', 'ACTIVO'),
(26, '2402.20.20.00', 'Cigarrillos de tabaco rubio', 'Tabaco', 6.00, TRUE, 'MINSA / DIGESA', 'ACTIVO'),
(27, '1905.31.00.00', 'Galletas dulces y snacks procesados', 'Alimentos', 6.00, TRUE, 'DIGESA', 'ACTIVO'),
(28, '0808.10.00.00', 'Manzanas frescas', 'Agropecuario', 6.00, TRUE, 'SENASA', 'ACTIVO'),
(29, '8418.10.19.00', 'Refrigerador doméstico combinado', 'Electrohogar', 0.00, FALSE, NULL, 'ACTIVO'),
(30, '8450.11.00.00', 'Lavadora automática de ropa', 'Electrohogar', 0.00, FALSE, NULL, 'ACTIVO'),
(31, '9403.60.00.00', 'Muebles de madera para el hogar', 'Hogar', 11.00, FALSE, NULL, 'ACTIVO'),
(32, '4011.10.00.00', 'Neumáticos nuevos para automóviles', 'Autopartes', 6.00, FALSE, NULL, 'ACTIVO'),
(33, '9302.00.00.00', 'Armas de fuego de uso civil (pistolas/revólveres)', 'Seguridad', 6.00, TRUE, 'SUCAMEC', 'ACTIVO');

-- =============================================================================
-- 6. USUARIOS DEL SISTEMA (CUENTAS OPERATIVAS CON PASSWORD: Password123!)
-- =============================================================================
INSERT INTO usuario (id_usuario, id_rol, id_sede, username, password_hash, nombres, apellidos, correo_institucional, tipo_documento, numero_documento, telefono, cargo_titulo, codigo_sunat, ultimo_acceso, estado_activo) VALUES
(1, 1, 1, 'admin', '$2a$10$vbDgD4A8kg.sizd9I.P/AO44N1VeIvOSzvk8BbxZoqb5j9iJZ48Ee', 'Administrador', 'General del Sistema', 'admin@sga.pe', 'DNI', '45892104', '+51 987 654 321', 'Administrador General', 'ADM-SUNAT-01', NOW(), TRUE),
(2, 2, 1, 'admin_ti', '$2a$10$vbDgD4A8kg.sizd9I.P/AO44N1VeIvOSzvk8BbxZoqb5j9iJZ48Ee', 'Soporte', 'Sistemas e Infraestructura', 'ti@sga.pe', 'DNI', '46903215', '+51 987 654 322', 'Ingeniero de Infraestructura TI', 'TI-SUNAT-02', NOW(), TRUE),
(3, 3, 1, 'gerente', '$2a$10$vbDgD4A8kg.sizd9I.P/AO44N1VeIvOSzvk8BbxZoqb5j9iJZ48Ee', 'Carlos', 'Mendoza Véliz', 'gerente@sga.pe', 'DNI', '08765432', '+51 987 654 323', 'Gerente General de Operaciones', 'GER-SUNAT-03', NOW(), TRUE),
(4, 4, 1, 'vendedor', '$2a$10$vbDgD4A8kg.sizd9I.P/AO44N1VeIvOSzvk8BbxZoqb5j9iJZ48Ee', 'Sofía', 'Paredes Romero', 'ventas@sga.pe', 'DNI', '47891234', '+51 987 654 324', 'Ejecutiva Comercial Senior', 'VEN-SUNAT-04', NOW(), TRUE),
(5, 5, 1, 'supervisor', '$2a$10$vbDgD4A8kg.sizd9I.P/AO44N1VeIvOSzvk8BbxZoqb5j9iJZ48Ee', 'Jorge', 'Valenzuela Castro', 'supervisor@sga.pe', 'DNI', '43219876', '+51 987 654 325', 'Supervisor de Operaciones Aduaneras', 'SUP-SUNAT-05', NOW(), TRUE),
(6, 6, 2, 'agente', '$2a$10$vbDgD4A8kg.sizd9I.P/AO44N1VeIvOSzvk8BbxZoqb5j9iJZ48Ee', 'Miguel', 'Aduviri Ramos', 'agente@sga.pe', 'DNI', '10293847', '+51 987 654 326', 'Agente de Aduanas Titular', 'AGE-SUNAT-1029', NOW(), TRUE),
(7, 6, 3, 'agente2', '$2a$10$vbDgD4A8kg.sizd9I.P/AO44N1VeIvOSzvk8BbxZoqb5j9iJZ48Ee', 'Lucía', 'Navarro Quispe', 'agente2@sga.pe', 'DNI', '10847291', '+51 987 654 327', 'Agente de Aduanas Adjunta', 'AGE-SUNAT-1084', NOW(), TRUE),
(8, 7, 5, 'logistica', '$2a$10$vbDgD4A8kg.sizd9I.P/AO44N1VeIvOSzvk8BbxZoqb5j9iJZ48Ee', 'Pedro', 'Gutiérrez Salcedo', 'logistica@sga.pe', 'DNI', '44556677', '+51 987 654 328', 'Coordinador de Depósitos y VGM', 'LOG-SUNAT-08', NOW(), TRUE),
(9, 8, 1, 'transporte', '$2a$10$vbDgD4A8kg.sizd9I.P/AO44N1VeIvOSzvk8BbxZoqb5j9iJZ48Ee', 'Raúl', 'Cárdenas Meléndez', 'transporte@sga.pe', 'DNI', '41235678', '+51 987 654 329', 'Jefe de Tráfico y Flota Terrestre', 'TRA-SUNAT-09', NOW(), TRUE),
(10, 9, 1, 'liquidador', '$2a$10$vbDgD4A8kg.sizd9I.P/AO44N1VeIvOSzvk8BbxZoqb5j9iJZ48Ee', 'Patricia', 'Vega Gonzales', 'liquidador@sga.pe', 'DNI', '49876512', '+51 987 654 330', 'Especialista en Liquidaciones SUNAT', 'LIQ-SUNAT-10', NOW(), TRUE),
(11, 10, 1, 'legal', '$2a$10$vbDgD4A8kg.sizd9I.P/AO44N1VeIvOSzvk8BbxZoqb5j9iJZ48Ee', 'Fernando', 'Arana Silva', 'legal@sga.pe', 'DNI', '41122334', '+51 987 654 331', 'Asesor Jurídico Aduanero', 'LEG-SUNAT-11', NOW(), TRUE),
(12, 11, 1, 'finanzas', '$2a$10$vbDgD4A8kg.sizd9I.P/AO44N1VeIvOSzvk8BbxZoqb5j9iJZ48Ee', 'Elena', 'Morales Benítez', 'finanzas@sga.pe', 'DNI', '48877665', '+51 987 654 332', 'Jefa de Cobranzas y Facturación', 'FIN-SUNAT-12', NOW(), TRUE);

-- =============================================================================
-- 7. CLIENTES Y PROSPECTOS COMERCIALES
-- =============================================================================
INSERT INTO cliente (id_cliente, tipo_documento, numero_documento, razon_social, contacto_representante, direccion_fiscal, correo_contacto, telefono_contacto, es_prospecto) VALUES
(1, 'RUC', '20512345671', 'IMPORTACIONES TECNOLÓGICAS DEL PERÚ S.A.C.', 'Ing. Roberto Wong Chan', 'Av. República de Panamá 3545, San Isidro, Lima', 'contacto@importecperu.com', '+51 987 112 233', FALSE),
(2, 'RUC', '20601234562', 'DISTRIBUIDORA AUTOMOTRIZ ANDINA S.A.', 'Lic. Mariana Fernández Díaz', 'Av. Nicolás Arriola 1840, La Victoria, Lima', 'operaciones@autoandina.pe', '+51 988 223 344', FALSE),
(3, 'RUC', '20100123453', 'CONSORCIO MINERO & INDUSTRIAL DEL SUR S.A.A.', 'Ing. Gustavo Morales Paz', 'Calle Las Begonias 441, Of. 802, San Isidro, Lima', 'logistica@minerasur.pe', '+51 999 334 455', FALSE),
(4, 'RUC', '20456789014', 'TEXTIL & MODA PERUANA EXPORT S.A.C.', 'Sra. Carmen Rosa Benavides', 'Av. Argentina 2850, Callao', 'comercial@textilmoda.pe', '+51 977 445 566', FALSE),
(5, 'RUC', '20556677885', 'AGROINDUSTRIAS EXPORT DEL NORTE E.I.R.L.', 'Don Alberto Salazar Cárdenas', 'Carretera Panamericana Norte Km 560, Trujillo, La Libertad', 'exportaciones@agronorte.pe', '+51 966 556 677', FALSE),
(6, 'RUC', '20609988776', 'FARMACÉUTICA BIO-SALUD INTERNACIONAL S.A.C.', 'Dra. Verónica Thorne Palacios', 'Av. Javier Prado Este 4200, Surco, Lima', 'aduanas@biosalud.com.pe', '+51 955 667 788', FALSE),
(7, 'RUC', '20543219877', 'NOVA LOGISTICS & CARGO PERU E.I.R.L.', 'Lic. Álvaro Mendoza Ríos', 'Av. Elmer Faucett 2100, Callao', 'prospecto@novalogistics.pe', '+51 944 778 899', TRUE),
(8, 'RUC', '20612233448', 'SUMINISTROS MÉDICOS GLOBALES DEL PACÍFICO S.A.C.', 'Dra. Natalia Ruiz Herrera', 'Av. Salaverry 1245, Jesús María, Lima', 'ventas@suministrosmedicos.pe', '+51 933 889 900', TRUE);

-- =============================================================================
-- 8. COTIZACIONES COMERCIALES (MÚLTIPLES ESTADOS: APROBADA, PENDIENTE, EN_REVISION, OBSERVADA, RECHAZADA)
-- =============================================================================
INSERT INTO cotizacion (id_cotizacion, codigo_cotizacion, id_cliente, id_usuario_comercial, id_mercancia, id_tipo_carga, regimen_aduanero, id_puerto_envio, id_puerto_destino, servicio_transporte, monto_flete, monto_honorarios, monto_tributos, total_cotizado, validez_dias, fecha_emision, estado, id_agente_revisor, fecha_revision, motivo_observacion, observaciones) VALUES
(1, 'COT-2026-0001', 1, 4, 1, 1, 'IMPORTACION_DEFINITIVA', 5, 1, TRUE, 3450.00, 650.00, 18200.00, 22300.00, 15, '2026-09-01', 'APROBADA', 6, '2026-09-02 11:30:00', NULL, 'Cotización aprobada por Agente Aduanero. Canal regular de importación tecnológica.'),
(2, 'COT-2026-0002', 2, 4, 7, 5, 'IMPORTACION_DEFINITIVA', 6, 1, TRUE, 6800.00, 1200.00, 34500.00, 42500.00, 30, '2026-09-05', 'APROBADA', 6, '2026-09-06 15:45:00', NULL, 'Vehículos nuevos con homologación MTC en regla y flete marítimo asegurado.'),
(3, 'COT-2026-0003', 3, 4, 2, 1, 'IMPORTACION_DEFINITIVA', 7, 4, TRUE, 2200.00, 550.00, 9800.00, 12550.00, 15, '2026-09-10', 'APROBADA', 7, '2026-09-11 09:15:00', NULL, 'Despacho aéreo express confirmado desde Miami a Aeropuerto Jorge Chávez.'),
(4, 'COT-2026-0004', 6, 4, 21, 2, 'IMPORTACION_DEFINITIVA', 8, 1, TRUE, 5100.00, 900.00, 15200.00, 21200.00, 20, '2026-09-15', 'APROBADA', 6, '2026-09-16 14:00:00', NULL, 'Carga refrigerada sensible. Cadena de frío y permisos DIGEMID validados.'),
(5, 'COT-2026-0005', 4, 4, 19, 1, 'EXPORTACION_DEFINITIVA', 1, 7, FALSE, 1950.00, 450.00, 0.00, 2400.00, 15, '2026-09-20', 'APROBADA', 7, '2026-09-21 16:30:00', NULL, 'Exportación definitiva de confecciones peruanas con beneficios de restitución arancelaria.'),
(6, 'COT-2026-0006', 5, 4, 28, 2, 'IMPORTACION_DEFINITIVA', 3, 1, TRUE, 3200.00, 600.00, 5400.00, 9200.00, 15, '2026-10-01', 'EN_REVISION', 6, '2026-10-02 10:00:00', NULL, 'En análisis técnico de permisos fitosanitarios SENASA para frutas frescas.'),
(7, 'COT-2026-0007', 7, 4, 33, 3, 'IMPORTACION_DEFINITIVA', 7, 1, FALSE, 4500.00, 1500.00, 18000.00, 24000.00, 10, '2026-10-05', 'OBSERVADA', 6, '2026-10-06 11:20:00', 'Falta adjuntar resolución de autorización previa de importación expedida por SUCAMEC.', 'Observada en revisión técnica por el agente aduanero.'),
(8, 'COT-2026-0008', 8, 4, 17, 1, 'IMPORTACION_DEFINITIVA', 5, 1, TRUE, 2800.00, 500.00, 7200.00, 10500.00, 15, '2026-10-10', 'PENDIENTE', NULL, NULL, NULL, 'Cotización pendiente de asignación y revisión de agente aduanero.'),
(9, 'COT-2026-0009', 2, 4, 32, 1, 'IMPORTACION_DEFINITIVA', 6, 2, FALSE, 3100.00, 600.00, 8900.00, 12600.00, 15, '2026-09-12', 'RECHAZADA', 6, '2026-09-13 17:00:00', 'Tarifas de flete y seguro marítimo rechazadas por el cliente.', 'Rechazada por el cliente debido a restricciones presupuestarias.');

-- =============================================================================
-- 9. EXPEDIENTES ADUANEROS (DIVERSIDAD DE CANALES SUNAT: VERDE, NARANJA, ROJO, PENDIENTE)
-- =============================================================================
INSERT INTO expediente (id_expediente, codigo_expediente, id_cotizacion, id_cliente, id_mercancia, id_tipo_carga, id_agente_aduanero, fecha_asignacion, contacto_exterior_nombre, contacto_exterior_telefono, contacto_exterior_correo, fecha_zarpe_etd, fecha_llegada_eta, fecha_arribo_aduana, numero_dam, canal_sunat, fecha_asignacion_canal, notificado_agente, fecha_notificacion, detalle_inspeccion, fecha_levante_autorizacion, numero_contenedor, precinto_aduanero, nave_viaje, estado_expediente, fecha_apertura, fecha_cierre, observaciones) VALUES
(1, 'EXP-2026-0001', 1, 1, 1, 1, 6, '2026-09-02 14:00:00', 'Shenzhen Tech Export Co. Ltd.', '+86 755 8392 1100', 'export@shenzhentech.cn', '2026-09-05', '2026-09-28', '2026-09-28 08:30:00', '118-2026-10-001234', 'VERDE', '2026-09-28 10:15:00', TRUE, '2026-09-28 10:20:00', 'Levante automático concedido por SUNAT sin revisión física ni documentaria.', '2026-09-28 11:00:00', 'MSKU7829104', 'SUNAT-PE-884192', 'CMA CGM ALEXANDER VON HUMBOLDT V.240W', 'LEVANTE_AUTORIZADO', '2026-09-02 14:00:00', NULL, 'Despacho con levante verde en tiempo récord.'),
(2, 'EXP-2026-0002', 2, 2, 7, 5, 6, '2026-09-06 16:00:00', 'Shanghai Auto Heavy Industries', '+86 21 6888 2233', 'shipping@shanghaiauto.com', '2026-09-10', '2026-10-02', '2026-10-02 14:00:00', '118-2026-10-002345', 'NARANJA', '2026-10-02 16:30:00', TRUE, '2026-10-02 17:00:00', 'Revisión documentaria conforme de facturas comerciales, BL y certificados MTC.', '2026-10-04 12:00:00', 'TGHU9920145', 'SUNAT-PE-912834', 'EVER GIVEN V.0452E', 'LEVANTE_AUTORIZADO', '2026-09-06 16:00:00', NULL, 'Documentación aprobada favorablemente por especialista de aduanas.'),
(3, 'EXP-2026-0003', 3, 3, 2, 1, 7, '2026-09-11 10:00:00', 'Miami Tech Distribution LLC', '+1 305 555 0199', 'orders@miamitechdist.com', '2026-09-12', '2026-09-13', '2026-09-13 04:45:00', '235-2026-10-003456', 'VERDE', '2026-09-13 06:00:00', TRUE, '2026-09-13 06:15:00', 'Carga aérea expedita con levante aduanero inmediato en terminal aéreo.', '2026-09-13 07:00:00', 'AWB-782-990123', 'AIR-SEC-44120', 'LATAM CARGO FLIGHT UC1502', 'CERRADO', '2026-09-11 10:00:00', '2026-09-15 18:00:00', 'Expediente aéreo liquidado y cerrado satisfactoriamente.'),
(4, 'EXP-2026-0004', 4, 6, 21, 2, 6, '2026-09-16 15:00:00', 'Bayer Pharma Logistics Hamburg', '+49 40 3000 888', 'logistics@bayerpharma.de', '2026-09-20', '2026-10-12', '2026-10-12 11:20:00', '118-2026-10-004567', 'ROJO', '2026-10-12 14:00:00', TRUE, '2026-10-12 14:30:00', 'Aforo físico y reconocimiento conjunto con inspector SUNAT y químico DIGEMID programado.', NULL, 'MEDU4455667', 'SUNAT-PE-334455', 'MSC INES V.242A', 'INSPECCION', '2026-09-16 15:00:00', NULL, 'Inspección física programada en depósito Ransa Callao.'),
(5, 'EXP-2026-0005', 5, 4, 19, 1, 7, '2026-09-21 17:00:00', 'American Apparel Retailers Miami', '+1 305 444 7722', 'customs@americanapparel.com', '2026-10-18', '2026-10-26', NULL, '118-2026-10-005678', 'PENDIENTE', NULL, FALSE, NULL, NULL, NULL, 'TCLU3109245', 'PE-EXP-778811', 'MAERSK MC-KINNEY V.092W', 'EN_TRAMITE', '2026-09-21 17:00:00', NULL, 'DAM numerada provisionalmente para posterior embarque marítimo.');

-- =============================================================================
-- 10. CONTROL DE ALMACÉN Y DEPÓSITOS TEMPORALES (CONTROL VGM Y BAHÍAS)
-- =============================================================================
INSERT INTO control_almacen (id_control_almacen, id_expediente, deposito_temporal, ubicacion_bahia, tipo_ingreso, finalidad_almacenaje, fecha_ingreso, dias_libres_almacen, fecha_limite_retiro, cantidad_bultos, peso_bruto_kg, ticket_pesaje_vgm, precinto_conforme, autorizado_movimiento, fecha_salida, estado_almacen, observaciones) VALUES
(1, 1, 'Depósito Ransa San Agustín Callao', 'A-12 Tech Bay', 'DIRECTO_VERDE', 'INGRESO_IMPORTACION', '2026-09-28 09:30:00', 15, '2026-10-13', 450, 8500.00, 'VGM-RNSA-2026-8812', TRUE, TRUE, '2026-09-29 08:30:00', 'DESPACHADO', 'Retirado por unidad de transporte con pase de salida autorizado.'),
(2, 2, 'Terminal APM Terminals Muelle Norte', 'C-04 Patio Automotriz', 'DIRECTO_VERDE', 'INGRESO_IMPORTACION', '2026-10-02 15:30:00', 15, '2026-10-17', 6, 18400.00, 'VGM-APM-2026-9934', TRUE, TRUE, NULL, 'EN_CUSTODIA', 'En custodia con levante aduanero. Listo para despacho terrestre.'),
(3, 3, 'Talma Servicios Aeroportuarios', 'Terminal Carga Aérea T-2', 'DIRECTO_VERDE', 'INGRESO_IMPORTACION', '2026-09-13 05:30:00', 5, '2026-09-18', 80, 1250.00, 'VGM-TLM-2026-1045', TRUE, TRUE, '2026-09-13 14:00:00', 'DESPACHADO', 'Mercadería aeroportuaria despachada con acta de entrega conforme.'),
(4, 4, 'Depósito Temporal Villas Oquendo Callao', 'Cámara Fría B-01', 'AFORO_FISICO_ROJO', 'INGRESO_IMPORTACION', '2026-10-12 12:30:00', 15, '2026-10-27', 320, 4200.00, 'VGM-VOQ-2026-3321', TRUE, FALSE, NULL, 'EN_CUSTODIA', 'Carga refrigerada conectada a toma reefer en espera de aforo físico SUNAT.');

-- =============================================================================
-- 11. CONDUCTORES DE TRANSPORTE TERRESTRE
-- =============================================================================
INSERT INTO conductor (id_conductor, nombres, apellidos, tipo_licencia, numero_licencia, telefono, estado_activo) VALUES
(1, 'Segundo Ramón', 'Pérez Huamán', 'A-IIIc', 'Q-45892104', '+51 984 123 456', TRUE),
(2, 'Wilmer Alberto', 'Castillo Flores', 'A-IIIc', 'Q-46903215', '+51 972 234 567', TRUE),
(3, 'José Carlos', 'Mamani Condori', 'A-IIIb', 'Q-42345678', '+51 961 345 678', TRUE),
(4, 'Héctor Manuel', 'Chávez Quispe', 'A-IIIc', 'Q-43456789', '+51 950 456 789', TRUE),
(5, 'Marcos Antonio', 'Espinoza Rojas', 'A-IIIc', 'Q-41567890', '+51 949 567 890', TRUE);

-- =============================================================================
-- 12. UNIDADES DE TRANSPORTE Y FLOTA VEHICULAR
-- =============================================================================
INSERT INTO unidad_transporte (id_unidad, placa_tracto, placa_carreta, marca_modelo, tipo_vehiculo, capacidad_carga_kg, estado_operativo, estado_activo) VALUES
(1, 'V7X-891', 'B8Q-982', 'Volvo FH 500 6x4', 'TRACTO_PLATAFORMA', 32000.00, 'EN_RUTA', TRUE),
(2, 'T5R-712', 'A4K-915', 'Scania R450 Streamline', 'TRACTO_PLATAFORMA', 30000.00, 'DISPONIBLE', TRUE),
(3, 'F3M-623', 'C7P-803', 'Mercedes-Benz Actros 2645', 'FURGON_REFRIGERADO', 28000.00, 'DISPONIBLE', TRUE),
(4, 'D2A-445', 'D9N-112', 'Kenworth T800 Heavy Duty', 'CAMA_BAJA', 45000.00, 'MANTENIMIENTO', TRUE),
(5, 'H6Y-904', NULL, 'Hyundai HD78 Carga Ligera', 'CAMION_FURGON', 6500.00, 'DISPONIBLE', TRUE);

-- =============================================================================
-- 13. DESPACHOS DE TRANSPORTE Y DISTRIBUCIÓN LOCAL
-- =============================================================================
INSERT INTO despacho_transporte (id_despacho_transporte, id_expediente, codigo_tracking, tipo_traslado, id_conductor, id_unidad, guia_remision_transporte, direccion_partida, direccion_llegada, distrito_llegada, contacto_recepcion, fecha_programada, fecha_salida_ruta, fecha_entrega_conforme, estado_transporte, detalle_incidencia, observaciones) VALUES
(1, 1, 'TRK-2026-0001', 'ENTREGA_IMPORTACION', 1, 1, 'GRT-001-0004521', 'Depósito Ransa San Agustín, Av. Faucett 5003', 'Av. República de Panamá 3545', 'San Isidro', 'Ing. Roberto Wong', '2026-09-29 09:00:00', '2026-09-29 09:30:00', '2026-09-29 11:45:00', 'ENTREGADO', NULL, 'Entrega en almacén central del cliente conforme sin novedades.'),
(2, 2, 'TRK-2026-0002', 'ENTREGA_IMPORTACION', 2, 2, 'GRT-001-0004522', 'Terminal APM Terminals Muelle Norte, Callao', 'Av. Nicolás Arriola 1840', 'La Victoria', 'Lic. Mariana Fernández', '2026-10-16 08:30:00', NULL, NULL, 'PROGRAMADO', NULL, 'Programado para traslado local de unidades vehiculares.'),
(3, 3, 'TRK-2026-0003', 'ENTREGA_IMPORTACION', 3, 5, 'GRT-001-0004523', 'Talma Carga Aérea, Aeropuerto Jorge Chávez', 'Calle Las Begonias 441, Of. 802', 'San Isidro', 'Ing. Gustavo Morales', '2026-09-13 15:00:00', '2026-09-13 15:30:00', '2026-09-13 17:15:00', 'ENTREGADO', NULL, 'Entrega express culminada con firma en guía de remisión.'),
(4, 4, 'TRK-2026-0004', 'ENTREGA_IMPORTACION', 4, 3, NULL, 'Depósito Temporal Villas Oquendo, Callao', 'Av. Javier Prado Este 4200', 'Santiago de Surco', 'Dra. Verónica Thorne', '2026-10-18 09:00:00', NULL, NULL, 'PENDIENTE_ASIGNACION', NULL, 'Pendiente de confirmación de fecha de levante SUNAT.');

-- =============================================================================
-- 14. LIQUIDACIONES FINANCIERAS Y COBRANZA
-- =============================================================================
INSERT INTO liquidacion_financiera (id_liquidacion, codigo_liquidacion, id_expediente, total_tributos_sunat, gastos_almacen_puerto, flete_transporte_local, honorarios_agencia, total_general_pen, tipo_cambio, estado_pago, numero_factura, metodo_pago, numero_operacion, fecha_emision, fecha_pago, observaciones) VALUES
(1, 'LIQ-2026-0001', 1, 68250.00, 2800.00, 1600.00, 2450.00, 75100.00, 3.7500, 'PAGADO', 'F001-0001289', 'TRANSFERENCIA_BANCARIA_BCP', 'BCP-984210452', '2026-09-29', '2026-09-30 11:20:00', 'Cancelación total acreditada en cuenta corriente BCP soles.'),
(2, 'LIQ-2026-0002', 2, 129375.00, 4200.00, 3200.00, 4500.00, 141275.00, 3.7500, 'PENDIENTE', 'F001-0001290', 'PENDIENTE', NULL, '2026-10-04', NULL, 'Factura emitida enviada a finanzas del cliente. Pendiente de pago dentro del plazo.'),
(3, 'LIQ-2026-0003', 3, 36750.00, 1850.00, 950.00, 2062.50, 41612.50, 3.7500, 'PAGADO', 'F001-0001291', 'TRANSFERENCIA_INTERBANCARIA_BBVA', 'BBVA-440192837', '2026-09-14', '2026-09-14 16:45:00', 'Liquidación aérea cancelada el mismo día de entrega.'),
(4, 'LIQ-2026-0004', 4, 57000.00, 3100.00, 2200.00, 3375.00, 65675.00, 3.7500, 'PENDIENTE', NULL, NULL, NULL, '2026-10-12', NULL, 'Pre-liquidación sujeta a confirmación final de aforo físico SUNAT.');

SET FOREIGN_KEY_CHECKS = 1;
