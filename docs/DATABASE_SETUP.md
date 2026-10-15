# 🗄️ Guía de Conexión y Configuración de Base de Datos - SGA

Esta guía documenta los procedimientos estándar para desplegar, conectar y administrar la base de datos relacional **MySQL 8.4+** para la plataforma **SGA (Sistema de Gestión Aduanera y Logística Integral)**, garantizando la persistencia de datos y el aislamiento seguro de credenciales locales en el backend.

---

## 📋 Requisitos del Entorno

| Parámetro | Especificación Requerida |
| :--- | :--- |
| **Motor RDBMS** | MySQL 8.0 o superior (Recomendado: **MySQL 8.4 LTS**) |
| **Storage Engine** | **InnoDB** (soporte ACID y claves foráneas en cascada) |
| **Charset / Collation** | `utf8mb4` con cotejamiento `utf8mb4_unicode_ci` |
| **Timezone** | `-05:00` (Hora oficial de Lima / Perú) |
| **Gestores de Contenedores** | [Podman](https://podman.io/) (con `podman-compose`) o [Docker](https://www.docker.com/) (con `docker compose`) |

---

## 🚀 Método 1: Orquestación con Compose (Recomendado)

En la carpeta [`database/`](file:///home/jiodev/Documents/Proyects/SGA/database) se incluye el archivo de orquestación [`docker-compose.yml`](file:///home/jiodev/Documents/Proyects/SGA/database/docker-compose.yml) configurado con MySQL 8.4, volumen persistente de datos y montaje automático de los scripts de inicialización.

### 1. Iniciar el servicio con Compose

Desde la raíz del proyecto o desde el directorio `database/`:

**Con Podman Compose:**
```bash
cd database
podman compose up -d
```

**Con Docker Compose:**
```bash
cd database
docker compose up -d
```

> [!NOTE]
> El servicio se inicializa con una base de datos `SGAdb` limpia y vacía. La carga de tablas se puede realizar de forma automática con JPA al iniciar el backend o manualmente ejecutando los scripts según se indica en la sección de poblamiento.

### 2. Comandos de ciclo de vida con Compose

```bash
# Consultar el estado y salud del contenedor
podman compose ps           # o: docker compose ps

# Visualizar los registros de actividad (logs)
podman compose logs -f      # o: docker compose logs -f

# Detener los servicios conservando los datos persistentes
podman compose stop         # o: docker compose stop

# Detener y remover contenedores y redes asociadas
podman compose down         # o: docker compose down
```

---

## 🐳 Método 2: Despliegue Manual con Contenedor Único

Si prefieres levantar el contenedor directamente sin Compose:

**Con Podman:**
```bash
podman run -d --name sga-mysql \
  -p 3306:3306 \
  -e MYSQL_ROOT_PASSWORD=tu_contraseña_aqui \
  -e MYSQL_DATABASE=SGAdb \
  -v sga-mysql-data:/var/lib/mysql \
  docker.io/library/mysql:8.4 \
  --character-set-server=utf8mb4 \
  --collation-server=utf8mb4_unicode_ci
```

**Con Docker:**
```bash
docker run -d --name sga-mysql \
  -p 3306:3306 \
  -e MYSQL_ROOT_PASSWORD=tu_contraseña_aqui \
  -e MYSQL_DATABASE=SGAdb \
  -v sga-mysql-data:/var/lib/mysql \
  mysql:8.4 \
  --character-set-server=utf8mb4 \
  --collation-server=utf8mb4_unicode_ci
```

---

## 💻 Método 3: Conexión a Instancia Nativa de MySQL

Si utilizas un servidor MySQL instalado localmente en el sistema operativo:

1. Ingresar a la consola administrativa de MySQL:
   ```bash
   mysql -u root -p
   ```
2. Asegurar la creación de la base de datos con la codificación adecuada:
   ```sql
   CREATE DATABASE IF NOT EXISTS SGAdb 
     CHARACTER SET utf8mb4 
     COLLATE utf8mb4_unicode_ci;
   ```

---

## 🔐 Configuración de Credenciales en el Backend

El backend gestiona las propiedades locales de forma desacoplada del control de versiones para proteger información confidencial.

### Paso 1: Generar el archivo local de configuración
Copia la plantilla de ejemplo situada en el directorio `backend/`:

```bash
cd backend
cp application-local.properties.example application-local.properties
```

### Paso 2: Ajustar los parámetros de conexión
Edita el archivo [`backend/application-local.properties`](file:///home/jiodev/Documents/Proyects/SGA/backend/application-local.properties):

```properties
# =============================================================================
# SGA - Configuración Local de Desarrollo (NO VERSIONAR)
# =============================================================================
spring.datasource.url=jdbc:mysql://localhost:3306/SGAdb?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
spring.datasource.username=root
spring.datasource.password=tu_contraseña_aqui
server.port=8085
```

> [!NOTE]
> El archivo principal [`application.properties`](file:///home/jiodev/Documents/Proyects/SGA/backend/src/main/resources/application.properties) incorpora automáticamente estas propiedades locales (`spring.config.import=optional:...`). En entornos CI/CD o contenedores productivos, las variables de entorno del sistema operativo (`DB_URL`, `DB_USERNAME`, `DB_PASSWORD`) actúan como mecanismo de provisión predeterminado.

---

## 📥 Poblamiento de Datos (Schema & Seeds)

El sistema soporta dos vías para la preparación de los catálogos e información operativa:

### Opción A: Carga Automatizada (JPA + DataInitializer)
Al ejecutar el backend Spring Boot:
1. Hibernate valida y actualiza el esquema de entidades relacionales en `SGAdb`.
2. El componente `DataInitializer` detecta tablas desiertas e inserta:
   * Sedes logísticas operativas (Callao, APM Terminals, DP World, Ransa).
   * Catálogos oficiales de puertos marítimos/aéreos y tipos de carga.
   * Usuario de administración general (`admin` / `Password123!`).
   * Cuentas operativas base para cada rol (`gerente`, `vendedor`, `supervisor`, `agente`).

### Opción B: Carga Manual mediante Scripts SQL
Para una reconstrucción estricta mediante consola o cliente SQL:

**Instancia Nativa:**
```bash
mysql -u root -p SGAdb < database/schema.sql
mysql -u root -p SGAdb < database/data.sql
```

**Instancia en Contenedor:**
```bash
# Con Podman:
podman exec -i sga-mysql mysql -u root -p<tu_contraseña> SGAdb < database/schema.sql
podman exec -i sga-mysql mysql -u root -p<tu_contraseña> SGAdb < database/data.sql

# Con Docker:
docker exec -i sga-mysql mysql -u root -p<tu_contraseña> SGAdb < database/schema.sql
docker exec -i sga-mysql mysql -u root -p<tu_contraseña> SGAdb < database/data.sql
```

---

## 🔍 Verificación de Disponibilidad

Antes de iniciar el backend, valida la conectividad ejecutando una consulta de prueba:

```bash
# Probar conexión al servicio contenedorizado
podman exec -i sga-mysql mysql -u root -p<tu_contraseña> -e "USE SGAdb; SHOW TABLES;"
```

---

## 🛠️ Diagnóstico y Resolución de Incidencias

### 1. `Communications link failure` / `Connection refused`
* **Diagnóstico:** El servicio de base de datos no está activo o el puerto `3306` no responde.
* **Acción:** Verificar el estado del contenedor con `podman ps` (o `docker ps`). Si se encuentra detenido, iniciarlo con `podman compose up -d` o `podman start sga-mysql`.

### 2. `Access denied for user 'root'@'localhost'`
* **Diagnóstico:** Discrepancia entre la contraseña configurada en MySQL y la indicada en el backend.
* **Acción:** Ajustar el valor de `spring.datasource.password` en [`backend/application-local.properties`](file:///home/jiodev/Documents/Proyects/SGA/backend/application-local.properties).

### 3. `Unknown database 'SGAdb'`
* **Diagnóstico:** El esquema base no ha sido aprovisionado.
* **Acción:** Crear la base de datos ejecutando `CREATE DATABASE SGAdb CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;`.

### 4. Mensaje *"Error al iniciar sesión. Verifique sus credenciales"* en el Frontend
* **Diagnóstico:** El proceso del backend Spring Boot (`http://localhost:8085`) fue detenido o interrumpido.
* **Acción:** Iniciar el backend en una terminal dedicada mediante `./mvnw spring-boot:run` y mantenerla en ejecución continua mientras interactúas con la plataforma.
