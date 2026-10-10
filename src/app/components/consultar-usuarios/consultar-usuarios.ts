import { HttpErrorResponse } from '@angular/common/http';
import {
  ChangeDetectionStrategy,
  Component,
  OnInit,
  computed,
  inject,
  signal,
} from '@angular/core';
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

  readonly usuarios = signal<Usuario[]>([]);
  readonly busquedaId = signal('');
  readonly filtroEliminados = signal<FiltroEliminados>('todos');
  readonly cargando = signal(true);
  readonly mensajeError = signal('');
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
}
