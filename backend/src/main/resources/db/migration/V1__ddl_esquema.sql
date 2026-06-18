/* ============================================================
   CHN - Sistema de Gestión de Préstamos Bancarios
   V1 - DDL: estructura completa (Microsoft SQL Server / Transact-SQL)

   Principios de diseño:
     - Soft delete (sin borrado físico) -> columnas activo / fecha_baja / usuario_baja_id.
     - Sin cálculo de interés, pero modelo escalable (tasa_interes, monto_total, cuota_prestamo).
     - Seguridad por matriz de permisos (modulo.accion) con excepciones por usuario.
   ============================================================ */

/* ---------- Seguridad: módulos y permisos (matriz) ---------- */
CREATE TABLE modulo (
    id           INT IDENTITY(1,1) PRIMARY KEY,
    codigo       VARCHAR(50)  NOT NULL,
    descripcion  VARCHAR(200) NULL,
    CONSTRAINT uq_modulo_codigo UNIQUE (codigo)
);

CREATE TABLE permiso (
    id           INT IDENTITY(1,1) PRIMARY KEY,
    modulo_id    INT          NOT NULL,
    accion       VARCHAR(50)  NOT NULL,
    codigo       VARCHAR(100) NOT NULL,          -- ej: clientes.crear
    descripcion  VARCHAR(200) NULL,
    CONSTRAINT uq_permiso_codigo UNIQUE (codigo),
    CONSTRAINT fk_permiso_modulo FOREIGN KEY (modulo_id) REFERENCES modulo(id)
);

CREATE TABLE rol (
    id           INT IDENTITY(1,1) PRIMARY KEY,
    codigo       VARCHAR(50)  NOT NULL,          -- OPERADOR | CLIENTE | ADMINISTRADOR
    descripcion  VARCHAR(200) NULL,
    activo       BIT          NOT NULL DEFAULT 1,
    CONSTRAINT uq_rol_codigo UNIQUE (codigo)
);

CREATE TABLE rol_permiso (
    rol_id       INT NOT NULL,
    permiso_id   INT NOT NULL,
    CONSTRAINT pk_rol_permiso PRIMARY KEY (rol_id, permiso_id),
    CONSTRAINT fk_rolperm_rol     FOREIGN KEY (rol_id)     REFERENCES rol(id),
    CONSTRAINT fk_rolperm_permiso FOREIGN KEY (permiso_id) REFERENCES permiso(id)
);

/* ---------- Clientes ---------- */
CREATE TABLE cliente (
    id                     INT IDENTITY(1,1) PRIMARY KEY,
    nombre                 VARCHAR(100) NOT NULL,
    apellido               VARCHAR(100) NOT NULL,
    numero_identificacion  VARCHAR(50)  NOT NULL,
    fecha_nacimiento       DATE         NOT NULL,
    direccion              VARCHAR(300) NULL,
    correo_electronico     VARCHAR(150) NULL,
    telefono               VARCHAR(30)  NULL,
    activo                 BIT          NOT NULL DEFAULT 1,
    fecha_creacion         DATETIME2    NOT NULL DEFAULT SYSUTCDATETIME(),
    fecha_actualizacion    DATETIME2    NULL,
    fecha_baja             DATETIME2    NULL,
    usuario_baja_id        INT          NULL
);
-- Identificación única solo entre clientes vigentes (soft delete preserva los dados de baja)
CREATE UNIQUE INDEX uq_cliente_identificacion_activo
    ON cliente (numero_identificacion) WHERE activo = 1;

/* ---------- Usuarios (acceso / autoservicio) ---------- */
CREATE TABLE usuario (
    id               INT IDENTITY(1,1) PRIMARY KEY,
    cliente_id       INT          NULL,           -- NULL para operador / administrador
    nombre_usuario   VARCHAR(100) NOT NULL,
    hash_contrasena  VARCHAR(200) NOT NULL,
    activo           BIT          NOT NULL DEFAULT 1,
    fecha_creacion   DATETIME2    NOT NULL DEFAULT SYSUTCDATETIME(),
    fecha_baja       DATETIME2    NULL,
    usuario_baja_id  INT          NULL,
    CONSTRAINT uq_usuario_nombre       UNIQUE (nombre_usuario),
    CONSTRAINT fk_usuario_cliente      FOREIGN KEY (cliente_id)      REFERENCES cliente(id),
    CONSTRAINT fk_usuario_usuario_baja FOREIGN KEY (usuario_baja_id) REFERENCES usuario(id)
);
-- cliente.usuario_baja_id se ata aquí porque 'usuario' se crea después de 'cliente'
ALTER TABLE cliente
    ADD CONSTRAINT fk_cliente_usuario_baja FOREIGN KEY (usuario_baja_id) REFERENCES usuario(id);

CREATE TABLE usuario_rol (
    usuario_id  INT NOT NULL,
    rol_id      INT NOT NULL,
    CONSTRAINT pk_usuario_rol PRIMARY KEY (usuario_id, rol_id),
    CONSTRAINT fk_usrol_usuario FOREIGN KEY (usuario_id) REFERENCES usuario(id),
    CONSTRAINT fk_usrol_rol     FOREIGN KEY (rol_id)     REFERENCES rol(id)
);

-- Excepciones por usuario: granted=1 otorga, granted=0 revoca
CREATE TABLE usuario_permiso (
    usuario_id  INT NOT NULL,
    permiso_id  INT NOT NULL,
    granted     BIT NOT NULL,
    CONSTRAINT pk_usuario_permiso PRIMARY KEY (usuario_id, permiso_id),
    CONSTRAINT fk_usperm_usuario FOREIGN KEY (usuario_id) REFERENCES usuario(id),
    CONSTRAINT fk_usperm_permiso FOREIGN KEY (permiso_id) REFERENCES permiso(id)
);

/* ---------- Catálogos de estado ---------- */
CREATE TABLE estado_solicitud (
    id           INT IDENTITY(1,1) PRIMARY KEY,
    codigo       VARCHAR(30)  NOT NULL,           -- EN_PROCESO | APROBADO | RECHAZADO
    descripcion  VARCHAR(100) NULL,
    CONSTRAINT uq_estado_solicitud_codigo UNIQUE (codigo)
);

CREATE TABLE estado_prestamo (
    id           INT IDENTITY(1,1) PRIMARY KEY,
    codigo       VARCHAR(30)  NOT NULL,           -- VIGENTE | PAGADO | EN_MORA
    descripcion  VARCHAR(100) NULL,
    CONSTRAINT uq_estado_prestamo_codigo UNIQUE (codigo)
);

CREATE TABLE metodo_pago (
    id           INT IDENTITY(1,1) PRIMARY KEY,
    codigo       VARCHAR(30)  NOT NULL,           -- EFECTIVO | ...
    descripcion  VARCHAR(100) NULL,
    CONSTRAINT uq_metodo_pago_codigo UNIQUE (codigo)
);

/* ---------- Solicitudes de préstamo ---------- */
CREATE TABLE solicitud_prestamo (
    id                   INT IDENTITY(1,1) PRIMARY KEY,
    cliente_id           INT           NOT NULL,
    usuario_registra_id  INT           NOT NULL,
    estado_id            INT           NOT NULL,
    monto_solicitado     DECIMAL(19,2) NOT NULL,
    plazo_meses          INT           NOT NULL,
    tasa_interes         DECIMAL(9,4)  NOT NULL DEFAULT 0,   -- escalable (hoy 0)
    proposito            VARCHAR(200)  NULL,
    detalles             VARCHAR(500)  NULL,
    fecha_solicitud      DATETIME2     NOT NULL DEFAULT SYSUTCDATETIME(),
    activo               BIT           NOT NULL DEFAULT 1,
    fecha_baja           DATETIME2     NULL,
    usuario_baja_id      INT           NULL,
    CONSTRAINT fk_solicitud_cliente FOREIGN KEY (cliente_id)          REFERENCES cliente(id),
    CONSTRAINT fk_solicitud_usuario FOREIGN KEY (usuario_registra_id) REFERENCES usuario(id),
    CONSTRAINT fk_solicitud_estado  FOREIGN KEY (estado_id)           REFERENCES estado_solicitud(id),
    CONSTRAINT fk_solicitud_usubaja FOREIGN KEY (usuario_baja_id)     REFERENCES usuario(id),
    CONSTRAINT ck_solicitud_monto   CHECK (monto_solicitado > 0),
    CONSTRAINT ck_solicitud_plazo   CHECK (plazo_meses > 0)
);

CREATE TABLE resolucion_solicitud (
    id                    INT IDENTITY(1,1) PRIMARY KEY,
    solicitud_id          INT          NOT NULL,
    usuario_resuelve_id   INT          NOT NULL,
    estado_resultante_id  INT          NOT NULL,
    motivo                VARCHAR(500) NULL,
    fecha_resolucion      DATETIME2    NOT NULL DEFAULT SYSUTCDATETIME(),
    CONSTRAINT uq_resolucion_solicitud UNIQUE (solicitud_id),
    CONSTRAINT fk_resol_solicitud FOREIGN KEY (solicitud_id)         REFERENCES solicitud_prestamo(id),
    CONSTRAINT fk_resol_usuario   FOREIGN KEY (usuario_resuelve_id)  REFERENCES usuario(id),
    CONSTRAINT fk_resol_estado    FOREIGN KEY (estado_resultante_id) REFERENCES estado_solicitud(id)
);

CREATE TABLE historial_estado_solicitud (
    id                  INT IDENTITY(1,1) PRIMARY KEY,
    solicitud_id        INT       NOT NULL,
    estado_anterior_id  INT       NULL,
    estado_nuevo_id     INT       NOT NULL,
    usuario_id          INT       NOT NULL,
    fecha_cambio        DATETIME2 NOT NULL DEFAULT SYSUTCDATETIME(),
    CONSTRAINT fk_hist_solicitud FOREIGN KEY (solicitud_id)       REFERENCES solicitud_prestamo(id),
    CONSTRAINT fk_hist_estado_a  FOREIGN KEY (estado_anterior_id) REFERENCES estado_solicitud(id),
    CONSTRAINT fk_hist_estado_n  FOREIGN KEY (estado_nuevo_id)    REFERENCES estado_solicitud(id),
    CONSTRAINT fk_hist_usuario   FOREIGN KEY (usuario_id)         REFERENCES usuario(id)
);

/* ---------- Préstamos aprobados ---------- */
CREATE TABLE prestamo (
    id               INT IDENTITY(1,1) PRIMARY KEY,
    solicitud_id     INT           NOT NULL,
    estado_pago_id   INT           NOT NULL,
    monto_aprobado   DECIMAL(19,2) NOT NULL,
    tasa_interes     DECIMAL(9,4)  NOT NULL DEFAULT 0,    -- escalable (hoy 0)
    plazo_meses      INT           NOT NULL,
    monto_total      DECIMAL(19,2) NOT NULL,              -- escalable = capital (+interés)
    saldo_pendiente  DECIMAL(19,2) NOT NULL,
    fecha_aprobacion DATETIME2     NOT NULL DEFAULT SYSUTCDATETIME(),
    version          BIGINT        NOT NULL DEFAULT 0,    -- bloqueo optimista (pagos concurrentes)
    activo           BIT           NOT NULL DEFAULT 1,
    fecha_baja       DATETIME2     NULL,
    usuario_baja_id  INT           NULL,
    CONSTRAINT uq_prestamo_solicitud UNIQUE (solicitud_id),
    CONSTRAINT fk_prestamo_solicitud FOREIGN KEY (solicitud_id)    REFERENCES solicitud_prestamo(id),
    CONSTRAINT fk_prestamo_estado    FOREIGN KEY (estado_pago_id)  REFERENCES estado_prestamo(id),
    CONSTRAINT fk_prestamo_usubaja   FOREIGN KEY (usuario_baja_id) REFERENCES usuario(id),
    CONSTRAINT ck_prestamo_saldo     CHECK (saldo_pendiente >= 0)
);

/* ---------- Pagos ---------- */
CREATE TABLE pago (
    id                   INT IDENTITY(1,1) PRIMARY KEY,
    prestamo_id          INT           NOT NULL,
    metodo_pago_id       INT           NOT NULL,
    usuario_registra_id  INT           NOT NULL,
    monto                DECIMAL(19,2) NOT NULL,
    numero_recibo        VARCHAR(50)   NULL,
    fecha_pago           DATETIME2     NOT NULL DEFAULT SYSUTCDATETIME(),
    activo               BIT           NOT NULL DEFAULT 1,
    fecha_baja           DATETIME2     NULL,
    usuario_baja_id      INT           NULL,
    CONSTRAINT fk_pago_prestamo    FOREIGN KEY (prestamo_id)         REFERENCES prestamo(id),
    CONSTRAINT fk_pago_metodo      FOREIGN KEY (metodo_pago_id)      REFERENCES metodo_pago(id),
    CONSTRAINT fk_pago_usuario     FOREIGN KEY (usuario_registra_id) REFERENCES usuario(id),
    CONSTRAINT fk_pago_usuario_baja FOREIGN KEY (usuario_baja_id)    REFERENCES usuario(id),
    CONSTRAINT ck_pago_monto       CHECK (monto > 0)
);

/* ---------- (Escalable) Plan de cuotas / amortización ----------
   Diseñada para activar interés/mora más adelante sin rediseñar el núcleo.
   No se usa en la entrega actual. */
CREATE TABLE cuota_prestamo (
    id                INT IDENTITY(1,1) PRIMARY KEY,
    prestamo_id       INT           NOT NULL,
    numero_cuota      INT           NOT NULL,
    fecha_vencimiento DATE          NOT NULL,
    monto_cuota       DECIMAL(19,2) NOT NULL,
    capital           DECIMAL(19,2) NOT NULL,
    interes           DECIMAL(19,2) NOT NULL DEFAULT 0,
    estado            VARCHAR(20)   NOT NULL DEFAULT 'PENDIENTE',  -- PENDIENTE|PAGADA|VENCIDA
    CONSTRAINT fk_cuota_prestamo FOREIGN KEY (prestamo_id) REFERENCES prestamo(id),
    CONSTRAINT uq_cuota_numero   UNIQUE (prestamo_id, numero_cuota)
);

/* ---------- Índices de apoyo a consultas frecuentes ---------- */
CREATE INDEX ix_solicitud_cliente ON solicitud_prestamo (cliente_id) WHERE activo = 1;
CREATE INDEX ix_pago_prestamo     ON pago (prestamo_id)              WHERE activo = 1;
