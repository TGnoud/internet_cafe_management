package com.SpringTest.SpringTest.service.impl;

import com.SpringTest.SpringTest.dto.request.CreateTaiKhoanRequest;
import com.SpringTest.SpringTest.dto.request.NapTienRequest;
import com.SpringTest.SpringTest.dto.response.TaiKhoanInfoResponse;
import com.SpringTest.SpringTest.entity.KhachHang;
import com.SpringTest.SpringTest.entity.LoaiKH;
import com.SpringTest.SpringTest.entity.TaiKhoan;
import com.SpringTest.SpringTest.exception.BadRequestException;
import com.SpringTest.SpringTest.exception.ResourceNotFoundException;
import com.SpringTest.SpringTest.repository.KhachHangRepository;
import com.SpringTest.SpringTest.repository.LoaiKHRepository;
import com.SpringTest.SpringTest.repository.PhienSuDungRepository;
import com.SpringTest.SpringTest.repository.TaiKhoanRepository;
import com.SpringTest.SpringTest.service.TaiKhoanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class TaiKhoanServiceImpl implements TaiKhoanService {

    @Autowired
    private TaiKhoanRepository taiKhoanRepository;

    @Autowired
    private KhachHangRepository khachHangRepository;

    @Autowired
    private LoaiKHRepository loaiKHRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private PhienSuDungRepository phienSuDungRepository;

    @Override
    public long countAllActiveAccounts() {
        // Đếm các tài khoản được liên kết với một khách hàng (tức là tài khoản khách).
        return taiKhoanRepository.countByKhachHangIsNotNull();
    }

    @Override
    public TaiKhoan findEntityByMaTK(String maTK) {
        return taiKhoanRepository.findById(maTK)
                .orElseThrow(() -> new ResourceNotFoundException("Tài khoản không tìm thấy: " + maTK));
    }

    @Override
    public Page<TaiKhoanInfoResponse> getAllKhachHangTaiKhoanPageable(Pageable pageable) {
        // Cách tiếp cận tối ưu: Tạo một phương thức trong repository để truy vấn trực tiếp các tài khoản của khách hàng.
        // Điều này đảm bảo phân trang và đếm tổng số phần tử chính xác.
        // Ví dụ: Page<TaiKhoan> findAllCustomerAccounts(Pageable pageable); trong TaiKhoanRepository
        // @Query("SELECT t FROM TaiKhoan t WHERE t.khachHang IS NOT NULL")
        Page<TaiKhoan> customerTaiKhoanPage = taiKhoanRepository.findByKhachHangIsNotNull(pageable); // Giả sử phương thức này đã tồn tại

        List<TaiKhoanInfoResponse> dtoList = customerTaiKhoanPage.getContent().stream()
                .map(this::mapToTaiKhoanInfoResponse)
                .collect(Collectors.toList());

        return new PageImpl<>(dtoList, pageable, customerTaiKhoanPage.getTotalElements());
    }

    @Override
    public long countNewAccountsSince(LocalDateTime startOfDay) {
        return 0;
    }

    @Override
    public TaiKhoanInfoResponse getTaiKhoanInfo(String maTK) {
        TaiKhoan taiKhoan = findEntityByMaTK(maTK);
        return mapToTaiKhoanInfoResponse(taiKhoan);
    }

    @Override
    public BigDecimal getSoDuTaiKhoan(String maTK) {
        TaiKhoan taiKhoan = findEntityByMaTK(maTK);
        return taiKhoan.getSoTienConLai();
    }

    @Override
    @Transactional
    public TaiKhoanInfoResponse createTaiKhoanKhachHang(CreateTaiKhoanRequest request) {
        if (taiKhoanRepository.findByTenTK(request.getTenTK()).isPresent()) {
            throw new BadRequestException("Tên tài khoản đã tồn tại: " + request.getTenTK());
        }
        if (khachHangRepository.findBySoDienThoai(request.getSoDienThoaiKH()).isPresent()) {
            throw new BadRequestException("Số điện thoại đã được đăng ký: " + request.getSoDienThoaiKH());
        }

        LoaiKH loaiKH = loaiKHRepository.findById(request.getMaLoaiKH())
                .orElseThrow(() -> new ResourceNotFoundException("Loại khách hàng không tồn tại: " + request.getMaLoaiKH()));

        KhachHang khachHang = new KhachHang();
        TaiKhoan taiKhoan = new TaiKhoan();
        khachHang.setMaKH("KH-" + UUID.randomUUID().toString());
        taiKhoan.setMaTK("TK-" + UUID.randomUUID().toString());
        khachHang.setHoTen(request.getHoTenKH());
        khachHang.setSoDienThoai(request.getSoDienThoaiKH());
        khachHang.setGioiTinh(request.getGioiTinhKH());
        khachHang.setLoaiKH(loaiKH);
        KhachHang savedKhachHang = khachHangRepository.save(khachHang);
        taiKhoan.setTenTK(request.getTenTK());
        taiKhoan.setMatKhau(passwordEncoder.encode(request.getMatKhau())); // **QUAN TRỌNG: Mã hóa mật khẩu**
        taiKhoan.setSoTienConLai(request.getSoTienNapBanDau() != null ? request.getSoTienNapBanDau() : BigDecimal.ZERO);
        taiKhoan.setKhachHang(savedKhachHang);
        TaiKhoan savedTaiKhoan = taiKhoanRepository.save(taiKhoan);

        return mapToTaiKhoanInfoResponse(savedTaiKhoan);
    }

    @Override
    @Transactional
    public TaiKhoanInfoResponse napTien(NapTienRequest request) { // Sửa lại để khớp với interface
        if (request.getSoTien().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("Số tiền nạp phải lớn hơn 0.");
        }

        // Lấy maTK từ đối tượng request.
        // Giả định rằng lớp NapTienRequest của bạn có phương thức getMaTK().
        String maTK = request.getMaTK();

        TaiKhoan taiKhoan = taiKhoanRepository.findById(maTK)
                .orElseThrow(() -> new ResourceNotFoundException("Tài khoản " + maTK + " không tồn tại."));

        BigDecimal soTienMoi = taiKhoan.getSoTienConLai().add(request.getSoTien());
        taiKhoan.setSoTienConLai(soTienMoi);

        // Lưu lại tài khoản đã được cập nhật
        TaiKhoan savedTaiKhoan = taiKhoanRepository.save(taiKhoan);

        // Mở rộng: Ghi lại giao dịch nạp tiền vào một bảng GiaoDich để đối soát
        // GiaoDich newTx = new GiaoDich(taiKhoan, request.getSoTien(), "Nạp tiền");
        // giaoDichRepository.save(newTx);

        // Trả về đúng kiểu dữ liệu TaiKhoanInfoResponse như interface yêu cầu
        return mapToTaiKhoanInfoResponse(savedTaiKhoan);
    }

    private TaiKhoanInfoResponse mapToTaiKhoanInfoResponse(TaiKhoan taiKhoan) {
        TaiKhoanInfoResponse response = new TaiKhoanInfoResponse();
        response.setMaTK(taiKhoan.getMaTK());
        response.setTenTK(taiKhoan.getTenTK());
        response.setSoTienConLai(taiKhoan.getSoTienConLai());
        if (taiKhoan.getKhachHang() != null) {
            response.setMaKH(taiKhoan.getKhachHang().getMaKH());
            response.setHoTenKH(taiKhoan.getKhachHang().getHoTen());
        }
        return response;
    }

    // --- Các phương thức gọi Stored Procedure ---



}