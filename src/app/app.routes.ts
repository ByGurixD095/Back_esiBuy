import { Routes } from '@angular/router';
import { AdministradorRegistro } from './components/administrador-registro/administrador-registro';
import { Catalogo } from './components/catalogo/catalogo';
import { InicioSesion } from './components/inicio-sesion/inicio-sesion';
import { RecoverPasswordComponent } from './components/recover-password-component/recover-password-component';
import { ResetPasswordComponent } from './components/reset-password-component/reset-password-component';
import { ProductoForm } from './components/producto-form/producto-form';
import { RegistroComponent } from './components/registro/registro';
import { RegistroInicio } from './components/registro/registro-inicio';

export const routes: Routes = [
  { path: '', pathMatch: 'full', redirectTo: 'products' },
  { path: 'products', component: Catalogo },
  { path: 'products/new', component: ProductoForm },
  { path: 'register', component: RegistroInicio },
  { path: 'register/clientes', component: RegistroComponent, data: { tipoRegistro: 'cliente' } },
  { path: 'register/vendedor', component: RegistroComponent, data: { tipoRegistro: 'vendedor' } },
  { path: 'cart', children: [] },
  { path: 'administradores/nuevo', component: AdministradorRegistro },
  { path: 'login', component: InicioSesion },
  { path: 'recover-password', component: RecoverPasswordComponent },
  { path: 'reset-password', component: ResetPasswordComponent },
];