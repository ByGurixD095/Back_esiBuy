import { HttpErrorResponse } from '@angular/common/http';
import { Component, inject, signal } from '@angular/core';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { RecoverPasswordService } from '../../services/recover-password-service/recover-password-service';
import { MarcoPagina } from '../../compartido/marco-pagina/marco-pagina';

@Component({
  selector: 'app-recover-password-component',
  imports: [ReactiveFormsModule, RouterLink, MarcoPagina],
  templateUrl: './recover-password-component.html',
  styleUrl: './recover-password-component.css',
})
export class RecoverPasswordComponent {
  private readonly recoverPasswordService = inject(RecoverPasswordService);

  readonly mensajeExito = signal('');
  readonly mensajeError = signal('');
  readonly enviando = signal(false);

  readonly form = new FormGroup({
    email: new FormControl('', {
      nonNullable: true,
      validators: [Validators.required, Validators.email],
    }),
  });

  enviar(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.mensajeExito.set('');
    this.mensajeError.set('');
    this.enviando.set(true);

    this.recoverPasswordService.solicitar(this.form.getRawValue()).subscribe({
      next: () => {
        this.enviando.set(false);
        this.mensajeExito.set(
          'Si existe una cuenta con ese correo, recibirás un enlace para restablecer la contraseña.',
        );
        this.form.reset();
      },
      error: (error: HttpErrorResponse) => {
        this.enviando.set(false);
        this.mensajeError.set(this.textoDeError(error));
      },
    });
  }

  esInvalido(): boolean {
    const control = this.form.controls.email;
    return control.invalid && (control.touched || control.dirty);
  }

  private textoDeError(error: HttpErrorResponse): string {
    if (error.status === 0) {
      return 'No se ha podido conectar con el servidor.';
    }
    return 'No se ha podido solicitar la recuperación. Inténtalo de nuevo.';
  }
}
