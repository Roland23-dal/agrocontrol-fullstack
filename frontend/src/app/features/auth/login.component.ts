import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { environment } from '../../../environments/environment';
import { AuthService } from '../../core/services/auth.service';
import { obtenerMensajeError } from '../../core/utils/http-error';
import { FieldErrorComponent } from '../../shared/ui/field-error.component';

/** Formulario de login: envía las credenciales a POST /api/auth/login y guarda el token JWT. */
@Component({
  selector: 'app-login',
  standalone: true,
  imports: [ReactiveFormsModule, RouterLink, FieldErrorComponent],
  templateUrl: './login.component.html',
  styleUrl: './login.component.scss',
})
export class LoginComponent {
  private readonly fb = inject(FormBuilder);
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);
  private readonly route = inject(ActivatedRoute);

  readonly enviando = signal(false);
  readonly error = signal('');
  readonly mostrarDemo = !environment.production;

  readonly form = this.fb.nonNullable.group({
    username: ['', [Validators.required]],
    password: ['', [Validators.required]],
  });

  get f() {
    return this.form.controls;
  }

  usarCuentaDemo(username: string, password: string): void {
    this.form.setValue({ username, password });
  }

  entrar(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    this.enviando.set(true);
    this.error.set('');

    this.auth.login(this.form.getRawValue()).subscribe({
      next: () => {
        // Redirección: vuelve a la página que se intentó abrir o va al inicio según el rol
        const returnUrl = this.route.snapshot.queryParamMap.get('returnUrl');
        this.router.navigateByUrl(returnUrl && returnUrl !== '/login' ? returnUrl : this.auth.rutaInicio());
      },
      error: (err) => {
        this.enviando.set(false);
        this.error.set(obtenerMensajeError(err));
      },
    });
  }
}
