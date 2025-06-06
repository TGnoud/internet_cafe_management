package com.SpringTest.SpringTest.service.impl;

import com.SpringTest.SpringTest.dto.response.PhienSuDungInfoResponse;
import com.SpringTest.SpringTest.entity.MayTinh;
import com.SpringTest.SpringTest.entity.PhienSuDung;
import com.SpringTest.SpringTest.entity.TaiKhoan;
import com.SpringTest.SpringTest.exception.BadRequestException; // Thêm import này
import com.SpringTest.SpringTest.exception.ResourceNotFoundException;
import com.SpringTest.SpringTest.repository.MayTinhRepository;
import com.SpringTest.SpringTest.repository.PhienSuDungRepository;
import com.SpringTest.SpringTest.repository.TaiKhoanRepository;
import com.SpringTest.SpringTest.service.PhienSuDungService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional; // Thêm import này

import java.math.BigDecimal;
import java.time.LocalDateTime; // Thêm import này
import java.util.stream.Collectors;
import java.util.List;
import java.time.temporal.ChronoUnit;

@Service
public class PhienSuDungServiceImpl implements PhienSuDungService {

    @Autowired
    private PhienSuDungRepository phienSuDungRepository;
    @Autowired
    private MayTinhRepository mayTinhRepository;
    // Helper method để map PhienSuDung sang PhienSuDungInfoResponse (đã có trong AuthServiceImpl, có thể tách ra class riêng hoặc copy)
    @Autowired
    private TaiKhoanRepository taiKhoanRepository;
    @Override
    @Transactional // Rất quan trọng: Đảm bảo toàn vẹn dữ liệu
    public PhienSuDungInfoResponse endSessionAndFinalize(Integer maPhien, boolean apDungUuDai) {
        // 1. Lấy phiên sử dụng từ CSDL
        PhienSuDung phien = phienSuDungRepository.findById(maPhien)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy phiên sử dụng với mã: " + maPhien));

        if (phien.getThoiGianKetThuc() != null) {
            throw new BadRequestException("Phiên này đã được kết thúc trước đó.");
        }

        // 2. Đặt thời gian kết thúc
        phien.setThoiGianKetThuc(LocalDateTime.now());
        phienSuDungRepository.saveAndFlush(phien); // Lưu để SQL function có thể thấy được thời gian kết thúc

        // 3. Tính tổng tiền giờ chơi (gọi SQL function đã tích hợp)
        BigDecimal chiPhiGioChoi = phienSuDungRepository.calculateSessionCost(maPhien, apDungUuDai);
        phien.setTongTien(chiPhiGioChoi);

        // (Mở rộng trong tương lai) 4. Tính tổng tiền dịch vụ
        // BigDecimal chiPhiDichVu = hoaDonService.calculateUnpaidServiceBills(phien.getTaiKhoan().getMaTK());
        // BigDecimal tongChiPhi = chiPhiGioChoi.add(chiPhiDichVu);

        // 5. Cập nhật số dư tài khoản
        TaiKhoan taiKhoan = phien.getTaiKhoan();
        BigDecimal soTienConLai = taiKhoan.getSoTienConLai().subtract(chiPhiGioChoi);

        if (soTienConLai.compareTo(BigDecimal.ZERO) < 0) {
            // Xử lý trường hợp không đủ tiền (ví dụ: không cho kết thúc hoặc ghi nợ)
            // Tạm thời vẫn cho phép để ghi nhận chi phí
            System.out.println("Cảnh báo: Tài khoản " + taiKhoan.getTenTK() + " có số dư âm.");
        }
        taiKhoan.setSoTienConLai(soTienConLai);
        taiKhoanRepository.save(taiKhoan);

        // 6. Cập nhật trạng thái máy tính
        MayTinh mayTinh = phien.getMayTinh();
        mayTinh.setTrangThai("Khả dụng");
        mayTinhRepository.save(mayTinh);

        PhienSuDung phienDaLuu = phienSuDungRepository.save(phien);

        return mapToPhienSuDungInfoResponse(phienDaLuu);
    }
    @Transactional
    public PhienSuDungInfoResponse endSessionAndCalculateCost(Integer maPhien, boolean apDungUuDai) {
        PhienSuDung phien = phienSuDungRepository.findById(maPhien)
                .orElseThrow(() -> new ResourceNotFoundException("Phiên " + maPhien + " không tìm thấy."));

        if (phien.getThoiGianKetThuc() != null) {
            throw new BadRequestException("Phiên " + maPhien + " đã kết thúc rồi.");
        }

        phien.setThoiGianKetThuc(LocalDateTime.now()); // Đặt thời gian kết thúc là hiện tại

        // Gọi SQL function TinhChiPhiPhienSuDung qua repository
        BigDecimal cost = phienSuDungRepository.calculateSessionCost(maPhien, apDungUuDai);
        phien.setTongTien(cost);

        // Cập nhật trạng thái máy tính thành "Khả dụng"
        MayTinh mayTinh = phien.getMayTinh();
        if (mayTinh != null) {
            mayTinh.setTrangThai("Khả dụng"); // Giả sử "Khả dụng" là trạng thái đúng
            mayTinhRepository.save(mayTinh);
        }

        PhienSuDung updatedPhien = phienSuDungRepository.save(phien);
        return mapToPhienSuDungInfoResponse(updatedPhien); // Trả về thông tin phiên đã cập nhật
    }
    private PhienSuDungInfoResponse mapToPhienSuDungInfoResponse(PhienSuDung phienSuDung) { //
        PhienSuDungInfoResponse response = new PhienSuDungInfoResponse(); //
        response.setMaPhien(phienSuDung.getMaPhien()); //
        response.setMaMay(phienSuDung.getMayTinh().getMaMay()); //
        response.setTenMay(phienSuDung.getMayTinh().getTenMay()); //
        response.setMaTK(phienSuDung.getTaiKhoan().getMaTK()); //
        response.setTenTK(phienSuDung.getTaiKhoan().getTenTK()); //
        response.setThoiGianBatDau(phienSuDung.getThoiGianBatDau()); //
        response.setSoTienConLai(phienSuDung.getTaiKhoan().getSoTienConLai()); //

        if (phienSuDung.getThoiGianKetThuc() != null) { //
            response.setThoiGianKetThuc(phienSuDung.getThoiGianKetThuc()); //
        }
        // Hiển thị tổng tiền nếu phiên đã kết thúc
        if (phienSuDung.getTongTien() != null) {
            response.setTongTienPhien(phienSuDung.getTongTien()); // Giả sử bạn thêm trường tongTienPhien vào DTO
        }


        BigDecimal giaTheoGio = phienSuDung.getMayTinh().getLoaiMay().getGiaTheoGio(); //
        if (phienSuDung.getThoiGianKetThuc() == null && //
                phienSuDung.getTaiKhoan().getSoTienConLai().compareTo(BigDecimal.ZERO) > 0 && //
                giaTheoGio.compareTo(BigDecimal.ZERO) > 0) { //
            BigDecimal thoiGianConLaiPhut = phienSuDung.getTaiKhoan().getSoTienConLai() //
                    .divide(giaTheoGio.divide(BigDecimal.valueOf(60), 2, BigDecimal.ROUND_HALF_UP), 0, BigDecimal.ROUND_DOWN); //
            response.setThoiGianConLaiDuKienPhut(thoiGianConLaiPhut.longValue()); //
        } else {
            response.setThoiGianConLaiDuKienPhut(0L); //
        }
        return response; //
    }


    @Override
    public List<PhienSuDungInfoResponse> getActiveSessions() {
        return phienSuDungRepository.findByThoiGianKetThucIsNullOrderByThoiGianBatDauDesc()
                .stream()
                .map(this::mapToPhienSuDungInfoResponse)
                .collect(Collectors.toList());
    }

    @Override
    public Page<PhienSuDungInfoResponse> getSessionHistoryByMachine(String maMay, Pageable pageable) {
        MayTinh mayTinh = mayTinhRepository.findById(maMay)
                .orElseThrow(() -> new ResourceNotFoundException("Máy " + maMay + " không tồn tại."));
        Page<PhienSuDung> phienPage = phienSuDungRepository.findByMayTinhOrderByThoiGianBatDauDesc(mayTinh, pageable);
        List<PhienSuDungInfoResponse> dtos = phienPage.getContent()
                .stream()
                .map(this::mapToPhienSuDungInfoResponse)
                .collect(Collectors.toList());
        return new PageImpl<>(dtos, pageable, phienPage.getTotalElements());
    }

    @Override
    public Page<PhienSuDungInfoResponse> getAllSessionHistory(Pageable pageable) {
        Page<PhienSuDung> phienPage = phienSuDungRepository.findAllByOrderByThoiGianBatDauDesc(pageable);
        List<PhienSuDungInfoResponse> dtos = phienPage.getContent()
                .stream()
                .map(this::mapToPhienSuDungInfoResponse)
                .collect(Collectors.toList());
        return new PageImpl<>(dtos, pageable, phienPage.getTotalElements());
    }

    @Override
    public BigDecimal getTotalRevenueToday() {
        return null;
    }

    @Override
    public long getThoiGianConLaiDuKienPhut(int maPhien) {
        PhienSuDung phienSuDung = phienSuDungRepository.findById(maPhien)
                .orElseThrow(() -> new ResourceNotFoundException("Phiên sử dụng không tồn tại: " + maPhien));

        if (phienSuDung.getThoiGianKetThuc() != null) {
            return 0L; // Phiên đã kết thúc
        }

        BigDecimal soTienConLai = phienSuDung.getTaiKhoan().getSoTienConLai();
        BigDecimal giaTheoGio = phienSuDung.getMayTinh().getLoaiMay().getGiaTheoGio();

        if (soTienConLai.compareTo(BigDecimal.ZERO) <= 0 || giaTheoGio.compareTo(BigDecimal.ZERO) <= 0) {
            return 0L;
        }

        // Thời gian còn lại (phút) = Số tiền / (Giá mỗi giờ / 60)
        BigDecimal thoiGianConLaiPhutDecimal = soTienConLai
                .divide(giaTheoGio.divide(BigDecimal.valueOf(60), 2, BigDecimal.ROUND_HALF_UP), 0, BigDecimal.ROUND_DOWN);
        return thoiGianConLaiPhutDecimal.longValue();
    }
}