# AgroControl Web (Angular) - APF3

Front-end del proyecto integrador **AgroControl** (Desarrollo Web Integrado - UTP).
Consume la API REST de Spring Boot (carpeta `../backend`) con `HttpClient` y JWT.

## Requisitos
- Node.js 20.19+ o 22 LTS (incluye npm)
- Backend en ejecucion en `http://localhost:8082`

## Comandos
```bash
npm install     # una sola vez
npm start       # http://localhost:4200  (abre el navegador)
npm run build:prod
```

La URL de la API se configura en `src/environments/environment.ts` (`apiUrl`).
Para desplegar, cambiar `environment.prod.ts` y agregar el dominio del front en
`app.cors.allowed-origins` del backend.

## Usuarios de prueba
| Usuario | Contrasena | Rol   | Pantalla inicial |
|---------|------------|-------|------------------|
| admin   | admin123   | ADMIN | /dashboard       |
| user    | user123    | USER  | /campanas        |
| guest   | guest123   | GUEST | /dashboard (solo lectura) |

## Estructura
```
src/app
  core/        models, services (HttpClient), interceptors (JWT, errores), guards, utils
  shared/      layout (shell con menu), ui (toast, confirmacion, errores de campo, paginador)
  features/    auth, dashboard, productores, parcelas, cultivos, campanas, cosechas, usuarios
src/styles/    SASS: _variables, _mixins, _base, _components
```
