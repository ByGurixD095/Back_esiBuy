import { Routes } from '@angular/router';
import { AdministradorRegistro } from './components/administrador-registro/administrador-registro';
import { Catalogo } from './components/catalogo/catalogo';
import { InicioSesion } from './components/inicio-sesion/inicio-sesion';
import { RecoverPasswordComponent } from './components/recover-password-component/recover-password-component';
import { ResetPasswordComponent } from './components/reset-password-component/reset-password-component';
import { ProductoForm } from './components/producto-form/producto-form';
import { roleGuard } from './guards/role.guard';

export const routes: Routes = [
  { path: '', pathMatch: 'full', redirectTo: 'products' },
  { path: 'products', component: Catalogo, canActivate: [roleGuard(['VENDEDOR'])] },
  { path: 'products/new', component: ProductoForm, canActivate: [roleGuard(['VENDEDOR'])] },
  { path: 'register', children: [] },
  { path: 'cart', children: [] },
  {
    path: 'administradores/nuevo',
    component: AdministradorRegistro,
    canActivate: [roleGuard(['ADMINISTRADOR'])],
  },
  { path: 'login', component: InicioSesion },
  { path: 'recover-password', component: RecoverPasswordComponent },
  { path: 'reset-password', component: ResetPasswordComponent },
];