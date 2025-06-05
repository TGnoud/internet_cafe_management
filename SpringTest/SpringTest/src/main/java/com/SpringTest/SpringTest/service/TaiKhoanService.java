package com.SpringTest.SpringTest.service;

import com.SpringTest.SpringTest.dto.request.CreateTaiKhoanRequest;
import com.SpringTest.SpringTest.dto.request.NapTienRequest;
import com.SpringTest.SpringTest.dto.response.TaiKhoanInfoResponse;
import com.SpringTest.SpringTest.entity.TaiKhoan;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;

public interface TaiKhoanService {
    TaiKhoanInfoResponse getTaiKhoanInfo(String maTK);
    BigDecimal getSoDuTaiKhoan(String maTK);
    TaiKhoanInfoResponse createTaiKhoanKhachHang(CreateTaiKhoanRequest request);
    TaiKhoanInfoResponse napTien(NapTienRequest request);

    long countAllActiveAccounts();

    TaiKhoan findEntityByMaTK(String maTK);

    Page<TaiKhoanInfoResponse> getAllKhachHangTaiKhoanPageable(Pageable pageable);
    // Thêm các phương thức khác nếu cần
}