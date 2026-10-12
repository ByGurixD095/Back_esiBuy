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
import { DatePipe } from '@angular/common';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { RouterLink } from '@angular/router';
import { map, type Observable } from 'rxjs';
import { AuthService } from '../../services/auth.service';
import {
  Usuarios,
  type Usuario,
  type UsuarioDetalle,
} from '../../services/consultar-usuarios-service/usuarios';
import { MarcoPagina } from '../marco-pagina/marco-pagina';

type FiltroEliminados = 'todos' | 'eliminados' | 'no-eliminados';
type AccionUsuario = 'eliminar' | 'bloquear' | 'desbloquear' | 'activar';

@Component({
  selector: 'app-consultar-usuarios',
  imports: [DatePipe, MarcoPagina, RouterLink],
  templateUrl: './consultar-usuarios.html',
  styleUrl: './consultar-usuarios.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ConsultarUsuarios implements OnInit {
  readonly tamanoPagina = 36;

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
  readonly usuarioSeleccionado = signal<Usuario | null>(null);
  readonly usuarioDetalle = signal<UsuarioDetalle | null>(null);
  readonly cargandoDetalle = signal(false);
  readonly errorDetalle = signal('');
  readonly eliminandoId = signal<string | null>(null);
  readonly accionandoId = signal<string | null>(null);
  readonly accionPendiente = signal<{ usuario: Usuario; accion: AccionUsuario } | null>(null);
  readonly paginaActual = signal(0);
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
  readonly totalPaginas = computed(() =>
    Math.ceil(this.usuariosFiltrados().length / this.tamanoPagina),
  );
  readonly usuariosPagina = computed(() => {
    const inicio = this.paginaActual() * this.tamanoPagina;
    return this.usuariosFiltrados().slice(inicio, inicio + this.tamanoPagina);
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
        this.paginaActual.set(0);
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
    this.paginaActual.set(0);
  }

  actualizarFiltroEliminados(event: Event): void {
    this.filtroEliminados.set((event.target as HTMLSelectElement).value as FiltroEliminados);
    this.paginaActual.set(0);
  }

  paginaAnterior(): void {
    if (this.paginaActual() === 0) return;
    this.paginaActual.update((pagina) => pagina - 1);
  }

  paginaSiguiente(): void {
    if (this.paginaActual() + 1 >= this.totalPaginas()) return;
    this.paginaActual.update((pagina) => pagina + 1);
  }

  verDetalleUsuario(usuario: Usuario): void {
    if (this.cargandoDetalle()) {
      return;
    }

    this.usuarioSeleccionado.set(usuario);
    this.usuarioDetalle.set(null);
    this.errorDetalle.set('');
    this.cargarDetalleUsuario(usuario);
  }

  reintentarDetalleUsuario(): void {
    const usuario = this.usuarioSeleccionado();
    if (usuario) {
      this.cargarDetalleUsuario(usuario);
    }
  }

  cerrarDetalleUsuario(): void {
    this.usuarioSeleccionado.set(null);
    this.usuarioDetalle.set(null);
    this.errorDetalle.set('');
    this.cargandoDetalle.set(false);
  }

  eliminarUsuario(usuario: Usuario): void {
    if (usuario.eliminado || this.esUsuarioActual(usuario) || this.hayAccionEnCurso()) {
      return;
    }

    this.accionPendiente.set({ usuario, accion: 'eliminar' });
  }

  cambiarBloqueoUsuario(usuario: Usuario): void {
    if (usuario.eliminado || this.hayAccionEnCurso()) {
      return;
    }

    this.accionPendiente.set({
      usuario,
      accion: usuario.bloqueado ? 'desbloquear' : 'bloquear',
    });
  }

  activarUsuario(usuario: Usuario): void {
    if (usuario.eliminado || usuario.activo || this.hayAccionEnCurso()) {
      return;
    }

    this.accionPendiente.set({ usuario, accion: 'activar' });
  }

  cancelarEliminacion(): void {
    this.accionPendiente.set(null);
  }

  confirmarEliminacion(): void {
    const pendiente = this.accionPendiente();
    if (!pendiente || this.hayAccionEnCurso()) {
      return;
    }

    const { usuario, accion } = pendiente;
    if (accion === 'eliminar' && this.esUsuarioActual(usuario)) {
      this.accionPendiente.set(null);
      this.mensajeErrorAccion.set('No puedes eliminar tu propia cuenta.');
      return;
    }

    this.accionPendiente.set(null);
    this.mensajeAccion.set('');
    this.mensajeErrorAccion.set('');
    this.accionandoId.set(usuario.id);
    if (accion === 'eliminar') {
      this.eliminandoId.set(usuario.id);
    }
    const peticion: Observable<void> =
      accion === 'bloquear'
        ? this.usuariosService.bloquear(usuario.id).pipe(map(() => undefined))
        : accion === 'desbloquear'
          ? this.usuariosService.desbloquear(usuario.id).pipe(map(() => undefined))
          : accion === 'activar'
            ? this.usuariosService.activar(usuario.id).pipe(map(() => undefined))
            : this.usuariosService.eliminar(usuario.id);

    peticion.pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
      next: () => {
        this.usuarios.update((usuarios) =>
          usuarios.map((actual) =>
            actual.id === usuario.id
              ? accion === 'eliminar'
                ? { ...actual, eliminado: true }
                : accion === 'activar'
                  ? { ...actual, activo: true }
                  : { ...actual, bloqueado: accion === 'bloquear' }
              : actual,
          ),
        );
        const ultimaPagina = Math.max(0, this.totalPaginas() - 1);
        if (this.paginaActual() > ultimaPagina) {
          this.paginaActual.set(ultimaPagina);
        }
        this.mensajeAccion.set(this.textoAccionExitosa(usuario, accion));
        this.limpiarTemporizadorMensaje();
        this.temporizadorMensaje = setTimeout(() => this.mensajeAccion.set(''), 5000);
        this.eliminandoId.set(null);
        this.accionandoId.set(null);
      },
      error: (error: HttpErrorResponse) => {
        this.mensajeErrorAccion.set(this.textoErrorAccion(error, accion));
        this.eliminandoId.set(null);
        this.accionandoId.set(null);
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

  private cargarDetalleUsuario(usuario: Usuario): void {
    this.cargandoDetalle.set(true);
    this.errorDetalle.set('');
    this.usuariosService
      .consultarPorId(usuario.id)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (detalle) => {
          this.usuarioDetalle.set(detalle);
          this.cargandoDetalle.set(false);
        },
        error: (error: HttpErrorResponse) => {
          this.errorDetalle.set(
            error.status === 404
              ? 'No se han podido encontrar los datos de este usuario.'
              : this.textoDeError(error),
          );
          this.cargandoDetalle.set(false);
        },
      });
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

  private textoErrorAccion(error: HttpErrorResponse, accion: AccionUsuario): string {
    if (accion === 'eliminar') {
      return this.textoErrorEliminacion(error);
    }
    if (error.status === 401) {
      return 'Tu sesión ha caducado. Inicia sesión de nuevo.';
    }
    if (error.status === 403) {
      return this.mensajeApi(error, 'No tienes permiso para cambiar el estado de este usuario.');
    }
    if (error.status === 404) {
      return 'El usuario ya no está disponible para cambiar su estado.';
    }
    if (error.status === 0) {
      return 'No se ha podido conectar con el servidor.';
    }
    return this.mensajeApi(
      error,
      accion === 'bloquear'
        ? 'No se ha podido bloquear el usuario. Inténtalo de nuevo.'
        : accion === 'activar'
          ? 'No se ha podido activar el usuario. Inténtalo de nuevo.'
          : 'No se ha podido desbloquear el usuario. Inténtalo de nuevo.',
    );
  }

  private textoAccionExitosa(usuario: Usuario, accion: AccionUsuario): string {
    const nombre = `${usuario.name} ${usuario.apellidos}`;
    if (accion === 'eliminar') {
      return `Se ha eliminado a ${nombre}.`;
    }
    if (accion === 'activar') {
      return `Se ha activado a ${nombre}.`;
    }
    return accion === 'bloquear'
      ? `Se ha bloqueado a ${nombre}.`
      : `Se ha desbloqueado a ${nombre}.`;
  }

  private hayAccionEnCurso(): boolean {
    return this.accionandoId() !== null;
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
