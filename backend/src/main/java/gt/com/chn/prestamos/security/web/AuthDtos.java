package gt.com.chn.prestamos.security.web;

import jakarta.validation.constraints.NotBlank;

import java.util.Set;

public final class AuthDtos {

    private AuthDtos() {
    }

    public record LoginRequest(
            @NotBlank String nombreUsuario,
            @NotBlank String contrasena) {
    }

    public record LoginResponse(
            String token,
            String nombreUsuario,
            Integer clienteId,
            Set<String> permisos) {
    }
}
