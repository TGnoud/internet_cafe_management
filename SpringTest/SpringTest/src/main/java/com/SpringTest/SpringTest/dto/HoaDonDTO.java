package com.SpringTest.SpringTest.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class HoaDonDTO {
    private String maHD;
    private LocalDateTime thoiDiemThanhToan;
    private String maTK;
    private String tenTK; // Tên tài khoản khách
    private String maNV;
    private String tenNV; // Tên nhân viên
    private String maUuDai;
    private BigDecimal tongTienTruocGiam;
    private BigDecimal soTienGiam;
    private BigDecimal tongTienSauGiam;
    private List<ChiTietHoaDonDTO> chiTiet;

    @Data
    public static class ChiTietHoaDonDTO {
        private String maDV;
        private String tenDV;
        private int soLuong;
        private BigDecimal donGiaLucBan; // Lấy từ CT_HoaDonDV
        private BigDecimal thanhTien;
    }
}