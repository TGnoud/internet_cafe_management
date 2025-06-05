package com.SpringTest.SpringTest.dto.request;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class CreateTaiKhoanRequest {
    // Thông tin KhachHang
    private String hoTenKH;
    private String soDienThoaiKH;
    private String gioiTinhKH;
    private String maLoaiKH; // Mặc định hoặc chọn

    // Thông tin TaiKhoan
    private String tenTK;
    private String matKhau;
    private BigDecimal soTienNapBanDau; // Optional
}