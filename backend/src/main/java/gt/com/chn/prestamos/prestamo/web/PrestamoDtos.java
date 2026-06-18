package gt.com.chn.prestamos.prestamo.web;

import gt.com.chn.prestamos.prestamo.domain.Prestamo;

import java.math.BigDecimal;
import java.time.Instant;

public final class PrestamoDtos {

    private PrestamoDtos() {
    }

    public record PrestamoResponse(
            Integer id,
            Integer solicitudId,
            Integer clienteId,
            String cliente,
            BigDecimal montoAprobado,
            Integer plazoMeses,
            BigDecimal saldoPendiente,
            String estadoPago,
            Instant fechaAprobacion,
            String proposito,          // detalles de la solicitud que originó el préstamo
            String detalles,
            String motivoAprobacion) { // motivo registrado al aprobar

        public static PrestamoResponse de(Prestamo p, String motivoAprobacion) {
            var s = p.getSolicitud();
            var c = s.getCliente();
            return new PrestamoResponse(
                    p.getId(),
                    s.getId(),
                    c.getId(),
                    c.getNombre() + " " + c.getApellido(),
                    p.getMontoAprobado(),
                    p.getPlazoMeses(),
                    p.getSaldoPendiente(),
                    p.getEstadoPago().getCodigo(),
                    p.getFechaAprobacion(),
                    s.getProposito(),
                    s.getDetalles(),
                    motivoAprobacion);
        }
    }

    /** Conciliación de saldo (MER §4.10): materializado vs. reconstruido desde pagos. */
    public record SaldoResponse(
            Integer prestamoId,
            BigDecimal montoAprobado,
            BigDecimal totalPagado,
            BigDecimal saldoCalculado,
            BigDecimal saldoMaterializado) {
    }
}
