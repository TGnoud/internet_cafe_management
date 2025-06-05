package com.SpringTest.SpringTest.service;

import com.SpringTest.SpringTest.entity.TaiKhoan;
import com.SpringTest.SpringTest.repository.TaiKhoanRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

@Service
public class TaiKhoanUserDetailsService implements UserDetailsService {

    @Autowired
    private TaiKhoanRepository taiKhoanRepository;

    @Override
    public UserDetails loadUserByUsername(String tenTK) throws UsernameNotFoundException {
        TaiKhoan taiKhoan = taiKhoanRepository.findByTenTK(tenTK)
                .orElseThrow(() -> new UsernameNotFoundException("Tài khoản không tồn tại: " + tenTK));

        Set<GrantedAuthority> authorities = new HashSet<>();
        // Giả sử bạn có một trường 'vaiTro' trong TaiKhoan entity (ví dụ: "ROLE_ADMIN", "ROLE_CUSTOMER")
        // Hoặc bạn cần join với bảng khác để lấy vai trò
        if (taiKhoan.getVaiTro() != null && !taiKhoan.getVaiTro().isEmpty()) {
            // Spring Security yêu cầu vai trò bắt đầu bằng "ROLE_"
            String role = taiKhoan.getVaiTro().startsWith("ROLE_") ? taiKhoan.getVaiTro() : "ROLE_" + taiKhoan.getVaiTro().toUpperCase();
            authorities.add(new SimpleGrantedAuthority(role));
        } else {
            // Gán vai trò mặc định nếu không có, ví dụ ROLE_USER
            authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
        }

        // Kiểm tra trạng thái tài khoản nếu có (ví dụ: isEnabled, isLocked)
        // boolean enabled = taiKhoan.getTrangThai() != null ? taiKhoan.getTrangThai() : true;
        // boolean accountNonExpired = true;
        // boolean credentialsNonExpired = true;
        // boolean accountNonLocked = true;

        return new User(
                taiKhoan.getTenTK(), // Username
                taiKhoan.getMatKhau(), // Mật khẩu đã được mã hóa trong DB
                authorities // Danh sách quyền/vai trò
                // enabled, accountNonExpired, credentialsNonExpired, accountNonLocked // Các trạng thái khác
        );
    }
}