
-- Tabla: estudiantes
CREATE TABLE estudiantes (
    id_estudiante         SERIAL PRIMARY KEY,
    dni                   VARCHAR(8)     NOT NULL UNIQUE,
    nombre                VARCHAR(100)   NOT NULL,
    apellido              VARCHAR(100)   NOT NULL,
    edad                  INT            NOT NULL CHECK (edad BETWEEN 18 AND 50),
    telefono              VARCHAR(9),
    correo                VARCHAR(150),
    promedio_notas        DECIMAL(4,2)   DEFAULT 11.0,
    porcentaje_asistencia DECIMAL(5,2)   DEFAULT 100.0,
    estado_universidad    VARCHAR(20)    DEFAULT 'ACTIVO',
    ciclo_actual          INT            DEFAULT 1 CHECK (ciclo_actual >= 1 AND ciclo_actual <= 10),
    fecha_registro        DATE           DEFAULT CURRENT_DATE
);

-- Tabla: prestamos (Incluye campos de mora y soporte QR)
CREATE TABLE prestamos (
    id_prestamo      SERIAL PRIMARY KEY,
    id_estudiante    INT            NOT NULL REFERENCES estudiantes(id_estudiante) ON DELETE CASCADE,
    monto            DECIMAL(10,2)  NOT NULL,
    cuotas           INT            NOT NULL CHECK (cuotas BETWEEN 1 AND 24),
    tasa_interes     DECIMAL(5,2)   DEFAULT 10.00,
    saldo            DECIMAL(10,2)  NOT NULL,
    estado           VARCHAR(20)    DEFAULT 'Activo',
    fecha_inicio     DATE           DEFAULT CURRENT_DATE,
    dias_mora        INT            DEFAULT 0 NOT NULL,
    interes_mora     DECIMAL(10,2)  DEFAULT 0.00 NOT NULL,
    codigo_qr        BYTEA
);

-- Tabla: pagos
CREATE TABLE pagos (
    id_pago          SERIAL PRIMARY KEY,
    id_prestamo      INT            NOT NULL REFERENCES prestamos(id_prestamo) ON DELETE CASCADE,
    monto            DECIMAL(10,2)  NOT NULL CHECK (monto > 0),
    cuota_numero     INT            NOT NULL,
    metodo_pago      VARCHAR(30)    DEFAULT 'EFECTIVO',
    fecha_pago       DATE           DEFAULT CURRENT_DATE,
    estado           VARCHAR(20)    DEFAULT 'Completado'
);

-- Tabla: solicitudes_prestamo
CREATE TABLE solicitudes_prestamo (
    id_solicitud          SERIAL PRIMARY KEY,
    id_estudiante         INT            NOT NULL REFERENCES estudiantes(id_estudiante) ON DELETE CASCADE,
    monto_solicitado      DECIMAL(10,2)  NOT NULL,
    cuotas_solicitadas    INT            NOT NULL CHECK (cuotas_solicitadas BETWEEN 1 AND 24),
    fecha_solicitud       DATE           DEFAULT CURRENT_DATE,
    estado_solicitud      VARCHAR(20)    DEFAULT 'PENDIENTE',
    recomendacion_sistema VARCHAR(30)    DEFAULT 'SIN EVALUAR',
    observacion           VARCHAR(255),
    atendido_por          VARCHAR(50)    DEFAULT 'ASISTENTE'
);

-- Tabla: boletas_digitales (Repositorio QR para app/interfaz)
CREATE TABLE boletas_digitales (
    id_boleta       SERIAL PRIMARY KEY,
    id_prestamo     INT            REFERENCES prestamos(id_prestamo) ON DELETE CASCADE,
    id_estudiante   INT            REFERENCES estudiantes(id_estudiante) ON DELETE CASCADE,
    fecha_emision   TIMESTAMP      DEFAULT CURRENT_TIMESTAMP,
    qr_binario      BYTEA,
    monto_total     NUMERIC(10,2)
);


-- inserccion de datos

INSERT INTO estudiantes (dni, nombre, apellido, edad, telefono, correo, promedio_notas, porcentaje_asistencia, estado_universidad, ciclo_actual)
VALUES
    ('12345678', 'DIEGO',      'HUACHACA',    21, '987654321', 'diego.huachaca@utp.edu.pe',   16.5, 92.0, 'ACTIVO',   5),
    ('87654321', 'DIANA',      'APOLINARIO',  20, '912345678', 'diana.apolinario@utp.edu.pe', 14.0, 85.0, 'ACTIVO',   4),
    ('11223344', 'EDUARDO',    'CAMAC',       22, '956789012', 'eduardo.camac@utp.edu.pe',    10.0, 65.0, 'ALERTA',   6),
    ('99887766', 'MARIA',      'QUISPE',      19, '945678901', 'maria.quispe@utp.edu.pe',     18.0, 98.0, 'ACTIVO',   3),
    ('55443322', 'CARLOS',     'RAMOS',       23, '934567890', 'carlos.ramos@utp.edu.pe',      8.5, 55.0, 'RETIRADO', 7),
    ('33221100', 'LUCIA',      'TORRES',      21, '923456789', 'lucia.torres@utp.edu.pe',     15.5, 88.0, 'ACTIVO',   5),
    ('44332211', 'JOSE',       'VILLANUEVA',  24, '967890123', 'jose.villanueva@utp.edu.pe',  13.0, 79.0, 'ACTIVO',   8),
    ('66554433', 'ANDREA',     'FLORES',      20, '978901234', 'andrea.flores@utp.edu.pe',    17.5, 95.0, 'ACTIVO',   4),
    ('77665544', 'ROBERTO',    'MENDOZA',     25, '989012345', 'roberto.mendoza@utp.edu.pe',   9.5, 60.0, 'ALERTA',   9),
    ('88776655', 'VALERIA',    'GUTIERREZ',   19, '990123456', 'valeria.gutierrez@utp.edu.pe',19.0, 99.0, 'ACTIVO',   2),
    ('22110099', 'MIGUEL',     'SALCEDO',     22, '901234567', 'miguel.salcedo@utp.edu.pe',   11.5, 72.0, 'ACTIVO',   6),
    ('10293847', 'FERNANDA',   'PAREDES',     21, '913579246', 'fernanda.paredes@utp.edu.pe', 12.5, 81.0, 'ACTIVO',   5),
    ('71234569', 'ALEJANDRO',  'CASTRO',      26, '922334455', 'alejandro.castro@utp.edu.pe', 14.5, 88.0, 'ACTIVO',  10),
    ('72345670', 'SOFIA',      'ROJAS',       18, '933445566', 'sofia.rojas@utp.edu.pe',      16.0, 96.0, 'ACTIVO',   1),
    ('73456781', 'GABRIEL',    'VARGAS',      20, '944556677', 'gabriel.vargas@utp.edu.pe',   10.5, 68.0, 'ALERTA',   3),
    ('74567892', 'CAMILA',     'ESPINOZA',    21, '955667788', 'camila.espinoza@utp.edu.pe',  17.0, 94.0, 'ACTIVO',   7),
    ('75678903', 'LEONARDO',   'HUAMAN',      24, '966778899', 'leonardo.huaman@utp.edu.pe',   9.0, 50.0, 'RETIRADO', 8),
    ('76789014', 'ISABELLA',   'CONDORI',     22, '977889900', 'isabella.condori@utp.edu.pe', 15.0, 90.0, 'ACTIVO',   6),
    ('77890125', 'SEBASTIAN',  'MAMANI',      23, '988990011', 'sebastian.mamani@utp.edu.pe', 13.5, 82.0, 'ACTIVO',   7),
    ('78901236', 'DANIELA',    'CHAVEZ',      19, '999001122', 'daniela.chavez@utp.edu.pe',   18.5, 97.0, 'ACTIVO',   2);

-- Insertando Prestamos
INSERT INTO prestamos (id_estudiante, monto, cuotas, tasa_interes, saldo, estado, dias_mora, interes_mora)
VALUES
    (1,  5000.00, 12, 10.00, 5500.00, 'Activo',  0, 0.00),   
    (1,  1000.00,  3, 10.00, 1100.00, 'Activo',  0, 0.00),   
    (2,  2000.00,  6, 10.00, 2200.00, 'Activo',  5, 15.50),  
    (3,  3000.00, 12, 10.00, 3300.00, 'Activo', 15, 50.00),  -- Eduardo Camac
    (4,  4000.00, 10, 10.00, 4400.00, 'Activo',  0, 0.00),   
    (5,  6000.00, 18, 10.00, 6600.00, 'Activo', 45, 180.00), -- Carlos Ramos
    (6,  3000.00,  8, 10.00, 3300.00, 'Activo',  0, 0.00),   
    (7,  1500.00,  6, 10.00, 1650.00, 'Activo', 12, 45.00),  
    (8,  5000.00, 24, 10.00, 5500.00, 'Activo',  0, 0.00),   
    (9,  2000.00,  8, 10.00, 2200.00, 'Activo',  5, 20.00),  -- Roberto Mendoza
    (10, 5000.00, 12, 10.00,    0.00, 'Pagado',  0, 0.00),   
    (11, 2500.00,  6, 10.00, 2750.00, 'Activo',  0, 0.00),   
    (13, 3500.00, 12, 10.00, 3850.00, 'Activo',  0, 0.00),
    (16, 1500.00,  3, 10.00,    0.00, 'Pagado',  0, 0.00),
    (18, 2000.00,  6, 10.00, 2200.00, 'Activo',  0, 0.00);

-- Insertando Pagos (Historial Transaccional)
INSERT INTO pagos (id_prestamo, monto, cuota_numero, metodo_pago)
VALUES
    (1, 458.33, 1, 'YAPE'),
    (1, 458.33, 2, 'TRANSFERENCIA BCP'),
    (1, 458.33, 3, 'YAPE'),
    (2, 366.67, 1, 'YAPE'),
    (3, 366.67, 1, 'PLIN'),
    (3, 366.67, 2, 'PLIN'),
    (4, 275.00, 1, 'TRANSFERENCIA BCP'), 
    (5, 440.00, 1, 'TRANSFERENCIA BCP'),
    (5, 440.00, 2, 'PAGOEFECTIVO'),
    (6, 366.67, 1, 'TRANSFERENCIA BCP'), 
    (7, 412.50, 1, 'YAPE'),
    (7, 412.50, 2, 'PAGOEFECTIVO'),
    (8, 275.00, 1, 'YAPE'),
    (9, 229.17, 1, 'PLIN'),
    (10, 275.00, 1, 'TRANSFERENCIA BCP'),
    (11, 458.33, 1, 'YAPE'), (11, 458.33, 2, 'YAPE'), (11, 458.33, 3, 'YAPE'),
    (11, 458.33, 4, 'PLIN'), (11, 458.33, 5, 'PLIN'), (11, 458.33, 6, 'PLIN'),
    (11, 458.33, 7, 'TRANSFERENCIA BCP'), (11, 458.33, 8, 'TRANSFERENCIA BCP'), (11, 458.33, 9, 'TRANSFERENCIA BCP'),
    (11, 458.33, 10, 'PAGOEFECTIVO'), (11, 458.33, 11, 'PAGOEFECTIVO'), (11, 458.41, 12, 'PAGOEFECTIVO'),
    (13, 320.83, 1, 'YAPE'),
    (14, 550.00, 1, 'YAPE'), (14, 550.00, 2, 'PLIN'), (14, 550.00, 3, 'TRANSFERENCIA BCP');

-- Insertando Solicitudes
INSERT INTO solicitudes_prestamo (id_estudiante, monto_solicitado, cuotas_solicitadas, estado_solicitud, recomendacion_sistema, observacion, atendido_por)
VALUES
    (1,  3000.00,  6, 'PENDIENTE', 'Aprobacion Recomendada',  'Promedio 16.5, asistencia 92%',        'ASISTENTE'),
    (2,  2000.00, 12, 'PENDIENTE', 'Revision Manual',         'Asistencia justa: 85%',                'ASISTENTE'),
    (3,  1500.00,  6, 'PENDIENTE', 'Denegacion Recomendada',  'Promedio bajo y asistencia 65%',       'ASISTENTE'),
    (4,  5000.00, 24, 'APROBADO',  'Aprobacion Recomendada',  'Promedio 18.0, asistencia 98%',        'ASISTENTE'),
    (6,  3000.00,  8, 'APROBADO',  'Aprobacion Recomendada',  'Cumple todos los criterios',           'ASISTENTE'),
    (7,  1500.00,  6, 'PENDIENTE', 'Revision Manual',         'Asistencia 79%, revisar manualmente',  'ASISTENTE'),
    (8,  5000.00, 12, 'APROBADO',  'Aprobacion Recomendada',  'Promedio 17.5, sin deuda previa',      'ASISTENTE'),
    (9,  1000.00,  3, 'DENEGADO',  'Denegacion Recomendada',  'Promedio 9.5 y asistencia 60%',        'ASISTENTE'),
    (10, 5000.00, 12, 'APROBADO',  'Aprobacion Recomendada',  'Promedio 19.0, pago prestamo anterior','ASISTENTE'),
    (11, 2500.00,  6, 'PENDIENTE', 'Revision Manual',         'Promedio 11.5, asistencia limite 72%', 'ASISTENTE'),
    (12, 2000.00,  8, 'ESCALADO',  'Revision Manual',         'Caso especial, enviado al admin',      'ASISTENTE'),
    (13, 1000.00,  3, 'APROBADO',  'Aprobacion Recomendada',  'Estudiante de decimo ciclo, fiable',   'ASISTENTE'),
    (15, 2500.00, 12, 'DENEGADO',  'Denegacion Recomendada',  'Asistencia 68%, riesgo alto',          'ASISTENTE'),
    (17, 4000.00, 24, 'DENEGADO',  'Denegacion Recomendada',  'Estudiante retirado, no elegible',     'SISTEMA'),
    (20, 1500.00,  6, 'APROBADO',  'Aprobacion Recomendada',  'Excelente promedio 18.5',              'ASISTENTE');
--vista

CREATE OR REPLACE VIEW vista_solicitudes AS
SELECT
    s.id_solicitud,
    e.nombre || ' ' || e.apellido AS estudiante,
    e.dni,
    e.promedio_notas,
    e.porcentaje_asistencia,
    e.estado_universidad,
    s.monto_solicitado,
    s.cuotas_solicitadas,
    s.estado_solicitud,
    s.recomendacion_sistema,
    s.atendido_por,
    s.fecha_solicitud
FROM solicitudes_prestamo s
JOIN estudiantes e ON s.id_estudiante = e.id_estudiante;


-- 5. OPTIMIZACIONES DE SEGURIDAD (Candado Criptográfico)

UPDATE solicitudes_prestamo SET estado_solicitud = UPPER(TRIM(estado_solicitud));
UPDATE estudiantes SET nombre = UPPER(TRIM(nombre)), apellido = UPPER(TRIM(apellido)), estado_universidad = UPPER(TRIM(estado_universidad));
UPDATE prestamos SET estado = TRIM(estado);



-- 6. PRUEBAS DE ENMASCARAMIENTO Y REGLAS DE NEGOCIO 


-- 1. Regresa la nota original de Eduardo Camac (era 10.0 en tu script original, aquí lo cambiamos para el test de tu UI)
UPDATE estudiantes SET promedio_notas = 19.0 WHERE apellido = 'CAMAC';
UPDATE estudiantes SET promedio_notas = 10.0 WHERE apellido = 'CAMAC';

-- 2. Si cambias de ventana o logras reactivar el combo, verás que el Hash vuelve a ser idéntico
ALTER TABLE estudiantes 
ADD COLUMN ciclo_actual INT DEFAULT 1 CHECK (ciclo_actual >= 1 AND ciclo_actual <= 10);
select*from estudainte
