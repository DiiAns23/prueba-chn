package gt.com.chn.prestamos.security.auth;

import gt.com.chn.prestamos.common.error.ApiExceptions;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * Acceso al usuario autenticado del contexto de seguridad.
 */
@Component
public class UsuarioActual {

    public UsuarioAutenticado get() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof UsuarioAutenticado u)) {
            throw new ApiExceptions.ForbiddenException("No hay usuario autenticado");
        }
        return u;
    }

    /**
     * Aislamiento horizontal: un usuario con rol Cliente solo puede operar sobre su propio id.
     * Operador/Administrador (sin clienteId) no se restringen aquí; su alcance lo define el permiso.
     * Complementa al control por permiso: este evita el IDOR (acceder a datos de otro cliente).
     */
    public void verificarAccesoCliente(Integer clienteId) {
        UsuarioAutenticado u = get();
        if (u.esCliente() && !u.clienteId().equals(clienteId)) {
            throw new ApiExceptions.ForbiddenException("No puede acceder a datos de otro cliente");
        }
    }
}
