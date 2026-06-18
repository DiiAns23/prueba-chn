package gt.com.chn.prestamos.security.admin;

import gt.com.chn.prestamos.cliente.repository.ClienteRepository;
import gt.com.chn.prestamos.common.error.ApiExceptions;
import gt.com.chn.prestamos.security.domain.Permiso;
import gt.com.chn.prestamos.security.domain.Rol;
import gt.com.chn.prestamos.security.domain.Usuario;
import gt.com.chn.prestamos.security.domain.UsuarioPermiso;
import gt.com.chn.prestamos.security.auth.UsuarioActual;
import gt.com.chn.prestamos.security.repository.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class SeguridadAdminService {

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final PermisoRepository permisoRepository;
    private final UsuarioPermisoRepository usuarioPermisoRepository;
    private final ClienteRepository clienteRepository;
    private final PasswordEncoder passwordEncoder;
    private final UsuarioActual usuarioActual;

    public SeguridadAdminService(UsuarioRepository usuarioRepository, RolRepository rolRepository,
                                 PermisoRepository permisoRepository,
                                 UsuarioPermisoRepository usuarioPermisoRepository,
                                 ClienteRepository clienteRepository, PasswordEncoder passwordEncoder,
                                 UsuarioActual usuarioActual) {
        this.usuarioRepository = usuarioRepository;
        this.rolRepository = rolRepository;
        this.permisoRepository = permisoRepository;
        this.usuarioPermisoRepository = usuarioPermisoRepository;
        this.clienteRepository = clienteRepository;
        this.passwordEncoder = passwordEncoder;
        this.usuarioActual = usuarioActual;
    }

    // ---------- Usuarios (CU-13) ----------

    @Transactional
    public SeguridadAdminDtos.UsuarioResponse crearUsuario(SeguridadAdminDtos.CrearUsuarioRequest req) {
        if (usuarioRepository.existsByNombreUsuario(req.nombreUsuario())) {
            throw new ApiExceptions.ConflictException(
                    "Ya existe el usuario " + req.nombreUsuario());
        }
        if (req.clienteId() != null) {
            clienteRepository.findByIdAndActivoTrue(req.clienteId())
                    .orElseThrow(() -> new ApiExceptions.NotFoundException(
                            "Cliente no encontrado: " + req.clienteId()));
            if (usuarioRepository.existsByClienteId(req.clienteId())) {
                throw new ApiExceptions.ConflictException(
                        "El cliente ya tiene un usuario asociado");
            }
        }
        Usuario u = new Usuario();
        u.setNombreUsuario(req.nombreUsuario());
        u.setHashContrasena(passwordEncoder.encode(req.contrasena()));
        u.setClienteId(req.clienteId());
        u.setRoles(resolverRoles(req.roles()));
        usuarioRepository.save(u);
        return toUsuarioResponse(u);
    }

    @Transactional(readOnly = true)
    public Page<SeguridadAdminDtos.UsuarioResponse> listarUsuarios(Pageable pageable) {
        return usuarioRepository.findByActivoTrue(pageable).map(this::toUsuarioResponse);
    }

    @Transactional(readOnly = true)
    public SeguridadAdminDtos.UsuarioResponse obtenerUsuario(Integer id) {
        return toUsuarioResponse(buscarUsuario(id));
    }

    /** Soft delete del usuario (inhabilitar acceso); no se borra físicamente. */
    @Transactional
    public void inhabilitarUsuario(Integer id) {
        Usuario u = buscarUsuario(id);
        u.darDeBaja(usuarioActual.get().usuarioId());
    }

    // ---------- Roles de un usuario (CU-14) ----------

    @Transactional
    public SeguridadAdminDtos.UsuarioResponse asignarRoles(Integer id, List<String> roles) {
        Usuario u = buscarUsuario(id);
        u.setRoles(resolverRoles(roles));
        return toUsuarioResponse(u);
    }

    // ---------- Excepciones de permiso por usuario (CU-16) ----------

    @Transactional
    public SeguridadAdminDtos.UsuarioResponse setExcepciones(
            Integer id, List<SeguridadAdminDtos.ExcepcionPermiso> excepciones) {
        Usuario u = buscarUsuario(id);
        // Reemplazo total del set de excepciones. El flush fuerza el DELETE antes de los INSERT;
        // sin él, Hibernate podría reordenar y chocar con la PK (usuario_id, permiso_id) al re-otorgar.
        usuarioPermisoRepository.deleteByUsuarioId(id);
        usuarioPermisoRepository.flush();
        if (excepciones != null) {
            for (SeguridadAdminDtos.ExcepcionPermiso e : excepciones) {
                Permiso p = permisoRepository.findByCodigo(e.permiso())
                        .orElseThrow(() -> new ApiExceptions.BusinessRuleException(
                                "Permiso inválido: " + e.permiso()));
                UsuarioPermiso up = new UsuarioPermiso();
                up.setUsuarioId(u.getId());
                up.setPermisoId(p.getId());
                up.setGranted(e.granted());
                usuarioPermisoRepository.save(up);
            }
        }
        return toUsuarioResponse(u);
    }

    // ---------- Matriz rol-permiso (CU-15) ----------

    @Transactional(readOnly = true)
    public List<SeguridadAdminDtos.RolResponse> listarRoles() {
        return rolRepository.findAll().stream().map(this::toRolResponse).toList();
    }

    @Transactional
    public SeguridadAdminDtos.RolResponse setPermisosRol(String codigoRol, List<String> permisos) {
        Rol rol = rolRepository.findByCodigo(codigoRol)
                .orElseThrow(() -> new ApiExceptions.NotFoundException("Rol no encontrado: " + codigoRol));
        rol.setPermisos(new HashSet<>(resolverPermisos(permisos)));
        return toRolResponse(rol);
    }

    @Transactional(readOnly = true)
    public List<SeguridadAdminDtos.PermisoResponse> listarPermisos() {
        return permisoRepository.findAll().stream()
                .map(p -> new SeguridadAdminDtos.PermisoResponse(
                        p.getCodigo(), p.getModulo().getCodigo(), p.getAccion(), p.getDescripcion()))
                .toList();
    }

    // ---------- Helpers ----------

    private Usuario buscarUsuario(Integer id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new ApiExceptions.NotFoundException("Usuario no encontrado: " + id));
    }

    private Set<Rol> resolverRoles(List<String> codigos) {
        if (codigos == null || codigos.isEmpty()) {
            throw new ApiExceptions.BusinessRuleException("Debe indicar al menos un rol");
        }
        Set<String> pedidos = new HashSet<>(codigos);
        List<Rol> encontrados = rolRepository.findByCodigoIn(pedidos);
        if (encontrados.size() != pedidos.size()) {
            throw new ApiExceptions.BusinessRuleException("Uno o más roles son inválidos: " + pedidos);
        }
        return new HashSet<>(encontrados);
    }

    private List<Permiso> resolverPermisos(List<String> codigos) {
        if (codigos == null) {
            throw new ApiExceptions.BusinessRuleException("La lista de permisos es obligatoria");
        }
        Set<String> pedidos = new HashSet<>(codigos);
        List<Permiso> encontrados = permisoRepository.findByCodigoIn(pedidos);
        if (encontrados.size() != pedidos.size()) {
            throw new ApiExceptions.BusinessRuleException("Uno o más permisos son inválidos: " + pedidos);
        }
        return encontrados;
    }

    private SeguridadAdminDtos.UsuarioResponse toUsuarioResponse(Usuario u) {
        Set<String> roles = u.getRoles().stream().map(Rol::getCodigo).collect(Collectors.toSet());
        Set<String> permisos = new HashSet<>(usuarioRepository.findPermisosEfectivos(u.getId()));
        return new SeguridadAdminDtos.UsuarioResponse(
                u.getId(), u.getNombreUsuario(), u.getClienteId(), u.isActivo(), roles, permisos);
    }

    private SeguridadAdminDtos.RolResponse toRolResponse(Rol rol) {
        Set<String> permisos = rol.getPermisos().stream().map(Permiso::getCodigo).collect(Collectors.toSet());
        return new SeguridadAdminDtos.RolResponse(
                rol.getCodigo(), rol.getDescripcion(), rol.isActivo(), permisos);
    }
}
