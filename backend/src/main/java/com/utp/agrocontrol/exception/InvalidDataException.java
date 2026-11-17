package com.utp.agrocontrol.exception;

/**
 * Se lanza cuando una regla de validacion de negocio, aplicada en la capa de
 * servicio, no se cumple (por ejemplo un DNI con formato incorrecto).
 */
public class InvalidDataException extends RuntimeException {
    public InvalidDataException(String message) {
        super(message);
    }
}
