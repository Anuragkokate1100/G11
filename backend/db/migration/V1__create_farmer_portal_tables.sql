-- Additive migration for Farmer Registration and crop records.
-- Back up trackbill_ai before running. This script does not modify existing owner tables.
CREATE TABLE IF NOT EXISTS farmer (
  id BIGINT NOT NULL AUTO_INCREMENT,
  name VARCHAR(100) NOT NULL,
  email VARCHAR(100) NOT NULL,
  phone VARCHAR(20) NOT NULL,
  password VARCHAR(255) NOT NULL,
  village VARCHAR(100) NOT NULL,
  taluka VARCHAR(100) NOT NULL,
  district VARCHAR(100) NOT NULL,
  state VARCHAR(100) NOT NULL DEFAULT 'Maharashtra',
  pincode VARCHAR(10) DEFAULT NULL,
  created_at DATETIME(6) DEFAULT NULL,
  updated_at DATETIME(6) DEFAULT NULL,
  PRIMARY KEY (id), UNIQUE KEY uk_farmer_email (email), UNIQUE KEY uk_farmer_phone (phone)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
CREATE TABLE IF NOT EXISTS farmer_crop (
  id BIGINT NOT NULL AUTO_INCREMENT,
  farmer_id BIGINT NOT NULL,
  crop_name VARCHAR(100) NOT NULL,
  land_area DECIMAL(10,2) NOT NULL,
  area_unit VARCHAR(30) NOT NULL DEFAULT 'acre',
  sowing_date DATE DEFAULT NULL,
  expected_harvest_date DATE DEFAULT NULL,
  status VARCHAR(30) DEFAULT 'GROWING',
  created_at DATETIME(6) DEFAULT NULL,
  PRIMARY KEY (id), KEY idx_farmer_crop_farmer (farmer_id),
  CONSTRAINT fk_farmer_crop_farmer FOREIGN KEY (farmer_id) REFERENCES farmer(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS farmer_transaction (
  id BIGINT NOT NULL AUTO_INCREMENT,
  farmer_id BIGINT NOT NULL,
  transaction_type VARCHAR(10) NOT NULL,
  category VARCHAR(60) NOT NULL,
  amount DECIMAL(12,2) NOT NULL,
  transaction_date DATE NOT NULL,
  description VARCHAR(200) DEFAULT NULL,
  farm_or_crop VARCHAR(150) DEFAULT NULL,
  created_at DATETIME(6) DEFAULT NULL,
  PRIMARY KEY (id),
  KEY idx_farmer_transaction_owner_date (farmer_id, transaction_date),
  CONSTRAINT fk_farmer_transaction_farmer FOREIGN KEY (farmer_id) REFERENCES farmer(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS farmer_notification (
  id BIGINT NOT NULL AUTO_INCREMENT,
  farmer_id BIGINT NOT NULL,
  title VARCHAR(100) NOT NULL,
  message VARCHAR(500) NOT NULL,
  event_type VARCHAR(40) DEFAULT NULL,
  is_read TINYINT(1) NOT NULL DEFAULT 0,
  created_at DATETIME(6) NOT NULL,
  PRIMARY KEY (id),
  KEY idx_farmer_notification_owner_read (farmer_id, is_read),
  CONSTRAINT fk_farmer_notification_farmer FOREIGN KEY (farmer_id) REFERENCES farmer(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS farmer_service_request (
  id BIGINT NOT NULL AUTO_INCREMENT,
  farmer_id BIGINT NOT NULL,
  tractor_owner_id BIGINT DEFAULT NULL,
  service_required VARCHAR(120) NOT NULL,
  location VARCHAR(200) NOT NULL,
  preferred_date DATE DEFAULT NULL,
  details VARCHAR(1000) DEFAULT NULL,
  status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
  created_at DATETIME(6) NOT NULL,
  PRIMARY KEY (id),
  KEY idx_service_request_farmer_status (farmer_id, status),
  CONSTRAINT fk_service_request_farmer FOREIGN KEY (farmer_id) REFERENCES farmer(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS farmer_bill_link (
  id BIGINT NOT NULL AUTO_INCREMENT,
  farmer_id BIGINT NOT NULL,
  billing_id BIGINT NOT NULL,
  linked_by_owner_id BIGINT NOT NULL,
  created_at DATETIME(6) NOT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_farmer_bill_link_billing (billing_id),
  KEY idx_farmer_bill_link_farmer (farmer_id),
  CONSTRAINT fk_farmer_bill_link_farmer FOREIGN KEY (farmer_id) REFERENCES farmer(id) ON DELETE CASCADE,
  CONSTRAINT fk_farmer_bill_link_billing FOREIGN KEY (billing_id) REFERENCES billing(id) ON DELETE CASCADE,
  CONSTRAINT fk_farmer_bill_link_owner FOREIGN KEY (linked_by_owner_id) REFERENCES tractor_owner(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
