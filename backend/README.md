# El Puntazo - Backend (Fase 1-6: BD + Auth + Vendedores + Clientes + Facturas + PDF)

Esta entrega incluye todo lo anterior mas:

- **Generacion de PDF** (`GET /api/invoices/{id}/pdf`): liquidacion profesional generada siempre a partir de los datos guardados en la base de datos (no se almacena un archivo aparte), con el mismo formato de referencia: encabezado El Puntazo, subtotal, descuento aplicado, flete, total a pagar, cliente, vendedor y mensaje de agradecimiento. Incluye una nota aclarando que no es una factura electronica DIAN.
- El backend queda **completo** para esta primera version del sistema.


## Formula del calculo (confirmada con el cliente)

```
valor_descuento = valor_base * (porcentaje_descuento / 100)
valor_despues_descuento = valor_base - valor_descuento
TOTAL = valor_despues_descuento + flete_de_envio
```

El flete lo ingresa el vendedor y se suma DESPUES de aplicar el descuento.



## Como probarlo localmente

1. Crea la base de datos ejecutando `db/schema.sql` en tu MySQL local.
2. Define estas variables de entorno (o crea un archivo `.env` y cárgalas en tu IDE):
   - `DB_USERNAME`, `DB_PASSWORD`
   - `JWT_SECRET` (una cadena larga y aleatoria, minimo 64 caracteres)
   - `INITIAL_ADMIN_EMAIL`, `INITIAL_ADMIN_PASSWORD` (para crear el primer administrador)
3. Ejecuta `mvn spring-boot:run`.
4. Al arrancar por primera vez, se crea automaticamente el usuario ADMIN con el correo y contrasena que configuraste.
5. Prueba el login:

```
POST http://localhost:8080/api/auth/login
{
  "email": "tu_admin@correo.com",
  "password": "tu_contrasena"
}
```

Debe devolver un token JWT. Con ese token, `GET /api/auth/me` (header `Authorization: Bearer <token>`) confirma quien eres.

## Que sigue (proximas entregas)

- Frontend en Angular (Fase 7 en adelante): login, pantalla "Nueva factura" optimizada para celular, historial, panel admin.
- Empaquetado con Docker y guia de despliegue en el VPS.

## Notas importantes

- Nunca se guardan contrasenas en texto plano (se usa BCrypt).
- Un vendedor desactivado no puede iniciar sesion (verificado en el backend, no solo escondido en la interfaz).
- `JWT_SECRET`, credenciales de base de datos y credenciales del administrador inicial se manejan siempre por variables de entorno, nunca quemadas en el codigo.
