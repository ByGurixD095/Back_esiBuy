import { HttpErrorResponse } from '@angular/common/http';
import { ChangeDetectionStrategy, Component, inject, signal } from '@angular/core';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { AuthService, LoginResponse } from '../../services/auth.service';
import { Registro as RegistroService } from '../../services/registro-service/registro';
import { MarcoPagina } from '../marco-pagina/marco-pagina';

type TipoRegistro = 'cliente' | 'vendedor';

const NO_VACIO = /\S/;

function fechaAnteriorAHoy(fecha: string): boolean {
  if (!fecha) {
    return false;
  }
  const hoy = new Date();
  const fechaLocal = new Date(`${fecha}T00:00:00`);
  hoy.setHours(0, 0, 0, 0);
  return !Number.isNaN(fechaLocal.getTime()) && fechaLocal < hoy;
}

@Component({
  selector: 'app-registro',
  imports: [ReactiveFormsModule, MarcoPagina],
  templateUrl: './registro.html',
  styleUrl: './registro.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class RegistroComponent {
  private readonly registroService = inject(RegistroService);
  private readonly authService = inject(AuthService);
  private readonly router = inject(Router);
  readonly tipo: TipoRegistro =
    inject(ActivatedRoute).snapshot.data['tipoRegistro'] === 'vendedor' ? 'vendedor' : 'cliente';
  readonly esVendedor = this.tipo === 'vendedor';
  readonly mensajeExito = signal('');
  readonly mensajeError = signal('');
  readonly enviando = signal(false);
  readonly cuentaCreada = signal(false);
  readonly puedeReintentarLogin = signal(false);
  readonly fechaMaxima = this.fechaDeHoy();

  readonly form = new FormGroup({
    nombre: new FormControl('', {
      nonNullable: true,
      validators: [Validators.required, Validators.pattern(NO_VACIO)],
    }),
    apellidos: new FormControl('', {
      nonNullable: true,
      validators: [Validators.required, Validators.pattern(NO_VACIO)],
    }),
    email: new FormControl('', {
      nonNullable: true,
      validators: [Validators.required, Validators.email],
    }),
    contrasena: new FormControl('', {
      nonNullable: true,
      validators: [Validators.required, Validators.pattern(NO_VACIO)],
    }),
    telefono: new FormControl('', { nonNullable: true }),
    imagenPerfil: new FormControl('', { nonNullable: true }),
    dni: new FormControl('', { nonNullable: true }),
    fechaNacimiento: new FormControl('', { nonNullable: true }),
    nombreComercial: new FormControl('', { nonNullable: true }),
    cifNif: new FormControl('', { nonNullable: true }),
    categoriaPrincipalId: new FormControl('', { nonNullable: true }),
  });

  constructor() {
    if (this.esVendedor) {
      this.form.controls.nombreComercial.addValidators([
        Validators.required,
        Validators.pattern(NO_VACIO),
      ]);
      this.form.controls.cifNif.addValidators([Validators.required, Validators.pattern(NO_VACIO)]);
      this.form.controls.categoriaPrincipalId.addValidators([
        Validators.required,
        Validators.pattern(NO_VACIO),
      ]);
      this.form.controls.nombreComercial.updateValueAndValidity();
      this.form.controls.cifNif.updateValueAndValidity();
      this.form.controls.categoriaPrincipalId.updateValueAndValidity();
      this.form.controls.dni.disable();
      this.form.controls.fechaNacimiento.disable();
    } else {
      this.form.controls.dni.addValidators([Validators.required, Validators.pattern(NO_VACIO)]);
      this.form.controls.fechaNacimiento.addValidators([
        Validators.required,
        (control) => (fechaAnteriorAHoy(control.value) ? null : { past: true }),
      ]);
      this.form.controls.dni.updateValueAndValidity();
      this.form.controls.fechaNacimiento.updateValueAndValidity();
      this.form.controls.nombreComercial.disable();
      this.form.controls.cifNif.disable();
      this.form.controls.categoriaPrincipalId.disable();
    }
    this.form.updateValueAndValidity();
  }

  enviar(): void {
    if (this.enviando() || this.cuentaCreada()) {
      return;
    }
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.mensajeExito.set('');
    this.mensajeError.set('');
    this.enviando.set(true);

    const datos = this.form.getRawValue();
    const peticion = this.esVendedor
      ? this.registroService.registrarVendedor({
          nombre: datos.nombre,
          apellidos: datos.apellidos,
          email: datos.email,
          contrasena: datos.contrasena,
          telefono: datos.telefono,
          imagenPerfil: datos.imagenPerfil,
          nombreComercial: datos.nombreComercial,
          cifNif: datos.cifNif,
          categoriaPrincipalId: datos.categoriaPrincipalId,
        })
      : this.registroService.registrarCliente({
          nombre: datos.nombre,
          apellidos: datos.apellidos,
          email: datos.email,
          contrasena: datos.contrasena,
          telefono: datos.telefono,
          imagenPerfil: datos.imagenPerfil,
          dni: datos.dni,
          fechaNacimiento: datos.fechaNacimiento,
        });

    peticion.subscribe({
      next: (respuesta) => {
        this.cuentaCreada.set(true);
        this.procesarRespuestaRegistro(respuesta);
      },
      error: (error: HttpErrorResponse) => {
        this.enviando.set(false);
        this.mensajeError.set(this.textoDeError(error));
      },
    });
  }

  reintentarInicioSesion(): void {
    if (!this.cuentaCreada() || this.enviando()) {
      return;
    }
    this.iniciarSesionTrasRegistro();
  }

  esInvalido(campo: keyof typeof this.form.controls): boolean {
    const control = this.form.controls[campo];
    return control.invalid && (control.touched || control.dirty);
  }

  private textoDeError(error: HttpErrorResponse): string {
    if (error.status === 0) {
      return 'No se ha podido conectar con el servidor.';
    }
    if (error.status === 409) {
      return 'Ya existe una cuenta con ese correo electrónico o documento.';
    }
    if (error.status === 400) {
      return 'No se ha podido crear la cuenta. Revisa los datos introducidos.';
    }
    return 'Ha ocurrido un error al crear la cuenta. Inténtalo de nuevo.';
  }

  private iniciarSesionTrasRegistro(): void {
    this.enviando.set(true);
    this.mensajeError.set('');
    this.puedeReintentarLogin.set(false);

    const { email, contrasena } = this.form.getRawValue();
    this.authService.login({ username: email, password: contrasena }).subscribe({
      next: (respuesta) => this.procesarRespuestaLogin(respuesta),
      error: () => {
        this.enviando.set(false);
        this.mensajeExito.set('');
        this.mensajeError.set(
          'La cuenta se creó, pero no se pudo iniciar sesión automáticamente. Puedes volver a intentarlo.',
        );
        this.puedeReintentarLogin.set(true);
      },
    });
  }

  private procesarRespuestaRegistro(respuesta: LoginResponse): void {
    this.enviando.set(false);
    if (respuesta.mfaStatus === 'SUCCESS') {
      this.authService.applyAuthenticationResponse(respuesta);
      this.mensajeExito.set('Cuenta creada e inicio de sesión completado.');
      void this.router.navigate(['/']);
      return;
    }

    this.mensajeExito.set('Cuenta creada. Continúa el inicio de sesión para configurar la autenticación.');
    void this.router.navigate(['/login'], {
      queryParams: { email: this.form.controls.email.value },
    });
  }

  private procesarRespuestaLogin(respuesta: LoginResponse): void {
    this.enviando.set(false);
    if (respuesta.mfaStatus === 'SUCCESS') {
      this.mensajeExito.set('Cuenta creada e inicio de sesión completado.');
      void this.router.navigate(['/']);
      return;
    }

    this.mensajeExito.set('');
    if (
      respuesta.mfaStatus === 'REQUIRES_MFA_SETUP' ||
      respuesta.mfaStatus === 'REQUIRES_MFA_VERIFICATION'
    ) {
      this.mensajeError.set(
        'La cuenta se creó, pero el servidor requiere completar la verificación MFA antes de iniciar sesión. Ese paso aún no está disponible.',
      );
      return;
    }

    this.mensajeError.set(
      'La cuenta se creó, pero el inicio de sesión no se completó. Inténtalo de nuevo.',
    );
    this.puedeReintentarLogin.set(true);
  }

  private fechaDeHoy(): string {
    const hoy = new Date();
    const mes = String(hoy.getMonth() + 1).padStart(2, '0');
    const dia = String(hoy.getDate()).padStart(2, '0');
    return `${hoy.getFullYear()}-${mes}-${dia}`;
  }
}
