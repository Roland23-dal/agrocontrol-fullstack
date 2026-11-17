# AgroControl - APF3 (Back-end Spring Boot + Front-end Angular)

Sistema Web para Gestion y Trazabilidad de Produccion Agricola - Desarrollo Web Integrado (UTP).

```
agrocontrol_APF3/
  backend/    API REST (Spring Boot 3.3, JPA, JWT, CORS)   -> http://localhost:8082
  frontend/   Angular 19 + SASS                             -> http://localhost:4200
  docs/       arquitectura y plan de pruebas / evidencias
  agrocontrol.code-workspace   abrir con VS Code
```

Inicio rapido (detalle en la "Guia de inicializacion" en PDF):
1. `cd backend && mvn spring-boot:run`  (o F5 en VS Code)
2. `cd frontend && npm install && npm start`
3. Abrir http://localhost:4200 e ingresar con `admin / admin123`.

Postman: `backend/AgroControl.postman_collection.json` (ejecutar primero Auth > Login ADMIN).
