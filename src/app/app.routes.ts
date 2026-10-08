import { Routes } from '@angular/router';
import { AdministradorRegistro } from './components/administrador-registro/administrador-registro';
import { Catalogo } from './catalogo/catalogo';
import { InicioSesion } from './components/inicio-sesion/inicio-sesion';
import { RecoverPasswordComponent } from './components/recover-password-component/recover-password-component';
import { ResetPasswordComponent } from './components/reset-password-component/reset-password-component';
import { ProductoForm } from './producto-form/producto-form';

export const routes: Routes = [
  { path: '', pathMatch: 'full', redirectTo: 'products' },
  { path: 'products', component: Catalogo },
  { path: 'products/new', component: ProductoForm },
  { path: 'register', children: [] },
  { path: 'cart', children: [] },
  { path: 'administradores/nuevo', component: AdministradorRegistro },
  { path: 'login', component: InicioSesion },
  { path: 'recover-password', component: RecoverPasswordComponent },
  { path: 'reset-password', component: ResetPasswordComponent },
];