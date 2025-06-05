package com.SpringTest.SpringTest.repository;

import com.SpringTest.SpringTest.entity.HoaDonDV;
import com.SpringTest.SpringTest.entity.NhanVien;
import com.SpringTest.SpringTest.entity.TaiKhoan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.time.LocalDateTime;

@Repository
public interface HoaDonDVRepository extends JpaRepository<HoaDonDV, String> {
    List<HoaDonDV> findByTaiKhoan(TaiKhoan taiKhoan);
    List<HoaDonDV> findByNhanVien(NhanVien nhanVien);
    List<HoaDonDV> findByThoiDiemThanhToanBetween(LocalDateTime startDate, LocalDateTime endDate);
}