# 🗄️ Guía de Conexión y Configuración de Base de Datos - SGA

Esta guía detalla los pasos para desplegar, conectar y administrar la base de datos **MySQL 8.4+** para el sistema **SGA (Sistema de Gestión Aduanera y Logística Integral)**, así como la configuración segura de credenciales locales en el backend sin exponer contraseñas al repositorio Git.

---

## 📋 Requisitos Previos

* **Motor de Base de Datos:** MySQL versión 8.0 o superior (recomendado: MySQL 8.4 LTS).
* **Collation recomendada:** `utf8mb4_unicode_ci` / `utf8mb4`.
* **Motor de almacenamiento:** `InnoDB` (con soporte para transacciones e integridad referencial).
* **Gestor de contenedores (Opcional, recomendado):** [Podman](https://podman.io/) o [Docker](https://www.docker.com/).

---

## 🐳 Opción 1: Despliegue con Contenedor (Podman / Docker)

Esta es la opción recomendada para mantener el entorno de desarrollo limpio y aislado.

### 1. Iniciar el contenedor MySQL 8.4
Ejecuta el siguiente comando para levantar el contenedor con la base de datos `SGAdb`:

**Con Podman:**
```bash
podman run -d --name mysql84 \
  -p 3306:3306 \
  -e MYSQL_ROOT_PASSWORD=tu_contraseña_aqui \
  -e MYSQL_DATABASE=SGAdb \
  docker.io/library/mysql:8.4
```

**Con Docker:**
```bash
docker run -d --name mysql84 \
  -p 3306:3306 \
  -e MYSQL_ROOT_PASSWORD=tu_contraseña_aqui \
  -e MYSQL_DATABASE=SGAdb \
  mysql:8.4
```

### 2. Comandos útiles de administración del contenedor
```bash
# Ver el estado del contenedor
podman ps -a  # o 'docker ps -a'

# Iniciar el contenedor si está detenido
podman start mysql84

# Detener el contenedor
podman stop mysql84

# Ver logs de la base de datos
podman logs -f mysql84
```

---

## 💻 Opción 2: Instalación Nativa Local de MySQL

Si tienes MySQL Server instalado directamente en tu sistema operativo:

1. Inicia sesión en la consola de MySQL como administrador:
   ```bash
   mysql -u root -p
   ```
2. Crea la base de datos si aún no existe:
   ```sql
   CREATE DATABASE IF NOT EXISTS SGAdb 
     CHARACTER SET utf8mb4 
     COLLATE utf8mb4_unicode_ci;
   ```

---

## 🔐 Configuración Segura de Credenciales en el Backend

Para evitar subir contraseñas personales a Git, el backend utiliza un archivo de configuración local ignorado por el control de versiones.

### Paso 1: Crear tu archivo local de propiedades
En la carpeta `backend/`, copia el archivo de plantilla:

```bash
cd backend
cp application-local.properties.example application-local.properties
```

### Paso 2: Configurar tus credenciales personales
Edita `backend/application-local.properties`:

```properties
# SGA - Configuración Local de Desarrollo (NO VERSIONAR)
spring.datasource.url=jdbc:mysql://localhost:3306/SGAdb?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
spring.datasource.username=root
spring.datasource.password=tu_contraseña_aqui
server.port=8085
```

> [!NOTE]
> * Este archivo `application-local.properties` está agregado en el `.gitignore` raíz y **nunca será subido a Git**.
> * El archivo principal [`application.properties`](file:///home/jiodev/Documents/Proyects/SGA/backend/src/main/resources/application.properties) lo carga de forma opcional (`spring.config.import=optional:...`). Si el archivo no existe, Spring Boot usará variables de entorno como fallback.

### Paso alternativo: Usar Variables de Entorno
También puedes definir las credenciales mediante variables de entorno antes de ejecutar el backend:

```bash
export DB_URL="jdbc:mysql://localhost:3306/SGAdb?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true"
export DB_USERNAME="root"
export DB_PASSWORD="tu_contraseña_aqui"

./mvnw spring-boot:run
```

---

## 📥 Inicialización de Tablas y Datos Semilla

Tienes dos alternativas para poblar la base de datos:

### Alternativa A: Automática (Spring Boot JPA + DataInitializer)
Al iniciar el backend por primera vez:
1. Hibernate creará/actualizará automáticamente las tablas en `SGAdb`.
2. El componente `DataInitializer` detectará la ausencia de datos e inyectará:
   * Sedes operativas (Callao, APM Terminals, DP World, Ransa).
   * Catálogos de puertos y tipos de carga aduanera.
   * Usuario administrador inicial `admin` con clave `Password123!`.
   * Usuarios semilla para cada rol del sistema.

### Alternativa B: Manual mediante Scripts SQL
Si prefieres inicializar la base de datos con los scripts DDL y DML estructurados:

**Desde consola nativa:**
```bash
# 1. Esquema DDL (Tablas y restricciones)
mysql -u root -p SGAdb < database/schema.sql

# 2. Datos Semilla Completos (Catálogos, usuarios, clientes, cotizaciones)
mysql -u root -p SGAdb < database/data.sql
```

**Si usas Podman/Docker:**
```bash
# 1. Ejecutar esquema
podman exec -i mysql84 mysql -u root -p<tu_contraseña> SGAdb < database/schema.sql

# 2. Ejecutar datos semilla
podman exec -i mysql84 mysql -u root -p<tu_contraseña> SGAdb < database/data.sql
```

---

## 🔍 Verificación de Conectividad

Para asegurarte de que la base de datos responde antes de arrancar el backend:

```bash
# Probar conexión al contenedor
podman exec -i mysql84 mysql -u root -p<tu_contraseña> -e "USE SGAdb; SHOW TABLES;"
```

---

## ⚠️ Diagnóstico y Solución de Problemas Frecuentes

### 1. `Communications link failure` o `Connection refused`
* **Causa:** El servicio o contenedor de MySQL está apagado.
* **Solución:** Revisa con `podman ps` (o `docker ps`). Si está detenido, arráncalo con `podman start mysql84`.

### 2. `Access denied for user 'root'@'localhost'`
* **Causa:** La contraseña en `backend/application-local.properties` no coincide con la configurada en tu base de datos MySQL.
* **Solución:** Corrige `spring.datasource.password` en `backend/application-local.properties`.

### 3. `Unknown database 'SGAdb'`
* **Causa:** La base de datos no fue creada previamente.
* **Solución:** Conéctate a MySQL y ejecuta `CREATE DATABASE SGAdb CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;`.

### 4. La pantalla de login muestra *"Error al iniciar sesión. Verifique sus credenciales"*
* **Causa:** El backend de Spring Boot se encuentra apagado en el puerto `8085`.
* **Solución:** Inicia el backend en una terminal independiente con `./mvnw spring-boot:run` y **no cierres ni presiones `Ctrl + C`** en esa terminal mientras uses el sistema.
