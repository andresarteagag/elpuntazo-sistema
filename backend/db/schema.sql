-- ============================================================
-- EL PUNTAZO - Esquema de base de datos
-- Ejecutar una sola vez para crear la base y las tablas.
-- ============================================================

CREATE DATABASE IF NOT EXISTS elpuntazo
    CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE elpuntazo;

-- ------------------------------------------------------------
-- USERS: administradores y vendedores
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS users (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    name            VARCHAR(150)    NOT NULL,
    email           VARCHAR(150)    NOT NULL UNIQUE,
    password_hash   VARCHAR(255)    NOT NULL,
    role            ENUM('ADMIN','VENDEDOR') NOT NULL,
    active          BOOLEAN         NOT NULL DEFAULT TRUE,
    created_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP
                                     ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- ------------------------------------------------------------
-- CLIENTS: compartidos entre todos los vendedores
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS clients (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    name            VARCHAR(150)    NOT NULL,
    identification  VARCHAR(50),
    phone           VARCHAR(30),
    email           VARCHAR(150),
    active          BOOLEAN         NOT NULL DEFAULT TRUE,
    created_by      BIGINT,
    created_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP
                                     ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_clients_created_by FOREIGN KEY (created_by) REFERENCES users(id),
    UNIQUE KEY uq_clients_identification (identification)
) ENGINE=InnoDB;

-- ------------------------------------------------------------
-- INVOICE_SEQUENCE: soporte para numeracion automatica y
-- correlativa de facturas (evita colisiones concurrentes)
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS invoice_sequence (
    id              INT PRIMARY KEY DEFAULT 1,
    last_number     BIGINT NOT NULL DEFAULT 0
) ENGINE=InnoDB;

INSERT IGNORE INTO invoice_sequence (id, last_number) VALUES (1, 0);

-- ------------------------------------------------------------
-- INVOICES
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS invoices (
    id                          BIGINT AUTO_INCREMENT PRIMARY KEY,
    invoice_number              VARCHAR(30)     NOT NULL UNIQUE,
    client_name                 VARCHAR(150)    NOT NULL,
    seller_id                   BIGINT          NOT NULL,
    base_value                  DECIMAL(14,2)   NOT NULL,
    discount_percentage         DECIMAL(5,2)    NOT NULL DEFAULT 0,
    discount_value              DECIMAL(14,2)   NOT NULL DEFAULT 0,
    value_after_discount        DECIMAL(14,2)   NOT NULL,
    shipping_value               DECIMAL(14,2)   NOT NULL DEFAULT 0,
    shipping_assumed_by_company DECIMAL(14,2)   NOT NULL DEFAULT 0,
    warranty_deduction          DECIMAL(14,2)   NOT NULL DEFAULT 0,
    total_value                 DECIMAL(14,2)   NOT NULL,
    status                      ENUM('ACTIVA','ANULADA') NOT NULL DEFAULT 'ACTIVA',
    created_by                  BIGINT          NOT NULL,
    updated_by                  BIGINT,
    created_at                  DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at                  DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP
                                                 ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_invoices_seller  FOREIGN KEY (seller_id)  REFERENCES users(id),
    CONSTRAINT fk_invoices_created_by FOREIGN KEY (created_by) REFERENCES users(id),
    CONSTRAINT fk_invoices_updated_by FOREIGN KEY (updated_by) REFERENCES users(id),
    INDEX idx_invoices_seller (seller_id),
    INDEX idx_invoices_status (status),
    INDEX idx_invoices_created_at (created_at)
) ENGINE=InnoDB;

-- ------------------------------------------------------------
-- Usuario administrador inicial
-- La contrasena real se genera con BCrypt desde la aplicacion;
-- este INSERT es solo un placeholder de ejemplo, ver README
-- para crear el primer admin de forma segura.
-- ------------------------------------------------------------
