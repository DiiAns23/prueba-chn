import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from './auth.service';

/**
 * Restringe una ruta a quien tenga el permiso indicado. Si no lo tiene (p. ej. un Cliente
 * intentando entrar a /clientes), lo redirige a sus solicitudes.
 */
export function permisoGuard(codigo: string): CanActivateFn {
  return () => {
    const auth = inject(AuthService);
    const router = inject(Router);
    return auth.tienePermiso(codigo) ? true : router.createUrlTree(['/solicitudes']);
  };
}
