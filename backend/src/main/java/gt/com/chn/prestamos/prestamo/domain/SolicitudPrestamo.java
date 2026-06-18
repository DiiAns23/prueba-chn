package gt.com.chn.prestamos.prestamo.domain;

import gt.com.chn.prestamos.cliente.domain.Cliente;
import gt.com.chn.prestamos.common.domain.EntidadAuditable;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "solicitud_prestamo")
@Getter
@Setter
public class SolicitudPrestamo extends EntidadAuditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cliente_id")
    private Cliente cliente;

    @Column(name = "usuario_registra_id", nullable = false)
    private Integer usuarioRegistraId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "estado_id")
    private EstadoSolicitud estado;

    @Column(name = "monto_solicitado", nullable = false)
    private BigDecimal montoSolicitado;

    @Column(name = "plazo_meses", nullable = false)
    private Integer plazoMeses;

    /** Escalable: hoy 0 (sin interés). */
    @Column(name = "tasa_interes", nullable = false)
    private BigDecimal tasaInteres = BigDecimal.ZERO;

    private String proposito;

    private String detalles;

    @Column(name = "fecha_solicitud", nullable = false)
    private Instant fechaSolicitud = Instant.now();

    public boolean estaEnProceso() {
        return estado != null && EstadoSolicitud.EN_PROCESO.equals(estado.getCodigo());
    }
}
