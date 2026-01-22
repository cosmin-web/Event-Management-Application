SET NAMES utf8mb4;
CREATE DATABASE IF NOT EXISTS eventdb CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE eventdb;

-- UTILIZATORI (IDM)
CREATE TABLE utilizatori (
    id INT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    parola VARCHAR(255) NOT NULL,
    rol ENUM('ADMIN', 'OWNER_EVENT', 'CLIENT', 'SERVICE_CLIENT') NOT NULL
);

-- EVENIMENTE
CREATE TABLE evenimente (
    id INT AUTO_INCREMENT PRIMARY KEY,
    id_owner INT NOT NULL,
    nume VARCHAR(255) NOT NULL UNIQUE,
    locatie VARCHAR(255),
    descriere VARCHAR(255),
    numarLocuri INT,
    FOREIGN KEY (id_owner) REFERENCES utilizatori(id)
);

-- PACHETE
CREATE TABLE pachete (
    id INT AUTO_INCREMENT PRIMARY KEY,
    id_owner INT NOT NULL,
    nume VARCHAR(255) NOT NULL UNIQUE,
    locatie VARCHAR(255),
    descriere VARCHAR(255),
    numarLocuri INT,
    FOREIGN KEY (id_owner) REFERENCES utilizatori(id)
);

-- PACHETE - EVENIMENTE
CREATE TABLE pachete_evenimente (
    pachet_id INT NOT NULL,
    eveniment_id INT NOT NULL,
    PRIMARY KEY (pachet_id, eveniment_id),
    FOREIGN KEY (pachet_id) REFERENCES pachete(id) ON DELETE CASCADE,
    FOREIGN KEY (eveniment_id) REFERENCES evenimente(id) ON DELETE CASCADE
);

-- BILETE
CREATE TABLE bilete (
    cod VARCHAR(64) PRIMARY KEY,
    pachet_id INT NULL,
    eveniment_id INT NULL,
    CHECK ((pachet_id IS NULL) <> (eveniment_id IS NULL)),
    FOREIGN KEY (pachet_id) REFERENCES pachete(id),
    FOREIGN KEY (eveniment_id) REFERENCES evenimente(id)
);

-- TOKEN BLACKLIST
CREATE TABLE token_blacklist (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    token VARCHAR(512) NOT NULL UNIQUE,
    expiry_date TIMESTAMP NOT NULL
);

