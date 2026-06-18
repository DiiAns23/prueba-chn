package gt.com.chn.prestamos.cliente.web;

import gt.com.chn.prestamos.cliente.domain.Cliente;
import jakarta.validation.constraints.*;

import java.time.LocalDate;

public final class ClienteDtos {

    private ClienteDtos() {
    }

    public record ClienteRequest(
            @NotBlank String nombre,
            @NotBlank String apellido,
            @NotBlank String numeroIdentificacion,
            @NotNull @Past LocalDate fechaNacimiento,
            String direccion,
            @Email String correoElectronico,
            String telefono) {
    }

    /** Autoservicio del cliente: solo datos de contacto modificables (no nombre ni identificación). */
    public record ContactoRequest(
            String direccion,
            @Email String correoElectronico,
            String telefono) {
    }

    public record ClienteResponse(
            Integer id,
            String nombre,
            String apellido,
            String numeroIdentificacion,
            LocalDate fechaNacimiento,
            String direccion,
            String correoElectronico,
            String telefono) {

        public static ClienteResponse de(Cliente c) {
            return new ClienteResponse(c.getId(), c.getNombre(), c.getApellido(),
                    c.getNumeroIdentificacion(), c.getFechaNacimiento(), c.getDireccion(),
                    c.getCorreoElectronico(), c.getTelefono());
        }
    }
}
