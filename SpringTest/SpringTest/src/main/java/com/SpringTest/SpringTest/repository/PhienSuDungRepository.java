package com.SpringTest.SpringTest.repository;

import com.SpringTest.SpringTest.entity.MayTinh;
import com.SpringTest.SpringTest.entity.PhienSuDung;
import com.SpringTest.SpringTest.entity.TaiKhoan;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.query.Procedure;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface PhienSuDungRepository extends JpaRepository<PhienSuDung, Integer> {
    List<PhienSuDung> findByTaiKhoan(TaiKhoan taiKhoan);
    List<PhienSuDung> findByMayTinh(MayTinh mayTinh);
    Optional<PhienSuDung> findByMayTinhAndThoiGianKetThucIsNull(MayTinh mayTinh);
    List<PhienSuDung> findByTaiKhoan_MaTKAndThoiGianKetThucIsNull(String maTK);
    List<PhienSuDung> findByThoiGianBatDauBetween(LocalDateTime start, LocalDateTime end);
    List<PhienSuDung> findByThoiGianKetThucIsNullOrderByThoiGianBatDauDesc();
    Page<PhienSuDung> findByMayTinhOrderByThoiGianBatDauDesc(MayTinh mayTinh, Pageable pageable);
    Page<PhienSuDung> findAllByOrderByThoiGianBatDauDesc(Pageable pageable); // Cho lịch sử tất cả
    @Procedure(procedureName = "BatDauPhienSuDung")
    void batDauPhienSuDung(@Param("p_MaTaiKhoan") Integer maTaiKhoan, @Param("p_MaMay") Integer maMay);

    @Procedure(procedureName = "KetThucPhienSuDung")
    void ketThucPhienSuDung(@Param("p_MaPhien") Integer maPhien);
    @Query(value = "SELECT TinhChiPhiPhienSuDung(:maPhien, :apDungUuDai)", nativeQuery = true)
    BigDecimal calculateSessionCost(@Param("maPhien") Integer maPhien, @Param("apDungUuDai") boolean apDungUuDai);
    @Query(value = "SELECT GetTotalRevenueToday()", nativeQuery = true)
    BigDecimal getTotalRevenueToday();
}