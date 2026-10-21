# Guía de Configuración Inicial del Entorno e Infraestructura
**Transaduanas Platform (Powered by SGA Core)**  
*Paso 0: Inicialización del Repositorio Git, Estructura de Carpetas y Contenedor MySQL 8.4*

---

## 1. Visión General de la Configuración Inicial

Antes de modelar las tablas relacionales o escribir una sola línea de código en backend o frontend, el **Mantenedor / DevOps / Lead (Persona 1)** debe dejar preparado el entorno base del proyecto. Esta guía establece las bases operativas compartidas:

```
┌────────────────────────────────────────────────────────────────────────┐
│                   CONFIGURACIÓN INICIAL (PASO 0)                       │
├────────────────────────────────────────────────────────────────────────┤
│ 1. Inicialización Git con ramas protegidas (main y develop)            │
│ 2. Reglas de exclusión globales en .gitignore                          │
│ 3. Creación del árbol de directorios (database/, backend/, frontend/)  │
│ 4. Orquestación del contenedor MySQL 8.4 InnoDB (Docker / Podman)      │
│ 5. Plantillas de variables de entorno y perfiles locales               │
│ 6. Verificación de conectividad en el puerto 3306                      │
└────────────────────────────────────────────────────────────────────────┘
```

Una vez completada esta configuración, la base de datos queda lista para recibir el esquema relacional detallado en [`database.md`](./database.md).

---

## 2. Estructura de Directorios Raíz

El proyecto adopta una estructura modular limpia de 3 carpetas principales:

```
transaduanas-platform/
│
├── database/                                  # Infraestructura de datos y scripts SQL
│   ├── docker-compose.yml                     # Contenedor oficial MySQL 8.4
│   └── backups/                               # Carpeta para respaldos y dumps
│
├── backend/                                   # Código fuente Java 21 / Spring Boot 4.1
│   └── application-local.properties.example   # Plantilla de credenciales locales
│
├── frontend/                                  # Código fuente Vue 3 / Vite 8 / PrimeVue
│
├── .gitignore                                 # Reglas de exclusión de Git
└── README.md                                  # Documentación general del proyecto
```

---

## 3. Secuencia de Commits de Configuración Inicial (Rama `infra/*`)

```
[main] ─────────────────────────────────────────────────────────────●
  │
  ▼
[develop]
  │
  └──► [infra/setup-initial-environment]
          │
          ├── Commit 0.1: Inicialización de Git, carpetas y .gitignore
          └── Commit 0.2: Contenedor MySQL 8.4 y plantillas de entorno
```

---

### Commit 0.1: Inicialización del Repositorio Git y `.gitignore`

**Objetivo:** Crear el repositorio local, configurar las ramas maestras `main` y `develop`, y definir las exclusiones universales de control de versiones.

#### 1. Comandos de terminal:
```bash
# Inicializar repositorio Git y ramas base
git init
git checkout -b main
git commit --allow-empty -m "chore: initial commit en main"
git checkout -b develop

# Crear rama de trabajo para la configuración inicial
git checkout -b infra/setup-initial-environment

# Crear estructura de carpetas
mkdir -p database/backups
mkdir -p backend
mkdir -p frontend
mkdir -p docs
```

#### 2. Crear archivo `.gitignore` en la raíz del proyecto:
```gitignore
# Archivos de sistema y temporales
*.log
.DS_Store
Thumbs.db

# Configuración local de base de datos y credenciales privadas
application-local.properties
backend/application-local.properties
.env
*.env.local

# Maven / Java
target/
pom.xml.tag
pom.xml.releaseBackup
pom.xml.versionsBackup
pom.xml.next
release.properties
.mvn/wrapper/maven-wrapper.jar

# Node / Vue / Vite
node_modules/
dist/
dist-ssr/
*.local

# IDEs (IntelliJ, VS Code, Eclipse)
.idea/
.vscode/
*.iml
*.sublime-project
*.sublime-workspace
```

#### 3. Comando Git:
```bash
git add .gitignore
git commit -m "chore(infra): inicializar repositorio git, estructura de directorios y reglas .gitignore"
```

---

### Commit 0.2: Orquestación del Contenedor MySQL 8.4 y Variables de Entorno

**Objetivo:** Configurar el archivo `docker-compose.yml` para levantar la base de datos MySQL 8.4 InnoDB con codificación `utf8mb4`, zona horaria de Perú (`-05:00`), volumen persistente y puerto `3306`.

#### 1. Crear el archivo `database/docker-compose.yml`:
```yaml
services:
  mysql:
    image: docker.io/library/mysql:8.4
    container_name: sga-mysql
    restart: unless-stopped
    command:
      - --character-set-server=utf8mb4
      - --collation-server=utf8mb4_unicode_ci
      - --default-time-zone=-05:00
    environment:
      MYSQL_ROOT_PASSWORD: root
      MYSQL_DATABASE: SGAdb
      MYSQL_USER: root
      MYSQL_PASSWORD: root
    ports:
      - "3306:3306"
    volumes:
      - sga-mysql-data:/var/lib/mysql

volumes:
  sga-mysql-data:
    name: sga-mysql-data
```

#### 2. Crear plantilla de variables de conexión `backend/application-local.properties.example`:
```properties
# =============================================================================
# Plantilla de Configuración Local para Desarrollo
# Copiar este archivo a: backend/application-local.properties (ignorado por Git)
# =============================================================================

SERVER_PORT=8085

# Conexión MySQL 8.4 InnoDB Local
DB_URL=jdbc:mysql://localhost:3306/SGAdb?useSSL=false&serverTimezone=America/Lima&allowPublicKeyRetrieval=true
DB_USERNAME=root
DB_PASSWORD=root

# Hibernate / JPA
JPA_DDL_AUTO=update
JPA_SHOW_SQL=true

# Seguridad JWT
JWT_SECRET=sga_platform_secret_key_super_segura_2026_jwt_token_auth
JWT_EXPIRATION_MS=86400000

# Directorio de almacenamiento de archivos
STORAGE_DIR=./uploads/documentos
```

#### 3. Comando Git:
```bash
git add database/docker-compose.yml backend/application-local.properties.example
git commit -m "feat(infra): configurar docker-compose mysql 8.4 inno-db y plantilla de configuracion local"
```

---

## 4. Verificación y Validación del Contenedor

Para certificar que la infraestructura base está operativa antes de crear las tablas:

```bash
# 1. Levantar el contenedor en segundo plano con Docker o Podman
docker compose -f database/docker-compose.yml up -d
# O si se utiliza Podman:
podman compose -f database/docker-compose.yml up -d

# 2. Verificar que el contenedor esté en estado RUNNING (Up)
docker ps --filter "name=sga-mysql"

# 3. Comprobar conectividad al puerto 3306 y creación de la BD SGAdb
mysql -h 127.0.0.1 -P 3306 -u root -proot -e "SELECT VERSION(), 'SGAdb OK' AS status FROM information_schema.SCHEMATA WHERE SCHEMA_NAME = 'SGAdb';"
```

*Salida esperada:*
```
+-----------+----------+
| VERSION() | status   |
+-----------+----------+
| 8.4.x     | SGAdb OK |
+-----------+----------+
```

---

## 5. Siguiente Paso

Una vez validado el contenedor y el repositorio, continuar con:
👉 **[`database.md`](./database.md)**: Modelado del esquema relacional en 3NF (`schema.sql`), sembrado de roles institucionales (`data.sql`) y pruebas de integridad.
