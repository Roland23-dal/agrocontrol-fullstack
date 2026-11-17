/**
 * Entorno de PRODUCCION (ng build --configuration production).
 * Cambiar apiUrl por la URL de la API desplegada en la nube (por ejemplo, Render).
 * Debe terminar en /api y estar incluida en app.cors.allowed-origins del backend.
 */
export const environment = {
  production: true,
  apiUrl: 'http://localhost:8082/api',
};
