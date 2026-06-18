package gt.com.chn.prestamos.security.admin;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.Set;

public final class SeguridadAdminDtos {

    private SeguridadAdminDtos() {
    }

    // ---- Usuarios (CU-13) ----
    public record CrearUsuarioRequest(
            @NotBlank String nombreUsuario,
            @NotBlank String contrasena,
            Integer clienteId,                 // opcional: vincula a un cliente (autoservicio)
            @NotEmpty List<String> roles) {     // códigos de rol: OPERADOR|CLIENTE|ADMINISTRADOR
    }

    public record UsuarioResponse(
            Integer id,
            String nombreUsuario,
            Integer clienteId,
            boolean activo,
            Set<String> roles,
            Set<String> permisosEfectivos) {
    }

    // ---- Roles de un usuario (CU-14) ----
    public record AsignarRolesRequest(
            @NotEmpty List<String> roles) {
    }

    // ---- Excepciones de permiso por usuario (CU-16) ----
    public record ExcepcionPermiso(
            @NotBlank String permiso,
            @NotNull Boolean granted) {         // true = otorga, false = revoca
    }

    public record SetExcepcionesRequest(
            @NotNull List<ExcepcionPermiso> excepciones) {  // [] para limpiar; null -> 400
    }

    // ---- Matriz rol-permiso (CU-15) ----
    public record RolResponse(
            String codigo,
            String descripcion,
            boolean activo,
            Set<String> permisos) {
    }

    public record SetPermisosRolRequest(
            @NotNull List<String> permisos) {
    }

    public record PermisoResponse(
            String codigo,
            String modulo,
            String accion,
            String descripcion) {
    }
}
