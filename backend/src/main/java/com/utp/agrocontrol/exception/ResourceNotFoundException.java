package com.utp.agrocontrol.exception;

/**
 * Se lanza cuando se busca una entidad por id y esta no existe en la base de datos.
 */
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
