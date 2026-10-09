import { ChangeDetectionStrategy, Component } from '@angular/core';
import { RouterLink } from '@angular/router';
import { MarcoPagina } from '../marco-pagina/marco-pagina';

@Component({
  selector: 'app-inicio',
  imports: [RouterLink, MarcoPagina],
  templateUrl: './inicio.html',
  styleUrl: './inicio.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class Inicio {}
