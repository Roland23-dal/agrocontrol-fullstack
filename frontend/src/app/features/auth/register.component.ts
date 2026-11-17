import { Component, inject, signal } from '@angular/core';
import { AbstractControl, FormBuilder, ReactiveFormsModule, ValidationErrors, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../core/services/auth.service';
import { ToastService } from '../../core/services/toast.service';
import { aplicarErroresServidor, obtenerMensajeError } from '../../core/utils/http-error';
import { FieldErrorComponent } from '../../shared/ui/field-error.component';

/** Validador de grupo: la confirmación de la contraseña debe coincidir. */
function contrasenasIguales(grupo: AbstractControl): ValidationErrors | null {
  const clave = grupo.get('password')?.value;
  const confirmacion = grupo.get('confirmar')?.value;
  return clave && confirmacion && clave !== confirmacion ? { noCoinciden: true } : null;
}

/** Registro de usuarios: POST /api/auth/register (el backend asigna el rol USER). */
@Component({
  selector: 'app-register',
  standalone: true,
  imports: [ReactiveFormsModule, RouterLink, FieldErrorComponent],
  templateUrl: './register.component.html',
  styleUrl: './register.component.scss',
})
export class RegisterComponent {
  private readonly fb = inject(FormBuilder);
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);
  private readonly toast = inject(ToastService);

  readonly enviando = signal(false);
  readonly error = signal('');

  readonly form = this.fb.nonNullable.group(
    {
      nombreCompleto: ['', [Validators.required, Validators.minLength(3), Validators.maxLength(120)]],
      email: ['', [Validators.required, Validators.email, Validators.maxLength(120)]],
      username: ['', [Validators.required, Validators.minLength(4), Validators.maxLength(50)]],
      password: ['', [Validators.required, Validators.minLength(6), Validators.maxLength(100)]],
      confirmar: ['', [Validators.required]],
    },
    { validators: contrasenasIguales },
  );

  get f() {
    return this.form.controls;
  }

  registrar(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    this.enviando.set(true);
    this.error.set('');

    const { confirmar, ...datos } = this.form.getRawValue();
    void confirmar;

    this.auth.registrar(datos).subscribe({
      next: () => {
        this.toast.exito('Cuenta creada correctamente. ¡Bienvenido a AgroControl!');
        this.router.navigateByUrl(this.auth.rutaInicio());
      },
      error: (err) => {
        this.enviando.set(false);
        if (!aplicarErroresServidor(err, this.form)) {
          this.error.set(obtenerMensajeError(err));
        }
      },
    });
  }
}
