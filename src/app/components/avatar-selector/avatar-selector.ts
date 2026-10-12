import { ChangeDetectionStrategy, Component, input, output, signal } from '@angular/core';

export const AVATARES_PERFIL = [
  { nombre: 'Coral', ruta: '/assets/avatares/avatar-01.svg' },
  { nombre: 'Menta', ruta: '/assets/avatares/avatar-02.svg' },
  { nombre: 'Lavanda', ruta: '/assets/avatares/avatar-03.svg' },
  { nombre: 'Sol', ruta: '/assets/avatares/avatar-04.svg' },
  { nombre: 'Cielo', ruta: '/assets/avatares/avatar-05.svg' },
  { nombre: 'Uva', ruta: '/assets/avatares/avatar-06.svg' },
] as const;

const TAMANO_MAXIMO_FOTO = 2 * 1024 * 1024;

export function avatarAleatorio(): string {
  const indice = Math.floor(Math.random() * AVATARES_PERFIL.length);
  return AVATARES_PERFIL[indice].ruta;
}

@Component({
  selector: 'app-avatar-selector',
  imports: [],
  templateUrl: './avatar-selector.html',
  styleUrl: './avatar-selector.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class AvatarSelector {
  readonly value = input('');
  readonly valueChange = output<string>();
  readonly avatares = AVATARES_PERFIL;
  readonly error = signal('');

  seleccionarAvatar(ruta: string): void {
    this.error.set('');
    this.valueChange.emit(ruta);
  }

  cargarFoto(event: Event): void {
    const inputArchivo = event.target as HTMLInputElement;
    const archivo = inputArchivo.files?.[0];
    inputArchivo.value = '';

    if (!archivo) {
      return;
    }
    if (!['image/jpeg', 'image/png', 'image/webp'].includes(archivo.type)) {
      this.error.set('Elige una imagen JPG, PNG o WebP.');
      return;
    }
    if (archivo.size > TAMANO_MAXIMO_FOTO) {
      this.error.set('La imagen no puede superar los 2 MB.');
      return;
    }

    const lector = new FileReader();
    lector.onload = () => {
      if (typeof lector.result !== 'string') {
        this.error.set('No se ha podido leer la imagen. Prueba con otro archivo.');
        return;
      }
      this.error.set('');
      this.valueChange.emit(lector.result);
    };
    lector.onerror = () => {
      this.error.set('No se ha podido leer la imagen. Prueba con otro archivo.');
    };
    lector.readAsDataURL(archivo);
  }
}
