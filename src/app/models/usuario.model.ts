import type { Rol } from '../services/auth.service';

export interface Usuario {
  id: string;
  name: string;
  apellidos: string;
  email: string;
  rol: Rol;
  activo: boolean;
  bloqueado: boolean;
  eliminado: boolean;
}
