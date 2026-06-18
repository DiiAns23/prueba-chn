package gt.com.chn.prestamos.security.auth;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Exige que el usuario autenticado posea el permiso indicado (código modulo.accion).
 * Resuelto por {@link RequierePermisoAspect}. Ej: {@code @RequierePermiso("prestamos.aprobar")}.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RequierePermiso {
    String value();
}
