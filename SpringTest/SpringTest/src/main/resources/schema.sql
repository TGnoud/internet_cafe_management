-- Tạo bảng maytinh nếu chưa tồn tại
CREATE TABLE IF NOT EXISTS maytinh (
    ma_may VARCHAR(10) PRIMARY KEY,
    ten_may VARCHAR(100) NOT NULL,
    trang_thai VARCHAR(20) NOT NULL,
    loai_may VARCHAR(50)
);

-- Tạo bảng chucvu nếu chưa tồn tại
CREATE TABLE IF NOT EXISTS chucvu (
    ma_chuc_vu VARCHAR(10) PRIMARY KEY,
    ten_chuc_vu VARCHAR(100) NOT NULL,
    luong_theo_gio DECIMAL(10,2) NOT NULL
);

-- Tạo bảng nhanvien nếu chưa tồn tại
CREATE TABLE IF NOT EXISTS nhanvien (
    ma_nv VARCHAR(10) PRIMARY KEY,
    ho_ten VARCHAR(100) NOT NULL,
    chuc_vu VARCHAR(10),
    so_dien_thoai VARCHAR(15),
    FOREIGN KEY (chuc_vu) REFERENCES chucvu(ma_chuc_vu)
); 