package com.utp.agrocontrol.model;

/**
 * Roles disponibles en el sistema. En Spring Security se exponen con el
 * prefijo "ROLE_" (por ejemplo ROLE_ADMIN), de modo que se pueden usar
 * expresiones como hasRole('ADMIN').
 *
 *  - ADMIN: acceso total (lectura, escritura, eliminacion y gestion de usuarios).
 *  - USER : lectura y escritura (crear / actualizar), sin eliminar.
 *  - GUEST: solo lectura.
 */
public enum NombreRol {
    ADMIN,
    USER,
    GUEST
}
