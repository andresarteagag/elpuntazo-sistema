export const environment = {
  production: true,
  // Ruta relativa a proposito: el navegador llama a /api en el mismo
  // dominio de Vercel y la funcion en frontend/api lo reenvia a Railway.
  // Al ser mismo origen, el navegador no aplica CORS, asi que la app
  // funciona en cualquier dominio de Vercel (produccion o preview) sin
  // tener que registrarlo en ALLOWED_ORIGINS del backend.
  apiUrl: '/api',
};
