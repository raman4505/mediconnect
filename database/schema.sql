CREATE DATABASE IF NOT EXISTS mediconnect CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE mediconnect;
CREATE TABLE IF NOT EXISTS users (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  name VARCHAR(120) NOT NULL,
  email VARCHAR(190) NOT NULL UNIQUE,
  password_hash VARCHAR(100) NOT NULL,
  role ENUM('ADMIN','PROFESSIONAL','PATIENT') NOT NULL,
  active BOOLEAN NOT NULL DEFAULT TRUE,
  protected_admin BOOLEAN NOT NULL DEFAULT FALSE,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE TABLE IF NOT EXISTS professional_profiles (
  user_id BIGINT PRIMARY KEY,
  specialty VARCHAR(120),
  phone VARCHAR(40),
  FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);
CREATE TABLE IF NOT EXISTS appointments (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  patient_id BIGINT NOT NULL,
  professional_id BIGINT NOT NULL,
  starts_at DATETIME NOT NULL,
  duration_minutes INT NOT NULL DEFAULT 30,
  consultation_type VARCHAR(40) NOT NULL,
  status ENUM('SCHEDULED','CONFIRMED','COMPLETED','CANCELLED') NOT NULL DEFAULT 'SCHEDULED',
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (patient_id) REFERENCES users(id),
  FOREIGN KEY (professional_id) REFERENCES users(id),
  INDEX idx_professional_time (professional_id, starts_at),
  INDEX idx_patient_time (patient_id, starts_at)
);
CREATE TABLE IF NOT EXISTS health_records (
  patient_id BIGINT PRIMARY KEY,
  blood_group VARCHAR(8), allergies TEXT, conditions_text TEXT, medications TEXT,
  medical_history TEXT, emergency_contact VARCHAR(180), updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  FOREIGN KEY (patient_id) REFERENCES users(id) ON DELETE CASCADE
);
CREATE TABLE IF NOT EXISTS consultations (
  appointment_id BIGINT PRIMARY KEY,
  notes TEXT NOT NULL,
  advice TEXT NOT NULL,
  saved_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (appointment_id) REFERENCES appointments(id) ON DELETE CASCADE
);
CREATE TABLE IF NOT EXISTS messages (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  sender_id BIGINT NOT NULL,
  recipient_id BIGINT NOT NULL,
  body TEXT NOT NULL,
  sent_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  read_at TIMESTAMP NULL,
  FOREIGN KEY (sender_id) REFERENCES users(id),
  FOREIGN KEY (recipient_id) REFERENCES users(id),
  INDEX idx_message_pair (sender_id, recipient_id, sent_at)
);
CREATE TABLE IF NOT EXISTS professional_availability (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  professional_id BIGINT NOT NULL,
  weekday TINYINT NOT NULL,
  start_time TIME NOT NULL,
  end_time TIME NOT NULL,
  available BOOLEAN NOT NULL DEFAULT TRUE,
  CHECK (weekday BETWEEN 1 AND 7), CHECK (start_time < end_time),
  UNIQUE KEY uq_professional_weekday (professional_id, weekday),
  FOREIGN KEY (professional_id) REFERENCES users(id) ON DELETE CASCADE
);
CREATE TABLE IF NOT EXISTS app_settings (
  setting_key VARCHAR(100) PRIMARY KEY,
  setting_value VARCHAR(500) NOT NULL
);
INSERT IGNORE INTO app_settings(setting_key, setting_value) VALUES ('application_name','MediConnect'),('appointment_duration_minutes','30'),('cancellation_notice_hours','0'),('default_currency','INR');
