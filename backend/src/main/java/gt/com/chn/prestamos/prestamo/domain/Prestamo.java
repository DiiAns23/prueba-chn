package gt.com.chn.prestamos.prestamo.domain;

import gt.com.chn.prestamos.common.domain.EntidadAuditable;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "prestamo")
@Getter
@Setter
public class Prestamo extends EntidadAuditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "solicitud_id")
    private SolicitudPrestamo solicitud;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "estado_pago_id")
    private EstadoPrestamo estadoPago;

    @Column(name = "monto_aprobado", nullable = false)
    private BigDecimal montoAprobado;

    /** Escalable: hoy 0 (sin interés). */
    @Column(name = "tasa_interes", nullable = false)
    private BigDecimal tasaInteres = BigDecimal.ZERO;

    @Column(name = "plazo_meses", nullable = false)
    private Integer plazoMeses;

    @Column(name = "monto_total", nullable = false)
    private BigDecimal montoTotal;

    @Column(name = "saldo_pendiente", nullable = false)
    private BigDecimal saldoPendiente;

    @Column(name = "fecha_aprobacion", nullable = false)
    private Instant fechaAprobacion = Instant.now();

    /** Bloqueo optimista para evitar doble descuento en pagos concurrentes. */
    @Version
    private Long version;

    public boolean estaVigente() {
        return estadoPago != null && EstadoPrestamo.VIGENTE.equals(estadoPago.getCodigo());
    }
}
