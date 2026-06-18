package gt.com.chn.prestamos.security.auth;

import java.util.Set;

/**
 * Principal autenticado, expuesto en el SecurityContext.
 *
 * @param usuarioId id del usuario
 * @param nombreUsuario nombre de inicio de sesión
 * @param clienteId id del cliente asociado (null si es operador/administrador)
 * @param permisos permisos efectivos (códigos modulo.accion)
 */
public record UsuarioAutenticado(Integer usuarioId,
                                 String nombreUsuario,
                                 Integer clienteId,
                                 Set<String> permisos) {

    public boolean esCliente() {
        return clienteId != null;
    }

    public boolean tienePermiso(String codigo) {
        return permisos.contains(codigo);
    }
}
