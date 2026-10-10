import { ChangeDetectionStrategy, Component, inject } from '@angular/core';
import { RouterLink } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import { MarcoPagina } from '../marco-pagina/marco-pagina';

@Component({
  selector: 'app-gestionar',
  imports: [RouterLink, MarcoPagina],
  templateUrl: './gestionar.html',
  styleUrl: './gestionar.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class Gestionar {
  readonly auth = inject(AuthService);
}
