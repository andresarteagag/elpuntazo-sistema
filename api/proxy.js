// ============================================================
// Proxy del API hacia el backend en Railway.
//
// El navegador llama a /api/... en el mismo dominio de Vercel y esta
// funcion reenvia la peticion al backend. Lo esencial: NO se reenvia la
// cabecera Origin.
//
// Por que importa: el navegador envia Origin en todo POST, incluso al
// mismo dominio. Si se reenvia, Spring Security la ve como peticion CORS
// y responde 403 "Invalid CORS request" salvo que el dominio este en
// ALLOWED_ORIGINS. Sin esa cabecera la atiende como una peticion normal,
// y la aplicacion deja de depender de esa variable del backend.
// ============================================================

const BACKEND = process.env.BACKEND_URL || 'https://elpuntazo-sistema-production.up.railway.app';

module.exports = async (req, res) => {
  // La reescritura de vercel.json pasa la ruta real en "ruta";
  // el resto de parametros (page, size, filtros) se conservan.
  const entrada = new URL(req.url, 'http://interno');
  const ruta = entrada.searchParams.get('ruta') || '';
  entrada.searchParams.delete('ruta');
  const resto = entrada.searchParams.toString();
  const destino = `${BACKEND}/api/${ruta}${resto ? '?' + resto : ''}`;

  const cabeceras = {};
  if (req.headers.authorization) cabeceras['authorization'] = req.headers.authorization;
  if (req.headers.accept) cabeceras['accept'] = req.headers.accept;

  let cuerpo;
  if (req.method !== 'GET' && req.method !== 'HEAD' && req.body !== undefined && req.body !== null) {
    if (Buffer.isBuffer(req.body) || typeof req.body === 'string') {
      cuerpo = req.body;
      if (req.headers['content-type']) cabeceras['content-type'] = req.headers['content-type'];
    } else {
      cuerpo = JSON.stringify(req.body);
      cabeceras['content-type'] = 'application/json';
    }
  }

  try {
    const respuesta = await fetch(destino, { method: req.method, headers: cabeceras, body: cuerpo });

    // arrayBuffer y no text(): las facturas se descargan como PDF binario.
    const datos = Buffer.from(await respuesta.arrayBuffer());

    // Content-Disposition es la que da nombre al PDF descargado.
    for (const nombre of ['content-type', 'content-disposition', 'cache-control']) {
      const valor = respuesta.headers.get(nombre);
      if (valor) res.setHeader(nombre, valor);
    }

    res.status(respuesta.status).send(datos);
  } catch (error) {
    res.status(502).json({
      timestamp: new Date().toISOString(),
      status: 502,
      message: 'No se pudo contactar el servidor. Intenta nuevamente.',
    });
  }
};
