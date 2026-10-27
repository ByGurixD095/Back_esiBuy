import { HttpErrorResponse } from '@angular/common/http';
import { ChangeDetectionStrategy, Component, OnInit, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { Categoria, Categorias } from '../../services/categorias.service';
import { MarcoPagina } from '../marco-pagina/marco-pagina';

/** HU 31: listado de categorías del catálogo para el administrador. */
@Component({
  selector: 'app-listar-categorias',
  imports: [RouterLink, MarcoPagina],
  templateUrl: './listar-categorias.html',
  styleUrl: './listar-categorias.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ListarCategorias implements OnInit {
  private readonly categoriasService = inject(Categorias);

  readonly categorias = signal<Categoria[]>([]);
  readonly cargando = signal(true);
  readonly error = signal('');

  ngOnInit(): void {
    this.cargar();
  }

  cargar(): void {
    this.cargando.set(true);
    this.error.set('');
    this.categoriasService.listar().subscribe({
      next: (categorias) => {
        this.categorias.set(categorias);
        this.cargando.set(false);
      },
      error: (error: HttpErrorResponse) => {
        this.error.set(this.textoDeError(error));
        this.cargando.set(false);
      },
    });
  }

  private textoDeError(error: HttpErrorResponse): string {
    if (error.status === 401 || error.status === 403) {
      return 'No tienes permisos para ver las categorías. Inicia sesión como administrador.';
    }
    if (error.status === 0) {
      return 'No se ha podido conectar con el servidor.';
    }
    return 'No se han podido cargar las categorías. Inténtalo de nuevo.';
  }
}
