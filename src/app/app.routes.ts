import { Routes } from '@angular/router';
import { AdministradorRegistro } from './components/administrador-registro/administrador-registro';
import { InicioSesion } from './components/inicio-sesion/inicio-sesion';

export const routes: Routes = [
  { path: 'administradores/nuevo', component: AdministradorRegistro },
  { path: 'login', component: InicioSesion },
];