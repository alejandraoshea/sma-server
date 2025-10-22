

CREATE TABLE IF NOT EXISTS users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(100) NOT NULL,
    role VARCHAR(20) NOT NULL
);

CREATE TABLE IF NOT EXISTS doctors (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id INT,
    name VARCHAR(100),
    last_name VARCHAR(100),
    gender VARCHAR(10),
    FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE TABLE IF NOT EXISTS patients (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id INT,
    name VARCHAR(100),
    last_name VARCHAR(100),
    gender VARCHAR(10),
    birth_date DATE,
    height INT,
    weight DOUBLE,
    FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE TABLE IF NOT EXISTS symptoms (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    patient_id BIGINT,
    type VARCHAR(50),
    value VARCHAR(100),
    timestamp DATETIME,
    FOREIGN KEY (patient_id) REFERENCES patients(id)
);

CREATE TABLE IF NOT EXISTS signals (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    patient_id BIGINT,
    measurement_session_id BIGINT,
    timestamp DATETIME,
    signal_type VARCHAR(10),
    patient_data TEXT,
    FOREIGN KEY (patient_id) REFERENCES patients(id)
);

CREATE TABLE IF NOT EXISTS measurement_sessions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    patient_id BIGINT,
    timestamp DATETIME,
    FOREIGN KEY (patient_id) REFERENCES patients(id)
);
