import { Component, Input } from '@angular/core';
import { AbstractControl } from '@angular/forms';

/**
 * Muestra el mensaje de error de un control de formulario (validaciones de Angular
 * y errores de validación que devuelve el backend) cuando el usuario ya lo tocó.
 */
@Component({
  selector: 'app-field-error',
  standalone: true,
  template: `@if (mensaje(); as m) {
    <span class="error-campo" role="alert">{{ m }}</span>
  }`,
})
export class FieldErrorComponent {
  @Input() control: AbstractControl | null = null;
  /** Mensaje a mostrar cuando falla el validador de patrón (formato). */
  @Input() patron = 'El formato ingresado no es válido';

  mensaje(): string | null {
    const c = this.control;
    if (!c || !c.errors || !(c.touched || c.dirty)) {
      return null;
    }
    const e = c.errors;
    if (e['servidor']) {
      return String(e['servidor']);
    }
    if (e['required']) {
      return 'Este campo es obligatorio';
    }
    if (e['email']) {
      return 'Ingresa un correo electrónico válido';
    }
    if (e['minlength']) {
      return `Debe tener al menos ${e['minlength'].requiredLength} caracteres`;
    }
    if (e['maxlength']) {
      return `No puede superar los ${e['maxlength'].requiredLength} caracteres`;
    }
    if (e['pattern']) {
      return this.patron;
    }
    if (e['min']) {
      return `El valor mínimo permitido es ${e['min'].min}`;
    }
    if (e['max']) {
      return `El valor máximo permitido es ${e['max'].max}`;
    }
    if (e['rangoFechas']) {
      return 'La fecha de fin no puede ser anterior a la fecha de inicio';
    }
    return 'El valor ingresado no es válido';
  }
}
