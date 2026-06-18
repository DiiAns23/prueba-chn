package gt.com.chn.prestamos.cliente.web;

import gt.com.chn.prestamos.cliente.domain.Cliente;
import gt.com.chn.prestamos.cliente.repository.ClienteRepository;
import gt.com.chn.prestamos.common.error.ApiExceptions;
import gt.com.chn.prestamos.prestamo.repository.SolicitudPrestamoRepository;
import gt.com.chn.prestamos.security.auth.UsuarioActual;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
public class ClienteService {

    private final ClienteRepository clienteRepository;
    private final SolicitudPrestamoRepository solicitudRepository;
    private final UsuarioActual usuarioActual;

    public ClienteService(ClienteRepository clienteRepository,
                          SolicitudPrestamoRepository solicitudRepository,
                          UsuarioActual usuarioActual) {
        this.clienteRepository = clienteRepository;
        this.solicitudRepository = solicitudRepository;
        this.usuarioActual = usuarioActual;
    }

    @Transactional
    public ClienteDtos.ClienteResponse crear(ClienteDtos.ClienteRequest req) {
        if (clienteRepository.existsByNumeroIdentificacionAndActivoTrue(req.numeroIdentificacion())) {
            throw new ApiExceptions.ConflictException(
                    "Ya existe un cliente con identificación " + req.numeroIdentificacion());
        }
        Cliente c = new Cliente();
        aplicar(c, req);
        return ClienteDtos.ClienteResponse.de(clienteRepository.save(c));
    }

    @Transactional(readOnly = true)
    public Page<ClienteDtos.ClienteResponse> listar(Pageable pageable) {
        // Aislamiento (defensa en profundidad además del permiso): un usuario con rol Cliente solo
        // puede verse a sí mismo, nunca el padrón completo. Operador/Administrador ven el listado.
        var u = usuarioActual.get();
        if (u.esCliente()) {
            List<ClienteDtos.ClienteResponse> propio = clienteRepository.findByIdAndActivoTrue(u.clienteId())
                    .map(ClienteDtos.ClienteResponse::de).map(List::of).orElseGet(List::of);
            return new PageImpl<>(propio, pageable, propio.size());
        }
        return clienteRepository.findByActivoTrue(pageable)
                .map(ClienteDtos.ClienteResponse::de);
    }

    @Transactional(readOnly = true)
    public ClienteDtos.ClienteResponse obtener(Integer id) {
        usuarioActual.verificarAccesoCliente(id);
        return ClienteDtos.ClienteResponse.de(buscarActivo(id));
    }

    /** Perfil del cliente autenticado (autoservicio). El id sale del token, no del cliente. */
    @Transactional(readOnly = true)
    public ClienteDtos.ClienteResponse miPerfil() {
        return ClienteDtos.ClienteResponse.de(buscarActivo(clienteIdAutenticado()));
    }

    /**
     * Actualiza SOLO los datos de contacto del propio cliente (correo, dirección, teléfono).
     * Nombre, identificación y fecha de nacimiento no se tocan: son datos no modificables por el cliente.
     */
    @Transactional
    public ClienteDtos.ClienteResponse actualizarContacto(ClienteDtos.ContactoRequest req) {
        Cliente c = buscarActivo(clienteIdAutenticado());
        c.setDireccion(req.direccion());
        c.setCorreoElectronico(req.correoElectronico());
        c.setTelefono(req.telefono());
        c.marcarActualizado();
        return ClienteDtos.ClienteResponse.de(c);
    }

    private Integer clienteIdAutenticado() {
        Integer id = usuarioActual.get().clienteId();
        if (id == null) {
            throw new ApiExceptions.BusinessRuleException("El usuario no está vinculado a un cliente");
        }
        return id;
    }

    /** Búsqueda por número de identificación (para que el operador seleccione el cliente). */
    @Transactional(readOnly = true)
    public ClienteDtos.ClienteResponse buscarPorIdentificacion(String identificacion) {
        Cliente c = clienteRepository.findByNumeroIdentificacionAndActivoTrue(identificacion)
                .orElseThrow(() -> new ApiExceptions.NotFoundException(
                        "Cliente no encontrado con identificación " + identificacion));
        usuarioActual.verificarAccesoCliente(c.getId());
        return ClienteDtos.ClienteResponse.de(c);
    }

    @Transactional
    public ClienteDtos.ClienteResponse actualizar(Integer id, ClienteDtos.ClienteRequest req) {
        usuarioActual.verificarAccesoCliente(id);
        Cliente c = buscarActivo(id);
        // El índice único de identificación es filtrado (WHERE activo = 1). Validamos en código solo
        // si la identificación cambió, para devolver un 409 claro en vez de un DataIntegrityViolation (500).
        if (!c.getNumeroIdentificacion().equals(req.numeroIdentificacion())
                && clienteRepository.existsByNumeroIdentificacionAndActivoTrueAndIdNot(
                        req.numeroIdentificacion(), id)) {
            throw new ApiExceptions.ConflictException(
                    "Ya existe otro cliente con identificación " + req.numeroIdentificacion());
        }
        aplicar(c, req);
        c.marcarActualizado();
        return ClienteDtos.ClienteResponse.de(c);
    }

    /**
     * Soft delete: nunca un DELETE físico (política de auditoría bancaria). Marca inactivo al cliente
     * y, en cascada lógica, a sus solicitudes; préstamos y pagos se conservan como historial financiero.
     */
    @Transactional
    public void eliminar(Integer id) {
        Cliente c = buscarActivo(id);
        Integer usuarioId = usuarioActual.get().usuarioId();
        c.darDeBaja(usuarioId);
        solicitudRepository.darDeBajaPorCliente(id, Instant.now(), usuarioId);
    }

    private Cliente buscarActivo(Integer id) {
        return clienteRepository.findByIdAndActivoTrue(id)
                .orElseThrow(() -> new ApiExceptions.NotFoundException("Cliente no encontrado: " + id));
    }

    private void aplicar(Cliente c, ClienteDtos.ClienteRequest req) {
        c.setNombre(req.nombre());
        c.setApellido(req.apellido());
        c.setNumeroIdentificacion(req.numeroIdentificacion());
        c.setFechaNacimiento(req.fechaNacimiento());
        c.setDireccion(req.direccion());
        c.setCorreoElectronico(req.correoElectronico());
        c.setTelefono(req.telefono());
    }
}
