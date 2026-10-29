/* =====================================================================
   Sistema Optica - Script de base de datos para SQL Server
   Base de datos: AvanzadaII (la misma que usa config.properties)

   NOTA: Si usas Azure SQL Database (avanzadaii.database.windows.net),
   la base de datos ya existe: NO ejecutes la seccion "CREAR BASE DE DATOS"
   y conectate directamente a AvanzadaII para ejecutar el resto.
   ===================================================================== */

-- ============ CREAR BASE DE DATOS (solo SQL Server local) ============
IF DB_ID('AvanzadaII') IS NULL
    CREATE DATABASE AvanzadaII;
GO
USE AvanzadaII;
GO

-- ============ LIMPIEZA (orden inverso por llaves foraneas) ===========
IF OBJECT_ID('dbo.Usuario',      'U') IS NOT NULL DROP TABLE dbo.Usuario;
IF OBJECT_ID('dbo.DetalleVenta', 'U') IS NOT NULL DROP TABLE dbo.DetalleVenta;
IF OBJECT_ID('dbo.Venta',        'U') IS NOT NULL DROP TABLE dbo.Venta;
IF OBJECT_ID('dbo.Medicion',     'U') IS NOT NULL DROP TABLE dbo.Medicion;
IF OBJECT_ID('dbo.Accesorio',    'U') IS NOT NULL DROP TABLE dbo.Accesorio;
IF OBJECT_ID('dbo.Luna',         'U') IS NOT NULL DROP TABLE dbo.Luna;
IF OBJECT_ID('dbo.Aro',          'U') IS NOT NULL DROP TABLE dbo.Aro;
IF OBJECT_ID('dbo.Producto',     'U') IS NOT NULL DROP TABLE dbo.Producto;
IF OBJECT_ID('dbo.Vendedor',     'U') IS NOT NULL DROP TABLE dbo.Vendedor;
IF OBJECT_ID('dbo.Cliente',      'U') IS NOT NULL DROP TABLE dbo.Cliente;
GO

-- ============ CLIENTE ================================================
-- (Inferida: Venta.getCliente().getNombreCompleto())
CREATE TABLE dbo.Cliente (
    idCliente   INT IDENTITY(1,1) PRIMARY KEY,
    dni         VARCHAR(15)   NULL,
    nombres     NVARCHAR(100) NOT NULL,
    apellidos   NVARCHAR(100) NOT NULL,
    telefono    VARCHAR(20)   NULL,
    email       NVARCHAR(120) NULL,
    direccion   NVARCHAR(200) NULL,
    fechaRegistro DATETIME2   NOT NULL DEFAULT SYSDATETIME(),
    activo      BIT           NOT NULL DEFAULT 1
);
CREATE UNIQUE INDEX UX_Cliente_dni ON dbo.Cliente(dni) WHERE dni IS NOT NULL;
GO

-- ============ VENDEDOR ===============================================
CREATE TABLE dbo.Vendedor (
    idVendedor  INT IDENTITY(1,1) PRIMARY KEY,
    nombres     NVARCHAR(100) NOT NULL,
    apellidos   NVARCHAR(100) NOT NULL,
    usuario     VARCHAR(50)   NOT NULL UNIQUE,
    telefono    VARCHAR(20)   NULL,
    activo      BIT           NOT NULL DEFAULT 1
);
GO

-- ============ USUARIO (seguridad y acceso) ===========================
-- passwordHash guarda "iteraciones:salt:hash" (PBKDF2-HMAC-SHA256, Base64).
-- NUNCA se guarda la contrasena en texto plano. Los usuarios demo los crea
-- la clase util.CrearUsuariosDemo (el hash se genera en Java).
-- Cada usuario esta ligado a UN Cliente o a UN Vendedor segun su rol.
CREATE TABLE dbo.Usuario (
    idUsuario        INT IDENTITY(1,1) PRIMARY KEY,
    usuario          VARCHAR(50)   NOT NULL,
    email            NVARCHAR(120) NOT NULL,
    passwordHash     VARCHAR(300)  NOT NULL,
    rol              VARCHAR(10)   NOT NULL
                     CONSTRAINT CK_Usuario_rol CHECK (rol IN ('CLIENTE','VENDEDOR')),
    idCliente        INT NULL,
    idVendedor       INT NULL,
    activo           BIT           NOT NULL DEFAULT 1,
    intentosFallidos INT           NOT NULL DEFAULT 0,
    bloqueadoHasta   DATETIME2     NULL,
    ultimoAcceso     DATETIME2     NULL,
    fechaCreacion    DATETIME2     NOT NULL DEFAULT SYSDATETIME(),
    CONSTRAINT FK_Usuario_Cliente  FOREIGN KEY (idCliente)  REFERENCES dbo.Cliente(idCliente),
    CONSTRAINT FK_Usuario_Vendedor FOREIGN KEY (idVendedor) REFERENCES dbo.Vendedor(idVendedor),
    -- CLIENTE debe tener idCliente (y no idVendedor); VENDEDOR al reves
    CONSTRAINT CK_Usuario_rol_ref CHECK (
        (rol = 'CLIENTE'  AND idCliente  IS NOT NULL AND idVendedor IS NULL) OR
        (rol = 'VENDEDOR' AND idVendedor IS NOT NULL AND idCliente  IS NULL))
);
CREATE UNIQUE INDEX UX_Usuario_usuario ON dbo.Usuario(usuario);
CREATE UNIQUE INDEX UX_Usuario_email   ON dbo.Usuario(email);
GO

-- ============ PRODUCTO (clase padre) =================================
-- Producto.tipo: ARO / LUNA / ACCESORIO
CREATE TABLE dbo.Producto (
    idProducto  INT IDENTITY(1,1) PRIMARY KEY,
    tipo        VARCHAR(10)   NOT NULL
                CONSTRAINT CK_Producto_tipo CHECK (tipo IN ('ARO','LUNA','ACCESORIO')),
    codigo      VARCHAR(30)   NOT NULL UNIQUE,
    nombre      NVARCHAR(120) NOT NULL,
    marca       NVARCHAR(80)  NULL,
    precio      DECIMAL(10,2) NOT NULL CONSTRAINT CK_Producto_precio CHECK (precio >= 0),
    stock       INT           NOT NULL DEFAULT 0 CONSTRAINT CK_Producto_stock CHECK (stock >= 0),
    stockMinimo INT           NOT NULL DEFAULT 0 CONSTRAINT CK_Producto_stockMin CHECK (stockMinimo >= 0),
    activo      BIT           NOT NULL DEFAULT 1
);
CREATE INDEX IX_Producto_nombre ON dbo.Producto(nombre);
CREATE INDEX IX_Producto_tipo   ON dbo.Producto(tipo);
GO

-- ============ ARO (hijo de Producto) =================================
CREATE TABLE dbo.Aro (
    idProducto  INT PRIMARY KEY,
    material    NVARCHAR(50)  NULL,
    forma       NVARCHAR(50)  NULL,
    color       NVARCHAR(50)  NULL,
    genero      NVARCHAR(20)  NULL,   -- Hombre / Mujer / Unisex / Niño
    medidas     NVARCHAR(50)  NULL,   -- ej. 52-18-140
    imagenUrl   NVARCHAR(300) NULL,
    CONSTRAINT FK_Aro_Producto FOREIGN KEY (idProducto)
        REFERENCES dbo.Producto(idProducto) ON DELETE CASCADE
);
GO

-- ============ LUNA (hijo de Producto) ================================
CREATE TABLE dbo.Luna (
    idProducto       INT PRIMARY KEY,
    tipoVision       NVARCHAR(50)  NULL,   -- Monofocal / Bifocal / Progresivo
    material         NVARCHAR(50)  NULL,   -- CR-39 / Policarbonato / Cristal
    indiceRefraccion DECIMAL(3,2)  NULL,   -- ej. 1.56, 1.61, 1.67
    tratamiento      NVARCHAR(100) NULL,   -- Antireflejo / Blue Cut / Fotocromatico
    CONSTRAINT FK_Luna_Producto FOREIGN KEY (idProducto)
        REFERENCES dbo.Producto(idProducto) ON DELETE CASCADE
);
GO

-- ============ ACCESORIO (hijo de Producto) ===========================
CREATE TABLE dbo.Accesorio (
    idProducto  INT PRIMARY KEY,
    categoria   NVARCHAR(80)  NULL,   -- Estuche / Liquido / Cordon / Paño
    descripcion NVARCHAR(300) NULL,
    CONSTRAINT FK_Accesorio_Producto FOREIGN KEY (idProducto)
        REFERENCES dbo.Producto(idProducto) ON DELETE CASCADE
);
GO

-- ============ MEDICION (examen visual del cliente) ===================
-- (Inferida: Venta.getMedicion())
CREATE TABLE dbo.Medicion (
    idMedicion  INT IDENTITY(1,1) PRIMARY KEY,
    idCliente   INT NOT NULL,
    fecha       DATETIME2     NOT NULL DEFAULT SYSDATETIME(),
    -- Ojo derecho
    odEsfera    DECIMAL(4,2)  NULL,
    odCilindro  DECIMAL(4,2)  NULL,
    odEje       SMALLINT      NULL CHECK (odEje BETWEEN 0 AND 180),
    -- Ojo izquierdo
    oiEsfera    DECIMAL(4,2)  NULL,
    oiCilindro  DECIMAL(4,2)  NULL,
    oiEje       SMALLINT      NULL CHECK (oiEje BETWEEN 0 AND 180),
    adicion     DECIMAL(4,2)  NULL,
    dip         DECIMAL(4,1)  NULL,   -- distancia interpupilar (mm)
    observaciones NVARCHAR(300) NULL,
    CONSTRAINT FK_Medicion_Cliente FOREIGN KEY (idCliente)
        REFERENCES dbo.Cliente(idCliente)
);
GO

-- ============ VENTA ==================================================
CREATE TABLE dbo.Venta (
    idVenta     INT IDENTITY(1,1) PRIMARY KEY,
    numeroVenta VARCHAR(20)   NOT NULL UNIQUE,
    idCliente   INT           NOT NULL,
    idVendedor  INT           NOT NULL,
    idMedicion  INT           NULL,
    fecha       DATETIME2     NOT NULL DEFAULT SYSDATETIME(),
    subtotal    DECIMAL(10,2) NOT NULL DEFAULT 0,
    impuesto    DECIMAL(10,2) NOT NULL DEFAULT 0,
    total       DECIMAL(10,2) NOT NULL DEFAULT 0,
    metodoPago  VARCHAR(20)   NOT NULL DEFAULT 'EFECTIVO'
                CONSTRAINT CK_Venta_metodoPago CHECK (metodoPago IN ('EFECTIVO','TARJETA','TRANSFERENCIA','YAPE','PLIN','OTRO')),
    estado      VARCHAR(20)   NOT NULL DEFAULT 'COMPLETADA'
                CONSTRAINT CK_Venta_estado CHECK (estado IN ('PENDIENTE','COMPLETADA','ANULADA')),
    CONSTRAINT FK_Venta_Cliente  FOREIGN KEY (idCliente)  REFERENCES dbo.Cliente(idCliente),
    CONSTRAINT FK_Venta_Vendedor FOREIGN KEY (idVendedor) REFERENCES dbo.Vendedor(idVendedor),
    CONSTRAINT FK_Venta_Medicion FOREIGN KEY (idMedicion) REFERENCES dbo.Medicion(idMedicion)
);
CREATE INDEX IX_Venta_fecha   ON dbo.Venta(fecha);
CREATE INDEX IX_Venta_cliente ON dbo.Venta(idCliente);
GO

-- ============ DETALLE VENTA ==========================================
CREATE TABLE dbo.DetalleVenta (
    idDetalle      INT IDENTITY(1,1) PRIMARY KEY,
    idVenta        INT           NOT NULL,
    idProducto     INT           NOT NULL,
    cantidad       INT           NOT NULL CONSTRAINT CK_Detalle_cantidad CHECK (cantidad > 0),
    precioUnitario DECIMAL(10,2) NOT NULL CONSTRAINT CK_Detalle_precio CHECK (precioUnitario >= 0),
    subtotalLinea  AS (cantidad * precioUnitario) PERSISTED,  -- getSubtotalLinea()
    CONSTRAINT FK_Detalle_Venta    FOREIGN KEY (idVenta)    REFERENCES dbo.Venta(idVenta) ON DELETE CASCADE,
    CONSTRAINT FK_Detalle_Producto FOREIGN KEY (idProducto) REFERENCES dbo.Producto(idProducto)
);
CREATE INDEX IX_Detalle_venta ON dbo.DetalleVenta(idVenta);
GO

-- ============ VISTA: PRODUCTOS CON STOCK BAJO ========================
-- (equivale a AroDAO.listarStockBajo / Producto.isStockBajo)
CREATE OR ALTER VIEW dbo.vw_StockBajo AS
SELECT idProducto, tipo, codigo, nombre, marca, stock, stockMinimo
FROM dbo.Producto
WHERE activo = 1 AND stock <= stockMinimo;
GO

-- ============ DATOS DE EJEMPLO =======================================
INSERT INTO dbo.Cliente (dni, nombres, apellidos, telefono, email)
VALUES ('12345678', N'Juan',  N'Pérez Gómez',  '987654321', 'juan@mail.com'),
       ('87654321', N'María', N'López Ruiz',   '912345678', 'maria@mail.com');

INSERT INTO dbo.Vendedor (nombres, apellidos, usuario, telefono)
VALUES (N'Mairon', N'Vendedor', 'mairon', '999888777');

-- Aros
INSERT INTO dbo.Producto (tipo, codigo, nombre, marca, precio, stock, stockMinimo)
VALUES ('ARO','ARO-001',N'Aro Clásico Acetato','Ray-Ban',250.00,10,3),
       ('ARO','ARO-002',N'Aro Metálico Slim','Oakley',320.00,2,3);
INSERT INTO dbo.Aro (idProducto, material, forma, color, genero, medidas)
VALUES ((SELECT idProducto FROM dbo.Producto WHERE codigo='ARO-001'), N'Acetato', N'Rectangular', N'Negro',   N'Unisex', '52-18-140'),
       ((SELECT idProducto FROM dbo.Producto WHERE codigo='ARO-002'), N'Metal',   N'Redondo',     N'Plateado',N'Hombre', '50-20-145');

-- Lunas
INSERT INTO dbo.Producto (tipo, codigo, nombre, marca, precio, stock, stockMinimo)
VALUES ('LUNA','LUN-001',N'Luna Monofocal Blue Cut','Essilor',120.00,50,10),
       ('LUNA','LUN-002',N'Luna Progresiva Fotocromática','Zeiss',480.00,15,5);
INSERT INTO dbo.Luna (idProducto, tipoVision, material, indiceRefraccion, tratamiento)
VALUES ((SELECT idProducto FROM dbo.Producto WHERE codigo='LUN-001'), N'Monofocal',  N'Policarbonato', 1.59, N'Blue Cut'),
       ((SELECT idProducto FROM dbo.Producto WHERE codigo='LUN-002'), N'Progresivo', N'CR-39',         1.61, N'Fotocromático');

-- Accesorios
INSERT INTO dbo.Producto (tipo, codigo, nombre, marca, precio, stock, stockMinimo)
VALUES ('ACCESORIO','ACC-001',N'Estuche Rígido','Generic',15.00,40,10),
       ('ACCESORIO','ACC-002',N'Líquido Limpiador 60ml','Optic Clean',8.50,60,15);
INSERT INTO dbo.Accesorio (idProducto, categoria, descripcion)
VALUES ((SELECT idProducto FROM dbo.Producto WHERE codigo='ACC-001'), N'Estuche',  N'Estuche rígido para lentes'),
       ((SELECT idProducto FROM dbo.Producto WHERE codigo='ACC-002'), N'Limpieza', N'Líquido limpiador antiempañante');

-- Medicion + Venta + Detalle de ejemplo
INSERT INTO dbo.Medicion (idCliente, odEsfera, odCilindro, odEje, oiEsfera, oiCilindro, oiEje, dip)
VALUES (1, -1.50, -0.50, 90, -1.25, -0.75, 85, 62.0);

INSERT INTO dbo.Venta (numeroVenta, idCliente, idVendedor, idMedicion, subtotal, impuesto, total, metodoPago, estado)
VALUES ('V-0001', 1, 1, 1, 370.00, 66.60, 436.60, 'EFECTIVO', 'COMPLETADA');

INSERT INTO dbo.DetalleVenta (idVenta, idProducto, cantidad, precioUnitario)
SELECT 1, idProducto, 1, precio FROM dbo.Producto WHERE codigo IN ('ARO-001','LUN-001');
GO

-- Verificacion
SELECT * FROM dbo.vw_StockBajo;
GO
