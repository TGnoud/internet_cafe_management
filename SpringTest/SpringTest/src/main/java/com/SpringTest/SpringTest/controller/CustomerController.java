package com.SpringTest.SpringTest.controller;

import com.SpringTest.SpringTest.dto.DichVuDTO;
import com.SpringTest.SpringTest.dto.response.TaiKhoanInfoResponse;
import com.SpringTest.SpringTest.entity.PhienSuDung;
import com.SpringTest.SpringTest.entity.TaiKhoan;
import com.SpringTest.SpringTest.exception.BadRequestException;
import com.SpringTest.SpringTest.exception.ResourceNotFoundException;
import com.SpringTest.SpringTest.repository.PhienSuDungRepository;
import com.SpringTest.SpringTest.repository.TaiKhoanRepository;
import com.SpringTest.SpringTest.service.PhienSuDungService;
import com.SpringTest.SpringTest.service.TaiKhoanService;
import com.SpringTest.SpringTest.dto.request.OrderServiceRequest;
import com.SpringTest.SpringTest.service.DichVuService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
// import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.util.List;

import org.springframework.security.core.Authentication; // Để lấy user hiện tại
import org.springframework.security.core.userdetails.UserDetails; // Để lấy user hiện tại

@RestController
@RequestMapping("/api/customer")
// @PreAuthorize("hasRole('CUSTOMER')") // Bảo vệ tất cả các endpoint trong controller này
public class CustomerController {

    @Autowired
    private TaiKhoanService taiKhoanService;

    @Autowired
    private PhienSuDungService phienSuDungService; // Cần tạo service này

    @Autowired
    private DichVuService dichVuService;

    @Autowired
    private TaiKhoanRepository taiKhoanRepository; // Cần để lấy MaTK từ Principal

    @Autowired
    private PhienSuDungRepository phienSuDungRepository; // Cần để lấy thông tin phiên sử dụng

    @GetMapping("/account/my-balance")
// @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<BigDecimal> getCurrentUserSoDu(Authentication authentication) {
        // String username = authentication.getName(); // Lấy username (ví dụ TenTK)
        // TaiKhoan taiKhoan = taiKhoanRepository.findByTenTK(username)
        // .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        // return ResponseEntity.ok(taiKhoanService.getSoDuTaiKhoan(taiKhoan.getMaTK()));

        // Placeholder - Cần logic thực tế để lấy MaTK từ Principal
        String maTKCurrentUser = getMaTKFromPrincipal(authentication);
        return ResponseEntity.ok(taiKhoanService.getSoDuTaiKhoan(maTKCurrentUser));
    }

    @GetMapping("/session/my-current-session/remaining-time")
// @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<Long> getCurrentSessionRemainingTime(Authentication authentication) {
        String maTKCurrentUser = getMaTKFromPrincipal(authentication);
        // Tìm phiên đang hoạt động của user hiện tại
        PhienSuDung activeSession = phienSuDungRepository.findByTaiKhoan_MaTKAndThoiGianKetThucIsNull(maTKCurrentUser)
                .stream().findFirst() // Giả sử mỗi user chỉ có 1 phiên active
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy phiên sử dụng nào đang hoạt động cho bạn."));
        return ResponseEntity.ok(phienSuDungService.getThoiGianConLaiDuKienPhut(activeSession.getMaPhien()));
    }
    @GetMapping("/account/my-info")
// @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<TaiKhoanInfoResponse> getCurrentUserInfo(Authentication authentication) {
        String maTKCurrentUser = getMaTKFromPrincipal(authentication);
        return ResponseEntity.ok(taiKhoanService.getTaiKhoanInfo(maTKCurrentUser));
    }

    // Helper method (cần triển khai tùy theo cách bạn cấu hình Principal)
    private String getMaTKFromPrincipal(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new BadRequestException("Người dùng chưa được xác thực.");
        }
        // Ví dụ: Nếu Principal là UserDetails và username là MaTK
        // UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        // return userDetails.getUsername();

        // Ví dụ: Nếu Principal là CustomUserDetails chứa MaTK
        // CustomUserDetails customUserDetails = (CustomUserDetails) authentication.getPrincipal();
        // return customUserDetails.getMaTK();

        // Tạm thời, bạn cần tự định nghĩa cách lấy MaTK này cho đúng.
        // Đây là một ví dụ giả định:
        Object principal = authentication.getPrincipal();
        if (principal instanceof UserDetails) {
            String username = ((UserDetails) principal).getUsername();
            // Giả sử username chính là MaTK hoặc bạn cần tra cứu MaTK từ username (TenTK)
            TaiKhoan tk = taiKhoanRepository.findByTenTK(username).orElse(null);
            if (tk != null) return tk.getMaTK();
            // Nếu username đã là MaTK (ví dụ khi CustomUserDetailsService load UserDetails bằng MaTK)
            // return username;
        }
        throw new BadRequestException("Không thể xác định Mã Tài Khoản từ thông tin xác thực.");

    @PostMapping("/order-service")
    // @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<String> orderService(@RequestBody OrderServiceRequest orderServiceRequest, Authentication authentication) {
        // UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        // String tenTKCurrentUser = userDetails.getUsername(); // Giả sử username là TenTK
        // TaiKhoan currentUser = taiKhoanRepository.findByTenTK(tenTKCurrentUser).orElseThrow(...);
        // String maTKCurrentUser = currentUser.getMaTK();
        // Hoặc nếu bạn lưu MaTK trực tiếp trong Principal/JWT
        // String maTKCurrentUser = authentication.getName(); // Nếu MaTK là username trong UserDetails

        // Tạm thời, giả sử maTK trong request là đúng và đã được xác thực
        // Cần logic để lấy maTK của người dùng đang đăng nhập thực sự
        if (orderServiceRequest.getMaTK() == null || orderServiceRequest.getMaTK().isEmpty()){
            throw new BadRequestException("Mã tài khoản không được để trống trong yêu cầu đặt dịch vụ.");
        }
        String maTKCurrentUser = orderServiceRequest.getMaTK(); // CẦN THAY THẾ BẰNG LOGIC LẤY USER ĐÃ AUTHENTICATE

        dichVuService.customerOrderService(orderServiceRequest, maTKCurrentUser);
        return ResponseEntity.ok("Yêu cầu dịch vụ đã được xử lý.");
    }

    @GetMapping("/services/available")
    // @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<List<DichVuDTO>> getAvailableServices() {
        return ResponseEntity.ok(dichVuService.findAvailableDichVu());
    }

    @GetMapping("/account/balance/{maTK}")
    public ResponseEntity<BigDecimal> getSoDuTaiKhoan(@PathVariable String maTK) {
        // TODO: Trong thực tế, nên lấy maTK từ Principal (người dùng đã xác thực)
        // thay vì truyền qua path variable để đảm bảo user chỉ xem được tài khoản của mình.
        return ResponseEntity.ok(taiKhoanService.getSoDuTaiKhoan(maTK));
    }

    @GetMapping("/account/info/{maTK}")
    public ResponseEntity<TaiKhoanInfoResponse> getTaiKhoanInfo(@PathVariable String maTK) {
        // TODO: Tương tự, lấy maTK từ Principal
        return ResponseEntity.ok(taiKhoanService.getTaiKhoanInfo(maTK));
    }

    @GetMapping("/session/remaining-time/{maPhien}")
    public ResponseEntity<Long> getThoiGianConLai(@PathVariable int maPhien) {
        // TODO: Kiểm tra xem maPhien có thuộc về user đang login không
        return ResponseEntity.ok(phienSuDungService.getThoiGianConLaiDuKienPhut(maPhien));
    }


    // Ví dụ API yêu cầu dịch vụ (cần DichVuService và logic phức tạp hơn)
    /*
    @PostMapping("/order-service")
    public ResponseEntity<?> orderService(@RequestBody OrderServiceRequest orderServiceRequest) {
        // TODO: Lấy maTK/maPhien của user hiện tại từ Principal
        // String currentMaTK = ... ;
        // orderServiceRequest.setMaTK(currentMaTK);
        // dichVuService.processOrder(orderServiceRequest);
        return ResponseEntity.ok("Yêu cầu dịch vụ đã được gửi.");
    }
    */
}