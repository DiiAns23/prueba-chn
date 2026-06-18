package gt.com.chn.prestamos.prestamo.web;

import gt.com.chn.prestamos.common.error.ApiExceptions;
import gt.com.chn.prestamos.pago.repository.PagoRepository;
import gt.com.chn.prestamos.prestamo.domain.EstadoPrestamo;
import gt.com.chn.prestamos.prestamo.domain.Prestamo;
import gt.com.chn.prestamos.prestamo.domain.SolicitudPrestamo;
import gt.com.chn.prestamos.prestamo.domain.ResolucionSolicitud;
import gt.com.chn.prestamos.prestamo.repository.EstadoPrestamoRepository;
import gt.com.chn.prestamos.prestamo.repository.PrestamoRepository;
import gt.com.chn.prestamos.prestamo.repository.ResolucionSolicitudRepository;
import gt.com.chn.prestamos.security.auth.UsuarioActual;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class PrestamoService {

    private final PrestamoRepository prestamoRepository;
    private final PagoRepository pagoRepository;
    private final EstadoPrestamoRepository estadoPrestamoRepository;
    private final ResolucionSolicitudRepository resolucionRepository;
    private final UsuarioActual usuarioActual;

    public PrestamoService(PrestamoRepository prestamoRepository, PagoRepository pagoRepository,
                           EstadoPrestamoRepository estadoPrestamoRepository,
                           ResolucionSolicitudRepository resolucionRepository, UsuarioActual usuarioActual) {
        this.prestamoRepository = prestamoRepository;
        this.pagoRepository = pagoRepository;
        this.estadoPrestamoRepository = estadoPrestamoRepository;
        this.resolucionRepository = resolucionRepository;
        this.usuarioActual = usuarioActual;
    }

    /**
     * Materializa el préstamo a partir de una solicitud aprobada. Sin interés:
     * monto_total = capital y saldo_pendiente = monto aprobado.
     */
    @Transactional
    public Prestamo crearDesdeSolicitud(SolicitudPrestamo solicitud) {
        EstadoPrestamo vigente = estadoPrestamoRepository.findByCodigo(EstadoPrestamo.VIGENTE)
                .orElseThrow(() -> new IllegalStateException("Estado VIGENTE no configurado"));
        Prestamo p = new Prestamo();
        p.setSolicitud(solicitud);
        p.setEstadoPago(vigente);
        p.setMontoAprobado(solicitud.getMontoSolicitado());
        p.setTasaInteres(solicitud.getTasaInteres());
        p.setPlazoMeses(solicitud.getPlazoMeses());
        p.setMontoTotal(solicitud.getMontoSolicitado());
        p.setSaldoPendiente(solicitud.getMontoSolicitado());
        return prestamoRepository.save(p);
    }

    @Transactional(readOnly = true)
    public Page<PrestamoDtos.PrestamoResponse> listarPorCliente(Integer clienteId, Pageable pageable) {
        usuarioActual.verificarAccesoCliente(clienteId);
        Page<Prestamo> pagina = prestamoRepository.findActivosPorCliente(clienteId, pageable);
        // Motivo de aprobación en bloque (la resolución se asocia por solicitud_id).
        List<Integer> solicitudIds = pagina.getContent().stream()
                .map(p -> p.getSolicitud().getId()).toList();
        Map<Integer, String> motivos = new HashMap<>();
        for (ResolucionSolicitud r : resolucionRepository.findBySolicitudIdIn(solicitudIds)) {
            if (r.getMotivo() != null) motivos.put(r.getSolicitudId(), r.getMotivo());
        }
        return pagina.map(p -> PrestamoDtos.PrestamoResponse.de(p, motivos.get(p.getSolicitud().getId())));
    }

    @Transactional(readOnly = true)
    public PrestamoDtos.SaldoResponse saldo(Integer prestamoId) {
        Prestamo p = prestamoRepository.findByIdAndActivoTrue(prestamoId)
                .orElseThrow(() -> new ApiExceptions.NotFoundException(
                        "Préstamo no encontrado: " + prestamoId));
        usuarioActual.verificarAccesoCliente(p.getSolicitud().getCliente().getId());

        BigDecimal totalPagado = pagoRepository.totalPagadoPorPrestamo(prestamoId);
        BigDecimal saldoCalculado = p.getMontoAprobado().subtract(totalPagado);
        return new PrestamoDtos.SaldoResponse(prestamoId, p.getMontoAprobado(), totalPagado,
                saldoCalculado, p.getSaldoPendiente());
    }
}
