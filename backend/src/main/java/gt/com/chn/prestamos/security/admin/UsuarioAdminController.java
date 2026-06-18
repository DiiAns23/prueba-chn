package gt.com.chn.prestamos.security.admin;

import gt.com.chn.prestamos.security.auth.RequierePermiso;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/usuarios")
public class UsuarioAdminController {

    private final SeguridadAdminService service;

    public UsuarioAdminController(SeguridadAdminService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @RequierePermiso("seguridad.usuarios")
    public SeguridadAdminDtos.UsuarioResponse crear(
            @Valid @RequestBody SeguridadAdminDtos.CrearUsuarioRequest req) {
        return service.crearUsuario(req);
    }

    @GetMapping
    @RequierePermiso("seguridad.usuarios")
    public Page<SeguridadAdminDtos.UsuarioResponse> listar(
            @PageableDefault(size = 20) Pageable pageable) {
        return service.listarUsuarios(pageable);
    }

    @GetMapping("/{id}")
    @RequierePermiso("seguridad.usuarios")
    public SeguridadAdminDtos.UsuarioResponse obtener(@PathVariable Integer id) {
        return service.obtenerUsuario(id);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @RequierePermiso("seguridad.usuarios")
    public void inhabilitar(@PathVariable Integer id) {
        service.inhabilitarUsuario(id);
    }

    @PutMapping("/{id}/roles")
    @RequierePermiso("seguridad.roles")
    public SeguridadAdminDtos.UsuarioResponse asignarRoles(
            @PathVariable Integer id, @Valid @RequestBody SeguridadAdminDtos.AsignarRolesRequest req) {
        return service.asignarRoles(id, req.roles());
    }

    @PutMapping("/{id}/permisos")
    @RequierePermiso("seguridad.permisos")
    public SeguridadAdminDtos.UsuarioResponse setExcepciones(
            @PathVariable Integer id, @Valid @RequestBody SeguridadAdminDtos.SetExcepcionesRequest req) {
        return service.setExcepciones(id, req.excepciones());
    }
}
