-- =================================================================
-- PHẦN 1: KHỞI TẠO DATABASE
-- =================================================================
CREATE DATABASE IF NOT EXISTS DB_QuanLyChungCu CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE DB_QuanLyChungCu;

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";

-- Tắt kiểm tra khóa ngoại tạm thời để tránh lỗi khi insert dữ liệu chéo
SET FOREIGN_KEY_CHECKS = 0;

-- =================================================================
-- PHẦN 2: TẠO BẢNG (TABLES) THEO ĐÚNG THỨ TỰ CHA -> CON
-- =================================================================

-- 1. Bảng Users (Độc lập)
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

-- 2. Bảng Buildings (Độc lập)
CREATE TABLE `buildings` (
  `id` bigint NOT NULL AUTO_INCREMENT PRIMARY KEY,
  `name` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  `address` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `manager_name` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `description` text COLLATE utf8mb4_unicode_ci,
  `is_deleted` tinyint(1) DEFAULT '0',
  `status` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT 'Đang hoạt động'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 3. Bảng Services (Độc lập)
CREATE TABLE `services` (
  `id` bigint NOT NULL AUTO_INCREMENT PRIMARY KEY,
  `service_name` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  `unit_price` decimal(15,2) NOT NULL,
  `unit_type` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `is_mandatory` tinyint(1) DEFAULT '0',
  `is_deleted` tinyint(1) DEFAULT '0'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 4. Bảng Residents (Độc lập)
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

-- 5. Bảng Floors (Phụ thuộc Buildings)
CREATE TABLE `floors` (
  `id` bigint NOT NULL AUTO_INCREMENT PRIMARY KEY,
  `building_id` bigint NOT NULL,
  `floor_number` int NOT NULL,
  `name` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `is_deleted` tinyint(1) DEFAULT '0',
  `status` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT 'Đang hoạt động',
  FOREIGN KEY (`building_id`) REFERENCES `buildings` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 6. Bảng Apartments (Phụ thuộc Floors)
CREATE TABLE `apartments` (
  `id` bigint NOT NULL AUTO_INCREMENT PRIMARY KEY,
  `floor_id` bigint NOT NULL,
  `room_number` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL,
  `area` double DEFAULT NULL,
  `status` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT 'AVAILABLE',
  `base_price` decimal(15,2) DEFAULT NULL,
  `description` text COLLATE utf8mb4_unicode_ci,
  `is_deleted` tinyint(1) DEFAULT '0',
  `apartment_type` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT 'Standard',
  `bedroom_count` int DEFAULT '1',
  `bathroom_count` int DEFAULT '1',
  FOREIGN KEY (`floor_id`) REFERENCES `floors` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 7. Bảng Contracts (Phụ thuộc Apartments, Residents)
CREATE TABLE `contracts` (
  `id` bigint NOT NULL AUTO_INCREMENT PRIMARY KEY,
  `contract_number` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL UNIQUE COMMENT 'Mã số hợp đồng',
  `apartment_id` bigint NOT NULL,
  `resident_id` bigint NOT NULL,
  `contract_type` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT 'RENTAL',
  `signed_date` date DEFAULT NULL,
  `start_date` date NOT NULL,
  `end_date` date DEFAULT NULL,
  `terminated_date` date DEFAULT NULL,
  `notes` text COLLATE utf8mb4_unicode_ci,
  `deposit_amount` decimal(15,2) DEFAULT NULL,
  `status` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT 'ACTIVE',
  `is_deleted` tinyint(1) DEFAULT '0',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  FOREIGN KEY (`apartment_id`) REFERENCES `apartments` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE,
  FOREIGN KEY (`resident_id`) REFERENCES `residents` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 8. Bảng Contract History (Phụ thuộc Contracts)
CREATE TABLE `contract_history` (
  `id` bigint NOT NULL AUTO_INCREMENT PRIMARY KEY,
  `contract_id` bigint NOT NULL,
  `action` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL,
  `old_value` text COLLATE utf8mb4_unicode_ci,
  `new_value` text COLLATE utf8mb4_unicode_ci,
  `old_end_date` date DEFAULT NULL,
  `new_end_date` date DEFAULT NULL,
  `reason` text COLLATE utf8mb4_unicode_ci,
  `created_by` bigint DEFAULT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (`contract_id`) REFERENCES `contracts` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 9. Bảng Contract Services (Phụ thuộc Contracts, Services)
CREATE TABLE `contract_services` (
  `id` bigint NOT NULL AUTO_INCREMENT PRIMARY KEY,
  `contract_id` bigint NOT NULL,
  `service_id` bigint NOT NULL,
  `applied_date` date NOT NULL,
  `unit_price` decimal(15,2) NOT NULL,
  `is_active` tinyint(1) DEFAULT '1',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY `unique_active_contract_service` (`contract_id`,`service_id`,`is_active`),
  FOREIGN KEY (`contract_id`) REFERENCES `contracts` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  FOREIGN KEY (`service_id`) REFERENCES `services` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 10. Bảng Household Members (Phụ thuộc Contracts)
CREATE TABLE `household_members` (
  `id` bigint NOT NULL AUTO_INCREMENT PRIMARY KEY,
  `contract_id` bigint NOT NULL,
  `full_name` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  `relationship` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL,
  `is_head` tinyint(1) DEFAULT '0',
  `gender` varchar(10) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `dob` date DEFAULT NULL,
  `identity_card` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `phone` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `is_active` tinyint(1) DEFAULT '1',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (`contract_id`) REFERENCES `contracts` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 11. Bảng Service Usage (Phụ thuộc Contracts, Services)
CREATE TABLE `service_usage` (
  `id` bigint NOT NULL AUTO_INCREMENT PRIMARY KEY,
  `contract_id` bigint NOT NULL,
  `service_id` bigint NOT NULL,
  `month` int NOT NULL,
  `year` int NOT NULL,
  `old_index` double DEFAULT '0',
  `new_index` double DEFAULT '0',
  `actual_usage` double DEFAULT NULL,
  FOREIGN KEY (`contract_id`) REFERENCES `contracts` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE,
  FOREIGN KEY (`service_id`) REFERENCES `services` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 12. Bảng Invoices (Phụ thuộc Contracts)
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
  FOREIGN KEY (`contract_id`) REFERENCES `contracts` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 13. Bảng Invoice Details (Phụ thuộc Invoices)
CREATE TABLE `invoice_details` (
  `id` bigint NOT NULL AUTO_INCREMENT PRIMARY KEY,
  `invoice_id` bigint NOT NULL,
  `service_name` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `unit_price` decimal(15,2) DEFAULT NULL,
  `quantity` double DEFAULT NULL,
  `amount` decimal(15,2) DEFAULT NULL,
  FOREIGN KEY (`invoice_id`) REFERENCES `invoices` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =================================================================
-- PHẦN 3: NẠP DỮ LIỆU MẪU (DATA SEEDING)
-- =================================================================

INSERT INTO `users` (`id`, `username`, `password`, `full_name`, `role`, `is_active`, `created_at`, `last_login`) VALUES
(1, 'admin', 'admin123', 'Administrator', 'ADMIN', 1, '2026-01-13 08:05:08', '2026-01-19 13:54:46'),
(3, 'staff', 'staff123', 'Staff Member', 'STAFF', 1, '2026-01-13 09:03:05', '2026-01-13 09:07:15'),
(4, 'accountant', 'acc123', 'Accountant', 'ACCOUNTANT', 1, '2026-01-13 09:03:05', '2026-01-13 09:08:35');

INSERT INTO `buildings` (`id`, `name`, `address`, `manager_name`, `description`, `is_deleted`, `status`) VALUES
(1, 'Nhà Trắng', '123 Ninh Kiều, Cần Thơ', 'Nguyễn Anh Quân', '', 0, 'Đang hoạt động'),
(2, 'Sunshine', '123 Cần Thơ', 'Nguyễn Văn Ninh', 'Tòa nhà hướng nắng', 0, 'Đang hoạt động'),
(3, 'LandMark 81', 'Thành Phố Hồ Chí Minh', 'Bùi Minh Nhựt', '', 0, 'Đang hoạt động'),
(4, 'FPT Plaza', 'Đường Võ Chí Công', 'Trần Kỹ Thuật', 'Căn hộ nhân viên', 0, 'Đang hoạt động');

INSERT INTO `services` (`id`, `service_name`, `unit_price`, `unit_type`, `is_mandatory`, `is_deleted`) VALUES
(1, 'Bảo vệ an ninh', 20000.00, 'tháng', 1, 0),
(2, 'Bãi xe', 50000.00, 'tháng', 1, 0),
(3, 'Thu gom và xử lí rác thải', 50000.00, 'tháng', 1, 0),
(4, 'Internet', 200000.00, 'Tháng', 0, 0);

INSERT INTO `residents` (`id`, `full_name`, `phone`, `email`, `identity_card`, `gender`, `dob`, `hometown`, `is_deleted`) VALUES
(1, 'Trần Văn Khánh', '0909123456', 'vankhanh@gmail.com', '096345464344', 'Nam', '1998-06-27', NULL, 0),
(2, 'Bùi Minh Nhựt', '03598965688', 'minhnhut@gmail.com', '094374378232', 'Nam', '2003-08-15', 'Cần Ther', 0);

INSERT INTO `floors` (`id`, `building_id`, `floor_number`, `name`, `is_deleted`, `status`) VALUES
(1, 1, 1, 'Tầng 1', 0, 'Đang hoạt động'),
(2, 1, 2, 'Tầng 2', 0, 'Đang hoạt động'),
(3, 1, 3, 'Tầng 3', 0, 'Đang hoạt động');

INSERT INTO `apartments` (`id`, `floor_id`, `room_number`, `area`, `status`, `base_price`, `description`, `is_deleted`, `apartment_type`, `bedroom_count`, `bathroom_count`) VALUES
(1, 2, 'P201', 55.5, 'RENTED', 5000000.00, NULL, 0, 'Standard', 1, 1),
(2, 2, 'P202', 70, 'AVAILABLE', 7500000.00, NULL, 1, 'Standard', 1, 1),
(3, 1, 'P201', 55.5, 'Available', NULL, NULL, 0, 'Standard', 1, 1),
(4, 1, '5', 14, 'Available', NULL, NULL, 0, 'Standard', 1, 1),
(5, 1, 'P202', 100, 'AVAILABLE', 10000000.00, 'Căn hộ Duplex', 0, 'Duplex', 2, 3);

INSERT INTO `contracts` (`id`, `contract_number`, `apartment_id`, `resident_id`, `contract_type`, `signed_date`, `start_date`, `end_date`, `terminated_date`, `notes`, `deposit_amount`, `status`, `is_deleted`, `created_at`, `updated_at`) VALUES
(1, 'HD20260109001', 1, 1, 'RENTAL', '2026-01-09', '2026-01-09', NULL, NULL, NULL, 5000000.00, 'ACTIVE', 0, '2026-01-18 07:45:33', '2026-01-18 07:46:52');

INSERT INTO `household_members` (`id`, `contract_id`, `full_name`, `relationship`, `is_head`, `gender`, `dob`, `identity_card`, `phone`, `is_active`, `created_at`) VALUES
(1, 1, 'Nguyễn Thị Hồng', 'Vợ', 0, 'Nữ', '1999-05-12', '019837332', '097463634', 1, '2026-01-17 06:11:55'),
(2, 1, 'Trần Anh Quốc', 'Con', 0, 'Nam', '2020-09-15', NULL, NULL, 1, '2026-01-17 07:46:39');

-- =================================================================
-- PHẦN 4: PROCEDURES & TRIGGERS
-- =================================================================

DELIMITER $$

-- Procedure 1
CREATE PROCEDURE `sp_check_apartment_availability` (IN `p_apartment_id` BIGINT, OUT `has_active_contract` BOOLEAN)
BEGIN
    DECLARE contract_count INT;
    SELECT COUNT(*) INTO contract_count FROM contracts
    WHERE apartment_id = p_apartment_id AND status IN ('ACTIVE', 'EXPIRING_SOON') AND is_deleted = 0;
    SET has_active_contract = (contract_count > 0);
END$$

-- Procedure 2
CREATE PROCEDURE `sp_generate_contract_number` (OUT `new_number` VARCHAR(50))
BEGIN
    DECLARE today_str VARCHAR(8);
    DECLARE next_seq INT;
    SET today_str = DATE_FORMAT(CURDATE(), '%Y%m%d');
    SELECT IFNULL(MAX(CAST(SUBSTRING(contract_number, 12, 3) AS UNSIGNED)), 0) + 1 INTO next_seq
    FROM contracts WHERE contract_number LIKE CONCAT('HD', today_str, '%');
    SET new_number = CONCAT('HD', today_str, LPAD(next_seq, 3, '0'));
END$$

-- Procedure 3
CREATE PROCEDURE `sp_terminate_contract` (IN `p_contract_id` BIGINT, IN `p_reason` TEXT, IN `p_terminated_date` DATE)
BEGIN
    DECLARE unpaid_count INT;
    SELECT COUNT(*) INTO unpaid_count FROM invoices WHERE contract_id = p_contract_id AND status = 'UNPAID' AND is_deleted = 0;
    IF unpaid_count > 0 THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Không thể kết thúc hợp đồng còn hóa đơn chưa thanh toán';
    END IF;
    UPDATE contracts SET status = 'TERMINATED', terminated_date = IFNULL(p_terminated_date, CURDATE()), notes = CONCAT(IFNULL(notes, ''), '\n[Kết thúc] ', p_reason), updated_at = CURRENT_TIMESTAMP WHERE id = p_contract_id;
    INSERT INTO contract_history (contract_id, action, reason, created_at) VALUES (p_contract_id, 'TERMINATED', p_reason, CURRENT_TIMESTAMP);
    UPDATE apartments a INNER JOIN contracts c ON a.id = c.apartment_id SET a.status = 'AVAILABLE' WHERE c.id = p_contract_id;
END$$

-- Trigger 1: After Insert Contract
CREATE TRIGGER `trg_contract_after_insert` AFTER INSERT ON `contracts` FOR EACH ROW
BEGIN
    IF NEW.status = 'ACTIVE' AND NEW.is_deleted = 0 THEN
        UPDATE apartments SET status = 'RENTED' WHERE id = NEW.apartment_id;
    END IF;
    INSERT INTO contract_history (contract_id, action, new_value, created_at)
    VALUES (NEW.id, 'CREATED', CONCAT('Số HĐ: ', NEW.contract_number, ', Loại: ', NEW.contract_type), CURRENT_TIMESTAMP);
END$$

-- Trigger 2: After Update Contract
CREATE TRIGGER `trg_contract_after_update` AFTER UPDATE ON `contracts` FOR EACH ROW
BEGIN
    DECLARE action_type VARCHAR(50);
    DECLARE change_desc TEXT;
    IF OLD.end_date != NEW.end_date THEN
        SET action_type = 'RENEWED';
        SET change_desc = CONCAT('Gia hạn từ ', OLD.end_date, ' đến ', NEW.end_date);
        INSERT INTO contract_history (contract_id, action, old_end_date, new_end_date, new_value, created_at)
        VALUES (NEW.id, action_type, OLD.end_date, NEW.end_date, change_desc, CURRENT_TIMESTAMP);
    END IF;
    IF OLD.status != NEW.status THEN
        SET action_type = 'STATUS_CHANGED';
        SET change_desc = CONCAT('Trạng thái: ', OLD.status, ' → ', NEW.status);
        INSERT INTO contract_history (contract_id, action, old_value, new_value, created_at)
        VALUES (NEW.id, action_type, OLD.status, NEW.status, CURRENT_TIMESTAMP);
    END IF;
END$$

DELIMITER ;

-- =================================================================
-- PHẦN 5: VIEWS (TẠO CUỐI CÙNG)
-- =================================================================

-- View 1: Contract Summary
CREATE OR REPLACE VIEW `v_contract_summary` AS
SELECT 
    `c`.`id` AS `contract_id`, `c`.`contract_number`, `c`.`contract_type`, 
    `c`.`signed_date`, `c`.`start_date`, `c`.`end_date`, `c`.`terminated_date`, 
    `c`.`deposit_amount`, `c`.`status`, 
    `a`.`room_number` AS `apartment_number`, `a`.`area` AS `apartment_area`, 
    `f`.`name` AS `floor_name`, `b`.`name` AS `building_name`, 
    `r`.`id` AS `resident_id`, `r`.`full_name` AS `resident_name`, `r`.`phone` AS `resident_phone`, 
    `r`.`identity_card` AS `resident_identity_card`, `r`.`email` AS `resident_email`, 
    (CASE 
        WHEN `c`.`status` = 'TERMINATED' THEN 'Đã hủy' 
        WHEN `c`.`end_date` IS NULL THEN 'Đang hiệu lực' 
        WHEN (TO_DAYS(`c`.`end_date`) - TO_DAYS(CURDATE())) < 0 THEN 'Đã hết hạn' 
        WHEN (TO_DAYS(`c`.`end_date`) - TO_DAYS(CURDATE())) <= 30 THEN 'Sắp hết hạn' 
        ELSE 'Đang hiệu lực' 
    END) AS `status_display`, 
    (TO_DAYS(`c`.`end_date`) - TO_DAYS(CURDATE())) AS `days_left`, 
    (SELECT COUNT(0) FROM `household_members` `hm` WHERE `hm`.`contract_id` = `c`.`id` AND `hm`.`is_active` = 1) AS `member_count`, 
    (SELECT COUNT(0) FROM `invoices` `i` WHERE `i`.`contract_id` = `c`.`id` AND `i`.`is_deleted` = 0) AS `invoice_count`, 
    (SELECT COUNT(0) FROM `invoices` `i` WHERE `i`.`contract_id` = `c`.`id` AND `i`.`status` = 'UNPAID' AND `i`.`is_deleted` = 0) AS `unpaid_invoice_count`, 
    `c`.`created_at`, `c`.`updated_at` 
FROM `contracts` `c` 
JOIN `apartments` `a` ON `c`.`apartment_id` = `a`.`id` 
JOIN `floors` `f` ON `a`.`floor_id` = `f`.`id` 
JOIN `buildings` `b` ON `f`.`building_id` = `b`.`id` 
JOIN `residents` `r` ON `c`.`resident_id` = `r`.`id` 
WHERE `c`.`is_deleted` = 0;

-- View 2: Active Contracts
CREATE OR REPLACE VIEW `v_active_contracts` AS
SELECT * FROM `v_contract_summary` 
WHERE `status` IN ('ACTIVE', 'EXPIRING_SOON');

-- View 3: Expiring Contracts
CREATE OR REPLACE VIEW `v_expiring_contracts` AS
SELECT * FROM `v_contract_summary` 
WHERE `end_date` IS NOT NULL 
AND (TO_DAYS(`end_date`) - TO_DAYS(CURDATE())) BETWEEN 0 AND 30 
AND `status` <> 'TERMINATED';

-- Bật lại kiểm tra khóa ngoại
SET FOREIGN_KEY_CHECKS = 1;
COMMIT;