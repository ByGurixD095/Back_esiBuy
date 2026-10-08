import { Routes } from '@angular/router';
import { AdministradorRegistro } from './administrador-registro/administrador-registro';
import { Catalogo } from './catalogo/catalogo';
import { InicioSesion } from './inicio-sesion/inicio-sesion';
import { ProductoForm } from './producto-form/producto-form';

export const routes: Routes = [
  { path: '', pathMatch: 'full', redirectTo: 'products' },
  { path: 'products', component: Catalogo },
  { path: 'products/new', component: ProductoForm },
  { path: 'register', children: [] },
  { path: 'cart', children: [] },
  { path: 'administradores/nuevo', component: AdministradorRegistro },
  { path: 'login', component: InicioSesion },
];