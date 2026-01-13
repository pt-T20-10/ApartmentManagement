-- 1. HEADER CHUẨN
CREATE DATABASE IF NOT EXISTS DB_QuanLyChungCu CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE DB_QuanLyChungCu;

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";

-- 2. TẠO CẤU TRÚC BẢNG (STRUCTURE) TRƯỚC
-- Lưu ý: Thứ tự tạo bảng quan trọng để tránh lỗi khóa ngoại

CREATE TABLE `buildings` (
  `id` bigint NOT NULL AUTO_INCREMENT PRIMARY KEY,
  `name` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  `address` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `manager_name` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `description` text COLLATE utf8mb4_unicode_ci,
  `is_deleted` tinyint(1) DEFAULT '0'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `floors` (
  `id` bigint NOT NULL AUTO_INCREMENT PRIMARY KEY,
  `building_id` bigint NOT NULL,
  `floor_number` int NOT NULL,
  `name` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `is_deleted` tinyint(1) DEFAULT '0',
  FOREIGN KEY (`building_id`) REFERENCES `buildings` (`id`) ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `apartments` (
  `id` bigint NOT NULL AUTO_INCREMENT PRIMARY KEY,
  `floor_id` bigint NOT NULL,
  `room_number` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL,
  `area` double DEFAULT NULL,
  `status` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT 'AVAILABLE',
  `base_price` decimal(15,2) DEFAULT NULL,
  `description` text COLLATE utf8mb4_unicode_ci,
  `is_deleted` tinyint(1) DEFAULT '0',
  FOREIGN KEY (`floor_id`) REFERENCES `floors` (`id`) ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `residents` (
  `id` bigint NOT NULL AUTO_INCREMENT PRIMARY KEY,
  `full_name` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  `phone` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL,
  `email` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `identity_card` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL,
  `gender` varchar(10) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `dob` date DEFAULT NULL,
  `hometown` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `is_deleted` tinyint(1) DEFAULT '0'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `contracts` (
  `id` bigint NOT NULL AUTO_INCREMENT PRIMARY KEY,
  `apartment_id` bigint NOT NULL,
  `resident_id` bigint NOT NULL,
  `start_date` date NOT NULL,
  `end_date` date DEFAULT NULL,
  `deposit_amount` decimal(15,2) DEFAULT NULL,
  `status` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT 'ACTIVE',
  `is_deleted` tinyint(1) DEFAULT '0',
  FOREIGN KEY (`apartment_id`) REFERENCES `apartments` (`id`) ON UPDATE CASCADE,
  FOREIGN KEY (`resident_id`) REFERENCES `residents` (`id`) ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `services` (
  `id` bigint NOT NULL AUTO_INCREMENT PRIMARY KEY,
  `service_name` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  `unit_price` decimal(15,2) NOT NULL,
  `unit_type` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `is_mandatory` tinyint(1) DEFAULT '0',
  `is_deleted` tinyint(1) DEFAULT '0'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `service_usage` (
  `id` bigint NOT NULL AUTO_INCREMENT PRIMARY KEY,
  `contract_id` bigint NOT NULL,
  `service_id` bigint NOT NULL,
  `month` int NOT NULL,
  `year` int NOT NULL,
  `old_index` double DEFAULT '0',
  `new_index` double DEFAULT '0',
  `actual_usage` double DEFAULT NULL,
  FOREIGN KEY (`contract_id`) REFERENCES `contracts` (`id`) ON UPDATE CASCADE,
  FOREIGN KEY (`service_id`) REFERENCES `services` (`id`) ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `invoices` (
  `id` bigint NOT NULL AUTO_INCREMENT PRIMARY KEY,
  `contract_id` bigint NOT NULL,
  `month` int NOT NULL,
  `year` int NOT NULL,
  `total_amount` decimal(15,2) DEFAULT '0.00',
  `status` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT 'UNPAID',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `payment_date` timestamp NULL DEFAULT NULL,
  `is_deleted` tinyint(1) DEFAULT '0',
  FOREIGN KEY (`contract_id`) REFERENCES `contracts` (`id`) ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `invoice_details` (
  `id` bigint NOT NULL AUTO_INCREMENT PRIMARY KEY,
  `invoice_id` bigint NOT NULL,
  `service_name` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `unit_price` decimal(15,2) DEFAULT NULL,
  `quantity` double DEFAULT NULL,
  `amount` decimal(15,2) DEFAULT NULL,
  FOREIGN KEY (`invoice_id`) REFERENCES `invoices` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `users` (
  `id` bigint NOT NULL AUTO_INCREMENT PRIMARY KEY,
  `username` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL UNIQUE,
  `password` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `full_name` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  `role` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT 'STAFF',
  `is_active` tinyint(1) DEFAULT '1',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `last_login` timestamp NULL DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 3. NẠP DỮ LIỆU MẪU (DATA SEEDING)

INSERT INTO `buildings` (`name`, `address`, `manager_name`, `description`, `is_deleted`) VALUES
('Sunshine Riverside', '123 Đường 3/2, Q. Ninh Kiều, Cần Thơ', 'Nguyễn Văn Quản Lý', 'Tòa nhà cao cấp view sông Hậu', 0),
('VinHomes Central Park', '208 Nguyễn Hữu Cảnh, TP.HCM', 'Phạm Thị Ban Quản Trị', 'Khu phức hợp căn hộ', 0),
('Chung Cư Hưng Phú', 'Khu Dân Cư Hưng Phú 1', 'Lê Văn Bảo Vệ', 'Nhà ở xã hội', 0),
('Tòa Nhà FPT Plaza', 'Đường Võ Chí Công', 'Trần Kỹ Thuật', 'Căn hộ nhân viên', 0);

INSERT INTO `floors` (`building_id`, `floor_number`, `name`, `is_deleted`) VALUES
(1, 1, 'Tầng 1 - Sảnh Thương Mại', 0),
(1, 2, 'Tầng 2 - Căn Hộ Cao Cấp', 0),
(1, 3, 'Tầng 3 - Căn Hộ Cao Cấp', 0),
(2, 1, 'Tầng G - Shophouse', 0),
(2, 2, 'Tầng 2 - Căn Hộ View Sông', 0),
(3, 1, 'Tầng Trệt - Nhà Xe', 0),
(3, 2, 'Tầng 2 - Căn Hộ Giá Rẻ', 0);

INSERT INTO `services` (`service_name`, `unit_price`, `unit_type`, `is_mandatory`, `is_deleted`) VALUES
('Tiền Điện Sinh Hoạt', 3500.00, 'KWH', 1, 0),
('Tiền Nước Sạch', 18000.00, 'KHOI', 1, 0),
('Phí Quản Lý Chung Cư', 250000.00, 'THANG', 1, 0),
('Phí Gửi Xe Máy', 120000.00, 'XE', 0, 0),
('Internet Viettel', 220000.00, 'THANG', 0, 0),
('Vệ Sinh Căn Hộ', 50000.00, 'GIO', 0, 0);

INSERT INTO `users` (`username`, `password`, `full_name`, `role`) VALUES
('admin', 'admin123', 'Administrator', 'ADMIN'),
('staff', 'staff123', 'Nhân Viên', 'STAFF');

COMMIT;