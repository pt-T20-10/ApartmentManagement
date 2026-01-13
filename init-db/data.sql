
CREATE DATABASE IF NOT EXISTS DB_QuanLyChungCu CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;


USE DB_QuanLyChungCu;





SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";



CREATE TABLE `apartments` (
  `id` bigint NOT NULL,
  `floor_id` bigint NOT NULL,
  `room_number` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL,
  `area` double DEFAULT NULL,
  `status` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT 'AVAILABLE',
  `base_price` decimal(15,2) DEFAULT NULL,
  `description` text COLLATE utf8mb4_unicode_ci,
  `is_deleted` tinyint(1) DEFAULT '0'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


INSERT INTO `apartments` (`id`, `floor_id`, `room_number`, `area`, `status`, `base_price`, `description`, `is_deleted`) VALUES
(1, 2, 'P201', 55.5, 'RENTED', 5000000.00, NULL, 0),
(2, 2, 'P202', 70, 'AVAILABLE', 7500000.00, NULL, 1),
(3, 1, 'P201', 55.5, 'Available', NULL, NULL, 0),
(4, 1, '5', 14, 'Available', NULL, NULL, 0);

-- --------------------------------------------------------

CREATE TABLE `buildings` (
  `id` bigint NOT NULL,
  `name` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  `address` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `manager_name` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `description` text COLLATE utf8mb4_unicode_ci,
  `is_deleted` tinyint(1) DEFAULT '0'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;



INSERT INTO `buildings` (`id`, `name`, `address`, `manager_name`, `description`, `is_deleted`) VALUES
(1, 'Sunshine Riverside', '123 Đường 3/2, Q. Ninh Kiều, Cần Thơ', 'Nguyễn Văn Quản Lý', 'Tòa nhà cao cấp view sông Hậu, đầy đủ tiện ích.', 0),
(2, 'VinHomes Central Park', '208 Nguyễn Hữu Cảnh, Bình Thạnh, TP.HCM', 'Phạm Thị Ban Quản Trị', 'Khu phức hợp căn hộ và công viên ven sông.', 0),
(3, 'Chung Cư Hưng Phú', 'Khu Dân Cư Hưng Phú 1, Q. Cái Răng', 'Lê Văn Bảo Vệ', 'Chung cư nhà ở xã hội, an ninh tốt.', 0),
(4, 'Tòa Nhà FPT Plaza', 'Đường Võ Chí Công, Q. Ngũ Hành Sơn', 'Trần Kỹ Thuật', 'Căn hộ dành cho nhân viên công nghệ.', 0);



CREATE TABLE `contracts` (
  `id` bigint NOT NULL,
  `apartment_id` bigint NOT NULL,
  `resident_id` bigint NOT NULL,
  `start_date` date NOT NULL,
  `end_date` date DEFAULT NULL,
  `deposit_amount` decimal(15,2) DEFAULT NULL,
  `status` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT 'ACTIVE',
  `is_deleted` tinyint(1) DEFAULT '0'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;



INSERT INTO `contracts` (`id`, `apartment_id`, `resident_id`, `start_date`, `end_date`, `deposit_amount`, `status`, `is_deleted`) VALUES
(1, 1, 1, '2026-01-09', NULL, 5000000.00, 'ACTIVE', 0);


CREATE TABLE `floors` (
  `id` bigint NOT NULL,
  `building_id` bigint NOT NULL,
  `floor_number` int NOT NULL,
  `name` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `is_deleted` tinyint(1) DEFAULT '0'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;



INSERT INTO `floors` (`id`, `building_id`, `floor_number`, `name`, `is_deleted`) VALUES
-- Tòa 1: Sunshine Riverside
(1, 1, 1, 'Tầng 1 - Sảnh Thương Mại', 0),
(2, 1, 2, 'Tầng 2 - Khu Căn Hộ Cao Cấp', 0),
(3, 1, 3, 'Tầng 3 - Khu Căn Hộ Cao Cấp', 0),

-- Tòa 2: VinHomes Central Park
(4, 2, 1, 'Tầng G - Shophouse', 0),
(5, 2, 2, 'Tầng 2 - Căn Hộ View Sông', 0),
(6, 2, 3, 'Tầng 3 - Căn Hộ View Sông', 0),

-- Tòa 3: Chung Cư Hưng Phú
(7, 3, 1, 'Tầng Trệt - Nhà Xe & Kỹ Thuật', 0),
(8, 3, 2, 'Tầng 2 - Căn Hộ Giá Rẻ', 0);

-- --------------------------------------------------------

--
-- Table structure for table `invoices`
--

CREATE TABLE `invoices` (
  `id` bigint NOT NULL,
  `contract_id` bigint NOT NULL,
  `month` int NOT NULL,
  `year` int NOT NULL,
  `total_amount` decimal(15,2) DEFAULT '0.00',
  `status` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT 'UNPAID',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `payment_date` timestamp NULL DEFAULT NULL,
  `is_deleted` tinyint(1) DEFAULT '0'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

structure for table `invoice_details`
--

CREATE TABLE `invoice_details` (
  `id` bigint NOT NULL,
  `invoice_id` bigint NOT NULL,
  `service_name` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `unit_price` decimal(15,2) DEFAULT NULL,
  `quantity` double DEFAULT NULL,
  `amount` decimal(15,2) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------

--
-- Table structure for table `residents`
--

CREATE TABLE `residents` (
  `id` bigint NOT NULL,
  `full_name` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  `phone` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL,
  `email` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `identity_card` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL,
  `gender` varchar(10) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `dob` date DEFAULT NULL,
  `hometown` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `is_deleted` tinyint(1) DEFAULT '0'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Dumping data for table `residents`
--

INSERT INTO `residents` (`id`, `full_name`, `phone`, `email`, `identity_card`, `gender`, `dob`, `hometown`, `is_deleted`) VALUES
(1, 'Trần Văn Khánh', '0909123456', NULL, '0123456789', 'Nam', NULL, NULL, 0);

-- --------------------------------------------------------

--
-- Table structure for table `services`
--

CREATE TABLE `services` (
  `id` bigint NOT NULL,
  `service_name` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  `unit_price` decimal(15,2) NOT NULL,
  `unit_type` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `is_mandatory` tinyint(1) DEFAULT '0',
  `is_deleted` tinyint(1) DEFAULT '0'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci; 



INSERT INTO `services` (`id`, `service_name`, `unit_price`, `unit_type`, `is_mandatory`, `is_deleted`) VALUES
(1, 'Tiền Điện Sinh Hoạt', 3500.00, 'KWH', 1, 0),        -- Bắt buộc
(2, 'Tiền Nước Sạch', 18000.00, 'KHOI', 1, 0),          -- Bắt buộc
(3, 'Phí Quản Lý Chung Cư', 250000.00, 'THANG', 1, 0),  -- Bắt buộc
(4, 'Phí Gửi Xe Máy', 120000.00, 'XE', 0, 0),           -- Không bắt buộc
(5, 'Phí Gửi Ô Tô', 1500000.00, 'XE', 0, 0),            -- Không bắt buộc
(6, 'Internet Viettel', 220000.00, 'THANG', 0, 0),      -- Không bắt buộc
(7, 'Vệ Sinh Căn Hộ', 50000.00, 'GIO', 0, 0);           -- Dịch vụ thêm

-- --------------------------------------------------------

--
-- Table structure for table `service_usage`
--

CREATE TABLE `service_usage` (
  `id` bigint NOT NULL,
  `contract_id` bigint NOT NULL,
  `service_id` bigint NOT NULL,
  `month` int NOT NULL,
  `year` int NOT NULL,
  `old_index` double DEFAULT '0',
  `new_index` double DEFAULT '0',
  `actual_usage` double DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------

--
-- Table structure for table `users`
--

CREATE TABLE `users` (
  `id` bigint NOT NULL,
  `username` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL,
  `password` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `full_name` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  `role` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT 'STAFF',
  `is_active` tinyint(1) DEFAULT '1',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `last_login` timestamp NULL DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Dumping data for table `users`
--

INSERT INTO `users` (`id`, `username`, `password`, `full_name`, `role`, `is_active`, `created_at`, `last_login`) VALUES
(1, 'admin', 'admin123', 'Administrator', 'ADMIN', 1, '2026-01-13 08:05:08', '2026-01-13 09:51:15'),
(3, 'staff', 'staff123', 'Staff Member', 'STAFF', 1, '2026-01-13 09:03:05', '2026-01-13 09:07:15'),
(4, 'accountant', 'acc123', 'Accountant', 'ACCOUNTANT', 1, '2026-01-13 09:03:05', '2026-01-13 09:08:35');

--
-- Indexes for dumped tables
--

--
-- Indexes for table `apartments`
--
ALTER TABLE `apartments`
  ADD PRIMARY KEY (`id`),
  ADD KEY `floor_id` (`floor_id`);

--
-- Indexes for table `buildings`
--
ALTER TABLE `buildings`
  ADD PRIMARY KEY (`id`);

--
-- Indexes for table `contracts`
--
ALTER TABLE `contracts`
  ADD PRIMARY KEY (`id`),
  ADD KEY `apartment_id` (`apartment_id`),
  ADD KEY `resident_id` (`resident_id`);

--
-- Indexes for table `floors`
--
ALTER TABLE `floors`
  ADD PRIMARY KEY (`id`),
  ADD KEY `building_id` (`building_id`);

--
-- Indexes for table `invoices`
--
ALTER TABLE `invoices`
  ADD PRIMARY KEY (`id`),
  ADD KEY `contract_id` (`contract_id`);

--
-- Indexes for table `invoice_details`
--
ALTER TABLE `invoice_details`
  ADD PRIMARY KEY (`id`),
  ADD KEY `invoice_id` (`invoice_id`);

--
-- Indexes for table `residents`
--
ALTER TABLE `residents`
  ADD PRIMARY KEY (`id`);

--
-- Indexes for table `services`
--
ALTER TABLE `services`
  ADD PRIMARY KEY (`id`);

--
-- Indexes for table `service_usage`
--
ALTER TABLE `service_usage`
  ADD PRIMARY KEY (`id`),
  ADD KEY `contract_id` (`contract_id`),
  ADD KEY `service_id` (`service_id`);

--
-- Indexes for table `users`
--
ALTER TABLE `users`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `username` (`username`);


--
ALTER TABLE `apartments`
  MODIFY `id` bigint NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=5;

--
-- AUTO_INCREMENT for table `buildings`
--
ALTER TABLE `buildings`
  MODIFY `id` bigint NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=6;

--
-- AUTO_INCREMENT for table `contracts`
--
ALTER TABLE `contracts`
  MODIFY `id` bigint NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=2;

--
-- AUTO_INCREMENT for table `floors`
--
ALTER TABLE `floors`
  MODIFY `id` bigint NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=4;

--
-- AUTO_INCREMENT for table `invoices`
--
ALTER TABLE `invoices`
  MODIFY `id` bigint NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `invoice_details`
--
ALTER TABLE `invoice_details`
  MODIFY `id` bigint NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `residents`
--
ALTER TABLE `residents`
  MODIFY `id` bigint NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=2;

--
-- AUTO_INCREMENT for table `services`
--
ALTER TABLE `services`
  MODIFY `id` bigint NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=5;

--
-- AUTO_INCREMENT for table `service_usage`
--
ALTER TABLE `service_usage`
  MODIFY `id` bigint NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `users`
--
ALTER TABLE `users`
  MODIFY `id` bigint NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=5;

--

--
ALTER TABLE `apartments`
  ADD CONSTRAINT `apartments_ibfk_1` FOREIGN KEY (`floor_id`) REFERENCES `floors` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE;

--
-- Constraints for table `contracts`
--
ALTER TABLE `contracts`
  ADD CONSTRAINT `contracts_ibfk_1` FOREIGN KEY (`apartment_id`) REFERENCES `apartments` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE,
  ADD CONSTRAINT `contracts_ibfk_2` FOREIGN KEY (`resident_id`) REFERENCES `residents` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE;

--
-- Constraints for table `floors`
--
ALTER TABLE `floors`
  ADD CONSTRAINT `floors_ibfk_1` FOREIGN KEY (`building_id`) REFERENCES `buildings` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE;

--
-- Constraints for table `invoices`
--
ALTER TABLE `invoices`
  ADD CONSTRAINT `invoices_ibfk_1` FOREIGN KEY (`contract_id`) REFERENCES `contracts` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE;

--
-- Constraints for table `invoice_details`
--
ALTER TABLE `invoice_details`
  ADD CONSTRAINT `invoice_details_ibfk_1` FOREIGN KEY (`invoice_id`) REFERENCES `invoices` (`id`) ON DELETE CASCADE ON UPDATE CASCADE;

--
-- Constraints for table `service_usage`
--
ALTER TABLE `service_usage`
  ADD CONSTRAINT `service_usage_ibfk_1` FOREIGN KEY (`contract_id`) REFERENCES `contracts` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE,
  ADD CONSTRAINT `service_usage_ibfk_2` FOREIGN KEY (`service_id`) REFERENCES `services` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE;
COMMIT;

