package gt.com.chn.prestamos.prestamo.web;

import gt.com.chn.prestamos.cliente.domain.Cliente;
import gt.com.chn.prestamos.cliente.repository.ClienteRepository;
import gt.com.chn.prestamos.common.error.ApiExceptions;
import gt.com.chn.prestamos.prestamo.domain.*;
import gt.com.chn.prestamos.prestamo.repository.*;
import gt.com.chn.prestamos.security.auth.UsuarioActual;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class SolicitudService {

    private final SolicitudPrestamoRepository solicitudRepository;
    private final ClienteRepository clienteRepository;
    private final EstadoSolicitudRepository estadoSolicitudRepository;
    private final ResolucionSolicitudRepository resolucionRepository;
    private final HistorialEstadoSolicitudRepository historialRepository;
    private final PrestamoService prestamoService;
    private final UsuarioActual usuarioActual;

    public SolicitudService(SolicitudPrestamoRepository solicitudRepository,
                            ClienteRepository clienteRepository,
                            EstadoSolicitudRepository estadoSolicitudRepository,
                            ResolucionSolicitudRepository resolucionRepository,
                            HistorialEstadoSolicitudRepository historialRepository,
                            PrestamoService prestamoService,
                            UsuarioActual usuarioActual) {
        this.solicitudRepository = solicitudRepository;
        this.clienteRepository = clienteRepository;
        this.estadoSolicitudRepository = estadoSolicitudRepository;
        this.resolucionRepository = resolucionRepository;
        this.historialRepository = historialRepository;
        this.prestamoService = prestamoService;
        this.usuarioActual = usuarioActual;
    }

    @Transactional
    public SolicitudDtos.SolicitudResponse crear(SolicitudDtos.SolicitudRequest req) {
        usuarioActual.verificarAccesoCliente(req.clienteId());
        Cliente cliente = clienteRepository.findByIdAndActivoTrue(req.clienteId())
                .orElseThrow(() -> new ApiExceptions.NotFoundException(
                        "Cliente no encontrado: " + req.clienteId()));
        EstadoSolicitud enProceso = estado(EstadoSolicitud.EN_PROCESO);

        SolicitudPrestamo s = new SolicitudPrestamo();
        s.setCliente(cliente);
        s.setUsuarioRegistraId(usuarioActual.get().usuarioId());
        s.setEstado(enProceso);
        s.setMontoSolicitado(req.montoSolicitado());
        s.setPlazoMeses(req.plazoMeses());
        s.setProposito(req.proposito());
        s.setDetalles(req.detalles());
        solicitudRepository.save(s);

        registrarHistorial(s.getId(), null, enProceso.getId());
        return SolicitudDtos.SolicitudResponse.de(s, null);
    }

    @Transactional(readOnly = true)
    public Page<SolicitudDtos.SolicitudResponse> listarPorCliente(Integer clienteId, Pageable pageable) {
        usuarioActual.verificarAccesoCliente(clienteId);
        Page<SolicitudPrestamo> pagina = solicitudRepository.findActivasPorCliente(clienteId, pageable);
        // Motivos de resolución en bloque (evita N+1) para mostrar la razón de aprobación/rechazo.
        List<Integer> ids = pagina.getContent().stream().map(SolicitudPrestamo::getId).toList();
        Map<Integer, String> motivos = new HashMap<>();
        for (ResolucionSolicitud r : resolucionRepository.findBySolicitudIdIn(ids)) {
            if (r.getMotivo() != null) motivos.put(r.getSolicitudId(), r.getMotivo());
        }
        return pagina.map(s -> SolicitudDtos.SolicitudResponse.de(s, motivos.get(s.getId())));
    }

    @Transactional
    public SolicitudDtos.SolicitudResponse aprobar(Integer solicitudId, String motivo) {
        SolicitudPrestamo s = resolver(solicitudId, EstadoSolicitud.APROBADO, motivo);
        // La materialización del préstamo se delega a PrestamoService (SRP); corre en esta misma
        // transacción, así que aprobar + crear préstamo es atómico.
        prestamoService.crearDesdeSolicitud(s);
        return SolicitudDtos.SolicitudResponse.de(s, motivo);
    }

    @Transactional
    public SolicitudDtos.SolicitudResponse rechazar(Integer solicitudId, String motivo) {
        SolicitudPrestamo s = resolver(solicitudId, EstadoSolicitud.RECHAZADO, motivo);
        return SolicitudDtos.SolicitudResponse.de(s, motivo);
    }

    private SolicitudPrestamo resolver(Integer solicitudId, String codigoEstadoNuevo, String motivo) {
        SolicitudPrestamo s = solicitudRepository.findByIdAndActivoTrue(solicitudId)
                .orElseThrow(() -> new ApiExceptions.NotFoundException(
                        "Solicitud no encontrada: " + solicitudId));

        // Única transición válida: EN_PROCESO -> {APROBADO|RECHAZADO}. Un doble clic o un reintento
        // de red sobre una solicitud ya resuelta cae aquí y se corta con 409 (idempotencia defensiva).
        if (!s.estaEnProceso()) {
            throw new ApiExceptions.ConflictException(
                    "La solicitud ya fue resuelta (estado: " + s.getEstado().getCodigo() + ")");
        }

        Integer estadoAnteriorId = s.getEstado().getId();
        EstadoSolicitud nuevo = estado(codigoEstadoNuevo);
        s.setEstado(nuevo);

        Integer usuarioId = usuarioActual.get().usuarioId();

        // Resolución = registro inmutable de la decisión; historial = traza de la transición.
        // Juntos responden "quién resolvió, cuándo y de qué estado a cuál" para auditoría.
        ResolucionSolicitud resolucion = new ResolucionSolicitud();
        resolucion.setSolicitudId(s.getId());
        resolucion.setUsuarioResuelveId(usuarioId);
        resolucion.setEstadoResultanteId(nuevo.getId());
        resolucion.setMotivo(motivo);
        resolucionRepository.save(resolucion);

        registrarHistorial(s.getId(), estadoAnteriorId, nuevo.getId());
        return s;
    }

    private void registrarHistorial(Integer solicitudId, Integer estadoAnteriorId, Integer estadoNuevoId) {
        HistorialEstadoSolicitud h = new HistorialEstadoSolicitud();
        h.setSolicitudId(solicitudId);
        h.setEstadoAnteriorId(estadoAnteriorId);
        h.setEstadoNuevoId(estadoNuevoId);
        h.setUsuarioId(usuarioActual.get().usuarioId());
        h.setFechaCambio(Instant.now());
        historialRepository.save(h);
    }

    private EstadoSolicitud estado(String codigo) {
        return estadoSolicitudRepository.findByCodigo(codigo)
                .orElseThrow(() -> new IllegalStateException("Estado no configurado: " + codigo));
    }
}
