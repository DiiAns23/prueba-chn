package gt.com.chn.prestamos.security.auth;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * Autenticación stateless: en cada petición reconstruye el usuario y sus permisos desde el JWT,
 * sin sesión en servidor. Se registra una sola vez en la cadena (ver SecurityConfig, que desactiva
 * el auto-registro del filtro para que no se ejecute dos veces).
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    public JwtAuthenticationFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (header != null && header.startsWith("Bearer ")) {
            try {
                UsuarioAutenticado u = jwtService.parsear(header.substring(7));
                List<SimpleGrantedAuthority> authorities = u.permisos().stream()
                        .map(SimpleGrantedAuthority::new)
                        .toList();
                var auth = new UsernamePasswordAuthenticationToken(u, null, authorities);
                auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(auth);
            } catch (Exception ex) {
                // Token inválido o expirado: no autenticamos y dejamos continuar la cadena.
                // El 401 (o el paso a una ruta pública) lo decide SecurityConfig, no este filtro.
                SecurityContextHolder.clearContext();
            }
        }
        filterChain.doFilter(request, response);
    }
}
