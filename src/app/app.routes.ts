import { Routes } from '@angular/router';
import { AdministradorRegistro } from './administrador-registro/administrador-registro';
import { InicioSesion } from './inicio-sesion/inicio-sesion';

export const routes: Routes = [
  { path: '', pathMatch: 'full', children: [] },
  { path: 'administradores/nuevo', component: AdministradorRegistro },
  { path: 'login', component: InicioSesion },
];