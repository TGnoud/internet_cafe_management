SET @ten_tk_hien_tai = SUBSTRING_INDEX(USER(), '@', 1);
CALL quan_ly_quan_net.sp_xem_thong_tin_ca_nhan();
-- Procedure dịch vụ NAP_TIEN
CALL sp_KhachHang_GiaoDich('DV003', 1);
-- Procedure dịch vụ MUA_HANG
CALL sp_KhachHang_GiaoDich('DV004', 2);
SELECT CURRENT_ROLE();
CALL sp_LayThongTinCaNhan();