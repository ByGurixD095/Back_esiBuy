import { Routes } from '@angular/router';
import { AdministradorRegistro } from './components/administrador-registro/administrador-registro';
import { InicioSesion } from './components/inicio-sesion/inicio-sesion';
import { RecoverPasswordComponent } from './components/recover-password-component/recover-password-component';
import { ResetPasswordComponent } from './components/reset-password-component/reset-password-component';

export const routes: Routes = [
  { path: '', pathMatch: 'full', redirectTo: 'products' },
  { path: 'products', loadComponent: () => import('./catalogo/catalogo').then((module) => module.Catalogo) },
  { path: 'products/new', loadComponent: () => import('./producto-form/producto-form').then((module) => module.ProductoForm) },
  { path: 'register', children: [] },
  { path: 'cart', children: [] },
  { path: 'administradores/nuevo', component: AdministradorRegistro },
  { path: 'login', component: InicioSesion },
  { path: 'recover-password', component: RecoverPasswordComponent },
  { path: 'reset-password', component: ResetPasswordComponent },
];