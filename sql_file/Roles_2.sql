-- Đảm bảo bạn đang sử dụng đúng cơ sở dữ liệu
USE quan_ly_quan_net;

-- -----------------------------------------------------
-- TẠO ROLES
-- -----------------------------------------------------

-- Tạo các roles mới với tên đã sửa
CREATE ROLE IF NOT EXISTS 'khach_hang_role'@'%';
CREATE ROLE IF NOT EXISTS 'nhan_vien_co_ban_role'@'%';
CREATE ROLE IF NOT EXISTS 'quan_ly_cua_hang_role'@'%';
CREATE ROLE IF NOT EXISTS 'quan_tri_vien_db_role'@'%';


-- -----------------------------------------------------
-- CẤP QUYỀN CHO ROLE: khach_hang_role
-- -----------------------------------------------------
-- Quyền SELECT trên các View dành cho Khách hàng
GRANT SELECT ON quan_ly_quan_net.View_kh_danhsachdichvu TO 'khach_hang_role'@'%';
GRANT SELECT ON quan_ly_quan_net.View_kh_danhsachmayvatrangthai TO 'khach_hang_role'@'%';
GRANT SELECT ON quan_ly_quan_net.View_KH_ThongTinCaNhan TO 'khach_hang_role'@'%';

## procedure for customer
GRANT EXECUTE ON PROCEDURE quan_ly_quan_net.sp_KhachHang_GiaoDich TO 'khach_hang_role'@'%';
GRANT EXECUTE ON PROCEDURE quan_ly_quan_net.sp_xem_thong_tin_ca_nhan TO 'khach_hang_role'@'%';


-- -----------------------------------------------------
-- CẤP QUYỀN CHO ROLE: nhan_vien_co_ban_role
-- -----------------------------------------------------
-- Quyền của nhân viên bao gồm quyền của khách hàng
GRANT 'khach_hang_role' TO 'nhan_vien_co_ban_role'@'%';


-- Quyền SELECT trên các View dành cho Nhân viên
GRANT SELECT ON quan_ly_quan_net.View_NV_TrangThaiMay TO 'nhan_vien_co_ban_role'@'%';
GRANT SELECT ON quan_ly_quan_net.View_NV_thongtincanhan TO 'nhan_vien_co_ban_role'@'%';
GRANT SELECT ON quan_ly_quan_net.View_NV_lichsumuadichvu TO 'nhan_vien_co_ban_role'@'%';
GRANT SELECT ON quan_ly_quan_net.View_NV_PhienDangHoatDongChiTiet TO 'nhan_vien_co_ban_role'@'%';
GRANT SELECT ON quan_ly_quan_net.View_NV_LichLamViecNhanVien TO 'nhan_vien_co_ban_role'@'%';
GRANT SELECT ON quan_ly_quan_net.View_KH_DanhSachMayVaTrangThai TO 'nhan_vien_co_ban_role'@'%'; -- Nhân viên có thể cần xem view này
GRANT EXECUTE ON PROCEDURE quan_ly_quan_net.sp_NhanVien_NapTien TO 'nhan_vien_co_ban_role'@'%';

-- Quyền SELECT trên các bảng cần thiết để tra cứu thông tin
GRANT SELECT ON quan_ly_quan_net.KhachHang TO 'nhan_vien_co_ban_role'@'%';
GRANT SELECT ON quan_ly_quan_net.TaiKhoan TO 'nhan_vien_co_ban_role'@'%';
GRANT SELECT ON quan_ly_quan_net.MayTinh TO 'nhan_vien_co_ban_role'@'%';
GRANT SELECT ON quan_ly_quan_net.LoaiMay TO 'nhan_vien_co_ban_role'@'%';
GRANT SELECT ON quan_ly_quan_net.DichVu TO 'nhan_vien_co_ban_role'@'%';
GRANT SELECT ON quan_ly_quan_net.UuDai TO 'nhan_vien_co_ban_role'@'%';
GRANT SELECT ON quan_ly_quan_net.LoaiKH TO 'nhan_vien_co_ban_role'@'%';
GRANT SELECT ON quan_ly_quan_net.PhienSuDung TO 'nhan_vien_co_ban_role'@'%';
GRANT SELECT ON quan_ly_quan_net.HoaDonDV TO 'nhan_vien_co_ban_role'@'%';
GRANT SELECT ON quan_ly_quan_net.CT_HoaDonDV TO 'nhan_vien_co_ban_role'@'%';
GRANT SELECT (MaNV, HoTen, MaChucVu) ON quan_ly_quan_net.NhanVien TO 'nhan_vien_co_ban_role'@'%'; -- Giới hạn cột cho thông tin nhân viên khác
GRANT SELECT ON quan_ly_quan_net.ChucVu TO 'nhan_vien_co_ban_role'@'%';
GRANT SELECT ON quan_ly_quan_net.CaLamViec TO 'nhan_vien_co_ban_role'@'%';
GRANT SELECT ON quan_ly_quan_net.NhanVien_CaLamViec TO 'nhan_vien_co_ban_role'@'%';

-- Quyền INSERT để tạo các bản ghi hoạt động
GRANT INSERT ON quan_ly_quan_net.PhienSuDung TO 'nhan_vien_co_ban_role'@'%'; -- Bắt đầu phiên sử dụng
GRANT INSERT ON quan_ly_quan_net.HoaDonDV TO 'nhan_vien_co_ban_role'@'%';    -- Tạo hóa đơn dịch vụ
GRANT INSERT ON quan_ly_quan_net.CT_HoaDonDV TO 'nhan_vien_co_ban_role'@'%'; -- Thêm chi tiết hóa đơn dịch vụ

-- Quyền UPDATE trên các bảng/cột cụ thể
GRANT UPDATE (ThoiGianKetThuc) ON quan_ly_quan_net.PhienSuDung TO 'nhan_vien_co_ban_role'@'%'; -- Kết thúc phiên
GRANT UPDATE (SoTienConLai) ON quan_ly_quan_net.TaiKhoan TO 'nhan_vien_co_ban_role'@'%'; -- Nạp tiền cho khách (LƯU Ý: Nên được thực hiện qua Stored Procedure đã kiểm soát)
GRANT UPDATE (TrangThai) ON quan_ly_quan_net.MayTinh TO 'nhan_vien_co_ban_role'@'%'; -- Ví dụ: chuyển sang 'Bảo trì' thủ công

GRANT EXECUTE ON PROCEDURE quan_ly_quan_net.sp_LayThongTinCaNhan TO 'khach_hang_role'@'%';

-- -----------------------------------------------------
-- CẤP QUYỀN CHO ROLE: quan_ly_cua_hang_role
-- -----------------------------------------------------
-- Quản lý có tất cả các quyền của nhân viên cơ bản
GRANT 'nhan_vien_co_ban_role' TO 'quan_ly_cua_hang_role'@'%';

-- Quyền SELECT trên tất cả các View của Quản lý
GRANT SELECT ON quan_ly_quan_net.View_QL_DoanhThuTienGioChiTiet TO 'quan_ly_cua_hang_role'@'%';
GRANT SELECT ON quan_ly_quan_net.View_QL_ChiTietHoaDonDichVu TO 'quan_ly_cua_hang_role'@'%';
GRANT SELECT ON quan_ly_quan_net.View_QL_ThongTinNhanVienDayDu TO 'quan_ly_cua_hang_role'@'%';
GRANT SELECT ON quan_ly_quan_net.View_QL_ThongKeTaiKhoanKhachHang TO 'quan_ly_cua_hang_role'@'%';

-- Quyền SELECT trên các bảng log và bảng toàn bộ (nếu chưa được bao gồm từ role nhân viên)
GRANT SELECT ON quan_ly_quan_net.NhanVien TO 'quan_ly_cua_hang_role'@'%'; -- Full select trên bảng NhanVien

-- Quyền INSERT, UPDATE, DELETE trên các bảng quản lý cấu hình và dữ liệu chính
GRANT INSERT, UPDATE, DELETE ON quan_ly_quan_net.LoaiMay TO 'quan_ly_cua_hang_role'@'%';
GRANT INSERT, UPDATE, DELETE ON quan_ly_quan_net.MayTinh TO 'quan_ly_cua_hang_role'@'%';
GRANT INSERT, UPDATE, DELETE ON quan_ly_quan_net.UuDai TO 'quan_ly_cua_hang_role'@'%';
GRANT INSERT, UPDATE, DELETE ON quan_ly_quan_net.LoaiKH TO 'quan_ly_cua_hang_role'@'%';
GRANT INSERT, UPDATE, DELETE ON quan_ly_quan_net.KhachHang TO 'quan_ly_cua_hang_role'@'%';
GRANT INSERT, UPDATE, DELETE ON quan_ly_quan_net.TaiKhoan TO 'quan_ly_cua_hang_role'@'%'; -- Quản lý tài khoản khách, điều chỉnh số dư
GRANT INSERT, UPDATE, DELETE ON quan_ly_quan_net.DichVu TO 'quan_ly_cua_hang_role'@'%';
GRANT INSERT, UPDATE, DELETE ON quan_ly_quan_net.ChucVu TO 'quan_ly_cua_hang_role'@'%';
GRANT INSERT, UPDATE, DELETE ON quan_ly_quan_net.NhanVien TO 'quan_ly_cua_hang_role'@'%';
GRANT INSERT, UPDATE, DELETE ON quan_ly_quan_net.CaLamViec TO 'quan_ly_cua_hang_role'@'%';
GRANT INSERT, UPDATE, DELETE ON quan_ly_quan_net.NhanVien_CaLamViec TO 'quan_ly_cua_hang_role'@'%';

-- Quản lý có thể cần quyền cập nhật một số trường trên hóa đơn trong trường hợp đặc biệt (cẩn trọng)
GRANT UPDATE (MaNV, MaUuDai, ThoiDiemThanhToan) ON quan_ly_quan_net.HoaDonDV TO 'quan_ly_cua_hang_role'@'%';
-- (Trigger `trg_Prevent_HoaDonDV_MaTK_Update` vẫn sẽ ngăn thay đổi MaTK)


-- -----------------------------------------------------
-- CẤP QUYỀN CHO ROLE: quan_tri_vien_db_role
-- -----------------------------------------------------
-- Role này có quyền hạn cao nhất trên schema quan_ly_quan_net

GRANT ALL PRIVILEGES ON quan_ly_quan_net.* TO 'quan_tri_vien_db_role'@'%';

-- -----------------------------------------------------
-- ÁP DỤNG THAY ĐỔI QUYỀN
-- -----------------------------------------------------

FLUSH PRIVILEGES;

-- -----------------------------------------------------
-- HƯỚNG DẪN SỬ DỤNG TIẾP THEO
-- -----------------------------------------------------
-- 1. Tạo người dùng (database user) cho từng nhân viên, quản lý, quản trị viên:
--    CREATE USER 'tên_user'@'host' IDENTIFIED BY 'mật_khẩu';
--    Ví dụ:
    CREATE USER IF NOT EXISTS 'user_an01'@'%' IDENTIFIED BY 'pass001';

-- Ví dụ cho nhân viên (lấy từ bảng TaiKhoanNhanVien)
CREATE USER IF NOT EXISTS 'nhanvien_binh_002'@'%' IDENTIFIED BY 'pass_nv002';

-- Ví dụ cho quản lý (lấy từ bảng TaiKhoanNhanVien)
CREATE USER IF NOT EXISTS 'quanly_an_001'@'%' IDENTIFIED BY 'pass_nv001';

-- Ví dụ cho quản trị CSDL
CREATE USER IF NOT EXISTS 'admin_db_qln'@'localhost' IDENTIFIED BY 'MatKhauAdminDB789!';


-- 2. Gán role đã tạo cho người dùng tương ứng
-- -----------------------------------------------------------------
-- Gán role cho khách hàng
GRANT 'khach_hang_role' TO 'user_an01'@'%';

-- Gán role cho nhân viên (vì NV002 có MaChucVu là CV002)
GRANT 'nhan_vien_co_ban_role' TO 'nhanvien_binh_002'@'%';

-- Gán role cho quản lý (vì NV001 có MaChucVu là CV001)
GRANT 'quan_ly_cua_hang_role' TO 'quanly_an_001'@'%';

-- Gán role cho quản trị CSDL
GRANT 'quan_tri_vien_db_role' TO 'admin_db_qln'@'localhost';


-- 3. Đặt role mặc định cho người dùng (Rất quan trọng)
-- -----------------------------------------------------------------
SET DEFAULT ROLE 'khach_hang_role' TO 'user_an01'@'%';
SET DEFAULT ROLE 'nhan_vien_co_ban_role' TO 'nhanvien_binh_002'@'%';
SET DEFAULT ROLE 'quan_ly_cua_hang_role' TO 'quanly_an_001'@'%';
SET DEFAULT ROLE 'quan_tri_vien_db_role' TO 'admin_db_qln'@'localhost';

-- Áp dụng thay đổi quyền
FLUSH PRIVILEGES;


-- 4. Kiểm tra lại quyền hạn của các user vừa tạo
-- -----------------------------------------------------------------
-- Xem quyền của user quản trị CSDL
SHOW GRANTS FOR 'admin_db_qln'@'localhost';

-- Xem quyền của user quản lý
SHOW GRANTS FOR 'quanly_an_001'@'%';

-- Xem quyền của user nhân viên
SHOW GRANTS FOR 'nhanvien_binh_002'@'%';

-- Xem quyền của user khách hàng
SHOW GRANTS FOR 'user_an01'@'%';

-- Xem danh sách tất cả các user trong hệ thống MySQL
SELECT user, host FROM mysql.user;

