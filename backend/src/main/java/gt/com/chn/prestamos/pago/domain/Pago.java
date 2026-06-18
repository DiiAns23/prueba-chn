package gt.com.chn.prestamos.pago.domain;

import gt.com.chn.prestamos.common.domain.EntidadAuditable;
import gt.com.chn.prestamos.prestamo.domain.Prestamo;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "pago")
@Getter
@Setter
public class Pago extends EntidadAuditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "prestamo_id")
    private Prestamo prestamo;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "metodo_pago_id")
    private MetodoPago metodoPago;

    @Column(name = "usuario_registra_id", nullable = false)
    private Integer usuarioRegistraId;

    @Column(nullable = false)
    private BigDecimal monto;

    @Column(name = "numero_recibo")
    private String numeroRecibo;

    @Column(name = "fecha_pago", nullable = false)
    private Instant fechaPago = Instant.now();
}
