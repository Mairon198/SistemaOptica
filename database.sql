-- =====================================================================
--  Sistema Óptica - Base de datos OpticaDB (SQL Server / Azure SQL)
--
--  Ejecutar con la base OpticaDB seleccionada.
--  Se puede ejecutar varias veces: solo crea lo que todavía no existe.
--
--  Los nombres de columnas siguen EXACTAMENTE lo que usa el código Java:
--   * cliente, vendedor, usuario  -> camelCase (idCliente, passwordHash...)
--     (UsuarioDAOImpl, CrearUsuariosDemo)
--   * producto, aro, luna, accesorio -> snake_case (id_producto, stock_minimo...)
--     (ProductoDAOImpl, AroDAOImpl, LunaDAOImpl, AccesorioDAOImpl)
--   * medicion, venta, detalle_venta -> todavía no tienen DAO; se usa
--     snake_case igual que producto.
-- =====================================================================


-- ---------------------------------------------------------------------
-- 1. PERSONAS Y ACCESO
-- ---------------------------------------------------------------------

IF OBJECT_ID('dbo.cliente', 'U') IS NULL
CREATE TABLE dbo.cliente (
    idCliente      INT IDENTITY(1,1) CONSTRAINT PK_cliente PRIMARY KEY,
    dni            VARCHAR(20)   NULL,
    nombres        VARCHAR(100)  NOT NULL,
    apellidos      VARCHAR(100)  NOT NULL,
    telefono       VARCHAR(20)   NULL,
    email          VARCHAR(150)  NULL,
    direccion      VARCHAR(200)  NULL,
    fechaRegistro  DATETIME2     NOT NULL CONSTRAINT DF_cliente_fecha  DEFAULT SYSDATETIME(),
    activo         BIT           NOT NULL CONSTRAINT DF_cliente_activo DEFAULT 1
);
GO

IF OBJECT_ID('dbo.vendedor', 'U') IS NULL
CREATE TABLE dbo.vendedor (
    idVendedor  INT IDENTITY(1,1) CONSTRAINT PK_vendedor PRIMARY KEY,
    nombres     VARCHAR(100)  NOT NULL,
    apellidos   VARCHAR(100)  NOT NULL,
    usuario     VARCHAR(50)   NULL,
    telefono    VARCHAR(20)   NULL,
    activo      BIT           NOT NULL CONSTRAINT DF_vendedor_activo DEFAULT 1
);
GO

-- Cuentas para iniciar sesión. La contraseña se guarda cifrada (PBKDF2) desde Java.
IF OBJECT_ID('dbo.usuario', 'U') IS NULL
CREATE TABLE dbo.usuario (
    idUsuario         INT IDENTITY(1,1) CONSTRAINT PK_usuario PRIMARY KEY,
    usuario           VARCHAR(50)   NOT NULL CONSTRAINT UQ_usuario_usuario UNIQUE,
    email             VARCHAR(150)  NOT NULL CONSTRAINT UQ_usuario_email   UNIQUE,
    passwordHash      VARCHAR(255)  NOT NULL,
    rol               VARCHAR(20)   NOT NULL CONSTRAINT CK_usuario_rol CHECK (rol IN ('CLIENTE', 'VENDEDOR')),
    idCliente         INT NULL CONSTRAINT FK_usuario_cliente  REFERENCES dbo.cliente(idCliente),
    idVendedor        INT NULL CONSTRAINT FK_usuario_vendedor REFERENCES dbo.vendedor(idVendedor),
    activo            BIT           NOT NULL CONSTRAINT DF_usuario_activo   DEFAULT 1,
    intentosFallidos  INT           NOT NULL CONSTRAINT DF_usuario_intentos DEFAULT 0,
    bloqueadoHasta    DATETIME2     NULL,
    ultimoAcceso      DATETIME2     NULL
);
GO


-- ---------------------------------------------------------------------
-- 2. INVENTARIO (producto + un detalle por tipo)
-- ---------------------------------------------------------------------

IF OBJECT_ID('dbo.producto', 'U') IS NULL
CREATE TABLE dbo.producto (
    id_producto   INT IDENTITY(1,1) CONSTRAINT PK_producto PRIMARY KEY,
    codigo        VARCHAR(30)    NOT NULL CONSTRAINT UQ_producto_codigo UNIQUE,
    tipo          VARCHAR(20)    NOT NULL CONSTRAINT CK_producto_tipo CHECK (tipo IN ('ARO', 'LUNA', 'ACCESORIO')),
    nombre        VARCHAR(100)   NOT NULL,
    marca         VARCHAR(60)    NULL,
    precio        DECIMAL(10,2)  NOT NULL CONSTRAINT CK_producto_precio CHECK (precio >= 0),
    stock         INT            NOT NULL CONSTRAINT DF_producto_stock     DEFAULT 0,
    stock_minimo  INT            NOT NULL CONSTRAINT DF_producto_stockmin  DEFAULT 0,
    activo        BIT            NOT NULL CONSTRAINT DF_producto_activo    DEFAULT 1
);
GO

IF OBJECT_ID('dbo.aro', 'U') IS NULL
CREATE TABLE dbo.aro (
    id_producto  INT CONSTRAINT PK_aro PRIMARY KEY
                     CONSTRAINT FK_aro_producto REFERENCES dbo.producto(id_producto),
    material     VARCHAR(50)   NULL,
    forma        VARCHAR(50)   NULL,
    color        VARCHAR(50)   NULL,
    genero       VARCHAR(20)   NULL,
    medidas      VARCHAR(50)   NULL,
    imagen_url   VARCHAR(500)  NULL
);
GO

IF OBJECT_ID('dbo.luna', 'U') IS NULL
CREATE TABLE dbo.luna (
    id_producto        INT CONSTRAINT PK_luna PRIMARY KEY
                           CONSTRAINT FK_luna_producto REFERENCES dbo.producto(id_producto),
    tipo_vision        VARCHAR(50)   NULL,
    material           VARCHAR(50)   NULL,
    indice_refraccion  DECIMAL(4,2)  NULL,
    tratamiento        VARCHAR(100)  NULL
);
GO

IF OBJECT_ID('dbo.accesorio', 'U') IS NULL
CREATE TABLE dbo.accesorio (
    id_producto  INT CONSTRAINT PK_accesorio PRIMARY KEY
                     CONSTRAINT FK_accesorio_producto REFERENCES dbo.producto(id_producto),
    categoria    VARCHAR(50)   NULL,
    descripcion  VARCHAR(255)  NULL
);
GO


-- ---------------------------------------------------------------------
-- 3. MEDICIONES Y VENTAS
-- ---------------------------------------------------------------------

-- Receta / medición de la vista (OD = ojo derecho, OI = ojo izquierdo)
IF OBJECT_ID('dbo.medicion', 'U') IS NULL
CREATE TABLE dbo.medicion (
    id_medicion    INT IDENTITY(1,1) CONSTRAINT PK_medicion PRIMARY KEY,
    id_cliente     INT NOT NULL CONSTRAINT FK_medicion_cliente REFERENCES dbo.cliente(idCliente),
    fecha          DATETIME2     NOT NULL CONSTRAINT DF_medicion_fecha DEFAULT SYSDATETIME(),
    od_esfera      DECIMAL(5,2)  NULL,
    od_cilindro    DECIMAL(5,2)  NULL,
    od_eje         INT           NULL CONSTRAINT CK_medicion_od_eje CHECK (od_eje BETWEEN 0 AND 180),
    oi_esfera      DECIMAL(5,2)  NULL,
    oi_cilindro    DECIMAL(5,2)  NULL,
    oi_eje         INT           NULL CONSTRAINT CK_medicion_oi_eje CHECK (oi_eje BETWEEN 0 AND 180),
    adicion        DECIMAL(5,2)  NULL,
    dip            DECIMAL(5,2)  NULL,   -- distancia interpupilar (mm)
    observaciones  VARCHAR(500)  NULL
);
GO

IF OBJECT_ID('dbo.venta', 'U') IS NULL
CREATE TABLE dbo.venta (
    id_venta      INT IDENTITY(1,1) CONSTRAINT PK_venta PRIMARY KEY,
    numero_venta  VARCHAR(30)    NOT NULL CONSTRAINT UQ_venta_numero UNIQUE,
    id_cliente    INT NOT NULL   CONSTRAINT FK_venta_cliente  REFERENCES dbo.cliente(idCliente),
    id_vendedor   INT NOT NULL   CONSTRAINT FK_venta_vendedor REFERENCES dbo.vendedor(idVendedor),
    id_medicion   INT NULL       CONSTRAINT FK_venta_medicion REFERENCES dbo.medicion(id_medicion),
    fecha         DATETIME2      NOT NULL CONSTRAINT DF_venta_fecha    DEFAULT SYSDATETIME(),
    subtotal      DECIMAL(10,2)  NOT NULL CONSTRAINT DF_venta_subtotal DEFAULT 0,
    impuesto      DECIMAL(10,2)  NOT NULL CONSTRAINT DF_venta_impuesto DEFAULT 0,
    total         DECIMAL(10,2)  NOT NULL CONSTRAINT DF_venta_total    DEFAULT 0,
    metodo_pago   VARCHAR(20)    NOT NULL CONSTRAINT DF_venta_metodo   DEFAULT 'EFECTIVO',
    estado        VARCHAR(20)    NOT NULL CONSTRAINT DF_venta_estado   DEFAULT 'COMPLETADA'
);
GO

IF OBJECT_ID('dbo.detalle_venta', 'U') IS NULL
CREATE TABLE dbo.detalle_venta (
    id_detalle       INT IDENTITY(1,1) CONSTRAINT PK_detalle_venta PRIMARY KEY,
    id_venta         INT NOT NULL CONSTRAINT FK_detalle_venta    REFERENCES dbo.venta(id_venta),
    id_producto      INT NOT NULL CONSTRAINT FK_detalle_producto REFERENCES dbo.producto(id_producto),
    cantidad         INT NOT NULL CONSTRAINT CK_detalle_cantidad CHECK (cantidad > 0),
    precio_unitario  DECIMAL(10,2) NOT NULL CONSTRAINT CK_detalle_precio CHECK (precio_unitario >= 0)
);
GO


-- ---------------------------------------------------------------------
-- 4. DATOS DE EJEMPLO (solo si las tablas están vacías)
-- ---------------------------------------------------------------------

IF NOT EXISTS (SELECT 1 FROM dbo.vendedor)
    INSERT INTO dbo.vendedor (nombres, apellidos, usuario, telefono)
    VALUES ('Mairon', 'Vendedor', 'mairon', '9999-0000');

IF NOT EXISTS (SELECT 1 FROM dbo.cliente)
    INSERT INTO dbo.cliente (dni, nombres, apellidos, telefono, email, direccion)
    VALUES ('0801-1990-00001', 'Juan', 'Pérez', '9999-1111', 'juan@mail.com', 'Tegucigalpa');
GO

IF NOT EXISTS (SELECT 1 FROM dbo.producto)
BEGIN
    DECLARE @id INT;

    INSERT INTO dbo.producto (codigo, tipo, nombre, marca, precio, stock, stock_minimo)
    VALUES ('ARO-001', 'ARO', 'Aro clásico metálico', 'Ray-Ban', 2500.00, 10, 3);
    SET @id = SCOPE_IDENTITY();
    INSERT INTO dbo.aro (id_producto, material, forma, color, genero, medidas, imagen_url)
    VALUES (@id, 'Metal', 'Redondo', 'Dorado', 'Unisex', '50-21-145', NULL);

    INSERT INTO dbo.producto (codigo, tipo, nombre, marca, precio, stock, stock_minimo)
    VALUES ('ARO-002', 'ARO', 'Aro acetato rectangular', 'Oakley', 1800.00, 2, 3);
    SET @id = SCOPE_IDENTITY();
    INSERT INTO dbo.aro (id_producto, material, forma, color, genero, medidas, imagen_url)
    VALUES (@id, 'Acetato', 'Rectangular', 'Negro', 'Masculino', '54-18-140', NULL);

    INSERT INTO dbo.producto (codigo, tipo, nombre, marca, precio, stock, stock_minimo)
    VALUES ('LUN-001', 'LUNA', 'Luna monofocal antirreflejo', 'Essilor', 1200.00, 20, 5);
    SET @id = SCOPE_IDENTITY();
    INSERT INTO dbo.luna (id_producto, tipo_vision, material, indice_refraccion, tratamiento)
    VALUES (@id, 'Monofocal', 'Policarbonato', 1.59, 'Antirreflejo');

    INSERT INTO dbo.producto (codigo, tipo, nombre, marca, precio, stock, stock_minimo)
    VALUES ('LUN-002', 'LUNA', 'Luna progresiva filtro azul', 'Zeiss', 3500.00, 8, 3);
    SET @id = SCOPE_IDENTITY();
    INSERT INTO dbo.luna (id_producto, tipo_vision, material, indice_refraccion, tratamiento)
    VALUES (@id, 'Progresivo', 'Orgánico', 1.67, 'Filtro luz azul');

    INSERT INTO dbo.producto (codigo, tipo, nombre, marca, precio, stock, stock_minimo)
    VALUES ('ACC-001', 'ACCESORIO', 'Estuche rígido', 'Genérico', 150.00, 30, 10);
    SET @id = SCOPE_IDENTITY();
    INSERT INTO dbo.accesorio (id_producto, categoria, descripcion)
    VALUES (@id, 'Estuche', 'Estuche rígido con cierre');

    INSERT INTO dbo.producto (codigo, tipo, nombre, marca, precio, stock, stock_minimo)
    VALUES ('ACC-002', 'ACCESORIO', 'Paño de microfibra', 'Genérico', 50.00, 50, 15);
    SET @id = SCOPE_IDENTITY();
    INSERT INTO dbo.accesorio (id_producto, categoria, descripcion)
    VALUES (@id, 'Limpieza', 'Paño de microfibra 15x15 cm');
END
GO

-- Las cuentas de usuario NO se insertan aquí (la contraseña se cifra en Java).
-- Después de ejecutar este script, ejecute Util/CrearUsuariosDemo.java (Run File):
--   mairon / Vendedor123  (VENDEDOR)
--   juan   / Cliente123   (CLIENTE)
