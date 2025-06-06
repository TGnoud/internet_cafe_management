package com.SpringTest.SpringTest.service.impl;

// import org.springframework.security.crypto.password.PasswordEncoder;
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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.stream.Collectors;
import java.math.BigDecimal;
import java.util.UUID; // Để tạo MaKH, MaTK ngẫu nhiên

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
    // Khi tạo tài khoản
    // taiKhoan.setMatKhau(passwordEncoder.encode(request.getMatKhau()));

    // Khi đổi mật khẩu
    // currentUser.setMatKhau(passwordEncoder.encode(newPassword));
    @Override
    public long countAllActiveAccounts() {
        // Giả định "active" có nghĩa là có thông tin KhachHang liên kết
        // Hoặc bạn có thể có một trường 'trangThai' trong TaiKhoan entity
        return taiKhoanRepository.countByKhachHangIsNotNull(); // Ví dụ
        // return taiKhoanRepository.count(); // Nếu muốn đếm tất cả tài khoản
    }

    @Override
    public TaiKhoan findEntityByMaTK(String maTK) { // Trả về Entity để AdminPageController có thể map
        return taiKhoanRepository.findById(maTK)
                .orElseThrow(() -> new ResourceNotFoundException("Tài khoản không tìm thấy: " + maTK));
    }


    @Override
    public Page<TaiKhoanInfoResponse> getAllKhachHangTaiKhoanPageable(Pageable pageable) {
        // Lấy tất cả tài khoản có liên kết KhachHang (tức là tài khoản của khách)
        // Điều này cần một custom query hoặc lọc sau khi lấy tất cả nếu không có query sẵn
        // Ví dụ đơn giản nếu không có custom query cho Page:
        Page<TaiKhoan> taiKhoanPage = taiKhoanRepository.findAll(pageable); // Lấy tất cả, cần filter

        // Lọc những tài khoản là của khách hàng và map sang DTO
        List<TaiKhoanInfoResponse> dtoList = taiKhoanPage.getContent().stream()
                .filter(tk -> tk.getKhachHang() != null) // Chỉ lấy tài khoản của khách hàng
                .map(this::mapToTaiKhoanInfoResponse) // Dùng lại hàm map đã có
                .collect(Collectors.toList());

        // Nếu muốn query trực tiếp từ DB để có Page tối ưu hơn:
        // Page<TaiKhoan> khachHangAccountsPage = taiKhoanRepository.findByKhachHangIsNotNull(pageable); // Cần tạo method này trong Repository
        // List<TaiKhoanInfoResponse> dtoListOptimized = khachHangAccountsPage.getContent().stream()
        //        .map(this::mapToTaiKhoanInfoResponse)
        //        .collect(Collectors.toList());
        // return new PageImpl<>(dtoListOptimized, pageable, khachHangAccountsPage.getTotalElements());

        // Tạm thời dùng cách filter sau khi lấy Page<TaiKhoan>
        // Lưu ý: Việc filter sau khi lấy Page có thể không chính xác về totalElements nếu bạn muốn chỉ đếm KH.
        // Cách tốt nhất là tạo một query trong Repository trả về Page<TaiKhoan> chỉ chứa tài khoản khách hàng.
        // Ví dụ, trong TaiKhoanRepository:
        // Page<TaiKhoan> findByKhachHangIsNotNull(Pageable pageable);
        // Hoặc:
        // @Query("SELECT t FROM TaiKhoan t WHERE t.khachHang IS NOT NULL")
        // Page<TaiKhoan> findAllCustomerAccounts(Pageable pageable);

        // Giả sử bạn đã có phương thức repository phù hợp:
        // Page<TaiKhoan> customerTaiKhoanPage = taiKhoanRepository.findAllCustomerAccounts(pageable);
        // List<TaiKhoanInfoResponse> dtoList = customerTaiKhoanPage.getContent().stream()
        //         .map(this::mapToTaiKhoanInfoResponse)
        //         .collect(Collectors.toList());
        // return new PageImpl<>(dtoList, pageable, customerTaiKhoanPage.getTotalElements());

        // Hiện tại, với filter thủ công:
        return new PageImpl<>(dtoList, pageable, taiKhoanPage.getTotalElements()); // TotalElements này là của tất cả TK
    }
    @Override
    public TaiKhoanInfoResponse getTaiKhoanInfo(String maTK) {
        TaiKhoan taiKhoan = taiKhoanRepository.findById(maTK)
                .orElseThrow(() -> new ResourceNotFoundException("Tài khoản không tìm thấy: " + maTK));
        return mapToTaiKhoanInfoResponse(taiKhoan);
    }

    @Override
    public BigDecimal getSoDuTaiKhoan(String maTK) {
        TaiKhoan taiKhoan = taiKhoanRepository.findById(maTK)
                .orElseThrow(() -> new ResourceNotFoundException("Tài khoản không tìm thấy: " + maTK));
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
        khachHang.setMaKH("KH-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase()); // Tạo MaKH tạm
        khachHang.setHoTen(request.getHoTenKH());
        khachHang.setSoDienThoai(request.getSoDienThoaiKH());
        khachHang.setGioiTinh(request.getGioiTinhKH());
        khachHang.setLoaiKH(loaiKH);
        // TaiKhoan sẽ được set sau khi tạo

        KhachHang savedKhachHang = khachHangRepository.save(khachHang);

        TaiKhoan taiKhoan = new TaiKhoan();
        taiKhoan.setMaTK("TK-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase()); // Tạo MaTK tạm
        taiKhoan.setTenTK(request.getTenTK());
        // taiKhoan.setMatKhau(passwordEncoder.encode(request.getMatKhau())); // Mã hóa mật khẩu
        taiKhoan.setMatKhau(request.getMatKhau()); // Tạm thời chưa mã hóa
        taiKhoan.setSoTienConLai(request.getSoTienNapBanDau() != null ? request.getSoTienNapBanDau() : BigDecimal.ZERO);
        taiKhoan.setKhachHang(savedKhachHang);
        TaiKhoan savedTaiKhoan = taiKhoanRepository.save(taiKhoan);

        // Cập nhật lại KhachHang entity với TaiKhoan vừa tạo nếu cần (do quan hệ 1-1, JPA có thể tự quản lý)
        // savedKhachHang.setTaiKhoan(savedTaiKhoan);
        // khachHangRepository.save(savedKhachHang);


        return mapToTaiKhoanInfoResponse(savedTaiKhoan);
    }

    @Override
    @Transactional
    public TaiKhoanInfoResponse napTien(NapTienRequest request) {
        TaiKhoan taiKhoan = taiKhoanRepository.findById(request.getMaTK())
                .orElseThrow(() -> new ResourceNotFoundException("Tài khoản không tìm thấy: " + request.getMaTK()));

        if (request.getSoTienNap().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("Số tiền nạp phải lớn hơn 0.");
        }

        taiKhoan.setSoTienConLai(taiKhoan.getSoTienConLai().add(request.getSoTienNap()));
        TaiKhoan updatedTaiKhoan = taiKhoanRepository.save(taiKhoan);
        return mapToTaiKhoanInfoResponse(updatedTaiKhoan);
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
    // Ví dụ sử dụng @Procedure từ TaiKhoanRepository
    @Transactional // Đảm bảo tính nhất quán dữ liệu
    public void napTienTaiKhoan(Integer maTaiKhoan, BigDecimal soTien) {
        taiKhoanRepository.capNhatSoDu(maTaiKhoan, soTien);
    }

    @Transactional
    public void dangKyTaiKhoanMoi(String hoTen, String email, String sdt, String tenTK, String matKhau, Integer maLoaiKH) {
        // Nên có logic kiểm tra validate dữ liệu đầu vào ở đây
        // Nên mã hóa mật khẩu (matKhau) trước khi lưu
        taiKhoanRepository.themKhachHang(hoTen, email, sdt, tenTK, matKhau, maLoaiKH);
    }

    @Transactional
    public void moPhienSuDung(Integer maTaiKhoan, Integer maMay) {
        // Có thể thêm logic kiểm tra trạng thái máy, tài khoản ở đây trước khi gọi procedure
        try {
            phienSuDungRepository.batDauPhienSuDung(maTaiKhoan, maMay);
        } catch (Exception e) {
            // Xử lý lỗi, ví dụ: tài khoản không đủ tiền (do SIGNAL trong procedure)
            System.err.println("Lỗi khi bắt đầu phiên sử dụng: " + e.getMessage());
            throw new RuntimeException("Không thể bắt đầu phiên: " + e.getMessage());
        }
    }

    @Transactional
    public void dongPhienSuDung(Integer maPhien) {
        phienSuDungRepository.ketThucPhienSuDung(maPhien);
    }
}