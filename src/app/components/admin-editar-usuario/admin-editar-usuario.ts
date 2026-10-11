import { HttpErrorResponse } from '@angular/common/http';
import {
  ChangeDetectionStrategy,
  Component,
  DestroyRef,
  OnInit,
  inject,
  signal,
} from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import {
  AbstractControl,
  FormControl,
  FormGroup,
  ReactiveFormsModule,
  ValidationErrors,
  Validators,
} from '@angular/forms';
import { ActivatedRoute, RouterLink } from '@angular/router';
import {
  ModificarUsuarioRequest,
  UsuarioDetalle,
  Usuarios,
} from '../../services/consultar-usuarios-service/usuarios';
import { MarcoPagina } from '../marco-pagina/marco-pagina';

function validarMayorDeEdad(control: AbstractControl): ValidationErrors | null {
  if (!control.value) {
    return null;
  }

  const fechaNacimiento = new Date(`${control.value}T00:00:00`);
  if (Number.isNaN(fechaNacimiento.getTime())) {
    return { fechaInvalida: true };
  }
  const fechaLimite = new Date();
  fechaLimite.setFullYear(fechaLimite.getFullYear() - 18);
  fechaLimite.setHours(0, 0, 0, 0);
  return fechaNacimiento <= fechaLimite ? null : { menorDeEdad: true };
}

@Component({
  selector: 'app-admin-editar-usuario',
  imports: [MarcoPagina, ReactiveFormsModule, RouterLink],
  templateUrl: './admin-editar-usuario.html',
  styleUrl: './admin-editar-usuario.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class AdminEditarUsuario implements OnInit {
  private readonly longitudMaximaNombre = 30;
  private readonly route = inject(ActivatedRoute);
  private readonly usuariosService = inject(Usuarios);
  private readonly destroyRef = inject(DestroyRef);

  readonly usuario = signal<UsuarioDetalle | null>(null);
  readonly cargando = signal(true);
  readonly guardando = signal(false);
  readonly mensajeError = signal('');
  readonly mensajeExito = signal('');
  readonly maxFechaNacimiento = this.fechaHaceDieciochoAnios();

  readonly form = new FormGroup({
    nombre: new FormControl('', {
      nonNullable: true,
      validators: [Validators.required, Validators.pattern(/\S/)],
    }),
    apellidos: new FormControl('', {
      nonNullable: true,
      validators: [Validators.required, Validators.pattern(/\S/)],
    }),
    telefono: new FormControl('', {
      nonNullable: true,
      validators: [Validators.pattern(/^\d{9}$/)],
    }),
    imagenPerfil: new FormControl('', { nonNullable: true }),
    dni: new FormControl('', { nonNullable: true }),
    fechaNacimiento: new FormControl('', {
      nonNullable: true,
      validators: [validarMayorDeEdad],
    }),
    tipoCliente: new FormControl<'NORMAL' | 'PREMIUM'>('NORMAL', { nonNullable: true }),
    nombreComercial: new FormControl('', { nonNullable: true }),
    cifNif: new FormControl('', { nonNullable: true }),
    categoriaPrincipalId: new FormControl('', { nonNullable: true }),
    sede: new FormControl('', { nonNullable: true }),
  });

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');
    if (!id) {
      this.cargando.set(false);
      this.mensajeError.set('No se ha indicado el usuario que quieres modificar.');
      return;
    }

    this.usuariosService
      .consultarPorId(id)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (usuario) => {
          this.usuario.set(usuario);
          this.form.patchValue({
            nombre: usuario.name,
            apellidos: usuario.apellidos,
            telefono: usuario.telefono ?? '',
            imagenPerfil: usuario.imagenPerfil ?? '',
            dni: usuario.dni ?? '',
            fechaNacimiento: usuario.fechaNacimiento ?? '',
            tipoCliente: usuario.tipoCliente ?? 'NORMAL',
            nombreComercial: usuario.nombreComercial ?? '',
            cifNif: usuario.cifNif ?? '',
            categoriaPrincipalId: usuario.categoriaPrincipalId ?? '',
            sede: usuario.sede ?? '',
          });
          this.configurarCamposPorRol(usuario.rol);
          this.cargando.set(false);
        },
        error: (error: HttpErrorResponse) => {
          this.mensajeError.set(
            this.textoError(error, 'No se han podido cargar los datos del usuario.'),
          );
          this.cargando.set(false);
        },
      });
  }

  truncarLongitud(event: Event, campo: 'nombre' | 'apellidos'): void {
    const input = event.target as HTMLInputElement;
    const valorTruncado = input.value.slice(0, this.longitudMaximaNombre);
    if (valorTruncado !== input.value) {
      input.value = valorTruncado;
      this.form.controls[campo].setValue(valorTruncado);
    }
  }

  guardar(): void {
    if (this.cargando() || this.guardando() || this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    const usuario = this.usuario();
    if (!usuario) {
      return;
    }

    const valores = this.form.getRawValue();
    const datos: ModificarUsuarioRequest = {
      nombre: valores.nombre.trim().slice(0, this.longitudMaximaNombre),
      apellidos: valores.apellidos.trim().slice(0, this.longitudMaximaNombre),
      ...(valores.telefono.trim() ? { telefono: valores.telefono.trim() } : {}),
      imagenPerfil: valores.imagenPerfil.trim(),
      ...(usuario.rol === 'CLIENTE'
        ? {
            dni: valores.dni.trim(),
            fechaNacimiento: valores.fechaNacimiento,
            tipoCliente: valores.tipoCliente,
          }
        : {}),
      ...(usuario.rol === 'VENDEDOR'
        ? {
            nombreComercial: valores.nombreComercial.trim(),
            cifNif: valores.cifNif.trim(),
            categoriaPrincipalId: valores.categoriaPrincipalId.trim(),
          }
        : {}),
      ...(usuario.rol === 'ADMINISTRADOR' ? { sede: valores.sede.trim() } : {}),
    };

    this.guardando.set(true);
    this.mensajeError.set('');
    this.mensajeExito.set('');
    this.usuariosService
      .modificar(usuario.id, datos)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: () => {
          this.usuario.update((actual) => (actual ? this.aplicarCambios(actual, datos) : actual));
          this.guardando.set(false);
          this.mensajeExito.set('Los datos del usuario se han actualizado correctamente.');
        },
        error: (error: HttpErrorResponse) => {
          this.guardando.set(false);
          this.mensajeError.set(
            this.textoError(error, 'No se han podido guardar los cambios. Inténtalo de nuevo.'),
          );
        },
      });
  }

  esInvalido(campo: keyof typeof this.form.controls): boolean {
    const control = this.form.controls[campo];
    return control.invalid && (control.touched || control.dirty);
  }

  private configurarCamposPorRol(rol: UsuarioDetalle['rol']): void {
    if (rol !== 'CLIENTE') {
      this.form.controls.dni.disable();
      this.form.controls.fechaNacimiento.disable();
      this.form.controls.tipoCliente.disable();
    } else {
      this.form.controls.dni.addValidators([Validators.required, Validators.pattern(/\S/)]);
      this.form.controls.fechaNacimiento.addValidators(Validators.required);
      this.form.controls.dni.updateValueAndValidity();
      this.form.controls.fechaNacimiento.updateValueAndValidity();
    }

    if (rol !== 'VENDEDOR') {
      this.form.controls.nombreComercial.disable();
      this.form.controls.cifNif.disable();
      this.form.controls.categoriaPrincipalId.disable();
    } else {
      this.form.controls.nombreComercial.addValidators([
        Validators.required,
        Validators.pattern(/\S/),
      ]);
      this.form.controls.cifNif.addValidators([Validators.required, Validators.pattern(/\S/)]);
      this.form.controls.categoriaPrincipalId.addValidators([
        Validators.required,
        Validators.pattern(/\S/),
      ]);
      this.form.controls.nombreComercial.updateValueAndValidity();
      this.form.controls.cifNif.updateValueAndValidity();
      this.form.controls.categoriaPrincipalId.updateValueAndValidity();
    }

    if (rol !== 'ADMINISTRADOR') {
      this.form.controls.sede.disable();
    }
  }

  private aplicarCambios(usuario: UsuarioDetalle, datos: ModificarUsuarioRequest): UsuarioDetalle {
    return {
      ...usuario,
      name: datos.nombre,
      apellidos: datos.apellidos,
      telefono: datos.telefono ?? usuario.telefono,
      imagenPerfil: datos.imagenPerfil,
      ...(datos.dni !== undefined ? { dni: datos.dni } : {}),
      ...(datos.fechaNacimiento !== undefined ? { fechaNacimiento: datos.fechaNacimiento } : {}),
      ...(datos.tipoCliente !== undefined ? { tipoCliente: datos.tipoCliente } : {}),
      ...(datos.nombreComercial !== undefined ? { nombreComercial: datos.nombreComercial } : {}),
      ...(datos.cifNif !== undefined ? { cifNif: datos.cifNif } : {}),
      ...(datos.categoriaPrincipalId !== undefined
        ? { categoriaPrincipalId: datos.categoriaPrincipalId }
        : {}),
      ...(datos.sede !== undefined ? { sede: datos.sede } : {}),
    };
  }

  private textoError(error: HttpErrorResponse, fallback: string): string {
    if (error.status === 401) {
      return 'Tu sesión ha caducado. Inicia sesión de nuevo.';
    }
    if (error.status === 403) {
      return 'No tienes permisos para modificar usuarios.';
    }
    if (error.status === 404) {
      return 'El usuario no existe o ya no está disponible.';
    }
    if (error.status === 0) {
      return 'No se ha podido conectar con el servidor.';
    }

    const mensaje = error.error?.message ?? error.error?.mensaje ?? error.error;
    return typeof mensaje === 'string' && mensaje.length > 0 ? mensaje : fallback;
  }

  private fechaHaceDieciochoAnios(): string {
    const fecha = new Date();
    fecha.setFullYear(fecha.getFullYear() - 18);
    const mes = String(fecha.getMonth() + 1).padStart(2, '0');
    const dia = String(fecha.getDate()).padStart(2, '0');
    return `${fecha.getFullYear()}-${mes}-${dia}`;
  }
}
