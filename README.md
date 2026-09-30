# El Puntazo - Sistema completo

Este paquete contiene TODO el sistema, listo para desplegar:

```
elpuntazo-sistema/
  backend/          Codigo del backend (Spring Boot)
  frontend/         Codigo del frontend (Angular)
  backup/           Scripts de backup y restauracion
  docker-compose.yml
  .env.example      Plantilla de configuracion (copiala a .env)
  DEPLOYMENT.md     Guia paso a paso, desde instalar Docker hasta tenerlo en internet
```

## Empieza aqui

Abre **DEPLOYMENT.md** y sigue la guia en orden. Esta escrita asumiendo
que nunca has usado Docker ni desplegado un servidor, con cada comando
explicado.

## Resumen de lo que incluye el sistema

- Autenticacion con roles (Administrador / Vendedor).
- Creacion de facturas con calculo automatico de descuento, flete y total.
- Numeracion automatica y correlativa de facturas.
- Generacion de PDF de la liquidacion.
- Gestion de clientes (compartidos entre vendedores) y vendedores (solo admin).
- Dashboard con metricas basicas.
- Historial con filtros, y anulacion de facturas (sin borrado fisico).
- Disenado para funcionar en PC, tablet y celular (la pantalla de
  "Nueva factura" esta optimizada especialmente para movil).
