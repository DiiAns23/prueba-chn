package gt.com.chn.prestamos.prestamo.web;

import gt.com.chn.prestamos.prestamo.domain.SolicitudPrestamo;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.Instant;

public final class SolicitudDtos {

    private SolicitudDtos() {
    }

    public record SolicitudRequest(
            @NotNull Integer clienteId,
            @NotNull @DecimalMin(value = "0.01") BigDecimal montoSolicitado,
            @NotNull @Positive Integer plazoMeses,
            String proposito,
            String detalles) {
    }

    public record ResolucionRequest(String motivo) {
    }

    public record SolicitudResponse(
            Integer id,
            Integer clienteId,
            String cliente,
            BigDecimal montoSolicitado,
            Integer plazoMeses,
            String proposito,
            String detalles,
            String estado,
            String motivo,          // motivo de la resolución (aprobación/rechazo), null si EN_PROCESO
            Instant fechaSolicitud) {

        public static SolicitudResponse de(SolicitudPrestamo s, String motivo) {
            return new SolicitudResponse(
                    s.getId(),
                    s.getCliente().getId(),
                    s.getCliente().getNombre() + " " + s.getCliente().getApellido(),
                    s.getMontoSolicitado(),
                    s.getPlazoMeses(),
                    s.getProposito(),
                    s.getDetalles(),
                    s.getEstado().getCodigo(),
                    motivo,
                    s.getFechaSolicitud());
        }
    }
}
