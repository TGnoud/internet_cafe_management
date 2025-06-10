-- -----------------------------------------------------
-- Kiểm tra trạng thái máy
-- -----------------------------------------------------


DELIMITER $$

CREATE PROCEDURE KiemTraTrangThaiMay (
    IN p_MaMay VARCHAR(10),
    OUT p_TrangThai VARCHAR(45)
)
BEGIN
    DECLARE v_DangSuDung INT;

    -- Lấy trạng thái từ bảng MayTinh
    SELECT TrangThai
    INTO p_TrangThai
    FROM MayTinh
    WHERE MaMay = p_MaMay;

    -- Kiểm tra xem máy có đang được sử dụng (phiên chưa kết thúc)
    SELECT COUNT(*)
    INTO v_DangSuDung
    FROM PhienSuDung
    WHERE MaMay = p_MaMay AND ThoiGianKetThuc IS NULL;

    IF v_DangSuDung > 0 THEN
        SET p_TrangThai = 'Đang sử dụng';
    END IF;
END $$
DELIMITER ;

CALL KiemTraTrangThaiMay('PC01', @TrangThai);
SELECT @TrangThai AS TrangThai;
###### Nap tien
DELIMITER $$

CREATE PROCEDURE sp_NhanVien_NapTien(
    IN p_MaTK VARCHAR(10),
    IN p_SoTienNap DECIMAL(10, 2)
)
BEGIN
    -- Kiểm tra số tiền nạp vào phải là số dương
    IF p_SoTienNap > 0 THEN
        UPDATE TaiKhoan
        SET SoTienConLai = SoTienConLai + p_SoTienNap
        WHERE MaTK = p_MaTK;

        SELECT 'Nạp tiền thành công!' AS Message;
    ELSE
        -- Ném ra một lỗi nếu số tiền nạp không hợp lệ
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Số tiền nạp phải lớn hơn 0.';
    END IF;
END$$

DELIMITER ;

### khach hang mua dich vu
DELIMITER $$

CREATE PROCEDURE sp_KhachHang_GiaoDich(
    IN p_MaDV VARCHAR(10),
    IN p_SoLuong INT
)
SQL SECURITY DEFINER -- Chạy với quyền của người tạo để đảm bảo bảo mật
BEGIN
    -- Khai báo các biến
    DECLARE v_TenDangNhap VARCHAR(50);
    DECLARE v_MaTK VARCHAR(10);
    DECLARE v_SoDuHienTai DECIMAL(10, 2);
    DECLARE v_DonGia DECIMAL(10, 2);
    DECLARE v_LoaiDichVu VARCHAR(10);
    DECLARE v_TrangThaiDichVu VARCHAR(45);
    DECLARE v_ThanhTien DECIMAL(10, 2);
    DECLARE v_MaHD_moi VARCHAR(15);
    DECLARE v_SoLuongGhiNhan INT;

    -- *** SỬA LỖI TẠI ĐÂY ***
    -- Lấy tên đăng nhập từ biến phiên do người dùng thiết lập
    SET v_TenDangNhap = @ten_tk_hien_tai;

    -- Kiểm tra xem biến phiên đã được thiết lập chưa
    IF v_TenDangNhap IS NULL THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Lỗi: Phiên làm việc không hợp lệ. Vui lòng thiết lập lại danh tính.';
    END IF;

    -- ---- BƯỚC KIỂM TRA DỮ LIỆU ----
    SELECT MaTK, SoTienConLai INTO v_MaTK, v_SoDuHienTai
    FROM TaiKhoan WHERE TenTK = v_TenDangNhap;

    IF v_MaTK IS NULL THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Lỗi: Không tìm thấy tài khoản người dùng từ danh tính đã cung cấp.';
    END IF;

    SELECT DonGia, LoaiDichVu, TrangThaiDichVu INTO v_DonGia, v_LoaiDichVu, v_TrangThaiDichVu
    FROM DichVu WHERE MaDV = p_MaDV;

    IF v_DonGia IS NULL THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Lỗi: Mã dịch vụ không hợp lệ.';
    END IF;

    IF v_TrangThaiDichVu <> 'Còn hàng' THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Lỗi: Dịch vụ này hiện không có sẵn.';
    END IF;

    -- ---- BƯỚC XỬ LÝ GIAO DỊCH ----
    START TRANSACTION;

    IF v_LoaiDichVu = 'NAP_TIEN' THEN
        SET v_SoLuongGhiNhan = 1;
        SET v_ThanhTien = v_DonGia;
        UPDATE TaiKhoan SET SoTienConLai = SoTienConLai + v_DonGia WHERE MaTK = v_MaTK;
    ELSE
        SET v_SoLuongGhiNhan = p_SoLuong;
        SET v_ThanhTien = v_DonGia * v_SoLuongGhiNhan;

        IF v_SoDuHienTai < v_ThanhTien THEN
            ROLLBACK;
            SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Số dư tài khoản không đủ để thực hiện giao dịch.';
        END IF;

        UPDATE TaiKhoan SET SoTienConLai = SoTienConLai - v_ThanhTien WHERE MaTK = v_MaTK;
    END IF;

    -- ---- BƯỚC GHI NHẬN HÓA ĐƠN ----
    SET v_MaHD_moi = CONCAT('HD', DATE_FORMAT(NOW(), '%y%m%d%H%i%s'));

    INSERT INTO HoaDonDV (MaHD, MaTK, ThoiDiemThanhToan)
    VALUES (v_MaHD_moi, v_MaTK, NOW());

    INSERT INTO CT_HoaDonDV (MaHD, MaDV, SoLuong)
    VALUES (v_MaHD_moi, p_MaDV, v_SoLuongGhiNhan);

    COMMIT;

    SELECT 'Giao dịch thành công!' AS Message, v_MaHD_moi AS MaHoaDon;

END$$

DELIMITER ;

## lay thong tin
DROP PROCEDURE IF EXISTS sp_NhanVien_LapHoaDon;
DROP PROCEDURE IF EXISTS sp_NhanVien_LapHoaDon;
DELIMITER $$

CREATE PROCEDURE sp_NhanVien_LapHoaDon(
    IN p_MaTK VARCHAR(10),      -- Mã tài khoản của khách hàng
    IN p_MaNV VARCHAR(10),      -- Mã của nhân viên đang thực hiện
    IN p_ChiTietGiaoDich JSON   -- Danh sách các dịch vụ dưới dạng JSON
)
BEGIN
    -- Khai báo biến
    DECLARE v_MaHD_moi VARCHAR(15);
    DECLARE v_SoTienCongThem_VaoTaiKhoan DECIMAL(12, 2) DEFAULT 0.00;
    DECLARE v_TongTienMuaHang_Goc DECIMAL(12, 2) DEFAULT 0.00;
    DECLARE v_TongTienDichVu_Goc DECIMAL(12, 2) DEFAULT 0.00; -- Biến mới để tính tổng
    DECLARE v_TienGiam DECIMAL(12, 2) DEFAULT 0.00;
    DECLARE v_TongTienThanhToan_Final DECIMAL(12, 2) DEFAULT 0.00;
    DECLARE v_MaUuDai_Auto VARCHAR(10) DEFAULT NULL;
    DECLARE v_PhanTramUuDai_Auto DECIMAL(5, 2) DEFAULT 0.00;

    -- Khai báo handler để rollback khi có lỗi
    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        ROLLBACK;
        RESIGNAL;
    END;

    START TRANSACTION;

    -- 1. LẤY THÔNG TIN ƯU ĐÃI CỦA KHÁCH HÀNG
    SELECT lkh.MaUuDai, IFNULL(u.MucUuDai, 0)
    INTO v_MaUuDai_Auto, v_PhanTramUuDai_Auto
    FROM TaiKhoan tk
    JOIN KhachHang kh ON tk.MaKH = kh.MaKH
    JOIN LoaiKH lkh ON kh.MaLoaiKH = lkh.MaLoaiKH
    LEFT JOIN UuDai u ON lkh.MaUuDai = u.MaUuDai
    WHERE tk.MaTK = p_MaTK;
    
    IF v_MaUuDai_Auto IS NULL THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Lỗi: Không tìm thấy tài khoản khách hàng với mã cung cấp.';
    END IF;

    -- 2. TẠO HÓA ĐƠN CHÍNH
    SET v_MaHD_moi = CONCAT('HD', DATE_FORMAT(NOW(), '%y%m%d%H%i%s'));
    INSERT INTO HoaDonDV (MaHD, MaTK, MaNV, MaUuDai, ThoiDiemThanhToan)
    VALUES (v_MaHD_moi, p_MaTK, p_MaNV, v_MaUuDai_Auto, NOW());

    -- 3. THÊM CHI TIẾT HÓA ĐƠN
    INSERT INTO CT_HoaDonDV (MaHD, MaDV, SoLuong)
    SELECT v_MaHD_moi, j.ma_dv, j.so_luong
    FROM JSON_TABLE(p_ChiTietGiaoDich, '$[*]' COLUMNS (ma_dv VARCHAR(10) PATH '$.ma_dv', so_luong INT PATH '$.so_luong')) AS j;

    -- 4. TÍNH TOÁN CÁC GIÁ TRỊ THEO LOGIC MỚI
    -- a. Tính tổng tiền "NAP_TIEN"
    SELECT COALESCE(SUM(d.DonGia * j.so_luong), 0) INTO v_SoTienCongThem_VaoTaiKhoan
    FROM JSON_TABLE(p_ChiTietGiaoDich, '$[*]' COLUMNS (ma_dv VARCHAR(10) PATH '$.ma_dv', so_luong INT PATH '$.so_luong')) AS j
    JOIN DichVu d ON j.ma_dv = d.MaDV WHERE d.LoaiDichVu = 'NAP_TIEN';

    -- b. Tính tổng tiền "MUA_HANG"
    SELECT COALESCE(SUM(d.DonGia * j.so_luong), 0) INTO v_TongTienMuaHang_Goc
    FROM JSON_TABLE(p_ChiTietGiaoDich, '$[*]' COLUMNS (ma_dv VARCHAR(10) PATH '$.ma_dv', so_luong INT PATH '$.so_luong')) AS j
    JOIN DichVu d ON j.ma_dv = d.MaDV WHERE d.LoaiDichVu = 'MUA_HANG';
    
    -- c. TÍNH TỔNG GIÁ TRỊ GỐC CỦA TẤT CẢ DỊCH VỤ (TIỀN NẠP + TIỀN HÀNG)
    SET v_TongTienDichVu_Goc = v_SoTienCongThem_VaoTaiKhoan + v_TongTienMuaHang_Goc;

    -- d. ÁP DỤNG ƯU ĐÃI TRÊN TỔNG TIỀN TIÊU
    IF v_PhanTramUuDai_Auto > 0 AND v_TongTienDichVu_Goc > 0 THEN
        SET v_TienGiam = ROUND(v_TongTienDichVu_Goc * (v_PhanTramUuDai_Auto / 100.0), 2);
    END IF;
    
    -- e. TÍNH TOÁN LẠI TỔNG THANH TOÁN CUỐI CÙNG
    SET v_TongTienThanhToan_Final = v_TongTienDichVu_Goc - v_TienGiam;

    -- 5. CẬP NHẬT TÀI KHOẢN KHÁCH HÀNG (CHỈ CỘNG TIỀN NẠP)
    IF v_SoTienCongThem_VaoTaiKhoan > 0 THEN
        UPDATE TaiKhoan SET SoTienConLai = SoTienConLai + v_SoTienCongThem_VaoTaiKhoan WHERE MaTK = p_MaTK;
    END IF;

    COMMIT;

    -- TRẢ VỀ KẾT QUẢ CHO NHÂN VIÊN
    SELECT 
        'Giao dịch thành công!' AS Message, 
        v_MaHD_moi AS MaHoaDonVuaTao,
        v_TongTienDichVu_Goc AS TongTienGoc,
        v_TienGiam AS TienGiamGia,
        v_TongTienThanhToan_Final AS TongTienMatCanThu;

END$$
DELIMITER ;

CALL sp_NhanVien_LapHoaDon(
    'TK003', 'NV003',
    '[{"ma_dv": "DV003", "so_luong": 1}, {"ma_dv": "DV004", "so_luong": 1}]'
);

