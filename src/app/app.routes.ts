import { Routes } from '@angular/router';
import { AdministradorRegistro } from './components/administrador-registro/administrador-registro';
import { Catalogo } from './components/catalogo/catalogo';
import { CatalogoVendedor } from './components/catalogo-vendedor/catalogo-vendedor';
import { InicioSesion } from './components/inicio-sesion/inicio-sesion';
import { RecoverPasswordComponent } from './components/recover-password-component/recover-password-component';
import { ResetPasswordComponent } from './components/reset-password-component/reset-password-component';
import { ProductoForm } from './components/producto-form/producto-form';
import { RegistroComponent } from './components/registro/registro';
import { RegistroInicio } from './components/registro/registro-inicio';
import { roleGuard } from './guards/role.guard';
import { Inicio } from './components/inicio/inicio';
import { Cuenta } from './components/cuenta/cuenta';
import { Gestionar } from './components/gestionar/gestionar';
import { ConsultarUsuarios } from './components/consultar-usuarios/consultar-usuarios';
import { AdminEditarUsuario } from './components/admin-editar-usuario/admin-editar-usuario';

export const routes: Routes = [
  { path: '', pathMatch: 'full', component: Inicio },
  { path: 'products', component: Catalogo },
  { path: 'products/catalogo', component: CatalogoVendedor, canActivate: [roleGuard(['VENDEDOR'])] },
  { path: 'products/new', component: ProductoForm, canActivate: [roleGuard(['VENDEDOR'])] },
  { path: 'register', component: RegistroInicio },
  { path: 'register/clientes', component: RegistroComponent, data: { tipoRegistro: 'cliente' } },
  { path: 'register/vendedor', component: RegistroComponent, data: { tipoRegistro: 'vendedor' } },
  { path: 'cart', children: [] },
  { path: 'account', component: Cuenta, canActivate: [roleGuard(['ADMINISTRADOR', 'VENDEDOR', 'CLIENTE'])] },
  {
    path: 'manage',
    component: Gestionar,
    canActivate: [roleGuard(['ADMINISTRADOR', 'VENDEDOR'])],
  },
  {
    path: 'administradores/nuevo',
    component: AdministradorRegistro,
    canActivate: [roleGuard(['ADMINISTRADOR'])],
  },
  {
    path: 'admin/usuarios',
    component: ConsultarUsuarios,
    canActivate: [roleGuard(['ADMINISTRADOR'])],
  },
  {
    path: 'admin/usuarios/:id/editar',
    component: AdminEditarUsuario,
    canActivate: [roleGuard(['ADMINISTRADOR'])],
  },
  { path: 'login', component: InicioSesion },
  { path: 'recover-password', component: RecoverPasswordComponent },
  { path: 'reset-password', component: ResetPasswordComponent },
];
