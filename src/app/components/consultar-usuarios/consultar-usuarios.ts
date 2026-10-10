import { HttpErrorResponse } from '@angular/common/http';
import {
  ChangeDetectionStrategy,
  Component,
  DestroyRef,
  OnInit,
  computed,
  inject,
  signal,
} from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { AuthService } from '../../services/auth.service';
import { Usuarios, type Usuario } from '../../services/consultar-usuarios-service/usuarios';
import { MarcoPagina } from '../marco-pagina/marco-pagina';

type FiltroEliminados = 'todos' | 'eliminados' | 'no-eliminados';

@Component({
  selector: 'app-consultar-usuarios',
  imports: [MarcoPagina],
  templateUrl: './consultar-usuarios.html',
  styleUrl: './consultar-usuarios.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ConsultarUsuarios implements OnInit {
  private readonly usuariosService = inject(Usuarios);
  private readonly authService = inject(AuthService);
  private readonly destroyRef = inject(DestroyRef);
  private temporizadorMensaje: ReturnType<typeof setTimeout> | undefined;

  constructor() {
    this.destroyRef.onDestroy(() => this.limpiarTemporizadorMensaje());
  }

  readonly usuarios = signal<Usuario[]>([]);
  readonly busquedaId = signal('');
  readonly filtroEliminados = signal<FiltroEliminados>('todos');
  readonly cargando = signal(true);
  readonly mensajeError = signal('');
  readonly mensajeErrorAccion = signal('');
  readonly mensajeAccion = signal('');
  readonly eliminandoId = signal<string | null>(null);
  readonly usuarioPendienteDeEliminar = signal<Usuario | null>(null);
  readonly usuariosFiltrados = computed(() => {
    const idBuscado = this.busquedaId().trim().toLocaleLowerCase();
    return this.usuarios().filter((usuario) => {
      const coincideId = !idBuscado || usuario.id.toLocaleLowerCase().includes(idBuscado);
      const coincideEliminado =
        this.filtroEliminados() === 'todos' ||
        usuario.eliminado === (this.filtroEliminados() === 'eliminados');
      return coincideId && coincideEliminado;
    });
  });

  ngOnInit(): void {
    this.consultarUsuarios();
  }

  consultarUsuarios(): void {
    this.cargando.set(true);
    this.mensajeError.set('');
    this.usuariosService.consultar().subscribe({
      next: (usuarios) => {
        this.usuarios.set(usuarios);
        this.cargando.set(false);
      },
      error: (error: HttpErrorResponse) => {
        this.mensajeError.set(this.textoDeError(error));
        this.cargando.set(false);
      },
    });
  }

  actualizarBusquedaId(event: Event): void {
    this.busquedaId.set((event.target as HTMLInputElement).value);
  }

  actualizarFiltroEliminados(event: Event): void {
    this.filtroEliminados.set((event.target as HTMLSelectElement).value as FiltroEliminados);
  }

  eliminarUsuario(usuario: Usuario): void {
    if (usuario.eliminado || this.esUsuarioActual(usuario) || this.eliminandoId() !== null) {
      return;
    }

    this.usuarioPendienteDeEliminar.set(usuario);
  }

  cancelarEliminacion(): void {
    this.usuarioPendienteDeEliminar.set(null);
  }

  confirmarEliminacion(): void {
    const usuario = this.usuarioPendienteDeEliminar();
    if (!usuario || this.eliminandoId() !== null) {
      return;
    }
    if (this.esUsuarioActual(usuario)) {
      this.usuarioPendienteDeEliminar.set(null);
      this.mensajeErrorAccion.set('No puedes eliminar tu propia cuenta.');
      return;
    }

    this.usuarioPendienteDeEliminar.set(null);
    this.eliminandoId.set(usuario.id);
    this.mensajeAccion.set('');
    this.mensajeErrorAccion.set('');
    this.usuariosService
      .eliminar(usuario.id)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: () => {
          this.usuarios.update((usuarios) =>
            usuarios.map((actual) =>
              actual.id === usuario.id ? { ...actual, eliminado: true } : actual,
            ),
          );
          this.mensajeAccion.set(
            `Se ha marcado como eliminado a ${usuario.name} ${usuario.apellidos}.`,
          );
          this.limpiarTemporizadorMensaje();
          this.temporizadorMensaje = setTimeout(() => this.mensajeAccion.set(''), 5000);
          this.eliminandoId.set(null);
        },
        error: (error: HttpErrorResponse) => {
          this.mensajeErrorAccion.set(this.textoErrorEliminacion(error));
          this.eliminandoId.set(null);
        },
      });
  }

  esUsuarioActual(usuario: Usuario): boolean {
    const correoActual = this.authService.email();
    return (
      correoActual !== null &&
      usuario.email.trim().toLocaleLowerCase() === correoActual.trim().toLocaleLowerCase()
    );
  }

  private textoDeError(error: HttpErrorResponse): string {
    if (error.status === 401) {
      return 'Tu sesión ha caducado. Inicia sesión de nuevo.';
    }
    if (error.status === 403) {
      return 'No tienes permisos para gestionar usuarios.';
    }
    if (error.status === 0) {
      return 'No se ha podido conectar con el servidor.';
    }
    return 'No se han podido cargar los usuarios. Inténtalo de nuevo.';
  }

  private textoErrorEliminacion(error: HttpErrorResponse): string {
    if (error.status === 401) {
      return 'Tu sesión ha caducado. Inicia sesión de nuevo.';
    }
    if (error.status === 403) {
      return this.mensajeApi(error, 'No tienes permiso para eliminar este usuario.');
    }
    if (error.status === 404) {
      return 'El usuario ya no está disponible para eliminar.';
    }
    if (error.status === 0) {
      return 'No se ha podido conectar con el servidor.';
    }
    return this.mensajeApi(error, 'No se ha podido eliminar el usuario. Inténtalo de nuevo.');
  }

  private mensajeApi(error: HttpErrorResponse, fallback: string): string {
    const mensaje = error.error?.message ?? error.error?.mensaje ?? error.error;
    return typeof mensaje === 'string' && mensaje.length > 0 ? mensaje : fallback;
  }

  private limpiarTemporizadorMensaje(): void {
    if (this.temporizadorMensaje !== undefined) {
      clearTimeout(this.temporizadorMensaje);
      this.temporizadorMensaje = undefined;
    }
  }
}
