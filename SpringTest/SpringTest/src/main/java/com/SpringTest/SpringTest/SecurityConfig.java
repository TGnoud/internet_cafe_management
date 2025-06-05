package com.SpringTest.SpringTest;

import com.SpringTest.SpringTest.service.TaiKhoanUserDetailsService; // Bạn sẽ cần tạo service này
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true, securedEnabled = true, jsr250Enabled = true) // Để dùng @PreAuthorize, @Secured
public class SecurityConfig {

    @Autowired
    private TaiKhoanUserDetailsService taiKhoanUserDetailsService; // Service để load user từ DB

    @Bean
    public static PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // Tạm thời disable CSRF cho API test, cần enable và xử lý token cho form production
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/login", "/css/**", "/js/**", "/images/**").permitAll() // Cho phép truy cập trang login, tài nguyên tĩnh
                        .requestMatchers("/api/auth/login").permitAll() // API login
                        .requestMatchers("/admin/**").hasAnyRole("ADMIN", "MANAGER") // Trang admin yêu cầu vai trò ADMIN hoặc MANAGER
                        .requestMatchers("/api/manager/**").hasAnyRole("ADMIN", "MANAGER")
                        .requestMatchers("/api/employee/**").hasAnyRole("ADMIN", "MANAGER", "EMPLOYEE")
                        .requestMatchers("/api/customer/**").hasRole("CUSTOMER") // API khách hàng
                        .requestMatchers("/customer/**").hasRole("CUSTOMER") // Trang khách hàng
                        .anyRequest().authenticated() // Tất cả các request khác cần xác thực
                )
                .formLogin(form -> form
                        .loginPage("/login") // Chỉ định trang login tùy chỉnh của bạn
                        .loginProcessingUrl("/login_perform") // URL mà Spring Security sẽ xử lý submit form login (Thymeleaf form nên trỏ tới đây)
                        .defaultSuccessUrl("/default-success", true) // Chuyển hướng sau khi login thành công
                        .failureUrl("/login?error=true") // Chuyển hướng khi login thất bại
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutRequestMatcher(new AntPathRequestMatcher("/logout_perform")) // URL để thực hiện logout
                        .logoutSuccessUrl("/login?logout=true") // Chuyển hướng sau khi logout
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID") // Xóa cookie session
                        .permitAll()
                )
                .exceptionHandling(exceptions -> exceptions
                        .accessDeniedPage("/access-denied") // Trang lỗi khi không có quyền
                );
        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(HttpSecurity http) throws Exception {
        AuthenticationManagerBuilder authenticationManagerBuilder =
                http.getSharedObject(AuthenticationManagerBuilder.class);
        authenticationManagerBuilder
                .userDetailsService(taiKhoanUserDetailsService) // Cung cấp UserDetailsService
                .passwordEncoder(passwordEncoder());        // Cung cấp PasswordEncoder
        return authenticationManagerBuilder.build();
    }
}