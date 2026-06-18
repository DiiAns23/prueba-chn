package gt.com.chn.prestamos.common.error;

/**
 * Excepciones de negocio mapeadas a códigos HTTP por {@link GlobalExceptionHandler}.
 */
public final class ApiExceptions {

    private ApiExceptions() {
    }

    /** Recurso inexistente -> 404. */
    public static class NotFoundException extends RuntimeException {
        public NotFoundException(String mensaje) {
            super(mensaje);
        }
    }

    /** Conflicto de estado o duplicado -> 409. */
    public static class ConflictException extends RuntimeException {
        public ConflictException(String mensaje) {
            super(mensaje);
        }
    }

    /** Regla de negocio violada -> 422. */
    public static class BusinessRuleException extends RuntimeException {
        public BusinessRuleException(String mensaje) {
            super(mensaje);
        }
    }

    /** Acceso denegado por permisos -> 403. */
    public static class ForbiddenException extends RuntimeException {
        public ForbiddenException(String mensaje) {
            super(mensaje);
        }
    }
}
