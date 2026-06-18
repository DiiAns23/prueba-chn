package gt.com.chn.prestamos.common.domain;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;

import java.time.Instant;

/**
 * Base para entidades con borrado lógico (soft delete) y auditoría de baja.
 * Aporta herencia + encapsulamiento del comportamiento de baja, reutilizado por
 * todas las entidades del dominio (polimorfismo de dominio).
 *
 * Sin setters: la baja solo puede hacerse vía {@link #darDeBaja(Integer)}, garantizando que
 * activo/fecha/autor queden siempre coherentes (no se puede dejar la entidad en un estado inválido).
 */
@MappedSuperclass
@Getter
public abstract class EntidadAuditable {

    @Column(nullable = false)
    private boolean activo = true;

    @Column(name = "fecha_baja")
    private Instant fechaBaja;

    @Column(name = "usuario_baja_id")
    private Integer usuarioBajaId;

    /** Borrado lógico: marca la entidad inactiva registrando quién y cuándo. */
    public void darDeBaja(Integer usuarioId) {
        this.activo = false;
        this.fechaBaja = Instant.now();
        this.usuarioBajaId = usuarioId;
    }
}
