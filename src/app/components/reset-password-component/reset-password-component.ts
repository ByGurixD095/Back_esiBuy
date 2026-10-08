import { HttpErrorResponse } from '@angular/common/http';
import { Component, inject, signal } from '@angular/core';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { ResetPasswordService } from '../../services/reset-password-service/reset-password-service';
import { MarcoPagina } from '../../compartido/marco-pagina/marco-pagina';

const PATRON_CONTRASENA = /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[^\w\s]).+$/;

@Component({
  selector: 'app-reset-password-component',
  imports: [ReactiveFormsModule, RouterLink, MarcoPagina],
  templateUrl: './reset-password-component.html',
  styleUrl: './reset-password-component.css',
})
export class ResetPasswordComponent {
  private readonly resetPasswordService = inject(ResetPasswordService);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly token = this.route.snapshot.queryParamMap.get('token') ?? '';

  readonly mensajeExito = signal('');
  readonly mensajeError = signal('');
  readonly enviando = signal(false);
  readonly tokenInvalido = signal(!this.token);
  readonly mostrarContrasena = signal(false);

  readonly form = new FormGroup({
    contrasena: new FormControl('', {
      nonNullable: true,
      validators: [
        Validators.required,
        Validators.minLength(8),
        Validators.maxLength(72),
        Validators.pattern(PATRON_CONTRASENA),
      ],
    }),
    confirmarContrasena: new FormControl('', {
      nonNullable: true,
      validators: [Validators.required],
    }),
  });

  alternarContrasena(): void {
    this.mostrarContrasena.update((visible) => !visible);
  }

  enviar(): void {
    this.form.controls.confirmarContrasena.setErrors(
      this.form.controls.contrasena.value === this.form.controls.confirmarContrasena.value
        ? null
        : { noCoinciden: true },
    );

    if (this.form.invalid || this.tokenInvalido()) {
      this.form.markAllAsTouched();
      if (this.tokenInvalido()) {
        this.mensajeError.set('El enlace de recuperación no es válido.');
      }
      return;
    }

    this.mensajeExito.set('');
    this.mensajeError.set('');
    this.enviando.set(true);
    const { contrasena, confirmarContrasena } = this.form.getRawValue();

    this.resetPasswordService
      .restablecer({ token: this.token, pwd1: contrasena, pwd2: confirmarContrasena })
      .subscribe({
        next: () => {
          this.enviando.set(false);
          this.mensajeExito.set('Tu contraseña se ha cambiado correctamente.');
          this.form.disable();
          setTimeout(() => {
            void this.router.navigate(['/login']);
          }, 1200);
        },
        error: (error: HttpErrorResponse) => {
          this.enviando.set(false);
          this.mensajeError.set(this.textoDeError(error));
        },
      });
  }

  esInvalido(campo: 'contrasena' | 'confirmarContrasena'): boolean {
    const control = this.form.controls[campo];
    return control.invalid && (control.touched || control.dirty);
  }

  private textoDeError(error: HttpErrorResponse): string {
    if (error.status === 0) {
      return 'No se ha podido conectar con el servidor.';
    }
    const mensaje = error.error?.message ?? error.error?.mensaje ?? error.error?.detail;
    if (typeof mensaje === 'string' && mensaje.length > 0) {
      return mensaje;
    }
    if (error.status === 400) {
      return 'El enlace no es válido o ha caducado. Solicita uno nuevo.';
    }
    return 'No se ha podido cambiar la contraseña. Inténtalo de nuevo.';
  }
}
