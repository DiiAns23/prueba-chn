/* ============================================================
   CHN - V2 - DML: catálogos de negocio
   Datos de referencia (estados y métodos). Los estados son catálogos
   y no enums fijos -> se agregan sin tocar código.
   ============================================================ */

INSERT INTO estado_solicitud (codigo, descripcion) VALUES
    ('EN_PROCESO', 'Solicitud en evaluación'),
    ('APROBADO',   'Solicitud aprobada'),
    ('RECHAZADO',  'Solicitud rechazada');

INSERT INTO estado_prestamo (codigo, descripcion) VALUES
    ('VIGENTE', 'Préstamo con saldo pendiente'),
    ('PAGADO',  'Préstamo liquidado'),
    ('EN_MORA', 'Préstamo en mora (escalable)');

INSERT INTO metodo_pago (codigo, descripcion) VALUES
    ('EFECTIVO', 'Pago en efectivo');
