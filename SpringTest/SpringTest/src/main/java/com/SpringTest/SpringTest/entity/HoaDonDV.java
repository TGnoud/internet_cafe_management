package com.SpringTest.SpringTest.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "HoaDonDV")
public class HoaDonDV {

    @Id
    @Column(name = "MaHD", length = 15)
    private String maHD;

    @Column(name = "ThoiDiemThanhToan", nullable = false)
    private LocalDateTime thoiDiemThanhToan;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MaTK", nullable = false)
    private TaiKhoan taiKhoan;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MaNV") // MaNV có thể null
    private NhanVien nhanVien;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MaUuDai") // MaUuDai có thể null
    private UuDai uuDai;

    @OneToMany(mappedBy = "hoaDonDV", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<ChiTietHoaDonDV> chiTietHoaDonDVs;
}