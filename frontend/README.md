# El Puntazo - Frontend (Angular)

Aplicacion web para vendedores y administrador. Ya compilada y verificada sin errores.

## Como probarlo localmente

1. `npm install`
2. Ajusta `src/environments/environment.ts` si tu backend no corre en `http://localhost:8080`.
3. `npm start` y abre `http://localhost:4200`.

Necesitas el backend (fase 1-6) corriendo al mismo tiempo para poder iniciar sesion.

## Estructura

- `core/` : servicios (auth, clientes, vendedores, facturas, dashboard), guards de rutas por rol, e interceptores HTTP (token JWT + manejo de errores).
- `layout/` : barra lateral en escritorio + navegacion inferior con boton "Nueva factura" destacado en celular.
- `pages/` : login, nueva-factura (la pantalla mas importante, optimizada para movil), historial, clientes, y dentro de `admin/`: dashboard y vendedores.
- `shared/` : pipe de moneda en pesos colombianos y el componente de notificaciones (toasts).

## Identidad visual

Paleta verde bodega (`--color-brand: #1f5d40`) sobre fondo neutro, tipografia IBM Plex Sans (texto) e IBM Plex Mono (cifras monetarias, para que las columnas de dinero se alineen). Sin gradientes ni tarjetas decorativas: diseño funcional para uso diario.

## Que falta para producción

- Construir con `npm run build` y servir los archivos estaticos resultantes detras de Nginx (ver estrategia de despliegue acordada: Docker Compose + VPS).
- Configurar `environment.prod.ts` con la URL real del backend.
