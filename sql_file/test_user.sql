SET @ten_tk_hien_tai = SUBSTRING_INDEX(USER(), '@', 1);
CALL quan_ly_quan_net.sp_xem_thong_tin_ca_nhan();
SELECT CURRENT_ROLE();
CALL sp_xem_lich_su_giao_dich();
