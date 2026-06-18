package gt.com.chn.prestamos.security.web;

import gt.com.chn.prestamos.security.auth.JwtService;
import gt.com.chn.prestamos.security.domain.Usuario;
import gt.com.chn.prestamos.security.repository.UsuarioRepository;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

@Service
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder,
                       JwtService jwtService) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public AuthDtos.LoginResponse login(AuthDtos.LoginRequest req) {
        Usuario usuario = usuarioRepository.findByNombreUsuarioAndActivoTrue(req.nombreUsuario())
                .orElseThrow(() -> new BadCredentialsException("Credenciales inválidas"));

        if (!passwordEncoder.matches(req.contrasena(), usuario.getHashContrasena())) {
            throw new BadCredentialsException("Credenciales inválidas");
        }

        Set<String> permisos = new HashSet<>(usuarioRepository.findPermisosEfectivos(usuario.getId()));
        String token = jwtService.generarToken(usuario, permisos);
        return new AuthDtos.LoginResponse(token, usuario.getNombreUsuario(),
                usuario.getClienteId(), permisos);
    }
}
