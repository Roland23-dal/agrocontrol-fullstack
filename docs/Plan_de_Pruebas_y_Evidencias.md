# AgroControl APF3 - Plan de pruebas funcionales y evidencias

Ejecutar con backend (`:8082`) y frontend (`:4200`) activos. Guardar las capturas en `docs/evidencias/`
con el nombre indicado y pegarlas en el informe. Resultado: marcar OK / FALLA.

| N | Prueba | Pasos | Resultado esperado | Captura | OK |
|---|---|---|---|---|---|
| 1 | Login correcto | /login, admin / admin123 | Redirige a /dashboard; token en localStorage (F12 > Application) | 01-login-ok.png | |
| 2 | Login incorrecto | admin / mala | Mensaje "Credenciales invalidas" (401) | 02-login-error.png | |
| 3 | Validacion login | Enviar vacio | Mensajes "obligatorio" | 03-login-validacion.png | |
| 4 | Registro | /registro con datos nuevos | Cuenta creada, entra como USER a /campanas | 04-registro.png | |
| 5 | Validacion registro | Clave corta, email invalido, claves distintas | Errores por campo | 05-registro-validacion.png | |
| 6 | Redireccion por rol | Entrar con admin, user y guest | admin y guest a /dashboard, user a /campanas | 06-roles.png | |
| 7 | Ruta protegida | Cerrar sesion y abrir /productores | Redirige a /login | 07-guard.png | |
| 8 | Token en peticiones | F12 > Network > cualquier GET | Header Authorization: Bearer ... | 08-token-network.png | |
| 9 | Consulta (GET) | Productores, Parcelas, Cultivos, Campanas, Cosechas | Tablas y tarjetas con datos | 09-consultas.png | |
| 10 | Filtros JPQL | Campanas por estado, fechas, cultivo, atrasadas | Resultados filtrados | 10-filtros.png | |
| 11 | Crear (POST) | Nuevo productor DNI 8 digitos | Aparece en la lista, mensaje de exito | 11-crear.png | |
| 12 | Validacion formulario | DNI de 5 digitos, correo invalido | Errores bajo el campo, no envia | 12-validacion.png | |
| 13 | DNI duplicado (backend) | Repetir un DNI | Mensaje de error del servidor | 13-duplicado.png | |
| 14 | Editar (PUT) | Editar un productor | Cambios visibles en lista | 14-editar.png | |
| 15 | Eliminar (DELETE) | admin elimina un productor sin datos importantes | Confirmacion y desaparece | 15-eliminar.png | |
| 16 | Finalizar campana | Detalle campana EN_CURSO > Finalizar | Estado FINALIZADA y cosecha registrada | 16-finalizar.png | |
| 17 | Autorizacion USER | user: no ve boton Eliminar; /usuarios redirige | Acceso restringido | 17-user.png | |
| 18 | Autorizacion GUEST | guest: sin botones de crear/editar | Solo lectura | 18-guest.png | |
| 19 | Administrar usuarios | admin cambia rol de un usuario | Rol actualizado | 19-usuarios.png | |
| 20 | Responsivo | F12 modo movil (375 px) | Menu hamburguesa, tablas con scroll | 20-movil.png | |
| 21 | Sesion expirada | Borrar el token / esperar 1 h y navegar | Aviso y redireccion a login | 21-expirada.png | |
| 22 | Backend caido | Detener backend y recargar lista | Aviso "sin conexion" | 22-sin-conexion.png | |

Tambien se recomienda grabar un video corto (2-3 min) recorriendo: login > consulta > crear > editar > eliminar > cerrar sesion.
