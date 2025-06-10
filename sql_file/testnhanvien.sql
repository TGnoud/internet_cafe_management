-- Nhân viên có thể tra cứu thông tin một khách hàng.
SELECT * FROM quan_ly_quan_net.KhachHang WHERE MaKH = 'KH003';

-- Nhân viên có thể tạo một phiên sử dụng mới (bật máy cho khách).
INSERT INTO quan_ly_quan_net.PhienSuDung (MaTK, MaMay, ThoiGianBatDau) VALUES ('TK003', 'PC07', NOW());

-- Nhân viên có thể cập nhật trạng thái máy tính sang 'Bảo trì'.
UPDATE quan_ly_quan_net.MayTinh SET TrangThai = 'Bảo trì' WHERE MaMay = 'PCview_kh_danhsachdichvu08';
CALL sp_NhanVien_NapTien('TK001', 50000.00);
CALL sp_NhanVien_LapHoaDon(
    'TK003', 'NV003',
    '[{"ma_dv": "DV001", "so_luong": 1}, {"ma_dv": "DV005", "so_luong": 1}]'
);