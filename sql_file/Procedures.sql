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
DELIMITER $$

CREATE PROCEDURE `sp_LayThongTinCaNhan`()
BEGIN
    -- Lấy tên người dùng đang đăng nhập
    DECLARE currentUserTenTK VARCHAR(255);
    SET currentUserTenTK = SUBSTRING_INDEX(CURRENT_USER(), '@', 1);

    -- Truy vấn dữ liệu dựa trên tên người dùng đó
    SELECT 
        tk.MaTK, 
        tk.TenTK, 
        tk.SoTienConLai, 
        kh.HoTen, 
        kh.SoDienThoai
    FROM 
        TaiKhoan AS tk
    JOIN 
        KhachHang AS kh ON tk.MaKH = kh.MaKH
    WHERE 
        tk.TenTK = currentUserTenTK;
END$$

DELIMITER ;

DELIMITER $$

CREATE PROCEDURE `sp_xem_thong_tin_ca_nhan`()
SQL SECURITY DEFINER
BEGIN
    SELECT
        tk.MaTK,
        tk.TenTK,
        tk.SoTienConLai,
        kh.HoTen,
        kh.SoDienThoai
    FROM
        TaiKhoan AS tk
    JOIN
        KhachHang AS kh ON tk.MaKH = kh.MaKH
    WHERE
        tk.TenTK = @ten_tk_hien_tai;
END$$

DELIMITER ;
