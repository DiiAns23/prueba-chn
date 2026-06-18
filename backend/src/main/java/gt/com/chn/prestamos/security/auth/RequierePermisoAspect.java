package gt.com.chn.prestamos.security.auth;

import gt.com.chn.prestamos.common.error.ApiExceptions;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

/**
 * Aplica {@link RequierePermiso}: verifica que el usuario autenticado tenga el permiso
 * efectivo requerido antes de ejecutar el método. Equivalente al Guard de autorización.
 */
@Aspect
@Component
public class RequierePermisoAspect {

    private final UsuarioActual usuarioActual;

    public RequierePermisoAspect(UsuarioActual usuarioActual) {
        this.usuarioActual = usuarioActual;
    }

    // @Before: corre antes del método anotado. Si falta el permiso lanza Forbidden (-> 403) y el
    // método nunca se ejecuta. Centralizar esto evita repetir el chequeo en cada endpoint (DRY).
    @Before("@annotation(requierePermiso)")
    public void verificar(RequierePermiso requierePermiso) {
        UsuarioAutenticado u = usuarioActual.get();
        if (!u.tienePermiso(requierePermiso.value())) {
            throw new ApiExceptions.ForbiddenException(
                    "Permiso requerido: " + requierePermiso.value());
        }
    }
}
