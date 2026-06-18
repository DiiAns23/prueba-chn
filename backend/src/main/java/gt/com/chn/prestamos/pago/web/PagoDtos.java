package gt.com.chn.prestamos.pago.web;

import gt.com.chn.prestamos.pago.domain.Pago;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.Instant;

public final class PagoDtos {

    private PagoDtos() {
    }

    public record PagoRequest(
            @NotNull @DecimalMin(value = "0.01") BigDecimal monto,
            String numeroRecibo) {
    }

    public record PagoResponse(
            Integer id,
            Integer prestamoId,
            BigDecimal monto,
            String numeroRecibo,
            Instant fechaPago,
            BigDecimal saldoPendiente,
            String estadoPago) {

        /** Requiere sesión de persistencia abierta: navega asociaciones LAZY (prestamo, estadoPago). */
        public static PagoResponse de(Pago p) {
            return new PagoResponse(
                    p.getId(),
                    p.getPrestamo().getId(),
                    p.getMonto(),
                    p.getNumeroRecibo(),
                    p.getFechaPago(),
                    p.getPrestamo().getSaldoPendiente(),
                    p.getPrestamo().getEstadoPago().getCodigo());
        }
    }

    /** Item del historial de pagos (no navega al préstamo; evita LAZY al listar). */
    public record PagoHistorialResponse(
            Integer id,
            BigDecimal monto,
            String numeroRecibo,
            Instant fechaPago) {

        public static PagoHistorialResponse de(Pago p) {
            return new PagoHistorialResponse(p.getId(), p.getMonto(), p.getNumeroRecibo(), p.getFechaPago());
        }
    }
}
