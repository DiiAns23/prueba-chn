package gt.com.chn.prestamos.pago.web;

import gt.com.chn.prestamos.common.error.ApiExceptions;
import gt.com.chn.prestamos.pago.domain.MetodoPago;
import gt.com.chn.prestamos.pago.domain.Pago;
import gt.com.chn.prestamos.pago.repository.MetodoPagoRepository;
import gt.com.chn.prestamos.pago.repository.PagoRepository;
import gt.com.chn.prestamos.prestamo.domain.EstadoPrestamo;
import gt.com.chn.prestamos.prestamo.domain.Prestamo;
import gt.com.chn.prestamos.prestamo.repository.EstadoPrestamoRepository;
import gt.com.chn.prestamos.prestamo.repository.PrestamoRepository;
import gt.com.chn.prestamos.security.auth.UsuarioActual;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class PagoService {

    private final PagoRepository pagoRepository;
    private final PrestamoRepository prestamoRepository;
    private final MetodoPagoRepository metodoPagoRepository;
    private final EstadoPrestamoRepository estadoPrestamoRepository;
    private final UsuarioActual usuarioActual;

    public PagoService(PagoRepository pagoRepository, PrestamoRepository prestamoRepository,
                       MetodoPagoRepository metodoPagoRepository,
                       EstadoPrestamoRepository estadoPrestamoRepository, UsuarioActual usuarioActual) {
        this.pagoRepository = pagoRepository;
        this.prestamoRepository = prestamoRepository;
        this.metodoPagoRepository = metodoPagoRepository;
        this.estadoPrestamoRepository = estadoPrestamoRepository;
        this.usuarioActual = usuarioActual;
    }

    /**
     * Registra un pago en efectivo de forma atómica: valida saldo, descuenta y, si llega a 0,
     * marca el préstamo como PAGADO. El bloqueo optimista (@Version) protege pagos concurrentes.
     */
    @Transactional
    public PagoDtos.PagoResponse registrar(Integer prestamoId, PagoDtos.PagoRequest req) {
        Prestamo prestamo = prestamoRepository.findByIdAndActivoTrue(prestamoId)
                .orElseThrow(() -> new ApiExceptions.NotFoundException(
                        "Préstamo no encontrado: " + prestamoId));

        // Primero el estado (un préstamo PAGADO/EN_MORA no recibe pagos por esta vía) y luego el monto.
        if (!prestamo.estaVigente()) {
            throw new ApiExceptions.ConflictException(
                    "El préstamo no admite pagos (estado: " + prestamo.getEstadoPago().getCodigo() + ")");
        }
        if (req.monto().compareTo(prestamo.getSaldoPendiente()) > 0) {
            throw new ApiExceptions.BusinessRuleException(
                    "El pago excede el saldo pendiente (" + prestamo.getSaldoPendiente() + ")");
        }

        MetodoPago efectivo = metodoPagoRepository.findByCodigo(MetodoPago.EFECTIVO)
                .orElseThrow(() -> new IllegalStateException("Método EFECTIVO no configurado"));

        Pago pago = new Pago();
        pago.setPrestamo(prestamo);
        pago.setMetodoPago(efectivo);
        pago.setUsuarioRegistraId(usuarioActual.get().usuarioId());
        pago.setMonto(req.monto());
        pago.setNumeroRecibo(req.numeroRecibo());
        pagoRepository.save(pago);

        BigDecimal nuevoSaldo = prestamo.getSaldoPendiente().subtract(req.monto());
        prestamo.setSaldoPendiente(nuevoSaldo);
        // compareTo, no equals: BigDecimal.equals distingue la escala (0.00 != 0.0); compareTo no.
        if (nuevoSaldo.compareTo(BigDecimal.ZERO) == 0) {
            prestamo.setEstadoPago(estadoPrestamoRepository.findByCodigo(EstadoPrestamo.PAGADO)
                    .orElseThrow(() -> new IllegalStateException("Estado PAGADO no configurado")));
        }
        // El préstamo está gestionado: el nuevo saldo y el incremento de @Version se escriben al
        // hacer commit. Dos pagos en paralelo chocan en @Version -> 409, y el segundo reintenta.
        return PagoDtos.PagoResponse.de(pago);
    }

    /** Historial de pagos de un préstamo (más recientes primero). Respeta el aislamiento del cliente. */
    @Transactional(readOnly = true)
    public List<PagoDtos.PagoHistorialResponse> historial(Integer prestamoId) {
        Prestamo prestamo = prestamoRepository.findByIdAndActivoTrue(prestamoId)
                .orElseThrow(() -> new ApiExceptions.NotFoundException(
                        "Préstamo no encontrado: " + prestamoId));
        usuarioActual.verificarAccesoCliente(prestamo.getSolicitud().getCliente().getId());
        return pagoRepository.findAllByPrestamo_IdAndActivoTrueOrderByFechaPagoDesc(prestamoId).stream()
                .map(PagoDtos.PagoHistorialResponse::de)
                .toList();
    }
}
