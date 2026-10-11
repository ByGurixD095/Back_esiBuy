import { RenderMode, ServerRoute } from '@angular/ssr';

export const serverRoutes: ServerRoute[] = [
  {
    path: 'admin/usuarios/:id/editar',
    renderMode: RenderMode.Client
  },
  {
    path: 'products/:id/editar',
    renderMode: RenderMode.Client
  },
  {
    path: 'products/:id',
    renderMode: RenderMode.Client
  },
  {
    path: '**',
    renderMode: RenderMode.Prerender
  }
];
