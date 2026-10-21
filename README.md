# SGA - Sistema de Gestión Aduanera y Logística Integral

Plataforma empresarial de alto rendimiento diseñada para la gestión integral del ciclo de vida operativo aduanero, multimodal y logístico en el Perú. Conecta en tiempo real la prospección comercial, apertura de cotizaciones, trámites DAM y canales SUNAT (Verde, Naranja, Rojo), control de almacén extraportuario con pesaje VGM, despacho y tracking de flotas terrestres, y liquidaciones financieras.

---

## 🏛️ Estructura del Repositorio

El repositorio está organizado en tres componentes fundamentales:

```
SGA/
├── backend/      # API RESTful desarrollada con Spring Boot 3 (Java 21), Spring Security & JPA
├── frontend/     # SPA moderna reactiva desarrollada con Vue 3, Vite & Vanilla CSS Design System
└── database/     # Scripts DDL (schema.sql), DML/Seeds (data.sql) y datos iniciales (initial-data.json)
```

---

## 🚀 Módulos Funcionales

1. **Seguridad y Accesos TI:** Control de accesos RBAC (8 Roles Institucionales: `ADMINISTRADOR_TI`, `GERENTE_GENERAL`, `AGENTE_COMERCIAL`, `SUPERVISOR_ADUANERO`, `AGENTE_ADUANERO`, `SUPERVISOR_TRANSPORTE`, `CONTADOR`, `ASESOR_LEGAL`), sedes operativas y autenticación JWT.
2. **Comercial & CRM:** Gestión de clientes y prospectos, catálogo arancelario y emisión de cotizaciones marítimas, aéreas y terrestres con cálculo de flete, tributos y honorarios.
3. **Operaciones Aduaneras:** Asignación de expedientes a agentes aduaneros colegiados, registro de numeración DAM, control de canales SUNAT y levantamiento aduanero.
4. **Almacenes y Depósitos Temporales:** Control de estadía en depósitos extraportuarios (APM Terminals, DP World, Ransa), control de días libres y pesaje de contenedor VGM conforme al convenio SOLAS.
5. **Transporte y Flota:** Asignación normalizada de conductores con licencias A-IIIc/A-IIIb, tractos/carretas y guías de remisión con códigos de tracking.
6. **Finanzas y Liquidación:** Liquidación contable de tributos SUNAT, gastos portuarios y fletes locales con emisión de facturas y control de cobranza.

---

## 📦 Puesta en Marcha

> 📖 **Guía Detallada:** Para instrucciones completas de instalación de base de datos con **Podman / Docker**, creación de usuarios, configuración segura sin subir credenciales y solución de errores, consulta la [**Guía de Conexión de Base de Datos**](docs/DATABASE_SETUP.md).

### 1. Base de Datos (MySQL 8.0+)
1. Levantar el servicio con Compose (recomendado con Podman o Docker):
   ```bash
   cd database
   podman compose up -d   # o: docker compose up -d
   ```
2. O inicializar manualmente en una base de datos local preexistente:
   ```bash
   mysql -u root -p < database/schema.sql
   mysql -u root -p < database/data.sql
   ```
   *(También puedes optar por la inicialización automática al arrancar el backend).*

### 2. Backend (Spring Boot 3 - Java 21)
1. Configurar credenciales locales creando tu archivo `application-local.properties` (ignorado por Git):
   ```bash
   cd backend
   cp application-local.properties.example application-local.properties
   ```
   Edita `application-local.properties` con tu contraseña personal de MySQL.
2. Ejecutar la aplicación con Maven Wrapper:
   ```bash
   ./mvnw spring-boot:run
   ```
   *El backend se ejecutará en `http://localhost:8085`. Recuerda mantener la terminal abierta.*

### 3. Frontend (Vue 3 + Vite)
1. Instalar dependencias y levantar el servidor de desarrollo:
   ```bash
   cd frontend
   npm install
   npm run dev
   ```
   *El frontend se ejecutará en `http://localhost:5173` o el puerto configurado por Vite.*

---

## 🔐 Credenciales de Prueba

Todos los usuarios de prueba tienen la contraseña predeterminada: `Password123!`

| Usuario | Rol | Cargo / Responsabilidad |
| :--- | :--- | :--- |
| `admin` | `ADMINISTRADOR_TI` | Administrador de Infraestructura y TI |
| `gerente` | `GERENTE_GENERAL` | Gerente General de Operaciones |
| `comercial` | `AGENTE_COMERCIAL` | Ejecutiva Comercial Senior |
| `supervisor` | `SUPERVISOR_ADUANERO` | Supervisor de Operaciones y Liquidación Aduanera |
| `agente` | `AGENTE_ADUANERO` | Agente de Aduanas Titular Callao |
| `transporte` | `SUPERVISOR_TRANSPORTE` | Supervisor de Tráfico y Flota Terrestre |
| `contador` | `CONTADOR` | Contadora y Control Financiero |
| `legal` | `ASESOR_LEGAL` | Asesor Jurídico y Aduanero |

---

## 🛠️ Tecnologías

- **Backend:** Java 21, Spring Boot 3, Spring Data JPA, Spring Security, JWT (jjwt), MySQL Connector, Lombok, Jackson.
- **Frontend:** Vue 3, Vue Router 4, Pinia, Vite 5, Axios, Lucide Icons, Vanilla CSS Design System.
- **Base de Datos:** MySQL 8 InnoDB, DDL normalizado en 3NF con integridad referencial en cascada.
