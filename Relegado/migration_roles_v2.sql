-- =============================================================================
-- Migración de Roles SGA v2 - Reestructuración Definitiva a 8 Roles Institucionales
-- =============================================================================

START TRANSACTION;

-- 1. Reasignar usuarios de ADMIN_TI al rol ADMINISTRADOR si ambos existen
UPDATE usuario 
SET id_rol = (SELECT r.id_rol FROM (SELECT id_rol FROM rol WHERE nombre_rol = 'ADMINISTRADOR') r)
WHERE id_rol IN (SELECT r2.id_rol FROM (SELECT id_rol FROM rol WHERE nombre_rol = 'ADMIN_TI') r2);

-- Eliminar rol redundante ADMIN_TI
DELETE FROM rol WHERE nombre_rol = 'ADMIN_TI';

-- Renombrar ADMINISTRADOR a ADMINISTRADOR_TI
UPDATE rol 
SET nombre_rol = 'ADMINISTRADOR_TI', descripcion = 'Administrador de TI'
WHERE nombre_rol = 'ADMINISTRADOR';

-- 2. Reasignar usuarios de LIQUIDADOR y SUPERVISOR al rol SUPERVISION si existen
UPDATE usuario 
SET id_rol = (SELECT r.id_rol FROM (SELECT id_rol FROM rol WHERE nombre_rol = 'SUPERVISION') r)
WHERE id_rol IN (SELECT r2.id_rol FROM (SELECT id_rol FROM rol WHERE nombre_rol IN ('LIQUIDADOR', 'SUPERVISOR')) r2);

-- Eliminar roles redundantes LIQUIDADOR y SUPERVISOR
DELETE FROM rol WHERE nombre_rol IN ('LIQUIDADOR', 'SUPERVISOR');

-- Renombrar SUPERVISION a SUPERVISOR_ADUANERO
UPDATE rol 
SET nombre_rol = 'SUPERVISOR_ADUANERO', descripcion = 'Supervisor Aduanero'
WHERE nombre_rol = 'SUPERVISION';

-- 3. Reasignar usuarios de LOGISTICA al rol AGENTE_ADUANERO si existen
UPDATE usuario 
SET id_rol = (SELECT r.id_rol FROM (SELECT id_rol FROM rol WHERE nombre_rol = 'AGENTE_ADUANERO') r)
WHERE id_rol IN (SELECT r2.id_rol FROM (SELECT id_rol FROM rol WHERE nombre_rol = 'LOGISTICA') r2);

-- Eliminar rol redundante LOGISTICA
DELETE FROM rol WHERE nombre_rol = 'LOGISTICA';

-- Actualizar descripción de AGENTE_ADUANERO
UPDATE rol 
SET descripcion = 'Agente Aduanero'
WHERE nombre_rol = 'AGENTE_ADUANERO';

-- 4. Renombrar 1 a 1 los roles restantes
UPDATE rol SET nombre_rol = 'GERENTE_GENERAL', descripcion = 'Gerente General' WHERE nombre_rol = 'GERENTE';
UPDATE rol SET nombre_rol = 'AGENTE_COMERCIAL', descripcion = 'Agente Comercial' WHERE nombre_rol = 'VENTAS';
UPDATE rol SET nombre_rol = 'SUPERVISOR_TRANSPORTE', descripcion = 'Supervisor de Transporte' WHERE nombre_rol = 'TRANSPORTE';
UPDATE rol SET nombre_rol = 'CONTADOR', descripcion = 'Contador' WHERE nombre_rol = 'FINANZAS';
UPDATE rol SET nombre_rol = 'ASESOR_LEGAL', descripcion = 'Asesor Legal' WHERE nombre_rol = 'LEGAL';

-- 5. Insertar roles que pudieran faltar para garantizar exactamente los 8
INSERT IGNORE INTO rol (nombre_rol, descripcion, estado_activo) VALUES
('ADMINISTRADOR_TI', 'Administrador de TI', 1),
('GERENTE_GENERAL', 'Gerente General', 1),
('AGENTE_COMERCIAL', 'Agente Comercial', 1),
('SUPERVISOR_ADUANERO', 'Supervisor Aduanero', 1),
('AGENTE_ADUANERO', 'Agente Aduanero', 1),
('SUPERVISOR_TRANSPORTE', 'Supervisor de Transporte', 1),
('CONTADOR', 'Contador', 1),
('ASESOR_LEGAL', 'Asesor Legal', 1);

-- 6. Eliminar cualquier otro rol no perteneciente a los 8
DELETE FROM rol WHERE nombre_rol NOT IN (
    'ADMINISTRADOR_TI',
    'GERENTE_GENERAL',
    'AGENTE_COMERCIAL',
    'SUPERVISOR_ADUANERO',
    'AGENTE_ADUANERO',
    'SUPERVISOR_TRANSPORTE',
    'CONTADOR',
    'ASESOR_LEGAL'
);

-- 7. Actualizar cargos de usuarios conocidos para coherencia
UPDATE usuario SET cargo_titulo = 'Administrador de TI' WHERE username = 'admin';
UPDATE usuario SET cargo_titulo = 'Gerente General' WHERE username = 'gerente';
UPDATE usuario SET cargo_titulo = 'Agente Comercial' WHERE username IN ('vendedor', 'comercial');
UPDATE usuario SET cargo_titulo = 'Supervisor Aduanero' WHERE username = 'supervisor';
UPDATE usuario SET cargo_titulo = 'Agente Aduanero' WHERE username = 'agente';
UPDATE usuario SET cargo_titulo = 'Supervisor de Transporte' WHERE username IN ('transporte', 'mikalv');
UPDATE usuario SET cargo_titulo = 'Contador' WHERE username IN ('finanzas', 'contador');
UPDATE usuario SET cargo_titulo = 'Asesor Legal' WHERE username = 'legal';

COMMIT;
