import { Routes } from '@angular/router';
import { AdministradorRegistro } from './administrador-registro/administrador-registro';
import { InicioSesion } from './inicio-sesion/inicio-sesion';

export const routes: Routes = [
  { path: '', pathMatch: 'full', redirectTo: 'products' },
  { path: 'products', loadComponent: () => import('./catalogo/catalogo').then((module) => module.Catalogo) },
  { path: 'products/new', loadComponent: () => import('./producto-form/producto-form').then((module) => module.ProductoForm) },
  { path: 'register', children: [] },
  { path: 'cart', children: [] },
  { path: 'administradores/nuevo', component: AdministradorRegistro },
  { path: 'login', component: InicioSesion },
];