package gt.com.chn.prestamos.prestamo.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "resolucion_solicitud")
@Getter
@Setter
public class ResolucionSolicitud {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "solicitud_id", nullable = false, unique = true)
    private Integer solicitudId;

    @Column(name = "usuario_resuelve_id", nullable = false)
    private Integer usuarioResuelveId;

    @Column(name = "estado_resultante_id", nullable = false)
    private Integer estadoResultanteId;

    private String motivo;

    @Column(name = "fecha_resolucion", nullable = false)
    private Instant fechaResolucion = Instant.now();
}
