package gt.com.chn.prestamos.security.admin;

import gt.com.chn.prestamos.security.auth.RequierePermiso;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin")
public class RolPermisoAdminController {

    private final SeguridadAdminService service;

    public RolPermisoAdminController(SeguridadAdminService service) {
        this.service = service;
    }

    @GetMapping("/roles")
    @RequierePermiso("seguridad.roles")
    public List<SeguridadAdminDtos.RolResponse> listarRoles() {
        return service.listarRoles();
    }

    @PutMapping("/roles/{codigo}/permisos")
    @RequierePermiso("seguridad.permisos")
    public SeguridadAdminDtos.RolResponse setPermisosRol(
            @PathVariable String codigo, @Valid @RequestBody SeguridadAdminDtos.SetPermisosRolRequest req) {
        return service.setPermisosRol(codigo, req.permisos());
    }

    @GetMapping("/permisos")
    @RequierePermiso("seguridad.permisos")
    public List<SeguridadAdminDtos.PermisoResponse> listarPermisos() {
        return service.listarPermisos();
    }
}
