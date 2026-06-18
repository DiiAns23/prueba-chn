package gt.com.chn.prestamos.security.auth;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import gt.com.chn.prestamos.security.domain.Usuario;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Date;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class JwtService {

    private final SecretKey clave;
    private final long expiracionMinutos;

    public JwtService(@Value("${app.security.jwt.secret}") String secret,
                      @Value("${app.security.jwt.expiration-minutes}") long expiracionMinutos) {
        this.clave = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expiracionMinutos = expiracionMinutos;
    }

    public String generarToken(Usuario usuario, Set<String> permisos) {
        Instant ahora = Instant.now();
        // Los permisos viajan dentro del token (se resuelven una vez al iniciar sesión). Esto evita
        // golpear la BD en cada petición, a cambio de que un cambio de permisos surta efecto al
        // renovar el token; por eso conviene un TTL corto (app.security.jwt.expiration-minutes).
        return Jwts.builder()
                .subject(usuario.getNombreUsuario())
                .claim("uid", usuario.getId())
                .claim("cid", usuario.getClienteId())
                .claim("perms", String.join(",", permisos))
                .issuedAt(Date.from(ahora))
                .expiration(Date.from(ahora.plus(expiracionMinutos, ChronoUnit.MINUTES)))
                .signWith(clave)
                .compact();
    }

    public UsuarioAutenticado parsear(String token) {
        Claims c = Jwts.parser().verifyWith(clave).build()
                .parseSignedClaims(token).getPayload();
        Integer clienteId = c.get("cid", Integer.class);
        String perms = c.get("perms", String.class);
        Set<String> permisos = (perms == null || perms.isBlank())
                ? Set.of()
                : Arrays.stream(perms.split(",")).collect(Collectors.toSet());
        return new UsuarioAutenticado(c.get("uid", Integer.class), c.getSubject(), clienteId, permisos);
    }
}
