/* ============================================================
   CHN - V3 - DML: seguridad (RBAC)
   Módulos, permisos (modulo.accion), roles y la matriz rol-permiso
   en su estado final. Los usuarios por defecto los crea la aplicación
   al arrancar (DataInitializer) para cifrar la contraseña con BCrypt.
   ============================================================ */

/* ---------- Módulos ---------- */
INSERT INTO modulo (codigo, descripcion) VALUES
    ('clientes',  'Gestión de clientes'),
    ('prestamos', 'Solicitudes y préstamos'),
    ('pagos',     'Pagos y saldos'),
    ('seguridad', 'Usuarios, roles y permisos');

/* ---------- Permisos (módulo.acción) ---------- */
INSERT INTO permiso (modulo_id, accion, codigo, descripcion)
SELECT m.id, v.accion, v.codigo, v.descripcion
FROM (VALUES
    ('clientes',  'crear',      'clientes.crear',      'Registrar clientes'),
    ('clientes',  'leer',       'clientes.leer',       'Consultar clientes'),
    ('clientes',  'actualizar', 'clientes.actualizar', 'Editar clientes'),
    ('clientes',  'eliminar',   'clientes.eliminar',   'Dar de baja clientes'),
    ('clientes',  'contacto',   'clientes.contacto',   'Actualizar datos de contacto propios'),
    ('prestamos', 'crear',      'prestamos.crear',     'Solicitar préstamos'),
    ('prestamos', 'leer',       'prestamos.leer',      'Consultar solicitudes y préstamos'),
    ('prestamos', 'aprobar',    'prestamos.aprobar',   'Aprobar solicitudes'),
    ('prestamos', 'rechazar',   'prestamos.rechazar',  'Rechazar solicitudes'),
    ('pagos',     'crear',      'pagos.crear',         'Registrar pagos'),
    ('pagos',     'leer',       'pagos.leer',          'Consultar pagos y saldos'),
    ('seguridad', 'usuarios',   'seguridad.usuarios',  'Gestionar usuarios'),
    ('seguridad', 'roles',      'seguridad.roles',     'Gestionar roles'),
    ('seguridad', 'permisos',   'seguridad.permisos',  'Gestionar permisos y excepciones')
) AS v(modulo, accion, codigo, descripcion)
JOIN modulo m ON m.codigo = v.modulo;

/* ---------- Roles ---------- */
INSERT INTO rol (codigo, descripcion) VALUES
    ('OPERADOR',      'Analista bancario: clientes, solicitudes y pagos'),
    ('CLIENTE',       'Autoservicio: sus propias solicitudes/préstamos y su contacto'),
    ('ADMINISTRADOR', 'Gestión de seguridad');

/* ---------- Matriz rol-permiso ---------- */
-- OPERADOR: toda la operación de negocio (aprobar y rechazar son permisos distintos)
INSERT INTO rol_permiso (rol_id, permiso_id)
SELECT r.id, p.id
FROM rol r, permiso p
WHERE r.codigo = 'OPERADOR'
  AND p.codigo IN ('clientes.crear','clientes.leer','clientes.actualizar','clientes.eliminar',
                   'prestamos.crear','prestamos.leer','prestamos.aprobar','prestamos.rechazar',
                   'pagos.crear','pagos.leer');

-- CLIENTE: autoservicio. Solo sus solicitudes/préstamos, consulta de pagos y su contacto.
-- No accede al módulo de clientes (salvo actualizar su propio contacto vía /mi-perfil).
INSERT INTO rol_permiso (rol_id, permiso_id)
SELECT r.id, p.id
FROM rol r, permiso p
WHERE r.codigo = 'CLIENTE'
  AND p.codigo IN ('prestamos.crear','prestamos.leer','pagos.leer','clientes.contacto');

-- ADMINISTRADOR: seguridad
INSERT INTO rol_permiso (rol_id, permiso_id)
SELECT r.id, p.id
FROM rol r, permiso p
WHERE r.codigo = 'ADMINISTRADOR'
  AND p.codigo IN ('seguridad.usuarios','seguridad.roles','seguridad.permisos');
