package com.SpringTest.SpringTest.repository;

import com.SpringTest.SpringTest.entity.TaiKhoan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface TaiKhoanRepository extends JpaRepository<TaiKhoan, String> {
    Optional<TaiKhoan> findByTenTK(String tenTK);
    Optional<TaiKhoan> findByKhachHang_MaKH(String maKH);
    long countByKhachHangIsNotNull();
}