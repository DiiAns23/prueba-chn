package gt.com.chn.prestamos.prestamo.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "historial_estado_solicitud")
@Getter
@Setter
public class HistorialEstadoSolicitud {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "solicitud_id", nullable = false)
    private Integer solicitudId;

    @Column(name = "estado_anterior_id")
    private Integer estadoAnteriorId;

    @Column(name = "estado_nuevo_id", nullable = false)
    private Integer estadoNuevoId;

    @Column(name = "usuario_id", nullable = false)
    private Integer usuarioId;

    @Column(name = "fecha_cambio", nullable = false)
    private Instant fechaCambio = Instant.now();
}
