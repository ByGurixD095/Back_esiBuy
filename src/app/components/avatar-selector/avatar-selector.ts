import { HttpClient } from '@angular/common/http';
import {environment} from '../../../environments/environment';
import { ChangeDetectionStrategy, Component, computed, inject, input, output, signal } from '@angular/core';

export const AVATARES_PERFIL = [
  { nombre: 'Coral', ruta: '/assets/avatares/avatar-01.svg' },
  { nombre: 'Menta', ruta: '/assets/avatares/avatar-02.svg' },
  { nombre: 'Lavanda', ruta: '/assets/avatares/avatar-03.svg' },
  { nombre: 'Sol', ruta: '/assets/avatares/avatar-04.svg' },
  { nombre: 'Cielo', ruta: '/assets/avatares/avatar-05.svg' },
  { nombre: 'Uva', ruta: '/assets/avatares/avatar-06.svg' },
] as const;

export const AVATARES_VENDEDOR = [
  { nombre: 'Vendedor oliva', ruta: '/assets/avatares/vendedor-01.svg' },
  { nombre: 'Vendedora coral', ruta: '/assets/avatares/vendedor-02.svg' },
  { nombre: 'Vendedora violeta', ruta: '/assets/avatares/vendedor-03.svg' },
  { nombre: 'Vendedor mostaza', ruta: '/assets/avatares/vendedor-04.svg' },
  { nombre: 'Vendedora azul', ruta: '/assets/avatares/vendedor-05.svg' },
  { nombre: 'Vendedor turquesa', ruta: '/assets/avatares/vendedor-06.svg' },
] as const;

const TAMANO_MAXIMO_FOTO = 2 * 1024 * 1024;

export function avatarAleatorio(esVendedor = false): string {
  const opciones = esVendedor ? AVATARES_VENDEDOR : AVATARES_PERFIL;
  const indice = Math.floor(Math.random() * opciones.length);
  return opciones[indice].ruta;
}

@Component({
  selector: 'app-avatar-selector',
  imports: [],
  templateUrl: './avatar-selector.html',
  styleUrl: './avatar-selector.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class AvatarSelector {
  private readonly http = inject(HttpClient);
  readonly value = input('');
  readonly esVendedor = input(false);
  readonly valueChange = output<string>();
  readonly avatares = computed(() => (this.esVendedor() ? AVATARES_VENDEDOR : AVATARES_PERFIL));
  readonly error = signal('');

  seleccionarAvatar(ruta: string): void {
    this.error.set('');
    this.valueChange.emit(ruta);
  }

  cargarFoto(event: Event): void {
    const inputArchivo = event.target as HTMLInputElement;
    const archivo = inputArchivo.files?.[0];
    inputArchivo.value = '';

    if (!archivo) return;

    if (!['image/jpeg', 'image/png', 'image/webp'].includes(archivo.type)) {
      this.error.set('Elige una imagen JPG, PNG o WebP.');
      return;
    }

    if (archivo.size > TAMANO_MAXIMO_FOTO) {
      this.error.set('La imagen no puede superar los 2 MB.');
      return;
    }

    const url = `https://api.cloudinary.com/v1_1/${environment.cloudinaryCloudName}/image/upload`;

    const formData = new FormData();
    formData.append('file', archivo);
    formData.append('upload_preset', environment.cloudinaryUploadPreset);
    formData.append('folder', environment.carpetaAvatares);

    this.error.set('Subiendo imagen a la nube...');

    this.http.post<{secure_url: string}>(url, formData).subscribe({
      next: (respuesta) => {
        this.error.set('');
        this.valueChange.emit(respuesta.secure_url);
      },
      error: () => {
        this.error.set('Error al subir la imagen. Inténtalo de nuevo.');
      }
    });
  }
}
