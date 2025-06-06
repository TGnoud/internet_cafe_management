package com.SpringTest.SpringTest.repository;

import com.SpringTest.SpringTest.entity.TaiKhoan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.query.Procedure;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface TaiKhoanRepository extends JpaRepository<TaiKhoan, String> {
    Optional<TaiKhoan> findByTenTK(String tenTK);
    Optional<TaiKhoan> findByKhachHang_MaKH(String maKH);
    long countByKhachHangIsNotNull();
    @Query(value = "SELECT TongThoiGianSuDung(:maTaiKhoan)", nativeQuery = true)
    String getTongThoiGianSuDung(@Param("maTaiKhoan") Integer maTaiKhoan); // MySQL TIME có thể map sang String
    @Procedure(procedureName = "CapNhatSoDuTaiKhoan")
    void capNhatSoDu(@Param("p_MaTaiKhoan") Integer maTaiKhoan, @Param("p_SoTienNap") BigDecimal soTienNap);

    // Đối với procedure ThemKhachHang (không trả về giá trị tường minh qua OUT params)
    @Procedure(procedureName = "ThemKhachHang")
    void themKhachHang(
            @Param("p_HoTen") String hoTen,
            @Param("p_Email") String email,
            @Param("p_SoDienThoai") String soDienThoai,
            @Param("p_TenTaiKhoan") String tenTaiKhoan,
            @Param("p_MatKhau") String matKhau,
            @Param("p_MaLoaiKH") Integer maLoaiKH
    );
    @Procedure(name = "TaiKhoan.capNhatSoDu") // Tham chiếu đến tên đã định nghĩa trong @NamedStoredProcedureQuery
    void capNhatSoDuNamed(@Param("p_MaTaiKhoan") Integer maTaiKhoan, @Param("p_SoTienNap") BigDecimal soTienNap);
    @Query("SELECT COUNT(tk) FROM TaiKhoan tk WHERE tk.ngayTao >= :startDate")
    long countNewAccountsSince(LocalDateTime startDate);
}