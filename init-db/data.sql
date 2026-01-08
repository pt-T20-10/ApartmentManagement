-- 1. Tạo Database
CREATE DATABASE IF NOT EXISTS DB_QuanLyChungCu CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE DB_QuanLyChungCu;

-- =======================================================
-- PHẦN 1: CẤU TRÚC TÒA NHÀ (Building -> Floor -> Apartment)
-- =======================================================

-- 2. Bảng Tòa Nhà (Buildings)
CREATE TABLE buildings (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,         -- Tên tòa (VD: Landmark 81, Tòa S1)
    address VARCHAR(255),               -- Địa chỉ
    manager_name VARCHAR(100),          -- Tên quản lý tòa nhà
    description TEXT,
    is_deleted BOOLEAN DEFAULT FALSE    -- Xóa mềm
);

-- 3. Bảng Tầng (Floors)
CREATE TABLE floors (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    building_id BIGINT NOT NULL,        -- Thuộc tòa nào
    floor_number INT NOT NULL,          -- Số tầng (1, 2, 13...)
    name VARCHAR(50),                   -- Tên hiển thị (Tầng trệt, Tầng thượng)
    is_deleted BOOLEAN DEFAULT FALSE,   -- Xóa mềm
    
    FOREIGN KEY (building_id) REFERENCES buildings(id) ON UPDATE CASCADE ON DELETE RESTRICT
);

-- 4. Bảng Căn Hộ (Apartments)
CREATE TABLE apartments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    floor_id BIGINT NOT NULL,           -- Thuộc tầng nào
    room_number VARCHAR(20) NOT NULL,   -- Số phòng (P101, P205)
    area DOUBLE,                        -- Diện tích (m2)
    status VARCHAR(20) DEFAULT 'AVAILABLE', -- Trạng thái: AVAILABLE (Trống), RENTED (Đã thuê), MAINTAINING (Bảo trì)
    base_price DECIMAL(15, 2),          -- Giá thuê cơ bản
    description TEXT,
    is_deleted BOOLEAN DEFAULT FALSE,   -- Xóa mềm
    
    FOREIGN KEY (floor_id) REFERENCES floors(id) ON UPDATE CASCADE ON DELETE RESTRICT
);

-- =======================================================
-- PHẦN 2: CƯ DÂN & HỢP ĐỒNG
-- =======================================================

-- 5. Bảng Cư Dân (Residents)
CREATE TABLE residents (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    full_name VARCHAR(100) NOT NULL,
    phone VARCHAR(20) NOT NULL,
    email VARCHAR(100),
    identity_card VARCHAR(20) NOT NULL, -- CCCD/CMND
    gender VARCHAR(10),                 -- Nam/Nữ
    dob DATE,                           -- Ngày sinh
    hometown VARCHAR(255),              -- Quê quán
    is_deleted BOOLEAN DEFAULT FALSE    -- Xóa mềm
);

-- 6. Bảng Hợp Đồng (Contracts)
CREATE TABLE contracts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    apartment_id BIGINT NOT NULL,
    resident_id BIGINT NOT NULL,
    start_date DATE NOT NULL,           -- Ngày bắt đầu thuê
    end_date DATE,                      -- Ngày kết thúc (NULL nếu thuê dài hạn)
    deposit_amount DECIMAL(15, 2),      -- Tiền cọc
    status VARCHAR(20) DEFAULT 'ACTIVE',-- ACTIVE, EXPIRED, TERMINATED
    is_deleted BOOLEAN DEFAULT FALSE,   -- Xóa mềm
    
    FOREIGN KEY (apartment_id) REFERENCES apartments(id) ON UPDATE CASCADE ON DELETE RESTRICT,
    FOREIGN KEY (resident_id) REFERENCES residents(id) ON UPDATE CASCADE ON DELETE RESTRICT
);

-- =======================================================
-- PHẦN 3: DỊCH VỤ & HÓA ĐƠN
-- =======================================================

-- 7. Bảng Danh Mục Dịch Vụ (Services)
CREATE TABLE services (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    service_name VARCHAR(100) NOT NULL, -- VD: Điện, Nước, Internet
    unit_price DECIMAL(15, 2) NOT NULL, -- Đơn giá hiện tại
    unit_type VARCHAR(20),              -- Đơn vị: KWH, KHOI, THANG
    is_mandatory BOOLEAN DEFAULT FALSE, -- Bắt buộc? (Điện/Nước là bắt buộc)
    is_deleted BOOLEAN DEFAULT FALSE    -- Xóa mềm
);

-- 8. Bảng Ghi Chỉ Số Sử Dụng (Service Usage) - Log hàng tháng
CREATE TABLE service_usage (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    contract_id BIGINT NOT NULL,
    service_id BIGINT NOT NULL,
    month INT NOT NULL,
    year INT NOT NULL,
    
    old_index DOUBLE DEFAULT 0,         -- Chỉ số cũ
    new_index DOUBLE DEFAULT 0,         -- Chỉ số mới
    actual_usage DOUBLE,                -- Số lượng dùng = Mới - Cũ
    
    FOREIGN KEY (contract_id) REFERENCES contracts(id) ON UPDATE CASCADE ON DELETE RESTRICT,
    FOREIGN KEY (service_id) REFERENCES services(id) ON UPDATE CASCADE ON DELETE RESTRICT
);

-- 9. Bảng Hóa Đơn Tổng (Invoices)
CREATE TABLE invoices (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    contract_id BIGINT NOT NULL,
    month INT NOT NULL,
    year INT NOT NULL,
    total_amount DECIMAL(15, 2) DEFAULT 0, -- Tổng tiền phải trả
    status VARCHAR(20) DEFAULT 'UNPAID',   -- UNPAID, PAID, PARTIAL
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    payment_date TIMESTAMP NULL,           -- Ngày khách trả tiền
    is_deleted BOOLEAN DEFAULT FALSE,      -- Hủy hóa đơn (Xóa mềm)
    
    FOREIGN KEY (contract_id) REFERENCES contracts(id) ON UPDATE CASCADE ON DELETE RESTRICT
);

-- 10. Bảng Chi Tiết Hóa Đơn (Invoice Details)
-- Lưu bản chụp (Snapshot) giá tại thời điểm tạo hóa đơn
CREATE TABLE invoice_details (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    invoice_id BIGINT NOT NULL,
    service_name VARCHAR(100),          -- Lưu cứng tên dịch vụ
    unit_price DECIMAL(15, 2),          -- Lưu cứng đơn giá lúc đó
    quantity DOUBLE,                    -- Số lượng dùng
    amount DECIMAL(15, 2),              -- Thành tiền = Giá * Số lượng
    
    FOREIGN KEY (invoice_id) REFERENCES invoices(id) ON UPDATE CASCADE ON DELETE CASCADE
);

-- =======================================================
-- PHẦN 4: DỮ LIỆU MẪU (SEED DATA) - Để test ngay
-- =======================================================

-- Tạo Tòa nhà
INSERT INTO buildings (name, address, manager_name) VALUES 
('Tòa S1 - Sunshine', '123 Đường A, Quận Ninh Kiều', 'Nguyễn Văn Quản Lý');

-- Tạo Tầng (Tòa S1 có 2 tầng mẫu)
INSERT INTO floors (building_id, floor_number, name) VALUES 
(1, 1, 'Tầng 1 - Thương mại'),
(1, 2, 'Tầng 2 - Căn hộ');

-- Tạo Phòng (Tầng 2 có 2 phòng)
INSERT INTO apartments (floor_id, room_number, area, base_price, status) VALUES 
(2, 'P201', 55.5, 5000000, 'AVAILABLE'),
(2, 'P202', 70.0, 7500000, 'AVAILABLE');

-- Tạo Dịch vụ cơ bản
INSERT INTO services (service_name, unit_price, unit_type, is_mandatory) VALUES 
('Tiền Điện', 3500, 'KWH', TRUE),
('Tiền Nước', 15000, 'KHOI', TRUE),
('Phí Quản Lý', 200000, 'THANG', TRUE),
('Gửi Xe Máy', 100000, 'XE', FALSE);

-- Tạo Cư dân mẫu
INSERT INTO residents (full_name, phone, identity_card, gender) VALUES 
('Trần Văn Khách', '0909123456', '0123456789', 'Nam');

-- Tạo Hợp đồng thuê (Khách thuê phòng P201)
INSERT INTO contracts (apartment_id, resident_id, start_date, deposit_amount, status) VALUES 
(1, 1, CURDATE(), 5000000, 'ACTIVE');

-- Cập nhật trạng thái phòng thành Đã thuê
UPDATE apartments SET status = 'RENTED' WHERE id = 1;