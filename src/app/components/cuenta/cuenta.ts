import { ChangeDetectionStrategy, Component, inject } from '@angular/core';
import { Router } from '@angular/router';
import { MarcoPagina } from '../marco-pagina/marco-pagina';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-cuenta',
  imports: [MarcoPagina],
  templateUrl: './cuenta.html',
  styleUrl: './cuenta.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class Cuenta {
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);

  cerrarSesion(): void {
    this.auth.logout().subscribe({
      next: () => void this.router.navigate(['/']),
      error: () => void this.router.navigate(['/']),
    });
  }
}
