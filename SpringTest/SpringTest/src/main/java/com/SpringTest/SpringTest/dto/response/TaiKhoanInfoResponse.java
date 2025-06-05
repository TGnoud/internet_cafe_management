package com.SpringTest.SpringTest.dto.response;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class TaiKhoanInfoResponse {
    private String maTK;
    private String tenTK;
    private BigDecimal soTienConLai;
    private String maKH;
    private String hoTenKH;
    // Thêm các thông tin cần thiết khác
}