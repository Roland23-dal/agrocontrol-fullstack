# AgroControl – API REST con JPA, Spring Security y JWT (Avance de Proyecto Final 2)

Sistema Web para Gestión y Trazabilidad de Producción Agrícola.
Curso: Desarrollo Web Integrado (100000ST61) — UTP.
APF1 (Unidad 1): API REST · **APF2 (Unidad 2): persistencia JPA/Hibernate, JPQL, transacciones, Spring Security y JWT.**

> Este README resume la documentación técnica del proyecto. Los pasos detallados
> de instalación y ejecución están en el documento aparte **"Guía de ejecución"**.

## 1. Descripción del sistema

Una asociación de productores agrícolas necesita centralizar la información de
sus **productores**, **parcelas**, el **catálogo de cultivos** que siembran y las
**campañas agrícolas** (el proceso productivo de un cultivo específico en una
parcela específica, durante un periodo determinado). Actualmente esta
información está dispersa en hojas de cálculo, lo que dificulta conocer el
costo real de producción y la trazabilidad de cada campaña.

El APF1 implementó el back-end (API REST) con CRUD y pruebas con TDD. El **APF2**
(este avance) continúa ese proyecto agregando: entidad `Cosecha`, consultas
personalizadas con **JPQL**, operaciones **`@Transactional`**, **Spring Security**
con roles ADMIN / USER / GUEST, un `UserDetailsService` propio y autenticación
**JWT** (`/api/auth/register` y `/api/auth/login`).

### Usuarios del sistema (roles esperados)

| Rol | Descripción |
|---|---|
| **Administrador** | Gestiona productores, parcelas, cultivos y campañas; acceso total. |
| **Técnico de campo / Productor** | Registra y actualiza sus propias parcelas y campañas agrícolas. |
| **Consultor / Directivo de la asociación** | Consulta reportes e indicadores (a implementarse en fases posteriores). |

*(Desde el APF2 la API exige un token JWT y aplica estos roles — ver sección 10.)*

## 2. Entidades y relaciones (modelo de dominio)

```
Productor (1) ────< (N) Parcela (1) ────< (N) CampanaAgricola >──── (N) Cultivo
                                                │ (1)
                                                └────< (N) Cosecha

Usuario (N) >────< (M) Rol          (seguridad)
```

- **Productor**: id, nombres, apellidos, dni, telefono, email.
- **Parcela**: id, nombre, ubicacion, areaHectareas, productor (N:1).
- **Cultivo**: id, nombre, variedad, cicloDiasEstimado (catálogo).
- **CampanaAgricola**: id, fechaInicio, fechaFinEstimada, estado (PLANIFICADA / EN_CURSO / FINALIZADA), parcela (N:1), cultivo (N:1), cosechas (1:N).
- **Cosecha** *(APF2)*: id, fecha, cantidadKg, observacion, campana (N:1).
- **Usuario** *(APF2)*: id, username (único), password (BCrypt), email (único), nombreCompleto, enabled, roles (N:M).
- **Rol** *(APF2)*: id, nombre (`ADMIN`, `USER`, `GUEST`).

Un productor tiene muchas parcelas; una parcela puede tener muchas campañas
agrícolas a lo largo del tiempo, y cada campaña usa un cultivo del catálogo.

## 3. Arquitectura del proyecto

```
Cliente (Postman / futuro front Angular)
        │  HTTP/JSON  +  Authorization: Bearer <JWT>
        ▼
  Filtro JWT (JwtAuthenticationFilter) + Spring Security  ──► 401 / 403
        │
        ▼
  Controller  (@RestController, @PreAuthorize / @Secured)
        │
        ▼
  Service     (interfaz + implementación, lógica de negocio y validaciones)
        │
        ▼
  Repository  (Spring Data JPA: JpaRepository + consultas @Query JPQL)
        │
        ▼
  Base de datos en memoria H2 (simulada para esta fase)
```

Paquetes: `controller`, `service` (+ `service.impl`), `model`, `repository`, `dto`, `exception`, `security` (JWT + configuración), `config` (carga inicial).

## 4. Inversión de Control / Dependency Injection

Todos los `Service` y `Controller` reciben sus dependencias (repositorios,
servicios) **por constructor**, anotado con `@Autowired`. Es Spring quien
crea e inyecta las instancias (beans), y no el propio código, aplicando así
IoC: por ejemplo, `ProductorServiceImpl` no crea su propio
`ProductorRepository`, lo recibe ya construido por el framework.

## 5. Endpoints implementados

| Recurso | Método | URL | Descripción | Body de entrada | Respuesta |
|---|---|---|---|---|---|
| Productores | GET | `/api/productores` | Lista todos los productores | – | `200` + lista |
| Productores | GET | `/api/productores/{id}` | Obtiene un productor por id | – | `200` / `404` |
| Productores | POST | `/api/productores` | Registra un productor | `ProductorRequestDTO` (nombres, apellidos, dni, telefono, email) | `201` + productor creado |
| Productores | PUT | `/api/productores/{id}` | Actualiza un productor | `ProductorRequestDTO` | `200` / `404` |
| Productores | DELETE | `/api/productores/{id}` | Elimina un productor | – | `204` / `404` |
| Parcelas | GET | `/api/parcelas` `/api/parcelas?productorId=` | Lista parcelas (opcionalmente filtradas por productor) | – | `200` + lista |
| Parcelas | GET | `/api/parcelas/{id}` | Obtiene una parcela por id | – | `200` / `404` |
| Parcelas | POST | `/api/parcelas` | Registra una parcela | `ParcelaRequestDTO` (nombre, ubicacion, areaHectareas, productorId) | `201` |
| Parcelas | PUT | `/api/parcelas/{id}` | Actualiza una parcela | `ParcelaRequestDTO` | `200` / `404` |
| Parcelas | DELETE | `/api/parcelas/{id}` | Elimina una parcela | – | `204` / `404` |
| Cultivos | GET | `/api/cultivos` | Lista el catálogo de cultivos | – | `200` |
| Cultivos | POST | `/api/cultivos` | Registra un cultivo | `Cultivo` (nombre, variedad, cicloDiasEstimado) | `201` |
| Cultivos | PUT | `/api/cultivos/{id}` | Actualiza un cultivo | `Cultivo` | `200` |
| Cultivos | DELETE | `/api/cultivos/{id}` | Elimina un cultivo | – | `204` |
| Campañas | GET | `/api/campanas` `/api/campanas?parcelaId=` | Lista campañas agrícolas | – | `200` |
| Campañas | POST | `/api/campanas` | Registra una campaña | `CampanaAgricola` (fechaInicio, fechaFinEstimada, estado, cultivo.id, parcela.id) | `201` |
| Campañas | PUT | `/api/campanas/{id}` | Actualiza una campaña | `CampanaAgricola` | `200` |
| Campañas | DELETE | `/api/campanas/{id}` | Elimina una campaña | – | `204` |

Todas las respuestas de error (datos inválidos, recurso no encontrado) se
devuelven en formato JSON consistente gracias a `GlobalExceptionHandler`
(`@RestControllerAdvice`).

## 6. Desarrollo guiado por pruebas (TDD)

Se aplicó TDD sobre la capa de servicio (`ProductorServiceImpl` y
`ParcelaServiceImpl`), usando **JUnit 5** y **Mockito** para aislar la
lógica de negocio del repositorio real y de la base de datos.

**¿Qué se probó primero?**
Antes de escribir la validación en el código de producción, se escribió la
prueba `debeRechazarProductorConDniInvalido()` (y su equivalente
`debeRechazarParcelaConAreaInvalida()`), que esperaba una
`InvalidDataException` al recibir un DNI con menos de 8 dígitos / un área
negativa. Al ejecutarla por primera vez fallaba (rojo), porque el método
`validarDatos()` todavía no existía.

**¿Cómo influyó en la implementación?**
Escribir la prueba primero obligó a definir el *contrato* del servicio antes
de programarlo: qué excepción lanzar y con qué mensaje. Esto llevó a decidir
que la validación de reglas de negocio (formato de DNI, unicidad, área
positiva, existencia del productor referenciado) se hiciera explícitamente
en la capa de `Service` —y no solo con anotaciones en el DTO— para que sea
una unidad de código aislada, fácil de probar sin levantar el contexto de
Spring ni una base de datos real. Después de la validación en rojo se
implementó `validarDatos()` hasta pasar la prueba (verde), y luego se
añadieron casos adicionales (registro válido, duplicados, recurso no
encontrado, listar, eliminar) siguiendo el mismo ciclo rojo–verde–refactor.

Pruebas incluidas:
- `ProductorServiceTest`: registro válido, DNI inválido, nombre vacío, DNI
  duplicado, búsqueda inexistente (`ResourceNotFoundException`), listar
  todos, eliminar.
- `ParcelaServiceTest`: registro válido, área inválida, nombre vacío,
  productor inexistente, listar por productor, búsqueda inexistente.

## 7. Herramientas de prueba

Se incluye la colección **`AgroControl.postman_collection.json`** en la raíz
del proyecto, lista para importar en Postman, con ejemplos de todos los
endpoints (autenticación, CRUD, consultas JPQL, transacciones y pruebas de
seguridad 401/403). Ejecutar primero **Auth > Login ADMIN**: guarda el token
en la variable `{{token}}` y el resto de requests lo envía automáticamente.

## 8. Base de datos

Se usa **H2 en memoria** (simulada) para esta primera fase; se recrea en
cada arranque (`ddl-auto=create-drop`) y se pre-carga con datos de ejemplo
(`data.sql`). Consola disponible en `http://localhost:8082/h2-console`
(JDBC URL: `jdbc:h2:mem:agrocontrol_db`, usuario `sa`, sin contraseña).

## 9. Conclusiones del APF1

- Se implementó una primera versión funcional de la API REST con entidades
  relacionadas, CRUD y validación de entradas (Bean Validation + capa de servicio).
- Aplicar TDD desde el inicio ayudó a detectar casos límite y a mantener la lógica
  de negocio desacoplada de Spring y de la base de datos.

---

# APF2 – Persistencia, consultas JPQL, transacciones y seguridad JWT

## 10. Cumplimiento de las consignas del APF2

### 10.1 Estructura de persistencia con JPA – Hibernate

| Requisito | Dónde está |
|---|---|
| Entidades de dominio (mínimo 3) con relaciones | `Productor`, `Parcela`, `Cultivo`, `CampanaAgricola`, `Cosecha`, `Usuario`, `Rol` en el paquete `model`. Relaciones `@OneToMany` (Productor→Parcela, Parcela→Campaña, Campaña→Cosecha), `@ManyToOne` (inversas) y `@ManyToMany` (Usuario↔Rol) |
| Base de datos H2 o MySQL en `application.properties` | **H2** por defecto en `application.properties`; **MySQL** con el perfil `mysql` (`application-mysql.properties`) |
| Repositorios que extienden `JpaRepository` | Paquete `repository` (7 repositorios) |
| Servicio con lógica de negocio y CRUD | Paquete `service` + `service.impl` (Productor, Parcela, Cultivo, Campaña, Cosecha, Usuario, Auth) |
| Validación con Postman | `AgroControl.postman_collection.json` |

### 10.2 Consultas JPQL y transacciones

Consultas `@Query` (JPQL) en los repositorios, expuestas como endpoints:

| Endpoint | Criterio | Consulta |
|---|---|---|
| `GET /api/campanas/estado/{estado}` | por **estado** | `buscarPorEstado` |
| `GET /api/campanas/fechas?desde=&hasta=` | por **fecha** | `buscarPorRangoDeFechas` (`BETWEEN`) |
| `GET /api/campanas/cultivo?nombre=` | por **categoría** (cultivo) | `buscarPorNombreCultivo` (`LIKE`) |
| `GET /api/campanas/atrasadas` | EN_CURSO con fecha fin vencida | `buscarAtrasadas` |
| `GET /api/campanas/resumen` | conteo por estado | `contarPorEstado` (`GROUP BY`) |
| `GET /api/parcelas/area-minima?hectareas=` | por área | `buscarPorAreaMinima` |
| `GET /api/parcelas/buscar?ubicacion=` | por ubicación | `buscarPorUbicacion` |
| `GET /api/parcelas/productor/{id}/area-total` | suma de hectáreas | `calcularAreaTotalPorProductor` (`SUM`) |
| `GET /api/productores/buscar?texto=` | por nombre/apellido | `buscarPorNombreOApellido` |
| `GET /api/productores/con-parcelas-mayores-a?hectareas=` | join con parcelas | `buscarConParcelasMayoresA` |
| `GET /api/cosechas/fechas?desde=&hasta=` | por fecha | `buscarPorRangoDeFechas` |
| `GET /api/cosechas/campana/{id}/total` | total kg | `calcularTotalKgPorCampana` (`SUM`) |

**`@Transactional`**: los servicios usan `@Transactional(readOnly = true)` a nivel de clase y
`@Transactional` en cada método que escribe. Operaciones con varias escrituras atómicas:

- `POST /api/campanas/{id}/finalizar` → `finalizarCampana()`: guarda la **cosecha** y cambia la
  campaña a **FINALIZADA** en una sola transacción. Si falla (p. ej. campaña ya finalizada) se
  hace *rollback* y no queda ningún cambio.
- `PUT /api/parcelas/{id}/transferir` → `transferir()`: cambia el propietario de una parcela
  validando que el nuevo productor exista y sea distinto.

### 10.3 Spring Security

- Dependencia `spring-boot-starter-security`; configuración en `security/SecurityConfig.java`
  (sin sesiones, `STATELESS`; CSRF deshabilitado porque no se usan cookies).
- **Roles**: `ADMIN`, `USER`, `GUEST` (enum `NombreRol`, tabla `roles`, relación N:M con `usuarios`).
- **Seguridad a nivel de endpoint** con `@PreAuthorize` (controladores de negocio, constantes en
  `security/Permisos`) y `@Secured` (`UsuarioController`), habilitados con `@EnableMethodSecurity`.
- **`CustomUserDetailsService`**: implementa `UserDetailsService` y recupera al usuario desde la
  base de datos (`UsuarioRepository.findByUsername`).
- Contraseñas cifradas con **BCrypt**.

| Operación | ADMIN | USER | GUEST |
|---|:---:|:---:|:---:|
| Lectura (`GET`) | ✔ | ✔ | ✔ |
| Escritura (`POST`, `PUT`) | ✔ | ✔ | ✘ (403) |
| Eliminación (`DELETE`) | ✔ | ✘ (403) | ✘ (403) |
| Gestión de usuarios (`/api/usuarios`) | ✔ | ✘ | ✘ |

Usuarios de prueba (se crean al iniciar, ver `config/DataInitializer`):

| Usuario | Contraseña | Rol |
|---|---|---|
| `admin` | `admin123` | ADMIN |
| `user` | `user123` | USER |
| `guest` | `guest123` | GUEST |

### 10.4 Autenticación con JWT

| Endpoint | Acceso | Descripción |
|---|---|---|
| `POST /api/auth/register` | público | Crea un usuario con rol **USER** (nunca se puede auto-asignar ADMIN) y devuelve su token |
| `POST /api/auth/login` | público | Valida credenciales y devuelve el token JWT |
| Resto de `/api/**` | **token válido** | Sin token / token inválido o vencido → `401`; sin permiso por rol → `403` |

Ejemplo de login:

```http
POST /api/auth/login
Content-Type: application/json

{ "username": "admin", "password": "admin123" }
```

Respuesta: `{ "token": "eyJhbGciOi...", "tipo": "Bearer", "username": "admin", "roles": ["ROLE_ADMIN"], "expiraEnMs": 3600000 }`

Luego, en cada solicitud: `Authorization: Bearer <token>`.

**Flujo de autenticación:**

1. El cliente envía usuario y contraseña a `/api/auth/login`.
2. `AuthServiceImpl` llama al `AuthenticationManager`, que usa `CustomUserDetailsService` (BD) y
   compara la contraseña con BCrypt.
3. Si es correcta, `JwtService` genera un token firmado (HS256) con `sub` (usuario), `roles`,
   `iat` y `exp` (1 hora).
4. El cliente guarda el token y lo envía en el header `Authorization` en cada petición.
5. `JwtAuthenticationFilter` (una vez por solicitud) valida la firma y la expiración, carga el usuario y lo
   registra en el `SecurityContext`.
6. `@PreAuthorize` / `@Secured` revisan el rol; si no corresponde se responde `403`.

La clave de firma está en `app.jwt.secret` (puede sobrescribirse con la variable de entorno `JWT_SECRET`).

## 11. Pruebas automáticas

```
mvn test
```

| Clase | Tipo | Qué verifica |
|---|---|---|
| `ProductorServiceTest`, `ParcelaServiceTest` | Unitaria (APF1) | Validaciones de negocio |
| `CampanaAgricolaServiceTest` | Unitaria | Finalizar campaña (transacción), rango de fechas, resumen |
| `ParcelaTransferenciaTest` | Unitaria | Transferencia de parcelas y consultas |
| `JwtServiceTest` | Unitaria | Generar, validar, expirar y alterar tokens JWT |
| `AuthServiceTest` | Unitaria | Registro (BCrypt, rol USER) y login |
| `SecurityIntegrationTest` | Integración (MockMvc) | 401/403, login, registro, roles, consultas JPQL |

## 12. Base de datos MySQL (opcional)

Con MySQL 8 activo en `localhost:3306`:

```
mvn spring-boot:run -Dspring-boot.run.profiles=mysql
```

Ajustar usuario/contraseña en `src/main/resources/application-mysql.properties`.

## 13. Conclusiones del APF2

- Se completó la capa de persistencia con JPA/Hibernate (7 entidades y 7 repositorios), consultas JPQL
  para filtrar por estado, fecha, cultivo, ubicación y totales, y operaciones transaccionales atómicas.
- Spring Security con JWT permite una API *stateless*: cada solicitud se autentica con un token y se
  autoriza por rol (ADMIN, USER, GUEST), con contraseñas cifradas con BCrypt.
- Quedan para la siguiente unidad: dashboard de indicadores (costos y rendimiento por parcela) y el
  consumo de la API desde un front-end Angular.

---

# APF3 – Integración con el front-end Angular

- Se habilitó **CORS** (`SecurityConfig.corsConfigurationSource`) para el front-end en `http://localhost:4200`.
  Los orígenes se configuran con `app.cors.allowed-origins` en `application.properties`.
- Las solicitudes `OPTIONS` (preflight) están permitidas; el resto de la seguridad JWT/roles no cambió.
- El front-end está en `../frontend` (ver su README y la guía de inicialización).
