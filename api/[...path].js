// ============================================================
// Proxy del API
//
// El navegador llama a /api/... en el MISMO dominio de Vercel y esta
// funcion reenvia la peticion al backend en Railway. Como la llamada
// final la hace el servidor de Vercel y no el navegador, no interviene
// CORS en ningun momento.
//
// A proposito NO se reenvia la cabecera "Origin": si se reenviara,
// Spring Security tratararia la peticion como CORS y la rechazaria con
// 403 "Invalid CORS request" salvo que el dominio este en
// ALLOWED_ORIGINS. Omitiendola, el backend la atiende como una peticion
// normal y deja de hacer falta configurar un dominio nuevo cada vez.
// ============================================================

const BACKEND = process.env.BACKEND_URL || 'https://elpuntazo-sistema-production.up.railway.app';

module.exports = async (req, res) => {
  const segments = [].concat(req.query.path || []);
  const queryIndex = req.url.indexOf('?');
  const search = queryIndex === -1 ? '' : req.url.slice(queryIndex);
  const target = `${BACKEND}/api/${segments.join('/')}${search}`;

  // Solo se reenvia lo necesario. Nada de origin, referer, host ni cookies.
  const headers = {};
  if (req.headers.authorization) headers['authorization'] = req.headers.authorization;
  if (req.headers.accept) headers['accept'] = req.headers.accept;

  let body;
  if (req.method !== 'GET' && req.method !== 'HEAD' && req.body !== undefined && req.body !== null) {
    if (Buffer.isBuffer(req.body) || typeof req.body === 'string') {
      body = req.body;
      if (req.headers['content-type']) headers['content-type'] = req.headers['content-type'];
    } else {
      // Vercel ya convirtio el JSON en objeto; hay que volver a serializarlo.
      body = JSON.stringify(req.body);
      headers['content-type'] = 'application/json';
    }
  }

  try {
    const upstream = await fetch(target, { method: req.method, headers, body });

    // arrayBuffer y no text(): las facturas se descargan como PDF binario.
    const payload = Buffer.from(await upstream.arrayBuffer());

    // Content-Disposition es la que da el nombre al PDF descargado.
    for (const name of ['content-type', 'content-disposition', 'cache-control']) {
      const value = upstream.headers.get(name);
      if (value) res.setHeader(name, value);
    }

    res.status(upstream.status).send(payload);
  } catch (error) {
    // Mismo formato de error que usa el backend, para que el interceptor
    // del frontend sepa mostrarlo igual que cualquier otro.
    res.status(502).json({
      timestamp: new Date().toISOString(),
      status: 502,
      message: 'No se pudo contactar el servidor. Intenta nuevamente.',
    });
  }
};
