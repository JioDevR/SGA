# Guía Esencial de Git y GitHub: De Principiante a Flujo Profesional

Esta guía está diseñada para que domines los fundamentos de Git y GitHub paso a paso, asegurando que comprendas qué ocurre en cada etapa de tu flujo de desarrollo.

## 1. Fundamentos: ¿Qué son Git y GitHub?

* **Git**: Es un sistema de control de versiones distribuido. Funciona de manera local en tu computadora para registrar el historial de cambios en tus archivos.
* **GitHub**: Es una plataforma en la nube que aloja repositorios remotos de Git, permitiendo respaldar código, colaborar en equipo y gestionar proyectos.

### El ciclo de vida de los archivos en Git

Git maneja tres áreas principales antes de que un cambio quede guardado:

```
[ Working Directory ]  ---> `git add` --->  [ Staging Area ]  ---> `git commit` ---> [ Local Repository ]
  (Archivos en edición)                       (Listos para guardar)                     (Historial confirmado)
```

## 2. Configuración Inicial de Git

Antes de crear cualquier proyecto, debes identificarte en tu sistema:

```bash
# Define tu nombre de autor
git config --global user.name "Tu Nombre o Alias"

# Define tu correo (debe coincidir con el de tu cuenta de GitHub)
git config --global user.email "tu-correo@example.com"

# Configura 'main' como el nombre predeterminado para la rama principal
git config --global init.defaultBranch main
```

Puedes verificar tu configuración actual ejecutando:

```bash
git config --list
```

## 3. Conexión Segura con GitHub mediante SSH

El protocolo SSH permite interactuar con GitHub sin tener que escribir tu usuario o contraseña en cada subida o descarga.

### Paso 1: Generar un nuevo par de llaves SSH

Abre tu terminal y ejecuta:

```bash
ssh-keygen -t ed25519 -C "tu-correo@example.com"
```

* Presiona **Enter** para aceptar la ruta por defecto (`~/.ssh/id_ed25519`).
* Ingresa una contraseña corta (*passphrase*) de seguridad opcional o presiona **Enter** dos veces para omitirla.

### Paso 2: Activar el agente SSH y añadir tu llave

```bash
# Iniciar el agente SSH en segundo plano
eval "$(ssh-agent -s)"

# Añadir tu llave privada al agente
ssh-add ~/.ssh/id_ed25519
```

### Paso 3: Copiar tu llave pública

Imprime el contenido de tu llave pública y cópialo completo:

* En Linux / macOS:
  ```bash
  cat ~/.ssh/id_ed25519.pub
  ```
* En Windows (PowerShell):
  ```powershell
  Get-Content ~/.ssh/id_ed25519.pub
  ```

### Paso 4: Añadir la llave en GitHub

1. Inicia sesión en [GitHub](https://github.com).
2. Ve a tu foto de perfil (esquina superior derecha) $\rightarrow$ **Settings**.
3. En la barra lateral izquierda, selecciona **SSH and GPG keys**.
4. Haz clic en **New SSH key**.
5. Asigna un título representativo (por ejemplo: `Laptop Personal`) y pega la llave copiada en el campo **Key**.
6. Haz clic en **Add SSH key**.

### Paso 5: Probar la conexión

Ejecuta en tu terminal:

```bash
ssh -T git@github.com
```

Si todo es correcto, verás un mensaje como:
> *Hi username! You've successfully authenticated, but GitHub does not provide shell access.*

---

## 4. Crear un Repositorio Propio y Subirlo a GitHub

### Paso 1: Inicializar el repositorio local

Crea una carpeta para tu proyecto o entra a una existente:

```bash
mkdir mi-proyecto
cd mi-proyecto

# Inicializar Git en este directorio
git init
```

### Paso 2: Crear el archivo `.gitignore`

El archivo `.gitignore` le indica a Git qué archivos o carpetas **nunca** deben subirse (credenciales, librerías pesadas, archivos temporales del sistema):

Crea un archivo llamado `.gitignore` y agrega patrones como:

```gitignore
# Dependencias
node_modules/
vendor/

# Archivos de entorno y secretos
.env
*.pem

# Archivos del sistema
.DS_Store
Thumbs.db
```

### Paso 3: Primer commit

Crea un archivo inicial (por ejemplo, `README.md`):

```bash
echo "# Mi Primer Proyecto" > README.md

# Mover los cambios al área de preparación (Staging Area)
git add .

# Confirmar los cambios con un mensaje claro y descriptivo
git commit -m "chore: primer commit e inicialización del proyecto"
```

### Paso 4: Conectar con el repositorio remoto en GitHub

1. En GitHub, crea un nuevo repositorio haciendo clic en el botón **New**.
2. Dale un nombre y déjalo **vacío** (no marques la opción de añadir README, .gitignore ni licencia, ya que los tienes en local).
3. Selecciona la opción **SSH** y copia la URL (tendrá el formato `git@github.com:usuario/mi-proyecto.git`).
4. Conéctalo y sube tu código:

```bash
# Asignar el repositorio remoto bajo el alias 'origin'
git remote add origin git@github.com:tu-usuario/mi-proyecto.git

# Subir la rama principal y configurar el seguimiento remoto (-u / --set-upstream)
git push -u origin main
```

---

## 5. El Flujo de Trabajo Diario

Una vez configurado cualquier repositorio, este es el ciclo habitual de trabajo:

```bash
# 1. Comprobar el estado de los archivos
git status

# 2. Ver las diferencias línea por línea de lo que has modificado
git diff

# 3. Preparar archivos modificados
git add archivo.js       # Un archivo específico
git add .                # Todos los archivos modificados y nuevos

# 4. Guardar los cambios con un mensaje descriptivo
git commit -m "feat: implementar pantalla de inicio de sesión"

# 5. Enviar los cambios al repositorio remoto
git push
```

Para consultar el historial de confirmaciones de forma compacta y visual:

```bash
git log --oneline --graph --decorate --all
```

---

## 6. Trabajo con Ramas (*Branching*)

Las ramas permiten desarrollar nuevas funcionalidades o corregir errores en aislamiento sin alterar la versión estable (`main`).

```
          (feat/login)  o---o---o
                       /         \
(main)  o-------------o-----------o (merge)
```

### Crear y cambiar de ramas

```bash
# Ver todas las ramas locales (y la rama actual marcada con *)
git branch

# Crear y cambiarse a una nueva rama simultáneamente (método moderno)
git switch -c feat/login

# Alternativa clásica:
# git checkout -b feat/login
```

### Subir una nueva rama a GitHub

```bash
git push -u origin feat/login
```

### Integrar ramas (*Merge*)

Cuando la funcionalidad esté terminada y probada:

```bash
# 1. Regresar a la rama destino (por ejemplo, main)
git switch main

# 2. Asegurarte de tener los últimos cambios del equipo
git pull

# 3. Fusionar la rama secundaria en la rama actual
git merge feat/login

# 4. Subir la integración a GitHub
git push
```

### Eliminar ramas que ya no se usan

```bash
# Eliminar la rama en local
git branch -d feat/login

# Eliminar la rama en el servidor remoto (GitHub)
git push origin --delete feat/login
```

---

## 7. Trabajar en un Repositorio Ajeno como Colaborador

Cuando alguien te invita a su repositorio como colaborador con permisos de escritura (directorio de la empresa, proyecto de un amigo, etc.), **no necesitas hacer un Fork**. Tienes permisos directos para clonar, crear ramas y subir ramas al repositorio remoto.

### Paso 1: Aceptar la invitación
1. El dueño del repositorio te enviará una invitación a tu correo o notificación en GitHub.
2. Entra al enlace y haz clic en **Accept invitation**. Si no aceptas la invitación, GitHub rechazará tus intentos de subir cambios (`Permission to user/repo denied`).

### Paso 2: Clonar el repositorio mediante SSH
Copia la URL SSH del repositorio ajeno (`git@github.com:dueno-del-repo/nombre-del-proyecto.git`):

```bash
git clone git@github.com:dueno-del-repo/nombre-del-proyecto.git
cd nombre-del-proyecto
```

Al clonar, Git configura automáticamente el remoto llamado `origin` apuntando al repositorio del propietario.

### Paso 3: Flujo de trabajo colaborativo recomendado

> ⚠️ **Regla de oro:** Nunca trabajes ni hagas `git push` directamente sobre la rama `main` de un repositorio colaborativo a menos que el equipo lo autorice expresamente. Trabaja siempre en una rama propia.

#### 1. Actualizar tu rama `main` local
Antes de empezar a trabajar, asegúrate de tener la última versión del proyecto:

```bash
git switch main
git pull origin main
```

#### 2. Crear tu rama de trabajo con un prefijo descriptivo
Usa prefijos estándar según la tarea (`feat/`, `fix/`, `refactor/`) o incluye tus iniciales/nombre de usuario si el equipo lo prefiere:

```bash
# Crear tu rama desde la versión actualizada de main
git switch -c feat/modulo-pagos
```

#### 3. Trabajar y confirmar cambios
Realiza tus modificaciones habituales:

```bash
git add .
git commit -m "feat: integrar pasarela de pagos con Stripe"
```

#### 4. Subir tu rama al repositorio compartido
Sube tu rama al servidor de GitHub:

```bash
git push -u origin feat/modulo-pagos
```

#### 5. Abrir un Pull Request (PR) en GitHub
1. Abre el repositorio en el navegador de GitHub. Verás un cartel amarillo que dice:  
   `feat/modulo-pagos had recent pushes X minutes ago` $\rightarrow$ Haz clic en **Compare & pull request**.
2. Configura:
   * **base:** `main` (la rama receptora del dueño).
   * **compare:** `feat/modulo-pagos` (tu rama de trabajo).
3. Describe qué cambios hiciste y por qué.
4. Asigna como revisor (*Reviewer*) al dueño del repositorio o al compañero encargado.
5. Haz clic en **Create pull request**.

### Paso 4: Trabajar con ramas creadas por otros colaboradores

Si otro colaborador subió una rama y necesitas continuar su trabajo o revisarla en local:

```bash
# 1. Traer la información de todas las ramas remotas creadas por otros
git fetch origin

# 2. Listar todas las ramas (locales y remotas)
git branch -a

# 3. Cambiarte a la rama remota (Git creará automáticamente una copia local conectada)
git switch nombre-de-la-rama-del-companero
```

---

## 8. Sincronización y Manejo de Conflictos

### Sincronización con el equipo

* `git fetch`: Consulta el servidor remoto y descarga la información de cambios nuevos sin alterar tus archivos de trabajo.
* `git pull`: Realiza un `git fetch` y de inmediato un `git merge` para actualizar tu rama local con los cambios remotos.

```bash
# Traer y fusionar los cambios de la rama actual desde GitHub
git pull origin main
```

### Manejo de Conflictos

Ocurre un conflicto cuando dos personas modifican las mismas líneas de un archivo en ramas distintas y Git no puede decidir automáticamente qué cambio conservar.

Git marcará el archivo con delimitadores:

```
<<<<<<< HEAD
console.log("Versión local modificada");
=======
console.log("Versión remota proveniente de GitHub");
>>>>>>> rama-remota
```

**Resolución:**
1. Abre el archivo y edita manualmente el bloque, eliminando las marcas `<<<<<<<`, `=======` y `>>>>>>>`.
2. Guarda el archivo con el código final deseado.
3. Agrega el archivo resuelto y completa el commit:

   ```bash
   git add archivo_con_conflicto.js
   git commit -m "fix: resolver conflicto de integración en login"
   git push
   ```

---

## 9. Herramientas de Supervivencia Esenciales

### Guardar trabajo temporal sin hacer commit (`git stash`)

Útil cuando necesitas cambiar de rama de inmediato pero tu trabajo actual está a medias:

```bash
# Guardar temporalmente los cambios no confirmados
git stash

# Volver a aplicar los cambios guardados cuando regreses
git stash pop
```

### Deshacer cambios con seguridad

```bash
# Descartar cambios no guardados en un archivo del directorio de trabajo
git restore nombre-del-archivo.js

# Sacar un archivo del Staging Area sin perder las modificaciones
git restore --staged nombre-del-archivo.js

# Revertir un commit público ya subido a GitHub (crea un nuevo commit que deshace los cambios)
git revert <HASH_DEL_COMMIT>
```

---

## 10. Hoja de Atajos Rápidos (Cheat Sheet)

| Comando | Acción |
| :--- | :--- |
| `git clone <url-ssh>` | Clona un repositorio remoto en tu máquina local. |
| `git status` | Muestra el estado de los archivos (modificados, en stage, sin seguimiento). |
| `git add .` | Añade todos los archivos modificados al área de preparación. |
| `git commit -m "mensaje"` | Confirma los cambios preparados en el historial local. |
| `git push -u origin <rama>` | Sube una nueva rama local al repositorio remoto y la vincula. |
| `git push` | Sube los commits locales de la rama ya vinculada. |
| `git pull origin <rama>` | Descarga e integra las modificaciones remotas en tu rama local. |
| `git fetch origin` | Descarga las ramas y cambios nuevos del servidor sin fusionarlos todavía. |
| `git switch -c <rama>` | Crea una rama y se traslada a ella. |
| `git switch <rama>` | Cambia a una rama existente (local o remota). |
| `git merge <rama>` | Fusiona la rama indicada dentro de la rama actual. |
| `git branch -a` | Muestra todas las ramas (tanto locales como en el servidor remoto). |
| `git log --oneline` | Muestra el historial de commits resumido en una sola línea. |
| `git stash` | Guarda cambios locales temporalmente sin confirmar. |
| `git stash pop` | Recupera los cambios almacenados en el stash. |