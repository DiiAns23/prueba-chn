package gt.com.chn.prestamos.security.config;

import gt.com.chn.prestamos.cliente.domain.Cliente;
import gt.com.chn.prestamos.cliente.repository.ClienteRepository;
import gt.com.chn.prestamos.security.domain.Rol;
import gt.com.chn.prestamos.security.domain.Usuario;
import gt.com.chn.prestamos.security.repository.RolRepository;
import gt.com.chn.prestamos.security.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.util.Set;

/**
 * Crea los usuarios por defecto al arrancar, cifrando la contraseña con BCrypt.
 * Los roles y permisos provienen de las migraciones Flyway.
 *
 * <p>admin y operador siempre se crean. El usuario de autoservicio (rol CLIENTE) solo se
 * siembra si se define {@code CLIENTE_PASSWORD}, porque va atado a un registro de cliente:
 * se crea un cliente demo y se enlaza. Útil para tener login de cliente reproducible.
 */
@Component
public class DataInitializer implements ApplicationRunner {

    /** Identificación del cliente demo (estable, para que el seed sea idempotente). */
    private static final String CLIENTE_DEMO_IDENTIFICACION = "2999000000101";

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final ClienteRepository clienteRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.bootstrap.admin-username}")
    private String adminUsername;
    @Value("${app.bootstrap.admin-password}")
    private String adminPassword;
    @Value("${app.bootstrap.operador-username}")
    private String operadorUsername;
    @Value("${app.bootstrap.operador-password}")
    private String operadorPassword;
    @Value("${app.bootstrap.cliente-username}")
    private String clienteUsername;
    @Value("${app.bootstrap.cliente-password:}")
    private String clientePassword;

    public DataInitializer(UsuarioRepository usuarioRepository, RolRepository rolRepository,
                           ClienteRepository clienteRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.rolRepository = rolRepository;
        this.clienteRepository = clienteRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(ApplicationArguments args) {
        crearUsuario(adminUsername, adminPassword, "ADMINISTRADOR", null);
        crearUsuario(operadorUsername, operadorPassword, "OPERADOR", null);
        sembrarClienteAutoservicio();
    }

    /** Cliente de autoservicio: opcional (gated por CLIENTE_PASSWORD) y enlazado a un cliente demo. */
    private void sembrarClienteAutoservicio() {
        if (!StringUtils.hasText(clientePassword) || usuarioRepository.existsByNombreUsuario(clienteUsername)) {
            return;
        }
        Cliente cliente = clienteRepository
                .findByNumeroIdentificacionAndActivoTrue(CLIENTE_DEMO_IDENTIFICACION)
                .orElseGet(this::crearClienteDemo);
        crearUsuario(clienteUsername, clientePassword, "CLIENTE", cliente.getId());
    }

    private Cliente crearClienteDemo() {
        Cliente c = new Cliente();
        c.setNombre("Cliente");
        c.setApellido("Demo");
        c.setNumeroIdentificacion(CLIENTE_DEMO_IDENTIFICACION);
        c.setFechaNacimiento(LocalDate.of(1990, 1, 1));
        c.setCorreoElectronico("cliente.demo@example.com");
        c.setTelefono("00000000");
        return clienteRepository.save(c);
    }

    private void crearUsuario(String nombreUsuario, String contrasena, String codigoRol, Integer clienteId) {
        if (usuarioRepository.existsByNombreUsuario(nombreUsuario)) {
            return;
        }
        Rol rol = rolRepository.findByCodigo(codigoRol)
                .orElseThrow(() -> new IllegalStateException("Rol no encontrado: " + codigoRol));

        Usuario usuario = new Usuario();
        usuario.setNombreUsuario(nombreUsuario);
        usuario.setHashContrasena(passwordEncoder.encode(contrasena));
        usuario.setClienteId(clienteId);
        usuario.setRoles(Set.of(rol));
        usuarioRepository.save(usuario);
    }
}
