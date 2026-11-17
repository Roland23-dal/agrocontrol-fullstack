package com.utp.agrocontrol.security;

/**
 * Expresiones de autorizacion reutilizables para @PreAuthorize.
 * Matriz de permisos del sistema:
 *
 *   Operacion                     ADMIN   USER   GUEST
 *   Lectura (GET)                   X       X      X
 *   Escritura (POST / PUT)          X       X
 *   Eliminacion (DELETE)            X
 *   Gestion de usuarios             X
 */
public final class Permisos {

    public static final String LECTURA = "hasAnyRole('ADMIN','USER','GUEST')";
    public static final String ESCRITURA = "hasAnyRole('ADMIN','USER')";
    public static final String ELIMINACION = "hasRole('ADMIN')";

    private Permisos() {
    }
}
