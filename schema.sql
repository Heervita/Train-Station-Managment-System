-- Run this once in MySQL before starting the app
-- mysql -u root -p < schema.sql

CREATE DATABASE IF NOT EXISTS train_station;
USE train_station;

CREATE TABLE IF NOT EXISTS trains (
    train_id       INT AUTO_INCREMENT PRIMARY KEY,
    train_number   VARCHAR(20) NOT NULL UNIQUE,
    train_name     VARCHAR(100) NOT NULL,
    train_type     ENUM('EXPRESS','LOCAL','FREIGHT','INTERCITY') DEFAULT 'EXPRESS',
    total_seats    INT NOT NULL,
    available_seats INT NOT NULL,
    status         ENUM('ACTIVE','INACTIVE','MAINTENANCE') DEFAULT 'ACTIVE'
);

CREATE TABLE IF NOT EXISTS schedules (
    schedule_id    INT AUTO_INCREMENT PRIMARY KEY,
    train_id       INT NOT NULL,
    origin         VARCHAR(100) NOT NULL,
    destination    VARCHAR(100) NOT NULL,
    departure_time DATETIME NOT NULL,
    arrival_time   DATETIME,
    platform_no    VARCHAR(10),
    status         ENUM('SCHEDULED','DEPARTED','ARRIVED','DELAYED','CANCELLED') DEFAULT 'SCHEDULED',
    delay_minutes  INT DEFAULT 0,
    FOREIGN KEY (train_id) REFERENCES trains(train_id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS passengers (
    passenger_id INT AUTO_INCREMENT PRIMARY KEY,
    first_name   VARCHAR(50) NOT NULL,
    last_name    VARCHAR(50) NOT NULL,
    email        VARCHAR(100),
    phone        VARCHAR(20),
    id_number    VARCHAR(50) UNIQUE
);

CREATE TABLE IF NOT EXISTS tickets (
    ticket_id      INT AUTO_INCREMENT PRIMARY KEY,
    ticket_number  VARCHAR(30) NOT NULL UNIQUE,
    passenger_id   INT NOT NULL,
    schedule_id    INT NOT NULL,
    ticket_class   ENUM('ECONOMY','BUSINESS','FIRST') DEFAULT 'ECONOMY',
    price          DECIMAL(10,2) NOT NULL,
    status         ENUM('BOOKED','CANCELLED','USED') DEFAULT 'BOOKED',
    payment_method VARCHAR(20) DEFAULT 'CASH',
    booking_date   TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (passenger_id) REFERENCES passengers(passenger_id) ON DELETE CASCADE,
    FOREIGN KEY (schedule_id)  REFERENCES schedules(schedule_id)   ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS admin_users (
    admin_id      INT AUTO_INCREMENT PRIMARY KEY,
    username      VARCHAR(50) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    full_name     VARCHAR(100),
    role          ENUM('SUPER_ADMIN','OPERATOR') DEFAULT 'OPERATOR'
);

-- Seed data
INSERT IGNORE INTO admin_users (username, password_hash, full_name, role)
VALUES ('admin', 'admin123', 'Administrator', 'SUPER_ADMIN');

INSERT IGNORE INTO trains (train_number, train_name, train_type, total_seats, available_seats, status) VALUES
('TRN-001', 'Rajdhani Express', 'EXPRESS',   520, 320, 'ACTIVE'),
('TRN-002', 'Shatabdi Express', 'EXPRESS',   480, 480, 'ACTIVE'),
('TRN-003', 'Metro Link',       'LOCAL',    1200, 900, 'ACTIVE');

INSERT IGNORE INTO passengers (first_name, last_name, email, phone, id_number) VALUES
('Amit',  'Sharma',  'amit@email.com',  '+91-9000000001', 'PAS001'),
('Priya', 'Verma',   'priya@email.com', '+91-9000000002', 'PAS002');

INSERT IGNORE INTO schedules (train_id, origin, destination, departure_time, arrival_time, platform_no, status) VALUES
(1, 'Delhi',  'Mumbai', NOW() + INTERVAL 2 HOUR, NOW() + INTERVAL 10 HOUR, '1', 'SCHEDULED'),
(2, 'Mumbai', 'Pune',   NOW() + INTERVAL 4 HOUR, NOW() + INTERVAL 6 HOUR,  '2', 'SCHEDULED');
