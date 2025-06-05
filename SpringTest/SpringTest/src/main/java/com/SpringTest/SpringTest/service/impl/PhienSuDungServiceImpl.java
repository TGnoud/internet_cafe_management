package com.SpringTest.SpringTest.service.impl;

import com.SpringTest.SpringTest.dto.response.PhienSuDungInfoResponse;
import com.SpringTest.SpringTest.entity.MayTinh;
import com.SpringTest.SpringTest.entity.PhienSuDung;
import com.SpringTest.SpringTest.exception.ResourceNotFoundException;
import com.SpringTest.SpringTest.repository.MayTinhRepository;
import com.SpringTest.SpringTest.repository.PhienSuDungRepository;
import com.SpringTest.SpringTest.service.PhienSuDungService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import java.math.BigDecimal;
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
    private PhienSuDungInfoResponse mapToPhienSuDungInfoResponse(PhienSuDung phienSuDung) {
        PhienSuDungInfoResponse response = new PhienSuDungInfoResponse();
        response.setMaPhien(phienSuDung.getMaPhien());
        response.setMaMay(phienSuDung.getMayTinh().getMaMay());
        response.setTenMay(phienSuDung.getMayTinh().getTenMay());
        response.setMaTK(phienSuDung.getTaiKhoan().getMaTK());
        response.setTenTK(phienSuDung.getTaiKhoan().getTenTK());
        response.setThoiGianBatDau(phienSuDung.getThoiGianBatDau());
        response.setSoTienConLai(phienSuDung.getTaiKhoan().getSoTienConLai());
        // Thêm thời gian kết thúc nếu có
        if (phienSuDung.getThoiGianKetThuc() != null) {
            response.setThoiGianKetThuc(phienSuDung.getThoiGianKetThuc()); // Cần thêm trường này vào DTO
        }


        BigDecimal giaTheoGio = phienSuDung.getMayTinh().getLoaiMay().getGiaTheoGio();
        if (phienSuDung.getThoiGianKetThuc() == null && // Chỉ tính cho phiên đang chạy
                phienSuDung.getTaiKhoan().getSoTienConLai().compareTo(BigDecimal.ZERO) > 0 &&
                giaTheoGio.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal thoiGianConLaiPhut = phienSuDung.getTaiKhoan().getSoTienConLai()
                    .divide(giaTheoGio.divide(BigDecimal.valueOf(60), 2, BigDecimal.ROUND_HALF_UP), 0, BigDecimal.ROUND_DOWN);
            response.setThoiGianConLaiDuKienPhut(thoiGianConLaiPhut.longValue());
        } else {
            response.setThoiGianConLaiDuKienPhut(0L);
        }
        return response;
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