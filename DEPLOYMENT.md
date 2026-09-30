# Guia de despliegue de El Puntazo (paso a paso, desde cero)

Esta guia asume que **no tienes Docker instalado** y que nunca has desplegado
una aplicacion en un servidor. Vamos a hacerlo en orden, sin saltarnos nada.

Hay 3 partes:

- **Parte A:** probar todo en tu propio computador primero (recomendado).
- **Parte B:** contratar un servidor (VPS) y dejar el sistema funcionando ahi con una URL temporal.
- **Parte C:** cuando tengas dominio propio, agregar HTTPS.

---

## PARTE A - Probarlo primero en tu computador

Esto es opcional pero muy recomendable: asi te aseguras de que todo funciona
antes de pagar un servidor.

### A.1. Instalar Docker Desktop

1. Entra a **https://www.docker.com/products/docker-desktop/**
2. Descarga la version para tu sistema operativo (Windows o Mac).
3. Instala el programa como cualquier otro (siguiente, siguiente, finalizar).
4. Reinicia el computador si te lo pide.
5. Abre "Docker Desktop". Espera a que en la esquina inferior izquierda
   aparezca el icono de la ballena en color fijo (no parpadeando) — eso
   significa que ya esta listo.

Si estas en Windows, Docker Desktop te pedira instalar "WSL2" (Windows
Subsystem for Linux). Dale a "Instalar" cuando te lo pida y reinicia si
te lo solicita otra vez. Es un paso automatico, no tienes que configurar nada.

### A.2. Verificar que se instalo bien

Abre una terminal:

- **Windows:** busca "PowerShell" en el menu de inicio y abrelo.
- **Mac:** busca "Terminal" con Spotlight (Cmd + Espacio).

Escribe:

```
docker --version
```

Deberia mostrarte algo como `Docker version 27.x.x`. Si te muestra un
error, Docker Desktop no quedo bien instalado o no esta abierto — abrelo
y espera a que la ballena quede fija, luego intenta de nuevo.

### A.3. Descomprimir el proyecto

Descomprime la carpeta `elpuntazo-sistema` que te entregue en algun lugar
facil de encontrar, por ejemplo en `Documentos`.

Debe quedar con esta forma:

```
elpuntazo-sistema/
  backend/
  frontend/
  backup/
  docker-compose.yml
  .env.example
```

### A.4. Crear tu archivo de configuracion

Dentro de la carpeta `elpuntazo-sistema`, copia el archivo `.env.example`
y renombra la copia a `.env` (sin ".example").

- **Windows:** clic derecho sobre `.env.example` → Copiar, luego clic derecho
  en la carpeta → Pegar, y renombra la copia a `.env`.
- **Mac:** en Terminal, dentro de la carpeta del proyecto, ejecuta:
  ```
  cp .env.example .env
  ```

Abre el archivo `.env` con el Bloc de Notas (Windows) o TextEdit (Mac) y
reemplaza los valores de ejemplo por los tuyos. Para esta prueba local
puedes dejar contrasenas simples, pero en el servidor real usa contrasenas
fuertes (ver Parte B).

### A.5. Levantar todo con un solo comando

En la terminal, entra a la carpeta del proyecto:

```
cd Documentos/elpuntazo-sistema
```

(ajusta la ruta segun donde la hayas puesto), y ejecuta:

```
docker compose up --build
```

La primera vez esto puede tardar varios minutos (esta descargando e
instalando todo). Cuando termine de mostrar texto y se quede "quieto"
mostrando logs, esta listo.

### A.6. Probarlo

Abre tu navegador y entra a:

```
http://localhost
```

Deberias ver la pantalla de login de El Puntazo. Inicia sesion con el
correo y contrasena que pusiste en `INITIAL_ADMIN_EMAIL` /
`INITIAL_ADMIN_PASSWORD` dentro del archivo `.env`.

Para apagarlo, vuelve a la terminal y presiona `Ctrl + C`, luego ejecuta:

```
docker compose down
```

Si algun dia quieres borrar tambien los datos de prueba y empezar de cero:

```
docker compose down -v
```

(el `-v` borra tambien la base de datos; no lo uses en el servidor real
salvo que quieras borrar todo a proposito).

---

## PARTE B - Ponerlo en un servidor real (VPS)

Un VPS es una computadora en internet que alquilas, encendida las 24 horas,
donde va a vivir tu aplicacion. Vamos a usar **Hetzner** o **DigitalOcean**
(cualquiera de los dos sirve; los pasos son casi identicos). Uso
DigitalOcean como ejemplo por ser el mas sencillo de usar para alguien
que empieza.

### B.1. Crear la cuenta y el servidor

1. Entra a **https://www.digitalocean.com** y crea una cuenta.
2. Agrega un metodo de pago (tarjeta).
3. Haz clic en "Create" → "Droplets" (asi le llaman a sus servidores).
4. Elige:
   - **Imagen:** Ubuntu 24.04 (LTS) x64
   - **Plan:** el mas basico ("Basic"), tipo "Regular", el de menor precio
     (suele rondar los $6 USD/mes). Es suficiente para el volumen de
     facturas que manejan.
   - **Region:** elige la mas cercana a Colombia (por ejemplo, Nueva York
     o alguna que diga "NYC").
   - **Autenticacion:** elige "Password" (contrasena) para simplificar, y
     crea una contrasena fuerte. Guardala en un lugar seguro (no la
     compartas con nadie).
5. Dale un nombre al servidor, por ejemplo `elpuntazo-servidor`, y crea el
   Droplet.
6. Espera 1-2 minutos. DigitalOcean te va a mostrar una **direccion IP**,
   algo como `164.90.123.45`. Anotala, la vas a necesitar todo el tiempo.

### B.2. Conectarte al servidor por primera vez (SSH)

SSH es como "abrir una terminal dentro del servidor" desde tu propio
computador.

**En Windows:**

1. Abre PowerShell.
2. Escribe (reemplaza la IP por la tuya):
   ```
   ssh root@164.90.123.45
   ```
3. La primera vez te preguntara algo como "Are you sure you want to
   continue connecting?" — escribe `yes` y presiona Enter.
4. Te pedira la contrasena que creaste en el paso B.1. Escribela (no
   veras los caracteres mientras escribes, es normal) y presiona Enter.

**En Mac:** exactamente los mismos pasos, pero usando la app "Terminal".

Si todo salio bien, veras un texto de bienvenida y el simbolo cambiara a
algo como `root@elpuntazo-servidor:~#`. Ya estas "dentro" del servidor.

### B.3. Instalar Docker en el servidor

Una vez conectado por SSH (dentro del servidor), copia y pega estos
comandos **uno por uno**, presionando Enter despues de cada uno:

```
apt update
```

```
apt install -y ca-certificates curl gnupg
```

```
install -m 0755 -d /etc/apt/keyrings
curl -fsSL https://download.docker.com/linux/ubuntu/gpg -o /etc/apt/keyrings/docker.asc
chmod a+r /etc/apt/keyrings/docker.asc
```

```
echo "deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/docker.asc] https://download.docker.com/linux/ubuntu $(. /etc/os-release && echo "$VERSION_CODENAME") stable" | tee /etc/apt/sources.list.d/docker.list > /dev/null
```

```
apt update
```

```
apt install -y docker-ce docker-ce-cli containerd.io docker-compose-plugin
```

Al final, verifica que quedo instalado:

```
docker --version
```

Deberia mostrar la version de Docker instalada.

### B.4. Subir el proyecto al servidor

Necesitas copiar la carpeta `elpuntazo-sistema` de tu computador al
servidor. La forma mas simple es con `scp`, que ya viene incluido junto
con SSH.

**Desde tu propio computador** (NO desde la conexion SSH — abre otra
ventana de terminal en tu computador, o sal del servidor escribiendo `exit`):

```
scp -r ruta/a/elpuntazo-sistema root@164.90.123.45:/root/
```

Reemplaza `ruta/a/elpuntazo-sistema` por donde tengas la carpeta
descomprimida, y la IP por la de tu servidor. Te pedira la contrasena
del servidor otra vez.

Esto puede tardar unos minutos segun tu conexion a internet.

### B.5. Configurar las variables de entorno en el servidor

Vuelve a conectarte por SSH al servidor (`ssh root@TU_IP`), y entra a la
carpeta del proyecto:

```
cd /root/elpuntazo-sistema
cp .env.example .env
nano .env
```

Se abrira un editor de texto simple dentro de la terminal. Cambia cada
valor por uno real y seguro:

- `DB_ROOT_PASSWORD`, `DB_PASSWORD`: contrasenas fuertes y distintas entre si.
- `JWT_SECRET`: una cadena larga y aleatoria. Puedes generarla ejecutando
  en otra linea de la terminal (antes o despues de editar el archivo):
  ```
  openssl rand -base64 64
  ```
  y copiando el resultado.
- `ALLOWED_ORIGINS`: por ahora, pon `http://TU_IP` (por ejemplo
  `http://164.90.123.45`).
- `INITIAL_ADMIN_EMAIL` / `INITIAL_ADMIN_PASSWORD`: las credenciales con
  las que vas a entrar la primera vez como administrador.

Para guardar en `nano`: presiona `Ctrl + O`, luego Enter, y para salir
`Ctrl + X`.

### B.6. Levantar la aplicacion

Todavia dentro de la carpeta del proyecto en el servidor:

```
docker compose up --build -d
```

El `-d` hace que quede corriendo en segundo plano (no se apaga al cerrar
la terminal). La primera vez tardara varios minutos.

Para ver si todo esta bien:

```
docker compose ps
```

Deberias ver 3 servicios (`mysql`, `backend`, `frontend`) en estado "Up"
o "running".

Si algo no arranco, revisa los logs de ese servicio, por ejemplo:

```
docker compose logs backend
```

### B.7. Probarlo desde tu navegador

Desde cualquier computador o celular, entra a:

```
http://TU_IP
```

(por ejemplo `http://164.90.123.45`). Deberias ver el login de El Puntazo.
Inicia sesion con las credenciales de administrador que configuraste.

Ya con esto, El Puntazo esta funcionando en internet, disponible las 24
horas, desde cualquier dispositivo con esa direccion.

### B.8. Configurar backups automaticos

Vamos a programar que todas las noches se guarde una copia de la base de
datos automaticamente.

Dentro del servidor (por SSH), dale permiso de ejecucion a los scripts:

```
cd /root/elpuntazo-sistema
chmod +x backup/backup.sh backup/restore.sh
```

Prueba que funcione manualmente:

```
./backup/backup.sh
```

Deberia crear un archivo dentro de la carpeta `backup/`. Si funciona,
programa que se ejecute todas las noches a las 3 a.m.:

```
crontab -e
```

(la primera vez te preguntara que editor usar; elige `nano` escribiendo
el numero correspondiente). Al final del archivo que se abre, agrega esta
linea:

```
0 3 * * * /root/elpuntazo-sistema/backup/backup.sh >> /root/elpuntazo-sistema/backup/backup.log 2>&1
```

Guarda con `Ctrl + O`, Enter, y sal con `Ctrl + X`.

**Importante:** esto guarda los backups DENTRO del mismo servidor. Si el
servidor se dana por completo, esos backups tambien se perderian. Para
mayor seguridad, te recomiendo configurar despues una copia adicional a
un almacenamiento externo barato como Backblaze B2 — puedo ayudarte con
eso cuando quieras, es un paso opcional pero recomendado.

### B.9. Como restaurar un backup si algo sale mal

```
cd /root/elpuntazo-sistema
./backup/restore.sh backup/elpuntazo_2026-01-15_03-00-00.sql.gz
```

(usa el nombre real del archivo de backup que quieras restaurar). Te
pedira confirmacion escribiendo `SI` antes de hacer nada.

### B.10. Como actualizar la aplicacion cuando hagamos cambios

Cada vez que te entregue una version nueva del sistema:

1. Sube los archivos nuevos con `scp` (igual que en el paso B.4).
2. Conectate por SSH y ejecuta:
   ```
   cd /root/elpuntazo-sistema
   docker compose up --build -d
   ```

Esto reconstruye solo lo que cambio y reinicia los servicios sin perder
los datos guardados en la base de datos (los datos viven en un volumen
de Docker separado, no se tocan).

---

## PARTE C - Cuando tengas un dominio propio (mas adelante)

Esto **no es necesario ahora** (acordamos usar la IP por el momento), pero
lo dejo documentado para cuando compres el dominio (por ejemplo
`elpuntazo.com`) y quieras usar `https://app.elpuntazo.com` con candado
de seguridad.

1. En el proveedor donde compraste el dominio, crea un registro tipo
   **A** que apunte `app.elpuntazo.com` a la IP de tu servidor.
2. Espera unos minutos a que el cambio se propague (puede tardar hasta un
   par de horas).
3. Avisame y te ayudo a instalar Certbot (HTTPS gratuito) y ajustar la
   configuracion de Nginx para que use el dominio en vez de la IP. Es un
   paso rapido una vez el dominio ya este apuntando al servidor.

---

## Resumen de comandos que mas vas a usar

| Que quiero hacer | Comando (dentro de la carpeta del proyecto, en el servidor) |
|---|---|
| Ver si todo esta corriendo | `docker compose ps` |
| Ver que esta pasando (logs) | `docker compose logs -f` |
| Apagar todo | `docker compose down` |
| Prender todo | `docker compose up -d` |
| Reconstruir tras un cambio | `docker compose up --build -d` |
| Hacer un backup ahora mismo | `./backup/backup.sh` |
