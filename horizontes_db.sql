
--  Horizontes sin Límites -- Script de Base de Datos

CREATE DATABASE IF NOT EXISTS horizontes_db
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

USE horizontes_db;


-- USUARIOS
CREATE TABLE usuario (
    id INT NOT NULL AUTO_INCREMENT,
    nombre VARCHAR(60) NOT NULL,
    password VARCHAR(255) NOT NULL,
    tipo TINYINT NOT NULL COMMENT '1=Atencion Cliente, 2=Operaciones, 3=Administrador',
    activo TINYINT(1) NOT NULL DEFAULT 1,
    PRIMARY KEY (id),
    UNIQUE KEY uk_usuario_nombre (nombre)
)  ENGINE=INNODB;

-- DESTINOS
CREATE TABLE destino (
  id          INT          NOT NULL AUTO_INCREMENT,
  nombre      VARCHAR(100) NOT NULL,
  pais        VARCHAR(80)  NOT NULL,
  descripcion TEXT,
  clima       VARCHAR(100),
  imagen_url  VARCHAR(500),
  PRIMARY KEY (id),
  UNIQUE KEY uk_destino_nombre (nombre)
) ENGINE=InnoDB;

-- PROVEEDORES
CREATE TABLE proveedor (
  id       INT          NOT NULL AUTO_INCREMENT,
  nombre   VARCHAR(100) NOT NULL,
  tipo     TINYINT      NOT NULL COMMENT '1=Aerolinea, 2=Hotel, 3=Tour, 4=Traslado, 5=Otro',
  pais     VARCHAR(80)  NOT NULL,
  contacto VARCHAR(200),
  PRIMARY KEY (id),
  UNIQUE KEY uk_proveedor_nombre (nombre)
) ENGINE=InnoDB;

-- PAQUETES TURISTICOS
CREATE TABLE paquete (
  id            INT            NOT NULL AUTO_INCREMENT,
  nombre        VARCHAR(150)   NOT NULL,
  destino_id    INT            NOT NULL,
  duracion_dias INT            NOT NULL,
  descripcion   TEXT,
  precio_venta  DECIMAL(12, 2) NOT NULL,
  capacidad     INT            NOT NULL,
  activo        TINYINT(1)     NOT NULL DEFAULT 1,
  PRIMARY KEY (id),
  UNIQUE KEY uk_paquete_nombre (nombre),
  CONSTRAINT fk_paquete_destino FOREIGN KEY (destino_id) REFERENCES destino (id)
) ENGINE=InnoDB;

-- SERVICIOS DE PAQUETE 
CREATE TABLE servicio_paquete (
  id           INT            NOT NULL AUTO_INCREMENT,
  paquete_id   INT            NOT NULL,
  proveedor_id INT            NOT NULL,
  descripcion  VARCHAR(200)   NOT NULL,
  costo        DECIMAL(12, 2) NOT NULL,
  PRIMARY KEY (id),
  CONSTRAINT fk_sp_paquete   FOREIGN KEY (paquete_id)   REFERENCES paquete   (id),
  CONSTRAINT fk_sp_proveedor FOREIGN KEY (proveedor_id) REFERENCES proveedor (id)
) ENGINE=InnoDB;

-- CLIENTES
CREATE TABLE cliente (
  dpi          VARCHAR(20)  NOT NULL,
  nombre       VARCHAR(120) NOT NULL,
  fecha_nac    DATE         NOT NULL,
  telefono     VARCHAR(20),
  email        VARCHAR(100),
  nacionalidad VARCHAR(60),
  PRIMARY KEY (dpi)
) ENGINE=InnoDB;

-- RESERVACIONES

CREATE TABLE reservacion (
  id             INT            NOT NULL AUTO_INCREMENT,
  numero         VARCHAR(20)    NOT NULL COMMENT 'ej. RES-00001',
  fecha_creacion DATE           NOT NULL,
  fecha_viaje    DATE           NOT NULL,
  paquete_id     INT            NOT NULL,
  agente_id      INT            NOT NULL,
  costo_total    DECIMAL(12, 2) NOT NULL,
  estado         VARCHAR(15)    NOT NULL DEFAULT 'PENDIENTE'
                                COMMENT 'PENDIENTE | CONFIRMADA | CANCELADA | COMPLETADA',
  PRIMARY KEY (id),
  UNIQUE KEY uk_reservacion_numero (numero),
  CONSTRAINT fk_res_paquete FOREIGN KEY (paquete_id) REFERENCES paquete   (id),
  CONSTRAINT fk_res_agente  FOREIGN KEY (agente_id)  REFERENCES usuario   (id)
) ENGINE=InnoDB;

-- PASAJEROS POR RESERVACION  (N:M)

CREATE TABLE reservacion_pasajero (
  reservacion_id INT         NOT NULL,
  cliente_dpi    VARCHAR(20) NOT NULL,
  PRIMARY KEY (reservacion_id, cliente_dpi),
  CONSTRAINT fk_rp_reservacion FOREIGN KEY (reservacion_id) REFERENCES reservacion (id),
  CONSTRAINT fk_rp_cliente     FOREIGN KEY (cliente_dpi)    REFERENCES cliente     (dpi)
) ENGINE=InnoDB;


-- PAGOS

CREATE TABLE pago (
  id             INT            NOT NULL AUTO_INCREMENT,
  reservacion_id INT            NOT NULL,
  monto          DECIMAL(12, 2) NOT NULL,
  metodo         TINYINT        NOT NULL COMMENT '1=Efectivo, 2=Tarjeta, 3=Transferencia',
  fecha          DATE           NOT NULL,
  PRIMARY KEY (id),
  CONSTRAINT fk_pago_reservacion FOREIGN KEY (reservacion_id) REFERENCES reservacion (id)
) ENGINE=InnoDB;


-- CANCELACIONES

CREATE TABLE cancelacion (
  id               INT            NOT NULL AUTO_INCREMENT,
  reservacion_id   INT            NOT NULL,
  fecha            DATE           NOT NULL,
  monto_reembolso  DECIMAL(12, 2) NOT NULL,
  perdida_agencia  DECIMAL(12, 2) NOT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_cancelacion_reservacion (reservacion_id),
  CONSTRAINT fk_cancel_reservacion FOREIGN KEY (reservacion_id) REFERENCES reservacion (id)
) ENGINE=InnoDB;


--  USUARIO ADMINISTRADOR

INSERT INTO usuario (nombre, password, tipo, activo)
VALUES ('admin', '123456', 3, 1);