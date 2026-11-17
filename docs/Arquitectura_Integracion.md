# AgroControl APF3 - Arquitectura de la integracion Front-end / Back-end

```
Navegador (Angular :4200)
  LoginComponent --POST /api/auth/login--> AuthController --> JwtService (token HS256, 1 h)
  AuthService guarda el token (localStorage)
  authInterceptor  agrega  Authorization: Bearer <token>
  errorInterceptor 401 -> login | 403 -> aviso | 0 -> sin conexion
  Servicios (HttpClient) --GET/POST/PUT/DELETE--> Spring Boot (:8082)
        CORS (SecurityConfig) -> JwtAuthenticationFilter -> @PreAuthorize por rol -> Service -> JPA -> H2/MySQL
```

## Matriz de permisos (back-end) y reflejo en el front-end
| Operacion | ADMIN | USER | GUEST | En Angular |
|---|:-:|:-:|:-:|---|
| Consultar (GET) | si | si | si | listas y detalles |
| Crear / editar (POST, PUT) | si | si | no | botones y rutas /nuevo, /editar con `roleGuard` |
| Eliminar (DELETE) | si | no | no | boton Eliminar solo ADMIN |
| Usuarios | si | no | no | ruta /usuarios con `roleGuard(['ADMIN'])` |

La seguridad real la aplica siempre el backend (403); el front solo adapta la interfaz.

## Cambios en el backend (APF3)
- `SecurityConfig`: `.cors(...)`, permiso a `OPTIONS` y bean `CorsConfigurationSource`.
- `application.properties`: `app.cors.allowed-origins=http://localhost:4200,http://127.0.0.1:4200`.

## Endpoints consumidos por pantalla
| Pantalla | Endpoints |
|---|---|
| Login / Registro | POST /auth/login, POST /auth/register |
| Panel | GET /campanas/resumen, /campanas/atrasadas, /productores, /parcelas, /cultivos, /cosechas |
| Productores | GET/POST/PUT/DELETE /productores, GET /productores/buscar, GET /parcelas/productor/{id}/area-total |
| Parcelas | CRUD /parcelas, GET /buscar, /area-minima, ?productorId=, PUT /{id}/transferir |
| Cultivos | CRUD /cultivos, GET /campanas/cultivo |
| Campanas | CRUD /campanas, GET /estado/{e}, /fechas, /cultivo, /atrasadas, POST /{id}/finalizar |
| Cosechas | CRUD /cosechas, GET /fechas, ?campanaId=, /campana/{id}/total |
| Usuarios | GET /usuarios, /usuarios/me, PUT /usuarios/{id}/roles, DELETE /usuarios/{id} |
